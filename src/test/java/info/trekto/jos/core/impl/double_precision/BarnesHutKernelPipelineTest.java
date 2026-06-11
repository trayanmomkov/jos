package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Runs several full iterations (move + collisions, with one tree per iteration which the
 * collision phase reuses with a fattened pruning reach) through the kernels' plain Java entry
 * points, the way SimulationDouble.doIteration drives them. Kernel.execute() itself cannot run
 * on a machine without OpenCL (Aparapi fails in its profiling), so the execute path is part of
 * the manual GPU checklist; the code executed here is the same code Aparapi translates.
 */
public class BarnesHutKernelPipelineTest {
    private static final double DT = 0.001;
    private static final double MIN_DISTANCE = 1;
    private static final double COR = 0.75;
    private static final double THETA = 0.7;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);
    }

    @Test
    public void treeOverflowFallsBackToTheExactKernels() {
        /* Nearly coincident pairs burn ~50 tree nodes each and exceed the 4n+64 capacity,
         * so every phase must fall back to the exact kernels and the run must be identical
         * to a run without Barnes-Hut. */
        DataDouble withTheta = overflowingScene();
        DataDouble exact = overflowingScene();

        FlatBarnesHutTree tree = new FlatBarnesHutTree(withTheta.n);
        MoveObjectsLogicDouble bruteMoveA = new MoveObjectsLogicDouble(withTheta, DT, MIN_DISTANCE, 0, 0);
        BarnesHutMoveObjectsLogicDouble bhMove = new BarnesHutMoveObjectsLogicDouble(withTheta, tree, THETA * THETA, DT, MIN_DISTANCE, 0, 0);
        ProcessCollisionsLogicDouble bruteCollisionsA = new ProcessCollisionsLogicDouble(withTheta, false, COR);
        BarnesHutProcessCollisionsLogicDouble bhCollisions = new BarnesHutProcessCollisionsLogicDouble(withTheta, tree, false, COR);

        MoveObjectsLogicDouble bruteMoveB = new MoveObjectsLogicDouble(exact, DT, MIN_DISTANCE, 0, 0);
        ProcessCollisionsLogicDouble bruteCollisionsB = new ProcessCollisionsLogicDouble(exact, false, COR);

        for (int iteration = 0; iteration < 3; iteration++) {
            /* Barnes-Hut run: the tree never fits, so both phases use the exact kernels. */
            withTheta.copyToReadOnly(false);
            boolean fitted = tree.build(withTheta.readOnlyPositionX, withTheta.readOnlyPositionY, withTheta.readOnlyMass,
                                        withTheta.readOnlyRadius, withTheta.readOnlyDeleted, withTheta.n);
            assertFalse(fitted, "The chain scene should not fit in the tree");
            if (fitted) {
                bhMove.runOnCpu();
            } else {
                bruteMoveA.runOnCpu();
            }
            withTheta.copyToReadOnly(false);
            if (fitted) {
                bhCollisions.runOnSingleThread();
            } else {
                bruteCollisionsA.runOnSingleThread();
            }

            /* Exact run */
            exact.copyToReadOnly(false);
            bruteMoveB.runOnCpu();
            exact.copyToReadOnly(false);
            bruteCollisionsB.runOnSingleThread();
        }

        for (int i = 0; i < withTheta.n; i++) {
            assertEquals(withTheta.positionX[i], exact.positionX[i], 0.0, "Object " + i + " position X");
            assertEquals(withTheta.velocityX[i], exact.velocityX[i], 0.0, "Object " + i + " velocity X");
        }
    }

    @Test
    public void multipleIterationsWithTreeRebuildStayFiniteAndCloseToExact() {
        DataDouble bh = randomScene();
        DataDouble exact = randomScene();

        FlatBarnesHutTree tree = new FlatBarnesHutTree(bh.n);
        BarnesHutMoveObjectsLogicDouble bhMove = new BarnesHutMoveObjectsLogicDouble(bh, tree, THETA * THETA, DT, MIN_DISTANCE, 0, 0);
        BarnesHutProcessCollisionsLogicDouble bhCollisions = new BarnesHutProcessCollisionsLogicDouble(bh, tree, true, COR);

        MoveObjectsLogicDouble exactMove = new MoveObjectsLogicDouble(exact, DT, MIN_DISTANCE, 0, 0);
        ProcessCollisionsLogicDouble exactCollisions = new ProcessCollisionsLogicDouble(exact, true, COR);

        for (int iteration = 0; iteration < 5; iteration++) {
            bh.copyToReadOnly(true);
            assertTrue(tree.build(bh.readOnlyPositionX, bh.readOnlyPositionY, bh.readOnlyMass,
                                  bh.readOnlyRadius, bh.readOnlyDeleted, bh.n), "Tree should fit");
            bhMove.runOnCpu();
            /* The collision phase reuses the tree, fattened by how far the objects moved. */
            bhCollisions.setSlack(maxMoveSinceSnapshot(bh));
            bh.copyToReadOnly(true);
            bhCollisions.runOnSingleThread();

            exact.copyToReadOnly(true);
            exactMove.runOnCpu();
            exact.copyToReadOnly(true);
            exactCollisions.runOnSingleThread();
        }

        for (int i = 0; i < bh.n; i++) {
            assertFalse(Double.isNaN(bh.positionX[i]), "Position X of object " + i + " became NaN");
            assertFalse(Double.isNaN(bh.velocityX[i]), "Velocity X of object " + i + " became NaN");
            assertEquals(bh.deleted[i], exact.deleted[i], "Object " + i + " deleted flag");
            if (!bh.deleted[i]) {
                /* The approximation drifts slightly from the exact run; it must stay close. */
                assertEquals(bh.positionX[i], exact.positionX[i],
                             Math.abs(exact.positionX[i]) * 1e-3 + 1e-6, "Object " + i + " position X");
            }
        }
    }

    private static double maxMoveSinceSnapshot(DataDouble data) {
        double maxSquared = 0;
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                double dx = data.positionX[i] - data.readOnlyPositionX[i];
                double dy = data.positionY[i] - data.readOnlyPositionY[i];
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared > maxSquared) {
                    maxSquared = distanceSquared;
                }
            }
        }
        return Math.nextUp(Math.sqrt(maxSquared));
    }

    private static DataDouble overflowingScene() {
        int pairs = 10;
        DataDouble data = new DataDouble(pairs * 2);
        for (int p = 0; p < pairs; p++) {
            data.positionX[2 * p] = p * 1000;
            data.positionX[2 * p + 1] = p * 1000 + 1e-9;
            data.mass[2 * p] = 1e10;
            data.mass[2 * p + 1] = 1e10;
            data.radius[2 * p] = 1e-12;
            data.radius[2 * p + 1] = 1e-12;
        }
        data.copyToReadOnly(true);
        return data;
    }

    private static DataDouble randomScene() {
        Random random = new Random(41);
        int n = 40;
        DataDouble data = new DataDouble(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = (random.nextDouble() - 0.5) * 1000;
            data.positionY[i] = (random.nextDouble() - 0.5) * 1000;
            data.velocityX[i] = (random.nextDouble() - 0.5) * 4;
            data.velocityY[i] = (random.nextDouble() - 0.5) * 4;
            data.mass[i] = 1e10 * (1 + random.nextDouble());
            data.radius[i] = 1 + random.nextDouble() * 3;
        }
        data.copyToReadOnly(true);
        return data;
    }
}
