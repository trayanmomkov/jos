package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;
import info.trekto.jos.core.impl.single_precision.BarnesHutMoveObjectsLogicFloat;
import info.trekto.jos.core.impl.single_precision.BarnesHutProcessCollisionsLogicFloat;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.FloatBarnesHutTreeArrays;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The collision phase reuses the tree built for the move phase instead of rebuilding it: the
 * objects have moved since the tree was built, so the kernels fatten their pruning reach by
 * the biggest distance any object has moved (the slack). These tests pin the safety property -
 * an object cannot move out of the pruning reach and escape detection, no matter how fast -
 * and the equivalence with the freshly rebuilt tree the collision phase used before.
 */
public class BarnesHutTreeReuseTest {
    private static final double DT = 1;
    private static final double MIN_DISTANCE = 1;
    private static final double COR = 0.75;
    private static final double THETA = 0.7;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);
    }

    /**
     * Two fast objects meet in the middle of the scene. In the tree they are still ~100 apart,
     * so without the slack the pruning discards the partner's node and the collision is missed.
     * With the slack the reused tree must reproduce the brute force kernel bit for bit (each
     * object has a single contact, so no summation order is involved).
     */
    @Test
    public void fastMoverIsOnlyFoundWithTheSlack() {
        DataDouble brute = fastMoverScene();
        double bruteSlack = moveAndComputeSlack(brute);
        brute.copyToReadOnly(false);
        new ProcessCollisionsLogicDouble(brute, false, COR).runOnSingleThread();

        DataDouble withSlack = fastMoverScene();
        FlatBarnesHutTree tree = buildTree(withSlack);
        double slack = moveAndComputeSlack(withSlack, tree);
        withSlack.copyToReadOnly(false);
        BarnesHutProcessCollisionsLogicDouble reused = new BarnesHutProcessCollisionsLogicDouble(withSlack, tree, false, COR);
        reused.setSlack(slack);
        reused.runOnSingleThread();

        DataDouble withoutSlack = fastMoverScene();
        FlatBarnesHutTree staleTree = buildTree(withoutSlack);
        moveAndComputeSlack(withoutSlack, staleTree);
        withoutSlack.copyToReadOnly(false);
        new BarnesHutProcessCollisionsLogicDouble(withoutSlack, staleTree, false, COR).runOnSingleThread();

        assertEquals(slack, bruteSlack, 0.0, "Both runs must have moved identically");
        assertTrue(slack > 49, "The objects should have moved fast, otherwise the test proves nothing");
        assertTrue(brute.velocityX[0] != withoutSlack.velocityX[0]
                           || brute.velocityX[1] != withoutSlack.velocityX[1],
                   "Without the slack the stale tree should miss the collision, otherwise the test proves nothing");
        for (int i = 0; i < brute.n; i++) {
            assertEquals(withSlack.velocityX[i], brute.velocityX[i], 0.0, "Object " + i + " velocity X");
            assertEquals(withSlack.velocityY[i], brute.velocityY[i], 0.0, "Object " + i + " velocity Y");
            assertEquals(withSlack.positionX[i], brute.positionX[i], 0.0, "Object " + i + " position X");
            assertEquals(withSlack.positionY[i], brute.positionY[i], 0.0, "Object " + i + " position Y");
        }
    }

    /**
     * Merge decisions are taken from the read-only snapshots with an order-independent rule,
     * so the reused tree must reach exactly the same conclusions as a freshly rebuilt one.
     * Only the accumulation order on an object which absorbs several others differs, which
     * may change the last bits.
     */
    @Test
    public void reusedTreeMergeDecisionsMatchAFreshTree() {
        int merges = 0;
        for (int seed = 1; seed <= 10; seed++) {
            DataDouble reuse = denseScene(seed);
            DataDouble fresh = denseScene(seed);
            stepWithReusedTree(reuse, true);
            stepWithFreshTree(fresh, true);

            for (int i = 0; i < fresh.n; i++) {
                assertEquals(reuse.deleted[i], fresh.deleted[i], "Seed " + seed + ": object " + i + " deleted flag");
                if (fresh.deleted[i]) {
                    merges++;
                } else {
                    assertEquals(reuse.mass[i], fresh.mass[i], Math.abs(fresh.mass[i]) * 1e-12,
                                 "Seed " + seed + ": object " + i + " mass");
                    assertEquals(reuse.positionX[i], fresh.positionX[i],
                                 Math.abs(fresh.positionX[i]) * 1e-12 + 1e-15, "Seed " + seed + ": object " + i + " position X");
                    assertEquals(reuse.velocityX[i], fresh.velocityX[i],
                                 Math.abs(fresh.velocityX[i]) * 1e-12 + 1e-15, "Seed " + seed + ": object " + i + " velocity X");
                }
            }
        }
        assertTrue(merges > 0, "The scenes should contain merges, otherwise the test proves nothing");
    }

    /**
     * Elastic collisions read everything from the snapshots, so the only difference a reused
     * tree can make is the order in which several simultaneous contacts of one object are
     * accumulated - the last bits, like the tree-order/index-order difference of the brute
     * force kernel.
     */
    @Test
    public void reusedTreeElasticResultsMatchAFreshTreeWithinTolerance() {
        int collided = 0;
        for (int seed = 1; seed <= 10; seed++) {
            DataDouble reuse = denseScene(seed);
            DataDouble fresh = denseScene(seed);
            stepWithReusedTree(reuse, false);
            stepWithFreshTree(fresh, false);

            for (int i = 0; i < fresh.n; i++) {
                assertEquals(reuse.velocityX[i], fresh.velocityX[i],
                             Math.abs(fresh.velocityX[i]) * 1e-12 + 1e-15, "Seed " + seed + ": object " + i + " velocity X");
                assertEquals(reuse.velocityY[i], fresh.velocityY[i],
                             Math.abs(fresh.velocityY[i]) * 1e-12 + 1e-15, "Seed " + seed + ": object " + i + " velocity Y");
                assertEquals(reuse.positionX[i], fresh.positionX[i],
                             Math.abs(fresh.positionX[i]) * 1e-12 + 1e-15, "Seed " + seed + ": object " + i + " position X");
                /* The de-penetration push is the only thing which moves an object in the
                 * collision phase, so a moved object means a processed contact. */
                if (fresh.positionX[i] != fresh.readOnlyPositionX[i]) {
                    collided++;
                }
            }
        }
        assertTrue(collided > 0, "The scenes should contain collisions, otherwise the test proves nothing");
    }

    /** The float kernels must find the fast mover through the reused tree too. */
    @Test
    public void floatFastMoverIsFoundThroughTheReusedTree() {
        DataFloat brute = fastMoverSceneFloat();
        FlatBarnesHutTree bruteTree = new FlatBarnesHutTree(brute.n);
        FloatBarnesHutTreeArrays bruteArrays = new FloatBarnesHutTreeArrays(bruteTree);
        moveFloat(brute, bruteTree, bruteArrays);
        brute.copyToReadOnly(false);
        new ProcessCollisionsLogicFloat(brute, false, (float) COR).runOnCpu();

        DataFloat reuse = fastMoverSceneFloat();
        FlatBarnesHutTree tree = new FlatBarnesHutTree(reuse.n);
        FloatBarnesHutTreeArrays treeArrays = new FloatBarnesHutTreeArrays(tree);
        float slack = moveFloat(reuse, tree, treeArrays);
        reuse.copyToReadOnly(false);
        BarnesHutProcessCollisionsLogicFloat reusedKernel =
                new BarnesHutProcessCollisionsLogicFloat(reuse, tree, treeArrays, false, (float) COR);
        reusedKernel.setSlack(slack);
        reusedKernel.runOnCpu();

        assertTrue(slack > 49, "The objects should have moved fast, otherwise the test proves nothing");
        assertTrue(brute.velocityX[0] != fastMoverSceneFloat().velocityX[0], "The fast pair should have collided");
        for (int i = 0; i < brute.n; i++) {
            assertEquals(reuse.velocityX[i], brute.velocityX[i], 0.0f, "Object " + i + " velocity X");
            assertEquals(reuse.positionX[i], brute.positionX[i], 0.0f, "Object " + i + " position X");
        }
    }

    /* Two fast objects approaching head on, plus a far away cluster which gives the tree
     * enough structure for the pruning to actually discard the partner's stale node. */
    private static DataDouble fastMoverScene() {
        double[] x = {-50.2, 50.2, 300, 301.5, 300, 301.5, 300.7};
        double[] y = {0, 0, 300, 300, 301.5, 301.5, 300.7};
        double[] vx = {50, -50, 0, 0, 0, 0, 0};
        double[] vy = new double[7];
        double[] m = {1e10, 1e10, 1e10, 1e10, 1e10, 1e10, 1e10};
        double[] r = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.2};
        return data(x, y, vx, vy, m, r);
    }

    private static DataFloat fastMoverSceneFloat() {
        DataDouble source = fastMoverScene();
        DataFloat data = new DataFloat(source.n);
        for (int i = 0; i < source.n; i++) {
            data.positionX[i] = (float) source.positionX[i];
            data.positionY[i] = (float) source.positionY[i];
            data.velocityX[i] = (float) source.velocityX[i];
            data.velocityY[i] = (float) source.velocityY[i];
            data.mass[i] = (float) source.mass[i];
            data.radius[i] = (float) source.radius[i];
        }
        data.copyToReadOnly(true);
        return data;
    }

    /* A dense scene with many overlaps; slow enough that the slack stays small. */
    private static DataDouble denseScene(int seed) {
        Random random = new Random(seed);
        int n = 60;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] vx = new double[n];
        double[] vy = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = (random.nextDouble() - 0.5) * 200;
            y[i] = (random.nextDouble() - 0.5) * 200;
            vx[i] = (random.nextDouble() - 0.5) * 4;
            vy[i] = (random.nextDouble() - 0.5) * 4;
            m[i] = 1e10 * (1 + random.nextDouble());
            r[i] = 2 + random.nextDouble() * 4;
        }
        return data(x, y, vx, vy, m, r);
    }

    /** One iteration the way SimulationDouble drives it now: one tree, reused with the slack. */
    private static void stepWithReusedTree(DataDouble data, boolean merge) {
        data.copyToReadOnly(merge);
        FlatBarnesHutTree tree = buildTree(data);
        double slack = moveAndComputeSlack(data, tree);
        data.copyToReadOnly(merge);
        BarnesHutProcessCollisionsLogicDouble collisions = new BarnesHutProcessCollisionsLogicDouble(data, tree, merge, COR);
        collisions.setSlack(slack);
        collisions.runOnSingleThread();
    }

    /** One iteration the old way: a second tree built from the post-move snapshots. */
    private static void stepWithFreshTree(DataDouble data, boolean merge) {
        data.copyToReadOnly(merge);
        FlatBarnesHutTree tree = buildTree(data);
        moveAndComputeSlack(data, tree);
        data.copyToReadOnly(merge);
        FlatBarnesHutTree freshTree = buildTree(data);
        new BarnesHutProcessCollisionsLogicDouble(data, freshTree, merge, COR).runOnSingleThread();
    }

    private static double moveAndComputeSlack(DataDouble data) {
        data.copyToReadOnly(false);
        return moveAndComputeSlack(data, buildTree(data));
    }

    private static double moveAndComputeSlack(DataDouble data, FlatBarnesHutTree tree) {
        new BarnesHutMoveObjectsLogicDouble(data, tree, THETA * THETA, DT, MIN_DISTANCE, 0, 0).runOnCpu();
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

    private static float moveFloat(DataFloat data, FlatBarnesHutTree tree, FloatBarnesHutTreeArrays treeArrays) {
        data.copyToReadOnly(false);
        double[] sx = new double[data.n];
        double[] sy = new double[data.n];
        double[] sm = new double[data.n];
        double[] sr = new double[data.n];
        for (int i = 0; i < data.n; i++) {
            sx[i] = data.readOnlyPositionX[i];
            sy[i] = data.readOnlyPositionY[i];
            sm[i] = data.readOnlyMass[i];
            sr[i] = data.readOnlyRadius[i];
        }
        assertTrue(tree.build(sx, sy, sm, sr, data.readOnlyDeleted, data.n), "Tree should fit");
        treeArrays.refresh(tree);
        new BarnesHutMoveObjectsLogicFloat(data, tree, treeArrays, (float) (THETA * THETA), (float) DT,
                                           (float) MIN_DISTANCE, 0, 0).runOnCpu();
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

    private static FlatBarnesHutTree buildTree(DataDouble data) {
        FlatBarnesHutTree tree = new FlatBarnesHutTree(data.n);
        assertTrue(tree.build(data.readOnlyPositionX, data.readOnlyPositionY, data.readOnlyMass,
                              data.readOnlyRadius, data.readOnlyDeleted, data.n), "Tree should fit");
        return tree;
    }

    private static DataDouble data(double[] x, double[] y, double[] vx, double[] vy, double[] mass, double[] radius) {
        int n = x.length;
        DataDouble data = new DataDouble(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = x[i];
            data.positionY[i] = y[i];
            data.velocityX[i] = vx[i];
            data.velocityY[i] = vy[i];
            data.mass[i] = mass[i];
            data.radius[i] = radius[i];
        }
        data.copyToReadOnly(true);
        return data;
    }
}
