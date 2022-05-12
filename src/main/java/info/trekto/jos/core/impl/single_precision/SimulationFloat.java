package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Range;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.util.Utils.*;
import static java.util.stream.IntStream.range;

/**
 * This implementation uses Aparapi library and runs on GPU if possible.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationFloat extends SimulationAP implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationFloat.class);

    private final SimulationLogicFloat simulationLogic;
    private final Range simulationLogicRange;
    private final CollisionCheckFloat collisionCheckKernel;
    private final Range collisionCheckRange;
    private final float[] zeroArray;
    private final SimulationAP cpuSimulation;
    private boolean executingOnCpu;

    public SimulationFloat(SimulationProperties properties, SimulationAP cpuSimulation) {
        super(properties);
        final int n = properties.getNumberOfObjects();
        simulationLogic = new SimulationLogicFloat(n, properties.getSecondsPerIteration().floatValue());
        zeroArray = new float[n];
        simulationLogicRange = Range.create(n);
        simulationLogic.setExecutionMode(GPU);

        collisionCheckKernel = new CollisionCheckFloat(
                n,
                simulationLogic.positionX,
                simulationLogic.positionY,
                simulationLogic.radius,
                simulationLogic.deleted);
        collisionCheckRange = Range.create(n);
        collisionCheckKernel.setExecutionMode(GPU);
        this.cpuSimulation = cpuSimulation;
    }

    public void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) {
        deepCopy(simulationLogic.positionX, simulationLogic.readOnlyPositionX);
        deepCopy(simulationLogic.positionY, simulationLogic.readOnlyPositionY);
        deepCopy(simulationLogic.speedX, simulationLogic.readOnlySpeedX);
        deepCopy(simulationLogic.speedY, simulationLogic.readOnlySpeedY);
        deepCopy(simulationLogic.mass, simulationLogic.readOnlyMass);
        deepCopy(simulationLogic.radius, simulationLogic.readOnlyRadius);
        deepCopy(simulationLogic.color, simulationLogic.readOnlyColor);
        deepCopy(simulationLogic.deleted, simulationLogic.readOnlyDeleted);

        /* Execute in parallel on GPU if available */
        simulationLogic.execute(simulationLogicRange);
        if (iterationCounter == 1) {
            if (!GPU.equals(simulationLogic.getExecutionMode())) {
                warn(logger, "Simulation logic execution mode = " + simulationLogic.getExecutionMode());
            }
        }

        /* Collision and merging */
        collisionCheckKernel.prepare();

        /* Execute in parallel on GPU if available */
        collisionCheckKernel.execute(collisionCheckRange);
        if (iterationCounter == 1) {
            if (!GPU.equals(collisionCheckKernel.getExecutionMode())) {
                warn(logger, "Collision detection execution mode = " + simulationLogic.getExecutionMode());
            }
        }

        /* If collision/s exists execute sequentially on a single thread */
        if (collisionCheckKernel.collisionExists()) {
            simulationLogic.processCollisions();
        }

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(properties, iterationCounter, simulationLogic.positionX, simulationLogic.positionY,
                                                    zeroArray, simulationLogic.speedX, simulationLogic.speedY, zeroArray, simulationLogic.mass,
                                                    simulationLogic.radius, simulationLogic.id, simulationLogic.color, simulationLogic.deleted);
        }
    }

    public void initArrays(List<SimulationObject> initialObjects) {
        Arrays.fill(simulationLogic.deleted, true);
        for (int i = 0; i < initialObjects.size(); i++) {
            SimulationObject o = initialObjects.get(i);
            simulationLogic.positionX[i] = o.getX().floatValue();
            simulationLogic.positionY[i] = o.getY().floatValue();
            simulationLogic.speedX[i] = o.getSpeed().getX().floatValue();
            simulationLogic.speedY[i] = o.getSpeed().getY().floatValue();
            simulationLogic.mass[i] = o.getMass().floatValue();
            simulationLogic.radius[i] = o.getRadius().floatValue();
            simulationLogic.id[i] = o.getId();
            simulationLogic.color[i] = o.getColor();
            simulationLogic.deleted[i] = false;
        }
    }

    @Override
    public void startSimulation() throws SimulationException {
        init();

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
                        doStop();
                        break;
                    }
                    while (C.isPaused()) {
                        Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                    }

                    iterationCounter = i + 1;
                    int numberOfObjects = executingOnCpu ? cpuSimulation.getObjects().size() : countObjects();

                    if (cpuSimulation != null && !executingOnCpu && numberOfObjects <= C.getCpuThreshold()) {
                        cpuSimulation.initSwitchingFromGpu(convertToSimulationObjects());
                        executingOnCpu = true;
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
                            C.getVisualizer().visualize(cpuSimulation.getObjects(), iterationCounter);
                        } else {
                            SimulationLogicFloat sl = simulationLogic;
                            C.getVisualizer().visualize(iterationCounter, numberOfObjects, sl.id, sl.deleted,
                                                        range(0, sl.positionX.length).mapToDouble(j -> sl.positionX[j]).toArray(),
                                                        range(0, sl.positionY.length).mapToDouble(j -> sl.positionY[j]).toArray(),
                                                        range(0, sl.radius.length).mapToDouble(j -> sl.radius[j]).toArray(),
                                                        sl.color);
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

    private List<SimulationObject> convertToSimulationObjects() {
        List<SimulationObject> objects = new ArrayList<>();
        SimulationLogicFloat sl = simulationLogic;

        for (int i = 0; i < sl.positionX.length; i++) {
            if (!sl.deleted[i]) {
                SimulationObject simo = new SimulationObjectImpl();
                simo.setId(sl.id[i]);

                simo.setX(New.num(sl.positionX[i]));
                simo.setY(New.num(sl.positionY[i]));
                simo.setZ(New.num(0));

                simo.setMass(New.num(sl.mass[i]));

                simo.setSpeed(new TripleNumber(New.num(sl.speedX[i]),
                                               New.num(sl.speedY[i]),
                                               New.num(0)));

                simo.setRadius(New.num(sl.radius[i]));
                simo.setColor(sl.color[i]);

                objects.add(simo);
            }
        }

        return objects;
    }

    public void init() throws SimulationException {
        initArrays(properties.getInitialObjects());
        if (duplicateIdExists(simulationLogic.id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(simulationLogic.positionX, simulationLogic.positionY, simulationLogic.radius)) {
            throw new SimulationException("Initial collision exists!");
        }

        executingOnCpu = false;

        info(logger, "Done.\n");
        Utils.printConfiguration(this);
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

    public int countObjects() {
        int numberOfObjects = 0;
        for (int j = 0; j < simulationLogic.deleted.length; j++) {
            if (!simulationLogic.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }

    public boolean collisionExists(float[] positionX, float[] positionY, float[] radius) {
        for (int i = 0; i < positionX.length; i++) {
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }
                // distance between centres
                float distance = SimulationLogicFloat.calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);

                if (distance < radius[i] + radius[j]) {
                    info(logger, String.format("Collision between object A(x:%f, y:%f, r:%f) and B(x:%f, y:%f, r:%f)",
                                               positionX[i], positionY[i], radius[i], positionX[j], positionY[j], radius[j]));
                    return true;
                }
            }
        }
        return false;
    }
}
