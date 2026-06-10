package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.ProcessCollisionsLogicDouble;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Overlapping objects must be pushed apart (de-penetration): each object of the pair moves out
 * along the line between the centers by its inverse-mass share of half the overlap per iteration,
 * so the pair's center of mass stays in place and resting objects relax until they only touch.
 * Velocities must not be affected by the push.
 */
public class DepenetrationTest {
    public static final int PRECISION = 16;

    /* The double and float values in these scenarios are exact binary fractions. The arbitrary
     * precision square root is correct only up to PRECISION digits, hence the small tolerance. */
    private static final double AP_TOLERANCE = 1e-13;

    private static class Result {
        String name;
        double tolerance;
        double[] positionX;
        double[] positionY;
        double[] velocityX;
    }

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void restingOverlappingPairIsPushedApartPreservingCenterOfMass() {
        /* r=1 each at x=0 and x=1.5: overlap 0.5. Each object moves out by
         * 0.5 (overlap) * 0.5 (equal mass share) * 0.5 (rate) = 0.125. */
        for (Result result : runAll(new double[]{0, 1.5}, new double[]{1, 1})) {
            assertEquals(result.positionX[0], -0.125, result.tolerance, result.name + ": left object pushed left");
            assertEquals(result.positionX[1], 1.625, result.tolerance, result.name + ": right object pushed right");
            assertEquals(result.velocityX[0], 0.0, 0.0, result.name + ": velocity not affected");
            assertEquals(result.velocityX[1], 0.0, 0.0, result.name + ": velocity not affected");
            assertEquals(centerOfMassX(result, new double[]{1, 1}), 0.75, result.tolerance, result.name + ": center of mass preserved");
        }
    }

    @Test
    public void pushIsSharedInverselyToMass() {
        /* Masses 1 and 3: the light object takes 3/4 of the push, the heavy one 1/4. */
        for (Result result : runAll(new double[]{0, 1.5}, new double[]{1, 3})) {
            assertEquals(result.positionX[0], -0.1875, result.tolerance, result.name + ": light object moves more");
            assertEquals(result.positionX[1], 1.5625, result.tolerance, result.name + ": heavy object moves less");
            assertEquals(centerOfMassX(result, new double[]{1, 3}), 1.125, result.tolerance, result.name + ": center of mass preserved");
        }
    }

    @Test
    public void coincidingCentersAreLeftUntouched() {
        /* No defined direction to push: positions must stay unchanged and must not become NaN. */
        for (Result result : runAll(new double[]{1, 1}, new double[]{1, 1})) {
            assertEquals(result.positionX[0], 1.0, 0.0, result.name);
            assertEquals(result.positionX[1], 1.0, 0.0, result.name);
            assertFalse(Double.isNaN(result.positionY[0]), result.name);
            assertFalse(Double.isNaN(result.positionY[1]), result.name);
        }
    }

    @Test
    public void objectsThatOnlyTouchOrAreApartAreNotMoved() {
        /* Distance 3 >= r1 + r2 = 2: no overlap, nothing happens. */
        for (Result result : runAll(new double[]{0, 3}, new double[]{1, 1})) {
            assertEquals(result.positionX[0], 0.0, 0.0, result.name);
            assertEquals(result.positionX[1], 3.0, 0.0, result.name);
        }
    }

    @Test
    public void approachingPairGetsBothImpulseAndPush() {
        /* Equal masses moving towards each other with cor=1 exchange (reverse) velocities,
         * and are also pushed apart by 0.125 each. */
        for (Result result : runAll(new double[]{0, 1.5}, new double[]{1, 1}, new double[]{1, -1})) {
            assertEquals(result.velocityX[0], -1.0, result.tolerance, result.name + ": velocity reversed");
            assertEquals(result.velocityX[1], 1.0, result.tolerance, result.name + ": velocity reversed");
            assertEquals(result.positionX[0], -0.125, result.tolerance, result.name + ": pushed apart");
            assertEquals(result.positionX[1], 1.625, result.tolerance, result.name + ": pushed apart");
        }
    }

    private static double centerOfMassX(Result result, double[] mass) {
        double weighted = 0;
        double total = 0;
        for (int i = 0; i < mass.length; i++) {
            weighted += result.positionX[i] * mass[i];
            total += mass[i];
        }
        return weighted / total;
    }

    private Result[] runAll(double[] x, double[] m) {
        return runAll(x, m, new double[x.length]);
    }

    private Result[] runAll(double[] x, double[] m, double[] vx) {
        return new Result[]{runDouble(x, m, vx), runFloat(x, m, vx), runAP(x, m, vx)};
    }

    private Result runDouble(double[] x, double[] m, double[] vx) {
        int n = x.length;
        DataDouble data = new DataDouble(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = x[i];
            data.mass[i] = m[i];
            data.radius[i] = 1;
            data.velocityX[i] = vx[i];
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicDouble(data, false, 1).runOnSingleThread();

        Result result = new Result();
        result.name = "Double";
        result.tolerance = 0;
        result.positionX = data.positionX;
        result.positionY = data.positionY;
        result.velocityX = data.velocityX;
        return result;
    }

    private Result runFloat(double[] x, double[] m, double[] vx) {
        int n = x.length;
        DataFloat data = new DataFloat(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = (float) x[i];
            data.mass[i] = (float) m[i];
            data.radius[i] = 1;
            data.velocityX[i] = (float) vx[i];
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicFloat(data, false, 1).runOnCpu();

        Result result = new Result();
        result.name = "Float";
        result.tolerance = 0;
        result.positionX = toDouble(data.positionX);
        result.positionY = toDouble(data.positionY);
        result.velocityX = toDouble(data.velocityX);
        return result;
    }

    private Result runAP(double[] x, double[] m, double[] vx) {
        int n = x.length;
        DataAP data = new DataAP(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(0);
            data.mass[i] = New.num(m[i]);
            data.radius[i] = ONE;
            data.velocityX[i] = New.num(vx[i]);
            data.velocityY[i] = New.num(0);
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicAP(data, false, ONE).runOnSingleThread();

        Result result = new Result();
        result.name = "AP";
        result.tolerance = AP_TOLERANCE;
        result.positionX = new double[n];
        result.positionY = new double[n];
        result.velocityX = new double[n];
        for (int i = 0; i < n; i++) {
            result.positionX[i] = data.positionX[i].doubleValue();
            result.positionY[i] = data.positionY[i].doubleValue();
            result.velocityX[i] = data.velocityX[i].doubleValue();
        }
        return result;
    }

    private static double[] toDouble(float[] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }
}
