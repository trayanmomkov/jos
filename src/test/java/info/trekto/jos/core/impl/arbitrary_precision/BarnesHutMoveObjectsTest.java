package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * The Barnes-Hut gravity calculation must behave like the exact brute force calculation
 * when the tree is fully opened (very small theta), stay within a small error for
 * moderate theta, and respect deleted objects, the minimum distance clamp and
 * self-exclusion exactly like the brute force code.
 */
public class BarnesHutMoveObjectsTest {
    public static final int PRECISION = 16;
    private static final int BODIES = 50;

    private double[] x;
    private double[] y;
    private double[] vx;
    private double[] vy;
    private double[] mass;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);

        /* Deterministic random scene used by several tests */
        Random random = new Random(42);
        x = new double[BODIES];
        y = new double[BODIES];
        vx = new double[BODIES];
        vy = new double[BODIES];
        mass = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            x[i] = (random.nextDouble() - 0.5) * 1000;
            y[i] = (random.nextDouble() - 0.5) * 1000;
            vx[i] = (random.nextDouble() - 0.5) * 2;
            vy[i] = (random.nextDouble() - 0.5) * 2;
            mass[i] = 1e10 * (1 + random.nextDouble());
        }
    }

    @Test
    public void nullThetaBehavesAsBruteForce() {
        /* Two equal masses 100 m apart, dt = 1: velocity change is G * m / d^2 = 6.6743E-5 m/s. */
        DataAP fiveArg = twoBodyData();
        new MoveObjectsLogicAP(fiveArg, ONE, ONE, 0, 0).runOnCpu();

        DataAP sixArgNull = twoBodyData();
        new MoveObjectsLogicAP(sixArgNull, ONE, ONE, null, 0, 0).runOnCpu();

        Number expected = New.num("0.000066743");
        assertTrue(areEqual(fiveArg.velocityX[0], expected, PRECISION - 2), "5-arg ctor: " + fiveArg.velocityX[0]);
        assertTrue(areEqual(sixArgNull.velocityX[0], expected, PRECISION - 2), "null theta: " + sixArgNull.velocityX[0]);
        assertTrue(areEqual(fiveArg.velocityX[1], expected.negate(), PRECISION - 2));
        assertTrue(areEqual(sixArgNull.velocityX[1], expected.negate(), PRECISION - 2));
    }

    @Test
    public void tinyThetaMatchesBruteForce() {
        /* With a tiny theta every node is opened, so all interactions resolve at the
         * leaves - the same pairs as brute force, only summed in a different order. */
        DataAP exact = sceneData();
        new MoveObjectsLogicAP(exact, New.num("0.001"), ONE, null, 0, 0).runOnCpu();

        DataAP barnesHut = sceneData();
        new MoveObjectsLogicAP(barnesHut, New.num("0.001"), ONE, New.num("0.0001"), 0, 0).runOnCpu();

        for (int i = 0; i < BODIES; i++) {
            assertTrue(areEqual(barnesHut.velocityX[i], exact.velocityX[i], PRECISION - 3), "Object " + i + " velocity X");
            assertTrue(areEqual(barnesHut.velocityY[i], exact.velocityY[i], PRECISION - 3), "Object " + i + " velocity Y");
            assertTrue(areEqual(barnesHut.positionX[i], exact.positionX[i], PRECISION - 3), "Object " + i + " position X");
            assertTrue(areEqual(barnesHut.positionY[i], exact.positionY[i], PRECISION - 3), "Object " + i + " position Y");
        }
    }

    @Test
    public void moderateThetaStaysWithinAFewPercentOfBruteForce() {
        DataAP exact = sceneData();
        new MoveObjectsLogicAP(exact, New.num("0.001"), ONE, null, 0, 0).runOnCpu();

        DataAP barnesHut = sceneData();
        new MoveObjectsLogicAP(barnesHut, New.num("0.001"), ONE, New.num("0.5"), 0, 0).runOnCpu();

        /* Velocity delta = acceleration * dt; compare as 2D vectors in double precision.
         * An object near the center of the scene has a small NET force (large opposite pulls
         * cancel), which makes its plain relative error ill-conditioned. Such objects are
         * therefore measured against the force scale of the scene instead. */
        double[] exactDx = new double[BODIES];
        double[] exactDy = new double[BODIES];
        double maxExactNorm = 0;
        for (int i = 0; i < BODIES; i++) {
            exactDx[i] = exact.velocityX[i].doubleValue() - vx[i];
            exactDy[i] = exact.velocityY[i].doubleValue() - vy[i];
            maxExactNorm = Math.max(maxExactNorm, Math.hypot(exactDx[i], exactDy[i]));
        }
        assertTrue(maxExactNorm > 0, "The scene should produce accelerations");

        double totalRelativeError = 0;
        for (int i = 0; i < BODIES; i++) {
            double bhDx = barnesHut.velocityX[i].doubleValue() - vx[i];
            double bhDy = barnesHut.velocityY[i].doubleValue() - vy[i];
            double exactNorm = Math.hypot(exactDx[i], exactDy[i]);
            double errorNorm = Math.hypot(bhDx - exactDx[i], bhDy - exactDy[i]);
            double relativeError = errorNorm / Math.max(exactNorm, 0.05 * maxExactNorm);
            totalRelativeError += relativeError;
            assertTrue(relativeError < 0.05, "Object " + i + " relative acceleration error: " + relativeError);
        }
        assertTrue(totalRelativeError / BODIES < 0.02,
                   "Mean relative acceleration error: " + totalRelativeError / BODIES);
    }

    @Test
    public void singleObjectExertsNoForceOnItself() {
        DataAP data = new DataAP(1);
        data.positionX[0] = New.num(1);
        data.positionY[0] = New.num(2);
        data.velocityX[0] = New.num(3);
        data.velocityY[0] = New.num(-4);
        data.mass[0] = New.num(1e10);
        data.radius[0] = ONE;
        data.copyToReadOnly(true);

        new MoveObjectsLogicAP(data, New.num("0.5"), ONE, New.num("0.5"), 0, 0).runOnCpu();

        assertTrue(areEqual(data.positionX[0], New.num("2.5"), PRECISION - 2));
        assertTrue(areEqual(data.velocityX[0], New.num(3), PRECISION - 2), "Velocity must not change: " + data.velocityX[0]);
        assertTrue(areEqual(data.velocityY[0], New.num(-4), PRECISION - 2), "Velocity must not change: " + data.velocityY[0]);
    }

    @Test
    public void coincidentClusterAttractsLikeBruteForce() {
        /* Three coincident objects (bucket path in the tree) and a distant probe. */
        double[] px = {0, 0, 0, 100};
        double[] py = {0, 0, 0, 0};
        double[] pm = {1e10, 1e10, 1e10, 1};

        DataAP exact = data(px, py, pm);
        new MoveObjectsLogicAP(exact, ONE, ONE, null, 0, 0).runOnCpu();

        DataAP barnesHut = data(px, py, pm);
        new MoveObjectsLogicAP(barnesHut, ONE, ONE, New.num("0.5"), 0, 0).runOnCpu();

        for (int i = 0; i < px.length; i++) {
            assertTrue(areEqual(barnesHut.velocityX[i], exact.velocityX[i], PRECISION - 3),
                       "Object " + i + ": " + barnesHut.velocityX[i] + " vs " + exact.velocityX[i]);
        }
        /* The probe is pulled by all three cluster objects: 3 * G * m / d^2 toward them. */
        assertTrue(areEqual(exact.velocityX[3], New.num("-0.000200229"), PRECISION - 3),
                   "Probe velocity: " + exact.velocityX[3]);
    }

    @Test
    public void minDistanceClampAppliesInTheTreePath() {
        /* Objects at distance 1 with minDistance = 10: force calculated as if at distance 10. */
        double[] px = {0, 1};
        double[] py = {0, 0};
        double[] pm = {1, 1e10};

        DataAP exact = data(px, py, pm);
        new MoveObjectsLogicAP(exact, ONE, New.num(10), null, 0, 0).runOnCpu();

        DataAP barnesHut = data(px, py, pm);
        new MoveObjectsLogicAP(barnesHut, ONE, New.num(10), New.num("0.0001"), 0, 0).runOnCpu();

        assertTrue(areEqual(barnesHut.velocityX[0], exact.velocityX[0], PRECISION - 3),
                   barnesHut.velocityX[0] + " vs " + exact.velocityX[0]);
        /* G * m2 / 10^2 / 10 = 6.6743E-4 */
        assertTrue(areEqual(exact.velocityX[0], New.num("0.00066743"), PRECISION - 3), "Clamped: " + exact.velocityX[0]);
    }

    @Test
    public void deletedObjectsExertNoForceInTheTreePath() {
        DataAP data = new DataAP(2);
        data.positionX[0] = ZERO;
        data.positionY[0] = ZERO;
        data.velocityX[0] = ZERO;
        data.velocityY[0] = ZERO;
        data.mass[0] = ONE;
        data.radius[0] = ONE;
        data.positionX[1] = New.num(10);
        data.positionY[1] = ZERO;
        data.velocityX[1] = New.num(5);
        data.velocityY[1] = ZERO;
        data.mass[1] = New.num(1e15);
        data.radius[1] = ONE;
        data.deleted[1] = true;
        data.copyToReadOnly(true);

        new MoveObjectsLogicAP(data, ONE, ONE, New.num("0.5"), 0, 0).runOnCpu();

        assertTrue(areEqual(data.velocityX[0], ZERO, PRECISION - 2), "No force from the deleted object");
        assertTrue(areEqual(data.positionX[1], New.num(10), PRECISION - 2), "Deleted object does not move");
    }

    @Test
    public void parallelRunEqualsSerialRun() {
        /* More objects than the ForkJoin threshold (20), so run() actually splits. */
        DataAP serial = sceneData();
        new MoveObjectsLogicAP(serial, New.num("0.001"), ONE, New.num("0.5"), 0, 0).runOnCpu();

        DataAP parallel = sceneData();
        new MoveObjectsLogicAP(parallel, New.num("0.001"), ONE, New.num("0.5"), 0, 0).run();

        for (int i = 0; i < BODIES; i++) {
            assertEquals(parallel.velocityX[i].compareTo(serial.velocityX[i]), 0, "Object " + i + " velocity X");
            assertEquals(parallel.velocityY[i].compareTo(serial.velocityY[i]), 0, "Object " + i + " velocity Y");
            assertEquals(parallel.positionX[i].compareTo(serial.positionX[i]), 0, "Object " + i + " position X");
            assertEquals(parallel.positionY[i].compareTo(serial.positionY[i]), 0, "Object " + i + " position Y");
        }
    }

    @Test
    public void simulationRunsEndToEndWithBarnesHut() {
        SimulationAP exact = createSimulation(ZERO);
        SimulationAP barnesHut = createSimulation(New.num("0.5"));
        try {
            exact.init(false);
            barnesHut.init(false);
            for (int i = 0; i < 10; i++) {
                exact.doIteration(false, i + 1);
                barnesHut.doIteration(false, i + 1);
            }
        } catch (Exception e) {
            throw new AssertionError("Simulation failed: " + e.getMessage(), e);
        }

        for (int i = 0; i < 3; i++) {
            assertFalse(Double.isNaN(barnesHut.getData().positionX[i].doubleValue()), "Position became NaN");
            assertTrue(areEqual(barnesHut.getData().positionX[i], exact.getData().positionX[i], PRECISION - 6),
                       "Object " + i + ": " + barnesHut.getData().positionX[i] + " vs " + exact.getData().positionX[i]);
        }
    }

    private static SimulationAP createSimulation(Number theta) {
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberOfObjects(3);
        properties.setSecondsPerIteration(New.num("0.001"));
        properties.setMinDistance(ONE);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMergeOnCollision(false);
        properties.setBarnesHutTheta(theta);

        List<SimulationObject> objects = new ArrayList<>();
        double[] px = {-1000, 0, 1000};
        for (int i = 0; i < 3; i++) {
            SimulationObject o = new SimulationObjectImpl();
            o.setId(String.valueOf(i));
            o.setX(New.num(px[i]));
            o.setY(New.num(i * 100));
            o.setZ(ZERO);
            o.setVelocity(new TripleNumber(ZERO, ZERO, ZERO));
            o.setMass(New.num(1e10));
            o.setRadius(ONE);
            objects.add(o);
        }
        properties.setInitialObjects(objects);
        return new SimulationAP(properties);
    }

    private DataAP twoBodyData() {
        return data(new double[]{0, 100}, new double[]{0, 0}, new double[]{1e10, 1e10});
    }

    private DataAP sceneData() {
        DataAP data = new DataAP(BODIES);
        for (int i = 0; i < BODIES; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(y[i]);
            data.velocityX[i] = New.num(vx[i]);
            data.velocityY[i] = New.num(vy[i]);
            data.mass[i] = New.num(mass[i]);
            data.radius[i] = ONE;
        }
        data.copyToReadOnly(true);
        return data;
    }

    private static DataAP data(double[] x, double[] y, double[] mass) {
        DataAP data = new DataAP(x.length);
        for (int i = 0; i < x.length; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(y[i]);
            data.velocityX[i] = ZERO;
            data.velocityY[i] = ZERO;
            data.mass[i] = New.num(mass[i]);
            data.radius[i] = ONE;
        }
        data.copyToReadOnly(true);
        return data;
    }
}
