package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;
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
 * The Barnes-Hut collision kernel finds the overlapping objects through the tree, but processes
 * them with exactly the same code as the brute force kernel. The pairs are only visited in tree
 * order instead of index order, so: merge decisions (deleted flags) and single-contact results
 * match the brute force kernel bit for bit, while accumulated values on objects with several
 * simultaneous contacts can differ in the last bits (different summation order).
 */
public class BarnesHutProcessCollisionsKernelTest {
    private static final int BODIES = 60;
    private static final double COR = 0.75;

    private double[] x;
    private double[] y;
    private double[] vx;
    private double[] vy;
    private double[] mass;
    private double[] radius;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);

        /* Deterministic dense scene with several overlapping pairs */
        Random random = new Random(31);
        x = new double[BODIES];
        y = new double[BODIES];
        vx = new double[BODIES];
        vy = new double[BODIES];
        mass = new double[BODIES];
        radius = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            x[i] = (random.nextDouble() - 0.5) * 200;
            y[i] = (random.nextDouble() - 0.5) * 200;
            vx[i] = (random.nextDouble() - 0.5) * 4;
            vy[i] = (random.nextDouble() - 0.5) * 4;
            mass[i] = 1e10 * (1 + random.nextDouble());
            radius[i] = 2 + random.nextDouble() * 4;
        }
    }

    @Test
    public void elasticSingleContactPairsMatchTheBruteKernelExactly() {
        /* Isolated overlapping pairs: every object has exactly one contact, so the
         * accumulation order does not matter and the results must be bit-identical. */
        double[] px = {0, 1.5, 100, 101.5, 200, 201.5};
        double[] pvx = {1, -1, 2, 0, 0, 0};
        double[] pm = {1e10, 1e10, 2e10, 1e10, 1e10, 3e10};
        double[] pr = {1, 1, 1, 1, 1, 1};

        DataDouble brute = data(px, new double[6], pvx, new double[6], pm, pr);
        new ProcessCollisionsLogicDouble(brute, false, COR).runOnSingleThread();

        DataDouble bh = data(px, new double[6], pvx, new double[6], pm, pr);
        new BarnesHutProcessCollisionsLogicDouble(bh, buildTree(bh), false, COR).runOnSingleThread();

        for (int i = 0; i < px.length; i++) {
            assertEquals(bh.velocityX[i], brute.velocityX[i], 0.0, "Object " + i + " velocity X");
            assertEquals(bh.velocityY[i], brute.velocityY[i], 0.0, "Object " + i + " velocity Y");
            assertEquals(bh.positionX[i], brute.positionX[i], 0.0, "Object " + i + " position X");
            assertEquals(bh.positionY[i], brute.positionY[i], 0.0, "Object " + i + " position Y");
        }
        assertTrue(brute.velocityX[0] != 1, "The first pair should have collided");
    }

    @Test
    public void elasticDenseSceneMatchesTheBruteKernelWithinTolerance() {
        DataDouble brute = data(x, y, vx, vy, mass, radius);
        new ProcessCollisionsLogicDouble(brute, false, COR).runOnSingleThread();

        DataDouble bh = data(x, y, vx, vy, mass, radius);
        new BarnesHutProcessCollisionsLogicDouble(bh, buildTree(bh), false, COR).runOnSingleThread();

        int collided = 0;
        for (int i = 0; i < BODIES; i++) {
            assertEquals(bh.velocityX[i], brute.velocityX[i],
                         Math.abs(brute.velocityX[i]) * 1e-12 + 1e-15, "Object " + i + " velocity X");
            assertEquals(bh.velocityY[i], brute.velocityY[i],
                         Math.abs(brute.velocityY[i]) * 1e-12 + 1e-15, "Object " + i + " velocity Y");
            assertEquals(bh.positionX[i], brute.positionX[i],
                         Math.abs(brute.positionX[i]) * 1e-12 + 1e-15, "Object " + i + " position X");
            if (brute.velocityX[i] != vx[i]) {
                collided++;
            }
        }
        assertTrue(collided > 0, "The scene should contain collisions, otherwise the test proves nothing");
    }

    @Test
    public void giantObjectIsDetectedAcrossTheScene() {
        /* The small object overlaps the giant only because of the giant's radius. If the tree
         * pruned nodes by the query object's radius alone, this collision would be missed.
         * The objects do not move, so only the de-penetration push applies. */
        double[] px = {0, 50, 300};
        double[] pr = {1, 100, 1};
        double[] pm = {1e10, 1e12, 1e10};

        DataDouble brute = data(px, new double[3], new double[3], new double[3], pm, pr);
        new ProcessCollisionsLogicDouble(brute, false, COR).runOnSingleThread();

        DataDouble bh = data(px, new double[3], new double[3], new double[3], pm, pr);
        new BarnesHutProcessCollisionsLogicDouble(bh, buildTree(bh), false, COR).runOnSingleThread();

        assertTrue(bh.positionX[0] < 0, "The small object must be pushed out of the giant");
        assertEquals(bh.positionX[0], brute.positionX[0], 0.0, "Same push as brute force");
        assertEquals(bh.positionX[1], brute.positionX[1], 0.0, "Same push of the giant");
        assertEquals(bh.positionX[2], 300.0, 0.0, "The far object must not move");
    }

    @Test
    public void separatingOverlapGetsPushButNoImpulse() {
        /* Overlapping but moving apart: the approach guard skips the impulse,
         * the de-penetration push still applies. Single pair - bit-identical. */
        double[] px = {0, 1.5};
        double[] pvx = {-1, 1};
        double[] pm = {1e10, 1e10};
        double[] pr = {1, 1};

        DataDouble brute = data(px, new double[2], pvx, new double[2], pm, pr);
        new ProcessCollisionsLogicDouble(brute, false, COR).runOnSingleThread();

        DataDouble bh = data(px, new double[2], pvx, new double[2], pm, pr);
        new BarnesHutProcessCollisionsLogicDouble(bh, buildTree(bh), false, COR).runOnSingleThread();

        assertEquals(bh.velocityX[0], -1.0, 0.0, "Velocity must not change");
        assertEquals(bh.velocityX[1], 1.0, 0.0, "Velocity must not change");
        assertEquals(bh.positionX[0], brute.positionX[0], 0.0, "Same push as brute force");
        assertTrue(bh.positionX[0] < 0, "Pushed apart");
    }

    @Test
    public void mergeDecisionsMatchTheBruteKernelExactly() {
        DataDouble brute = data(x, y, vx, vy, mass, radius);
        new ProcessCollisionsLogicDouble(brute, true, COR).runOnSingleThread();

        DataDouble bh = data(x, y, vx, vy, mass, radius);
        new BarnesHutProcessCollisionsLogicDouble(bh, buildTree(bh), true, COR).runOnSingleThread();

        int deletions = 0;
        for (int i = 0; i < BODIES; i++) {
            assertEquals(bh.deleted[i], brute.deleted[i], "Object " + i + " deleted flag");
            if (brute.deleted[i]) {
                deletions++;
            } else {
                assertEquals(bh.mass[i], brute.mass[i],
                             Math.abs(brute.mass[i]) * 1e-12, "Object " + i + " mass");
                assertEquals(bh.positionX[i], brute.positionX[i],
                             Math.abs(brute.positionX[i]) * 1e-12 + 1e-15, "Object " + i + " position X");
            }
        }
        assertTrue(deletions > 0, "The scene should contain merges, otherwise the test proves nothing");
    }

    @Test
    public void multiObjectMergeRulesHoldInTheKernel() {
        /* Sandwich: the middle object is absorbed only by the most massive neighbour. */
        DataDouble sandwich = data(new double[]{0, 1, 2}, new double[3], new double[]{0, 11, 0}, new double[3],
                                   new double[]{10, 1, 5}, new double[]{0.6, 0.5, 0.6});
        new BarnesHutProcessCollisionsLogicDouble(sandwich, buildTree(sandwich), true, COR).runOnSingleThread();

        assertEquals(sandwich.deleted[0], false);
        assertEquals(sandwich.deleted[1], true);
        assertEquals(sandwich.deleted[2], false);
        assertEquals(sandwich.mass[0], 11.0, 0.0, "Winner mass");
        assertEquals(sandwich.mass[2], 5.0, 0.0, "Untouched neighbour mass");
        assertEquals(sandwich.velocityX[0], 1.0, 0.0, "Momentum conserving velocity");

        /* Chain: the middle object is dying, so it must not eat the small one. */
        DataDouble chain = data(new double[]{2, 1, 0}, new double[3], new double[3], new double[3],
                                new double[]{1, 5, 10}, new double[]{0.6, 0.5, 0.6});
        new BarnesHutProcessCollisionsLogicDouble(chain, buildTree(chain), true, COR).runOnSingleThread();

        assertEquals(chain.deleted[0], false);
        assertEquals(chain.deleted[1], true);
        assertEquals(chain.deleted[2], false);
        assertEquals(chain.mass[2], 15.0, 0.0, "Big object absorbs only the middle one");
        assertEquals(chain.mass[0], 1.0, 0.0, "Small object survives untouched");
    }

    @Test
    public void floatKernelMatchesTheBruteFloatKernelWithinTolerance() {
        DataFloat brute = floatData(x, y, vx, vy, mass, radius);
        new ProcessCollisionsLogicFloat(brute, false, (float) COR).runOnCpu();

        DataFloat bh = floatData(x, y, vx, vy, mass, radius);
        FlatBarnesHutTree tree = new FlatBarnesHutTree(BODIES);
        double[] sx = new double[BODIES];
        double[] sy = new double[BODIES];
        double[] sm = new double[BODIES];
        double[] sr = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            sx[i] = bh.readOnlyPositionX[i];
            sy[i] = bh.readOnlyPositionY[i];
            sm[i] = bh.readOnlyMass[i];
            sr[i] = bh.readOnlyRadius[i];
        }
        assertTrue(tree.build(sx, sy, sm, sr, bh.readOnlyDeleted, BODIES));
        FloatBarnesHutTreeArrays treeArrays = new FloatBarnesHutTreeArrays(tree);
        treeArrays.refresh(tree);
        new BarnesHutProcessCollisionsLogicFloat(bh, tree, treeArrays, false, (float) COR).runOnCpu();

        for (int i = 0; i < BODIES; i++) {
            assertEquals(bh.velocityX[i], brute.velocityX[i],
                         Math.abs(brute.velocityX[i]) * 2e-5f + 1e-9f, "Object " + i + " velocity X");
            assertEquals(bh.positionX[i], brute.positionX[i],
                         Math.abs(brute.positionX[i]) * 2e-5f + 1e-9f, "Object " + i + " position X");
        }
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

    private static DataFloat floatData(double[] x, double[] y, double[] vx, double[] vy, double[] mass, double[] radius) {
        int n = x.length;
        DataFloat data = new DataFloat(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = (float) x[i];
            data.positionY[i] = (float) y[i];
            data.velocityX[i] = (float) vx[i];
            data.velocityY[i] = (float) vy[i];
            data.mass[i] = (float) mass[i];
            data.radius[i] = (float) radius[i];
        }
        data.copyToReadOnly(true);
        return data;
    }
}
