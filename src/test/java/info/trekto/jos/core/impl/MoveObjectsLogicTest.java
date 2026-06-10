package info.trekto.jos.core.impl;

import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.impl.arbitrary_precision.MoveObjectsLogicAP;
import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.MoveObjectsLogicDouble;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.MoveObjectsLogicFloat;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The move phase: position += velocity * dt, then the gravitational acceleration changes the
 * velocity, then objects optionally bounce from the screen borders.
 */
public class MoveObjectsLogicTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void movesObjectByVelocityTimesDt() {
        /* A single object: no gravity, position changes by v * dt, velocity stays. */
        DataDouble data = new DataDouble(1);
        data.positionX[0] = 1;
        data.positionY[0] = 2;
        data.velocityX[0] = 3;
        data.velocityY[0] = -4;
        data.mass[0] = 1;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 0.5, 1, 0, 0).runOnCpu();

        assertEquals(data.positionX[0], 2.5, 0.0);
        assertEquals(data.positionY[0], 0.0, 0.0);
        assertEquals(data.velocityX[0], 3.0, 0.0, "Velocity unchanged");
        assertEquals(data.velocityY[0], -4.0, 0.0, "Velocity unchanged");
    }

    @Test
    public void movesObjectByVelocityTimesDtFloat() {
        DataFloat data = new DataFloat(1);
        data.positionX[0] = 1;
        data.positionY[0] = 2;
        data.velocityX[0] = 3;
        data.velocityY[0] = -4;
        data.mass[0] = 1;
        data.copyToReadOnly(true);

        new MoveObjectsLogicFloat(data, 0.5f, 1, 0, 0).runOnCpu();

        assertEquals(data.positionX[0], 2.5f, 0.0f);
        assertEquals(data.positionY[0], 0.0f, 0.0f);
        assertEquals(data.velocityX[0], 3.0f, 0.0f, "Velocity unchanged");
        assertEquals(data.velocityY[0], -4.0f, 0.0f, "Velocity unchanged");
    }

    @Test
    public void movesObjectByVelocityTimesDtAP() {
        DataAP data = new DataAP(1);
        data.positionX[0] = New.num(1);
        data.positionY[0] = New.num(2);
        data.velocityX[0] = New.num(3);
        data.velocityY[0] = New.num(-4);
        data.mass[0] = ONE;
        data.radius[0] = ONE;
        data.copyToReadOnly(true);

        new MoveObjectsLogicAP(data, New.num("0.5"), ONE, 0, 0).runOnCpu();

        assertTrue(areEqual(data.positionX[0], New.num("2.5"), PRECISION - 2), "X: " + data.positionX[0]);
        assertTrue(areEqual(data.positionY[0], New.num(0), PRECISION - 2), "Y: " + data.positionY[0]);
        assertTrue(areEqual(data.velocityX[0], New.num(3), PRECISION - 2), "Velocity unchanged");
        assertTrue(areEqual(data.velocityY[0], New.num(-4), PRECISION - 2), "Velocity unchanged");
    }

    @Test
    public void gravityAcceleratesBodiesTowardsEachOtherConservingMomentum() {
        /* Two equal masses 100 m apart: each gains G * m / d^2 towards the other in dt = 1. */
        double m = 1e10;
        DataDouble data = new DataDouble(2);
        data.positionX[1] = 100;
        data.mass[0] = m;
        data.mass[1] = m;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 0, 0).runOnCpu();

        double expected = MoveObjectsLogicDouble.calculateForce(m, m, 100) * 100 / 100 / m;
        assertEquals(data.velocityX[0], expected, 0.0, "Left object accelerates right");
        assertEquals(data.velocityX[1], -expected, 0.0, "Right object accelerates left");
        assertEquals(m * data.velocityX[0] + m * data.velocityX[1], 0.0, 0.0, "Total momentum");
    }

    @Test
    public void minDistanceClampLimitsForceOfCloseObjects() {
        /* Objects at distance 1 with minDistance = 10: the force is calculated as if
         * the distance were 10, so close encounters do not produce infinite accelerations. */
        DataDouble data = new DataDouble(2);
        data.positionX[1] = 1;
        data.mass[0] = 1;
        data.mass[1] = 1e10;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 10, 0, 0).runOnCpu();

        double clamped = MoveObjectsLogicDouble.calculateForce(1, 1e10, 10) * 1 / 10 / 1;
        double unclamped = MoveObjectsLogicDouble.calculateForce(1, 1e10, 1) * 1 / 1 / 1;
        assertEquals(data.velocityX[0], clamped, 0.0, "Force calculated at the clamped distance");
        assertTrue(data.velocityX[0] < unclamped / 100, "Far smaller than the unclamped force");
    }

    @Test
    public void bounceFromScreenBordersReversesVelocity() {
        /* Screen 100x100: borders at +-50. The object reaches the right border and bounces. */
        DataDouble data = new DataDouble(1);
        data.positionX[0] = 49;
        data.velocityX[0] = 1;
        data.mass[0] = 1;
        data.radius[0] = 2;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 100, 100).runOnCpu();

        assertEquals(data.positionX[0], 50.0, 0.0);
        assertEquals(data.velocityX[0], -1.0, 0.0, "Velocity reversed on the border");

        /* Same on the Y axis. */
        data = new DataDouble(1);
        data.positionY[0] = -49;
        data.velocityY[0] = -1;
        data.mass[0] = 1;
        data.radius[0] = 2;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 100, 100).runOnCpu();

        assertEquals(data.positionY[0], -50.0, 0.0);
        assertEquals(data.velocityY[0], 1.0, 0.0, "Velocity reversed on the border");

        /* An object far from the borders is not affected. */
        data = new DataDouble(1);
        data.velocityX[0] = 1;
        data.mass[0] = 1;
        data.radius[0] = 2;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 100, 100).runOnCpu();

        assertEquals(data.positionX[0], 1.0, 0.0);
        assertEquals(data.velocityX[0], 1.0, 0.0, "Velocity kept away from the borders");
    }

    @Test
    public void deletedObjectsDoNotMoveAndExertNoForce() {
        DataDouble data = new DataDouble(2);
        data.mass[0] = 1;
        data.positionX[1] = 10;
        data.velocityX[1] = 5;
        data.mass[1] = 1e15;
        data.deleted[1] = true;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 0, 0).runOnCpu();

        assertEquals(data.velocityX[0], 0.0, 0.0, "No force from the deleted object");
        assertEquals(data.positionX[0], 0.0, 0.0, "Not attracted by the deleted object");
        assertEquals(data.positionX[1], 10.0, 0.0, "Deleted object does not move");
    }
}
