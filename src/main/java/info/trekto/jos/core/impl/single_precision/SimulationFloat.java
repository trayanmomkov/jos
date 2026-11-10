package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Range;
import info.trekto.jos.core.CpuSimulation;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.Data;
import info.trekto.jos.core.impl.FlatBarnesHutTree;
import info.trekto.jos.core.impl.ResumeScene;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.SoftContactParameters;
import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.GpuChecker.checkExecutionMode;
import static info.trekto.jos.core.GpuChecker.createRange;
import static info.trekto.jos.core.GpuChecker.gpuFloatAvailable;
import static info.trekto.jos.core.impl.Data.countObjects;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.util.Utils.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.deepCopy;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.nanoToHumanReadable;
import static info.trekto.jos.util.Utils.showRemainingTime;
import static info.trekto.jos.util.Utils.warn;
import static java.util.stream.IntStream.range;

/**
 * This implementation uses Aparapi library and runs on GPU if possible.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationFloat implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationFloat.class);
    public static final int PAUSE_SLEEP_MILLISECONDS = 100;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;

    private final MoveObjectsLogicFloat moveObjectsLogic;
    private final Range moveObjectsRange;
    private final ProcessCollisionsLogicFloat processCollisionsLogic;
    private final Range processCollisionsRange;
    private final float[] zeroArray;
    /* Positions of the objects before the move. */
    private final float[] beforeMovePositionX;
    private final float[] beforeMovePositionY;
    private boolean fastMotionWarned;
    private final CpuSimulation cpuSimulation;
    private boolean executingOnCpu;
    private final DataFloat data;
    private final SimulationProperties properties;

    /* Barnes-Hut (theta > 0): the tree is built on the CPU every iteration (in double, from
     * widened snapshots), narrowed to float and traversed by the Barnes-Hut kernels.
     * The exact kernels remain the fallback when the tree does not fit. */
    private final boolean barnesHut;
    private final FlatBarnesHutTree tree;
    private final FloatBarnesHutTreeArrays treeArrays;
    private final BarnesHutMoveObjectsLogicFloat barnesHutMoveObjectsLogic;
    private final BarnesHutProcessCollisionsLogicFloat barnesHutProcessCollisionsLogic;
    private final double[] scratchPositionX;
    private final double[] scratchPositionY;
    private final double[] scratchMass;
    private final double[] scratchRadius;
    private boolean treeOverflowWarned;

    /* Soft contact (DEM), see SimulationDouble. Here the small steps run on the GPU when there is one. */
    private final boolean softContactRequested;
    private boolean softContactActive;
    private boolean softContactOnGpu;
    private int softContactSubSteps = 1;
    private float softContactStiffness = 0;

    public SimulationFloat(SimulationProperties properties, CpuSimulation cpuSimulation) {
        this.properties = properties;
        final int n = properties.getNumberOfObjects();
        int screenWidth = 0;
        int screenHeight = 0;
        if (properties.isBounceFromScreenBorders()) {
            if (C.getVisualizer() == null) {
                C.setVisualizer(C.createVisualizer(properties));
            }
            screenWidth = C.getVisualizer().getVisualizationPanel().getWidth();
            screenHeight = C.getVisualizer().getVisualizationPanel().getHeight();
        }
        zeroArray = new float[n];
        beforeMovePositionX = new float[n];
        beforeMovePositionY = new float[n];
        data = new DataFloat(n);
        float coefficientOfRestitution = properties.getCoefficientOfRestitution().floatValue();

        moveObjectsLogic = new MoveObjectsLogicFloat(data, properties.getSecondsPerIteration().floatValue(), properties.getMinDistance().floatValue(),
                                                     screenWidth, screenHeight);
        moveObjectsRange = createRange(n);
        moveObjectsLogic.setExecutionMode(GPU);

        processCollisionsLogic = new ProcessCollisionsLogicFloat(data, properties.isMergeOnCollision(), coefficientOfRestitution,
                                                                 beforeMovePositionX, beforeMovePositionY);
        processCollisionsRange = createRange(n);
        processCollisionsLogic.setExecutionMode(GPU);

        Number theta = properties.getBarnesHutTheta();
        barnesHut = theta != null && theta.doubleValue() > 0;
        if (barnesHut) {
            tree = new FlatBarnesHutTree(n);
            treeArrays = new FloatBarnesHutTreeArrays(tree);
            scratchPositionX = new double[n];
            scratchPositionY = new double[n];
            scratchMass = new double[n];
            scratchRadius = new double[n];
            float thetaSquared = (float) (theta.doubleValue() * theta.doubleValue());
            barnesHutMoveObjectsLogic = new BarnesHutMoveObjectsLogicFloat(data, tree, treeArrays, thetaSquared,
                                                                           properties.getSecondsPerIteration().floatValue(),
                                                                           properties.getMinDistance().floatValue(),
                                                                           screenWidth, screenHeight);
            barnesHutMoveObjectsLogic.setExecutionMode(GPU);
            barnesHutProcessCollisionsLogic = new BarnesHutProcessCollisionsLogicFloat(data, tree, treeArrays,
                                                                                       properties.isMergeOnCollision(),
                                                                                       coefficientOfRestitution,
                                                                                       beforeMovePositionX, beforeMovePositionY);
            barnesHutProcessCollisionsLogic.setExecutionMode(GPU);
        } else {
            tree = null;
            treeArrays = null;
            barnesHutMoveObjectsLogic = null;
            barnesHutProcessCollisionsLogic = null;
            scratchPositionX = null;
            scratchPositionY = null;
            scratchMass = null;
            scratchRadius = null;
        }

        this.softContactRequested = properties.useSoftContact();

        this.cpuSimulation = cpuSimulation;
    }

    /* Builds the tree in double and copies it to float for the kernels. Returns false when it does not fit;
     * the exact kernels are used then. */
    private boolean buildTree() {
        for (int i = 0; i < data.n; i++) {
            scratchPositionX[i] = data.readOnlyPositionX[i];
            scratchPositionY[i] = data.readOnlyPositionY[i];
            scratchMass[i] = data.readOnlyMass[i];
            scratchRadius[i] = data.readOnlyRadius[i];
        }
        boolean fitted = tree.build(scratchPositionX, scratchPositionY, scratchMass, scratchRadius,
                                    data.readOnlyDeleted, data.n);
        if (fitted) {
            treeArrays.refresh(tree);
        } else if (!treeOverflowWarned) {
            treeOverflowWarned = true;
            warn(logger, "Barnes-Hut tree capacity exceeded. Using the exact kernels for the affected iterations.");
        }
        return fitted;
    }

    @Override
    public void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        if (softContactActive) {
            doSoftContactIteration(saveCurrentIterationToFile, iterationCounter);
            return;
        }
        data.copyToReadOnly(properties.isMergeOnCollision());
        captureBeforeMovePositions();
        /* One tree per iteration: the collision phase reuses the tree of the move phase. */
        boolean treeFitted = barnesHut && buildTree();
        if (treeFitted) {
            barnesHutMoveObjectsLogic.execute(moveObjectsRange); /* Execute in parallel on GPU if available */
            checkExecutionMode(iterationCounter, barnesHutMoveObjectsLogic);
            barnesHutProcessCollisionsLogic.setSlack(maxMoveSinceSnapshot());
        } else {
//        moveObjectsLogic.runOnCpu();
            moveObjectsLogic.execute(moveObjectsRange); /* Execute in parallel on GPU if available */
            checkExecutionMode(iterationCounter, moveObjectsLogic);
        }

        if (properties.isCollisionsEnabled()) {
            warnIfFastMotion();
            data.copyToReadOnly(properties.isMergeOnCollision());
            if (treeFitted) {
                barnesHutProcessCollisionsLogic.execute(processCollisionsRange); /* Collisions - Execute in parallel on GPU if available */
                checkExecutionMode(iterationCounter, barnesHutProcessCollisionsLogic);
            } else {
//        processCollisionsLogic.runOnCpu();
                processCollisionsLogic.execute(processCollisionsRange); /* Collisions - Execute in parallel on GPU if available */
                checkExecutionMode(iterationCounter, processCollisionsLogic);
            }
        }

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, data.positionX, data.positionY, zeroArray, data.velocityX,
                                                    data.velocityY, zeroArray, data.mass, data.radius, data.id, data.color, data.deleted,
                                                    data.accelerationX, data.accelerationY, zeroArray);
        }
    }

    /* One iteration with the soft contact - see SimulationDouble. */
    private void doSoftContactIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        captureBeforeMovePositions();
        if (softContactOnGpu) {
            runSoftContactSubStepsOnGpu(iterationCounter);
        } else {
            runSoftContactSubStepsOnCpu();
        }
        warnIfFastMotion();

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, data.positionX, data.positionY, zeroArray, data.velocityX,
                                                    data.velocityY, zeroArray, data.mass, data.radius, data.id, data.color, data.deleted,
                                                    data.accelerationX, data.accelerationY, zeroArray);
        }
    }

    private void runSoftContactSubStepsOnCpu() {
        final MoveObjectsLogicFloat logic = moveObjectsLogic;
        final int count = data.n;
        for (int subStep = 0; subStep < softContactSubSteps; subStep++) {
            range(0, count).parallel().forEach(logic::computeContactAcceleration);
            range(0, count).parallel().forEach(logic::integrateContact);
        }
    }

    private void runSoftContactSubStepsOnGpu(long iterationCounter) {
        try {
            moveObjectsLogic.uploadSoftContactState();
            for (int subStep = 0; subStep < softContactSubSteps; subStep++) {
                moveObjectsLogic.setContactPhase(0);   // first pass: accelerations
                moveObjectsLogic.execute(moveObjectsRange);
                moveObjectsLogic.setContactPhase(1);   // second pass: move
                moveObjectsLogic.execute(moveObjectsRange);
            }
            moveObjectsLogic.downloadSoftContactState();
            moveObjectsLogic.setContactPhase(-1);
            checkExecutionMode(iterationCounter, moveObjectsLogic);
        } catch (Exception e) {
            warn(logger, "Soft-contact GPU execution failed; using the multi-core CPU path for the rest of the run.", e);
            softContactOnGpu = false;
            moveObjectsLogic.setExplicit(false);
            moveObjectsLogic.setContactPhase(-1);
            runSoftContactSubStepsOnCpu();
        }
    }

    /* Calculates the contact parameters from the scene. Without them the bounce is used. */
    private void configureSoftContact() {
        softContactActive = false;
        if (!softContactRequested) {
            return;
        }
        int count = data.n;
        double[] x = new double[count], y = new double[count], vx = new double[count], vy = new double[count],
                 m = new double[count], r = new double[count];
        for (int i = 0; i < count; i++) {
            x[i] = data.positionX[i];
            y[i] = data.positionY[i];
            vx[i] = data.velocityX[i];
            vy[i] = data.velocityY[i];
            m[i] = data.mass[i];
            r[i] = data.radius[i];
        }
        SoftContactParameters params = SoftContactParameters.derive(count, x, y, vx, vy, m, r, data.deleted,
                properties.getSecondsPerIteration().doubleValue(), properties.getCoefficientOfRestitution().doubleValue(),
                properties.getMinDistance().doubleValue());
        if (params.stiffness <= 0) {
            warn(logger, "Soft contact requested but the stiffness could not be derived (no colliding objects?). "
                    + "Using the classic impulse model.");
            return;
        }
        moveObjectsLogic.enableSoftContact((float) params.stiffness, (float) params.dampingFactor);
        moveObjectsLogic.setSecondsPerIteration((float) params.subStepSeconds);
        softContactSubSteps = params.subSteps;
        softContactStiffness = (float) params.stiffness;
        softContactOnGpu = gpuFloatAvailable;
        softContactActive = true;
        info(logger, "Soft-sphere contact enabled (" + (softContactOnGpu ? "GPU, device-resident" : "multi-core CPU") + ", float). " + params);
        if (params.subSteps > 1) {
            info(logger, "Soft contact sub-steps per iteration: " + params.subSteps
                    + " (sub-step " + params.subStepSeconds + " s) for a stable contact integration.");
        }
    }

    public float getSoftContactStiffness() {
        return softContactStiffness;
    }

    public boolean isSoftContactActive() {
        return softContactActive;
    }

    public int getSoftContactSubSteps() {
        return softContactSubSteps;
    }

    private void captureBeforeMovePositions() {
        deepCopy(data.positionX, beforeMovePositionX);
        deepCopy(data.positionY, beforeMovePositionY);
    }

    /* An object which moves more than its radius per iteration can pass through another object. */
    private void warnIfFastMotion() {
        if (fastMotionWarned) {
            return;
        }
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                float dx = data.positionX[i] - beforeMovePositionX[i];
                float dy = data.positionY[i] - beforeMovePositionY[i];
                if (dx * dx + dy * dy > data.radius[i] * data.radius[i]) {
                    fastMotionWarned = true;
                    warn(logger, "An object moved more than its radius in one iteration. Fast objects can pass "
                            + "through each other (tunneling). Reduce 'seconds per iteration' to avoid it.");
                    return;
                }
            }
        }
    }

    /* The biggest distance an object has moved since the tree was built. The collision kernel
     * widens its search by it. Rounded up. */
    private float maxMoveSinceSnapshot() {
        double maxSquared = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                double dx = (double) data.positionX[i] - data.readOnlyPositionX[i];
                double dy = (double) data.positionY[i] - data.readOnlyPositionY[i];
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared > maxSquared) {
                    maxSquared = distanceSquared;
                }
            }
        }
        return Math.nextUp((float) Math.sqrt(maxSquared));
    }

    @Override
    public double calculateTotalMass() {
        double mass = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                mass += data.mass[i];
            }
        }
        return mass;
    }

    @Override
    public double calculateTotalMomentum() {
        double momentum = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                momentum += data.mass[i] * (Math.abs(data.velocityX[i]) + Math.abs(data.velocityY[i]));
            }
        }
        return momentum;
    }

    public void initArrays(List<SimulationObject> initialObjects) {
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            data.positionX[i] = o.getX().floatValue();
            data.positionY[i] = o.getY().floatValue();
            data.velocityX[i] = o.getVelocity().getX().floatValue();
            data.velocityY[i] = o.getVelocity().getY().floatValue();
            data.accelerationX[i] = o.getAcceleration().getX().floatValue();
            data.accelerationY[i] = o.getAcceleration().getY().floatValue();
            data.mass[i] = o.getMass().floatValue();
            data.radius[i] = o.getRadius().floatValue();
            data.id[i] = o.getId();
            data.color[i] = o.getColor();
            data.deleted[i] = false;
        }

        deepCopy(data.mass, data.readOnlyMass);
        deepCopy(data.deleted, data.readOnlyDeleted);
        deepCopy(data.color, data.readOnlyColor);
        deepCopy(data.positionX, data.readOnlyPositionX);
        deepCopy(data.positionY, data.readOnlyPositionY);
        deepCopy(data.radius, data.readOnlyRadius);
        deepCopy(data.velocityX, data.readOnlyVelocityX);
        deepCopy(data.velocityY, data.readOnlyVelocityY);
    }

    @Override
    public void startSimulation() throws SimulationException {
        init(true);

        info(logger, "Start simulation...");
        C.setEndText("END.");
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long previousVisualizationTime = startTime;
        long endTime;

        C.setRunning(true);
        C.setHasToStop(false);
        try {
            for (long i = 0; properties.isInfiniteSimulation() || i < properties.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop()) {
                        ResumeScene.write(properties, executingOnCpu ? cpuSimulation.getData() : convertToDataAP());
                        doStop();
                        break;
                    }
                    while (C.isPaused()) {
                        Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                    }

                    long iterationCounter = i + 1;
                    int numberOfObjects = countObjects(data);

                    if (cpuSimulation != null && !executingOnCpu && numberOfObjects <= C.getCpuThreshold()) {
                        info(logger, "Switching to CPU - Initialize simulation...");

                        cpuSimulation.setDataAndInitializeLogic(convertToDataAP());
                        executingOnCpu = true;

                        info(logger, "Done.\n");
                        Utils.printConfiguration(this);
                    }

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, properties.getNumberOfIterations(), numberOfObjects);
                        previousTime = System.nanoTime();
                    }

                    boolean visualize = false;
                    if (properties.isRealTimeVisualization()) {
                        if (properties.getPlayingSpeed() < 0) {
                            /* Slow down */
                            Thread.sleep(-properties.getPlayingSpeed());
                            visualize = true;
                        } else if ((System.nanoTime() - previousVisualizationTime) / NANOSECONDS_IN_ONE_MILLISECOND >= properties.getPlayingSpeed()) {
                            visualize = true;
                        }
                    }

                    if (visualize) {
                        if (executingOnCpu) {
                            DataAP cpuData = cpuSimulation.getData();
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, cpuData.id, cpuData.deleted,
                                                        Arrays.stream(cpuData.positionX).mapToDouble(Number::doubleValue).toArray(),
                                                        Arrays.stream(cpuData.positionY).mapToDouble(Number::doubleValue).toArray(),
                                                        Arrays.stream(cpuData.radius).mapToDouble(Number::doubleValue).toArray(),
                                                        cpuData.color);
                        } else {
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, data.id, data.deleted,
                                                        range(0, data.n).mapToDouble(j -> data.positionX[j]).toArray(),
                                                        range(0, data.n).mapToDouble(j -> data.positionY[j]).toArray(),
                                                        range(0, data.n).mapToDouble(j -> data.radius[j]).toArray(),
                                                        data.color);
                        }
                        previousVisualizationTime = System.nanoTime();
                    }

                    if (executingOnCpu) {
                        cpuSimulation.doIteration(i % properties.getSaveEveryNthIteration() == 0, iterationCounter);
                    } else {
                        doIteration(i % properties.getSaveEveryNthIteration() == 0, iterationCounter);
                    }
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
                    return;
                }
            }

            if (properties.isRealTimeVisualization()) {
                C.getVisualizer().end();
            }
            endTime = System.nanoTime();
        } finally {
            C.setRunning(false);
            if (properties.isSaveToFile()) {
                C.getReaderWriter().endFile();
            }
        }

        info(logger, "End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
    }

    public void init(boolean printInfo) throws SimulationException {
        if (printInfo) {
            info(logger, "Initialize simulation...");
        }

        initArrays(properties.getInitialObjects());

        if (duplicateIdExists(data.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        /* The soft contact handles overlapping objects, and without collisions they do not matter. */
        if (properties.isCollisionsEnabled() && !properties.useSoftContact()
                && collisionExists(data.positionX, data.positionY, data.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        configureSoftContact();

        executingOnCpu = false;

        if (printInfo) {
            info(logger, "Done.\n");
            Utils.printConfiguration(this);
        }
    }

    private boolean duplicateIdExists(String[] id) {
        Set<String> ids = new HashSet<>();
        for (String objectId : id) {
            if (!ids.add(objectId)) {
                return true;
            }
        }
        return false;
    }

    public boolean collisionExists(float[] positionX, float[] positionY, float[] radius) {
        ProcessCollisionsLogicFloat tempCollisionLogic = new ProcessCollisionsLogicFloat(data, true, 1);
        Range tempRange = createRange(data.n);

        data.copyToReadOnly(true);
        tempCollisionLogic.execute(tempRange);
        for (int i = 0; i < data.n; i++) {
            if (data.deleted[i]) {
                info(logger, String.format("Collision with object A(x:%f, y:%f, r:%f)", positionX[i], positionY[i], radius[i]));
                return true;
            }
        }
        return false;
    }

    public CpuSimulation getCpuSimulation() {
        return cpuSimulation;
    }

    protected void doStop() {
        C.setHasToStop(false);
        if (properties.isSaveToFile()) {
            C.getReaderWriter().endFile();
        }
        if (C.getVisualizer() != null) {
            C.setEndText("Stopped!");
            C.getVisualizer().closeWindow();
        }
    }

    @Override
    public SimulationProperties getProperties() {
        return properties;
    }

    @Override
    public Data getData() {
        return data;
    }

    private DataAP convertToDataAP() {
        DataAP dataAp = new DataAP(data.n);
        deepCopy(data.id, dataAp.id);
        deepCopy(data.deleted, dataAp.deleted);
        deepCopy(data.color, dataAp.color);
        deepCopy(data.deleted, dataAp.readOnlyDeleted);
        deepCopy(data.color, dataAp.readOnlyColor);

        for (int i = 0; i < data.n; i++) {
            dataAp.positionX[i] = New.num(data.positionX[i]);
            dataAp.positionY[i] = New.num(data.positionY[i]);
            dataAp.radius[i] = New.num(data.radius[i]);
            dataAp.velocityX[i] = New.num(data.velocityX[i]);
            dataAp.velocityY[i] = New.num(data.velocityY[i]);
            dataAp.accelerationX[i] = New.num(data.accelerationX[i]);
            dataAp.accelerationY[i] = New.num(data.accelerationY[i]);
            dataAp.mass[i] = New.num(data.mass[i]);
        }

        /* When merging is off, copyToReadOnly() does not refresh the mass snapshot because
         * the mass never changes. It has to be initialized here, like initArrays() does. */
        deepCopy(dataAp.mass, dataAp.readOnlyMass);

        return dataAp;
    }
}
