package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;
import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.impl.arbitrary_precision.MoveObjectsLogicAP;
import info.trekto.jos.core.impl.single_precision.BarnesHutMoveObjectsLogicFloat;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.FloatBarnesHutTreeArrays;
import info.trekto.jos.core.impl.single_precision.MoveObjectsLogicFloat;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The Barnes-Hut gravity kernel walks the flat tree, which mirrors the object tree used by
 * the CPU implementation. With double numbers the kernel must therefore produce bit-identical
 * results to the CPU Barnes-Hut path - that is the main correctness anchor, because the OpenCL
 * translation itself can only be verified on a real GPU. Here the kernels run their plain
 * Java path (runOnCpu).
 */
public class BarnesHutMoveObjectsKernelTest {
    private static final int BODIES = 60;
    private static final double DT = 0.001;
    private static final double MIN_DISTANCE = 1;
    private static final double THETA = 0.7;

    private double[] x;
    private double[] y;
    private double[] vx;
    private double[] vy;
    private double[] mass;
    private double[] radius;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);

        Random random = new Random(21);
        x = new double[BODIES];
        y = new double[BODIES];
        vx = new double[BODIES];
        vy = new double[BODIES];
        mass = new double[BODIES];
        radius = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            x[i] = (random.nextDouble() - 0.5) * 1000;
            y[i] = (random.nextDouble() - 0.5) * 1000;
            vx[i] = (random.nextDouble() - 0.5) * 4;
            vy[i] = (random.nextDouble() - 0.5) * 4;
            mass[i] = 1e10 * (1 + random.nextDouble());
            radius[i] = 1 + random.nextDouble() * 3;
        }
    }

    @Test
    public void kernelMatchesTheCpuBarnesHutBitwise() {
        assertKernelMatchesCpuTree(x, y, vx, vy, mass, radius);
    }

    @Test
    public void kernelMatchesTheCpuBarnesHutBitwiseWithCoincidentObjects() {
        /* Three coincident objects end in a depth-cap bucket; a probe sees them as one point mass. */
        assertKernelMatchesCpuTree(new double[]{0, 0, 0, 100}, new double[]{0, 0, 0, 0},
                                   new double[4], new double[4],
                                   new double[]{1e10, 1e10, 1e10, 1}, new double[]{1, 1, 1, 1});
    }

    private static void assertKernelMatchesCpuTree(double[] x, double[] y, double[] vx, double[] vy,
                                                   double[] mass, double[] radius) {
        int n = x.length;
        DataAP ap = apData(x, y, vx, vy, mass, radius);
        new MoveObjectsLogicAP(ap, New.num(DT), New.num(MIN_DISTANCE), New.num(THETA), 0, 0).runOnCpu();

        DataDouble data = doubleData(x, y, vx, vy, mass, radius);
        FlatBarnesHutTree tree = new FlatBarnesHutTree(n);
        assertTrue(tree.build(data.readOnlyPositionX, data.readOnlyPositionY, data.readOnlyMass,
                              data.readOnlyRadius, data.readOnlyDeleted, n));
        new BarnesHutMoveObjectsLogicDouble(data, tree, THETA * THETA, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        for (int i = 0; i < n; i++) {
            assertEquals(data.positionX[i], ap.positionX[i].doubleValue(), 0.0, "Object " + i + " position X");
            assertEquals(data.positionY[i], ap.positionY[i].doubleValue(), 0.0, "Object " + i + " position Y");
            assertEquals(data.velocityX[i], ap.velocityX[i].doubleValue(), 0.0, "Object " + i + " velocity X");
            assertEquals(data.velocityY[i], ap.velocityY[i].doubleValue(), 0.0, "Object " + i + " velocity Y");
        }
    }

    @Test
    public void tinyThetaMatchesTheBruteKernelWithinTolerance() {
        /* With a tiny theta every node is opened and all interactions resolve at the leaves -
         * the same pairs as the brute kernel, summed in tree order instead of index order. */
        DataDouble brute = doubleData(x, y, vx, vy, mass, radius);
        new MoveObjectsLogicDouble(brute, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        DataDouble bh = doubleData(x, y, vx, vy, mass, radius);
        FlatBarnesHutTree tree = new FlatBarnesHutTree(BODIES);
        assertTrue(tree.build(bh.readOnlyPositionX, bh.readOnlyPositionY, bh.readOnlyMass,
                              bh.readOnlyRadius, bh.readOnlyDeleted, BODIES));
        new BarnesHutMoveObjectsLogicDouble(bh, tree, 1e-4 * 1e-4, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        for (int i = 0; i < BODIES; i++) {
            assertEquals(bh.positionX[i], brute.positionX[i], 0.0, "Object " + i + " position X");
            assertEquals(bh.velocityX[i], brute.velocityX[i],
                         Math.abs(brute.velocityX[i]) * 1e-12 + 1e-18, "Object " + i + " velocity X");
            assertEquals(bh.velocityY[i], brute.velocityY[i],
                         Math.abs(brute.velocityY[i]) * 1e-12 + 1e-18, "Object " + i + " velocity Y");
        }
    }

    @Test
    public void moderateThetaStaysWithinAFewPercentOfTheBruteKernel() {
        DataDouble brute = doubleData(x, y, vx, vy, mass, radius);
        new MoveObjectsLogicDouble(brute, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        DataDouble bh = doubleData(x, y, vx, vy, mass, radius);
        FlatBarnesHutTree tree = new FlatBarnesHutTree(BODIES);
        assertTrue(tree.build(bh.readOnlyPositionX, bh.readOnlyPositionY, bh.readOnlyMass,
                              bh.readOnlyRadius, bh.readOnlyDeleted, BODIES));
        new BarnesHutMoveObjectsLogicDouble(bh, tree, 0.5 * 0.5, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        /* An object near the center of the scene has a small net force, which makes its plain
         * relative error ill conditioned - such objects are measured against the scene scale. */
        double maxExactNorm = 0;
        for (int i = 0; i < BODIES; i++) {
            maxExactNorm = Math.max(maxExactNorm, Math.hypot(brute.velocityX[i] - vx[i], brute.velocityY[i] - vy[i]));
        }
        for (int i = 0; i < BODIES; i++) {
            double exactDx = brute.velocityX[i] - vx[i];
            double exactDy = brute.velocityY[i] - vy[i];
            double errorNorm = Math.hypot(bh.velocityX[i] - brute.velocityX[i], bh.velocityY[i] - brute.velocityY[i]);
            double relativeError = errorNorm / Math.max(Math.hypot(exactDx, exactDy), 0.05 * maxExactNorm);
            assertTrue(relativeError < 0.05, "Object " + i + " relative acceleration error: " + relativeError);
        }
    }

    @Test
    public void minDistanceClampAppliesInTheKernel() {
        double[] px = {0, 1};
        double[] pm = {1, 1e10};
        DataDouble brute = doubleData(px, new double[2], new double[2], new double[2], pm, new double[]{0.1, 0.1});
        new MoveObjectsLogicDouble(brute, 1, 10, 0, 0).runOnCpu();

        DataDouble bh = doubleData(px, new double[2], new double[2], new double[2], pm, new double[]{0.1, 0.1});
        FlatBarnesHutTree tree = new FlatBarnesHutTree(2);
        assertTrue(tree.build(bh.readOnlyPositionX, bh.readOnlyPositionY, bh.readOnlyMass,
                              bh.readOnlyRadius, bh.readOnlyDeleted, 2));
        new BarnesHutMoveObjectsLogicDouble(bh, tree, 1e-4 * 1e-4, 1, 10, 0, 0).runOnCpu();

        assertEquals(bh.velocityX[0], brute.velocityX[0], 0.0, "Clamped force");
        assertTrue(bh.velocityX[0] > 0, "Object should be attracted");
    }

    @Test
    public void deletedObjectsDoNotMoveAndExertNoForce() {
        DataDouble data = doubleData(new double[]{0, 10}, new double[2], new double[]{0, 5}, new double[2],
                                     new double[]{1, 1e15}, new double[]{1, 1});
        data.deleted[1] = true;
        data.copyToReadOnly(true);

        FlatBarnesHutTree tree = new FlatBarnesHutTree(2);
        assertTrue(tree.build(data.readOnlyPositionX, data.readOnlyPositionY, data.readOnlyMass,
                              data.readOnlyRadius, data.readOnlyDeleted, 2));
        new BarnesHutMoveObjectsLogicDouble(data, tree, THETA * THETA, 1, 1, 0, 0).runOnCpu();

        assertEquals(data.velocityX[0], 0.0, 0.0, "No force from the deleted object");
        assertEquals(data.positionX[1], 10.0, 0.0, "Deleted object does not move");
    }

    @Test
    public void singleObjectJustMoves() {
        DataDouble data = doubleData(new double[]{1}, new double[]{2}, new double[]{3}, new double[]{-4},
                                     new double[]{1e10}, new double[]{1});
        FlatBarnesHutTree tree = new FlatBarnesHutTree(1);
        assertTrue(tree.build(data.readOnlyPositionX, data.readOnlyPositionY, data.readOnlyMass,
                              data.readOnlyRadius, data.readOnlyDeleted, 1));
        new BarnesHutMoveObjectsLogicDouble(data, tree, THETA * THETA, 0.5, 1, 0, 0).runOnCpu();

        assertEquals(data.positionX[0], 2.5, 0.0);
        assertEquals(data.positionY[0], 0.0, 0.0);
        assertEquals(data.velocityX[0], 3.0, 0.0, "Velocity unchanged");
        assertEquals(data.velocityY[0], -4.0, 0.0, "Velocity unchanged");
    }

    @Test
    public void floatKernelWithTinyThetaMatchesTheBruteFloatKernel() {
        DataFloat brute = floatData(x, y, vx, vy, mass, radius);
        new MoveObjectsLogicFloat(brute, (float) DT, (float) MIN_DISTANCE, 0, 0).runOnCpu();

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
        new BarnesHutMoveObjectsLogicFloat(bh, tree, treeArrays, 1e-4f * 1e-4f,
                                           (float) DT, (float) MIN_DISTANCE, 0, 0).runOnCpu();

        for (int i = 0; i < BODIES; i++) {
            assertEquals(bh.positionX[i], brute.positionX[i], 0.0f, "Object " + i + " position X");
            assertEquals(bh.velocityX[i], brute.velocityX[i],
                         Math.abs(brute.velocityX[i]) * 2e-5f + 1e-12f, "Object " + i + " velocity X");
            assertEquals(bh.velocityY[i], brute.velocityY[i],
                         Math.abs(brute.velocityY[i]) * 2e-5f + 1e-12f, "Object " + i + " velocity Y");
        }
    }

    private static DataAP apData(double[] x, double[] y, double[] vx, double[] vy, double[] mass, double[] radius) {
        int n = x.length;
        DataAP data = new DataAP(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(y[i]);
            data.velocityX[i] = New.num(vx[i]);
            data.velocityY[i] = New.num(vy[i]);
            data.mass[i] = New.num(mass[i]);
            data.radius[i] = New.num(radius[i]);
        }
        data.copyToReadOnly(true);
        return data;
    }

    private static DataDouble doubleData(double[] x, double[] y, double[] vx, double[] vy, double[] mass, double[] radius) {
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
