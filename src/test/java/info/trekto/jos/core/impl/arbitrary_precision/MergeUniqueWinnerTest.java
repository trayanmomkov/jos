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

/**
 * When several objects overlap in the same iteration, the mass of an absorbed object must be
 * added to exactly one absorber (the unique winner: the most massive overlapping bigger object,
 * smaller index on a tie), and an object which is being absorbed must not absorb anything itself.
 * Otherwise mass is duplicated (one object absorbed by two others) or lost (an object eaten by a
 * dying neighbour). The total mass of the alive objects must always be conserved.
 */
public class MergeUniqueWinnerTest {
    public static final int PRECISION = 16;

    private static class Result {
        boolean[] deleted;
        double[] mass;
        double[] velocityX;
        double[] positionX;
    }

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void smallObjectBetweenTwoBiggerIsAbsorbedOnlyByTheMostMassive() {
        /* Middle object (m=1, v=11) overlaps both neighbours; the neighbours do not overlap.
         * Only the most massive neighbour (m=10) absorbs it. The other neighbour stays untouched. */
        double[] x = {0, 1, 2};
        double[] m = {10, 1, 5};
        double[] r = {0.6, 0.5, 0.6};
        double[] vx = {0, 11, 0};

        for (Result result : runAll(x, m, r, vx)) {
            assertEquals(result.deleted, new boolean[]{false, true, false});
            assertEquals(result.mass[0], 11.0, 0.0, "Winner mass");
            assertEquals(result.mass[2], 5.0, 0.0, "Untouched neighbour mass");
            assertEquals(result.velocityX[0], 1.0, 0.0, "Momentum conserving velocity");
            assertEquals(result.velocityX[2], 0.0, 0.0, "Untouched neighbour velocity");
            assertEquals(result.positionX[2], 2.0, 0.0, "Untouched neighbour position");
            assertEquals(aliveMass(result), 16.0, 0.0, "Total mass");
        }
    }

    @Test
    public void onEqualMassesTheLowerIndexWins() {
        double[] x = {0, 1, 2};
        double[] m = {5, 1, 5};
        double[] r = {0.6, 0.5, 0.6};
        double[] vx = {0, 0, 0};

        for (Result result : runAll(x, m, r, vx)) {
            assertEquals(result.deleted, new boolean[]{false, true, false});
            assertEquals(result.mass[0], 6.0, 0.0, "Winner mass");
            assertEquals(result.mass[2], 5.0, 0.0, "Loser mass");
            assertEquals(aliveMass(result), 11.0, 0.0, "Total mass");
        }
    }

    @Test
    public void objectBeingAbsorbedDoesNotAbsorbItsSmallerNeighbour() {
        /* Chain: small (m=1) - middle (m=5) - big (m=10), only the neighbouring pairs overlap.
         * The middle object is absorbed by the big one, so it must not eat the small one,
         * and the small one must survive. The old logic lost the small object's mass entirely. */
        double[] x = {2, 1, 0};
        double[] m = {1, 5, 10};
        double[] r = {0.6, 0.5, 0.6};
        double[] vx = {0, 0, 0};

        for (Result result : runAll(x, m, r, vx)) {
            assertEquals(result.deleted, new boolean[]{false, true, false});
            assertEquals(result.mass[2], 15.0, 0.0, "Big object absorbs only the middle one");
            assertEquals(result.mass[0], 1.0, 0.0, "Small object survives untouched");
            assertEquals(result.positionX[0], 2.0, 0.0, "Small object position untouched");
            assertEquals(aliveMass(result), 16.0, 0.0, "Total mass");
        }
    }

    @Test
    public void twoOverlappingObjectsMergeIntoTheBigger() {
        double[] x = {0, 1};
        double[] m = {3, 1};
        double[] r = {0.6, 0.5};
        double[] vx = {0, 0};

        for (Result result : runAll(x, m, r, vx)) {
            assertEquals(result.deleted, new boolean[]{false, true});
            assertEquals(result.mass[0], 4.0, 0.0, "Merged mass");
            assertEquals(aliveMass(result), 4.0, 0.0, "Total mass");
        }
    }

    private static double aliveMass(Result result) {
        double sum = 0;
        for (int i = 0; i < result.mass.length; i++) {
            if (!result.deleted[i]) {
                sum += result.mass[i];
            }
        }
        return sum;
    }

    private Result[] runAll(double[] x, double[] m, double[] r, double[] vx) {
        return new Result[]{runDouble(x, m, r, vx), runFloat(x, m, r, vx), runAP(x, m, r, vx)};
    }

    private Result runDouble(double[] x, double[] m, double[] r, double[] vx) {
        int n = x.length;
        DataDouble data = new DataDouble(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = x[i];
            data.mass[i] = m[i];
            data.radius[i] = r[i];
            data.velocityX[i] = vx[i];
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicDouble(data, true, 1).runOnSingleThread();

        Result result = new Result();
        result.deleted = data.deleted;
        result.mass = data.mass;
        result.velocityX = data.velocityX;
        result.positionX = data.positionX;
        return result;
    }

    private Result runFloat(double[] x, double[] m, double[] r, double[] vx) {
        int n = x.length;
        DataFloat data = new DataFloat(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = (float) x[i];
            data.mass[i] = (float) m[i];
            data.radius[i] = (float) r[i];
            data.velocityX[i] = (float) vx[i];
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicFloat(data, true, 1).runOnCpu();

        Result result = new Result();
        result.deleted = data.deleted;
        result.mass = toDouble(data.mass);
        result.velocityX = toDouble(data.velocityX);
        result.positionX = toDouble(data.positionX);
        return result;
    }

    private Result runAP(double[] x, double[] m, double[] r, double[] vx) {
        int n = x.length;
        DataAP data = new DataAP(n);
        for (int i = 0; i < n; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(0);
            data.mass[i] = New.num(m[i]);
            data.radius[i] = New.num(r[i]);
            data.velocityX[i] = New.num(vx[i]);
            data.velocityY[i] = New.num(0);
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicAP(data, true, ONE).runOnSingleThread();

        Result result = new Result();
        result.deleted = data.deleted;
        result.mass = new double[n];
        result.velocityX = new double[n];
        result.positionX = new double[n];
        for (int i = 0; i < n; i++) {
            result.mass[i] = data.mass[i].doubleValue();
            result.velocityX[i] = data.velocityX[i].doubleValue();
            result.positionX[i] = data.positionX[i].doubleValue();
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
