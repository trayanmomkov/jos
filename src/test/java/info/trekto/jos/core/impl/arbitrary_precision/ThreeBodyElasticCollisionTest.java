package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.ProcessCollisionsLogicDouble;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Simultaneous collisions of one object with several others must conserve the total momentum.
 * The velocity deltas are computed from the read-only snapshots and accumulated, so the result
 * does not depend on the order in which the colliding pairs are processed.
 */
public class ThreeBodyElasticCollisionTest {
    public static final int PRECISION = 16;

    /* A, moving right, hits resting B in the same iteration in which C, moving left more slowly,
     * also hits B. A-B and B-C overlap; A-C do not. Expected for cor = 1 and equal masses:
     * A and C stop, B continues with the sum of both transferred velocities: 2 + (-1) = 1. */
    private static final double[] POSITION_X = {-2, 0, 2};
    private static final double[] VELOCITY_X = {2, 0, -1};
    private static final double[] EXPECTED_VELOCITY_X = {0, 1, 0};

    /* B is squeezed from both sides with equal and opposite velocities: everything stops. */
    private static final double[] SYMMETRIC_VELOCITY_X = {1, 0, -1};
    private static final double[] SYMMETRIC_EXPECTED_VELOCITY_X = {0, 0, 0};

    private static final double RADIUS = 1.2;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void simultaneousCollisionsConserveMomentumDouble() {
        checkDouble(VELOCITY_X, EXPECTED_VELOCITY_X);
    }

    @Test
    public void symmetricSqueezeStopsAllObjectsDouble() {
        checkDouble(SYMMETRIC_VELOCITY_X, SYMMETRIC_EXPECTED_VELOCITY_X);
    }

    @Test
    public void simultaneousCollisionsConserveMomentumFloat() {
        checkFloat(VELOCITY_X, EXPECTED_VELOCITY_X);
    }

    @Test
    public void symmetricSqueezeStopsAllObjectsFloat() {
        checkFloat(SYMMETRIC_VELOCITY_X, SYMMETRIC_EXPECTED_VELOCITY_X);
    }

    @Test
    public void simultaneousCollisionsConserveMomentumAP() {
        checkAP(VELOCITY_X, EXPECTED_VELOCITY_X);
    }

    @Test
    public void symmetricSqueezeStopsAllObjectsAP() {
        checkAP(SYMMETRIC_VELOCITY_X, SYMMETRIC_EXPECTED_VELOCITY_X);
    }

    /* A single pair processed through the Data path must give the same result as the
     * pair formula: equal masses moving towards each other with cor = 1 exchange velocities. */
    @Test
    public void twoObjectsThroughDataPathExchangeVelocities() {
        DataDouble data = new DataDouble(2);
        data.positionX[0] = -1;
        data.positionX[1] = 1;
        data.velocityX[0] = 1;
        data.velocityX[1] = -1;
        data.mass[0] = 1;
        data.mass[1] = 1;
        data.radius[0] = 1.5;
        data.radius[1] = 1.5;
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicDouble(data, false, 1).runOnSingleThread();

        assertEquals(data.velocityX[0], -1.0, 0.0);
        assertEquals(data.velocityX[1], 1.0, 0.0);
    }

    private void checkDouble(double[] velocity, double[] expected) {
        DataDouble data = new DataDouble(3);
        for (int i = 0; i < 3; i++) {
            data.positionX[i] = POSITION_X[i];
            data.velocityX[i] = velocity[i];
            data.mass[i] = 1;
            data.radius[i] = RADIUS;
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicDouble(data, false, 1).runOnSingleThread();

        double momentum = 0;
        double initialMomentum = 0;
        for (int i = 0; i < 3; i++) {
            assertEquals(data.velocityX[i], expected[i], 0.0, "Object " + i);
            assertEquals(data.velocityY[i], 0.0, 0.0, "Object " + i);
            momentum += data.mass[i] * data.velocityX[i];
            initialMomentum += data.mass[i] * velocity[i];
        }
        assertEquals(momentum, initialMomentum, 0.0, "Total momentum");
    }

    private void checkFloat(double[] velocity, double[] expected) {
        DataFloat data = new DataFloat(3);
        for (int i = 0; i < 3; i++) {
            data.positionX[i] = (float) POSITION_X[i];
            data.velocityX[i] = (float) velocity[i];
            data.mass[i] = 1;
            data.radius[i] = (float) RADIUS;
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicFloat(data, false, 1).runOnCpu();

        float momentum = 0;
        float initialMomentum = 0;
        for (int i = 0; i < 3; i++) {
            assertEquals(data.velocityX[i], (float) expected[i], 0.0f, "Object " + i);
            assertEquals(data.velocityY[i], 0.0f, 0.0f, "Object " + i);
            momentum += data.mass[i] * data.velocityX[i];
            initialMomentum += data.mass[i] * (float) velocity[i];
        }
        assertEquals(momentum, initialMomentum, 0.0f, "Total momentum");
    }

    private void checkAP(double[] velocity, double[] expected) {
        DataAP data = new DataAP(3);
        for (int i = 0; i < 3; i++) {
            data.positionX[i] = New.num(POSITION_X[i]);
            data.positionY[i] = ZERO;
            data.velocityX[i] = New.num(velocity[i]);
            data.velocityY[i] = ZERO;
            data.mass[i] = ONE;
            data.radius[i] = New.num(RADIUS);
        }
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicAP(data, false, ONE).runOnSingleThread();

        Number momentum = ZERO;
        Number initialMomentum = ZERO;
        for (int i = 0; i < 3; i++) {
            assertTrue(areEqual(data.velocityX[i], New.num(expected[i]), PRECISION - 2),
                       "Object " + i + ": " + data.velocityX[i]);
            assertTrue(areEqual(data.velocityY[i], ZERO, PRECISION - 2), "Object " + i);
            momentum = momentum.add(data.mass[i].multiply(data.velocityX[i]));
            initialMomentum = initialMomentum.add(data.mass[i].multiply(New.num(velocity[i])));
        }
        assertTrue(areEqual(momentum, initialMomentum, PRECISION - 2), "Total momentum: " + momentum);
    }
}
