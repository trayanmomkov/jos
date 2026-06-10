package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.ProcessCollisionsLogicDouble;
import info.trekto.jos.core.impl.single_precision.DataFloat;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * On merging, the resulting object must be placed at the common center of mass of the
 * two collided objects, its velocity must conserve the momentum and its mass must be the sum.
 *
 * Setup: bigger object m=3 resting at (0, 0), smaller m=1 at (2, 2) moving with (4, -4),
 * radii 1.5 (distance 2.83 < 3, overlapping). Expected merged object:
 * position = center of mass = (0.5, 0.5), velocity = (1, -1), mass = 4.
 */
public class MergeCenterOfMassTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void mergedObjectIsPlacedAtCenterOfMassDouble() {
        DataDouble data = new DataDouble(2);
        data.positionX[0] = 0;
        data.positionY[0] = 0;
        data.positionX[1] = 2;
        data.positionY[1] = 2;
        data.velocityX[1] = 4;
        data.velocityY[1] = -4;
        data.mass[0] = 3;
        data.mass[1] = 1;
        data.radius[0] = 1.5;
        data.radius[1] = 1.5;
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicDouble(data, true, 1).runOnSingleThread();

        assertFalse(data.deleted[0]);
        assertTrue(data.deleted[1], "Smaller object should be deleted");
        assertEquals(data.positionX[0], 0.5, 0.0, "Center of mass X");
        assertEquals(data.positionY[0], 0.5, 0.0, "Center of mass Y");
        assertEquals(data.velocityX[0], 1.0, 0.0, "Momentum conserving velocity X");
        assertEquals(data.velocityY[0], -1.0, 0.0, "Momentum conserving velocity Y");
        assertEquals(data.mass[0], 4.0, 0.0, "Total mass");
        assertTrue(data.radius[0] > 1.5, "Radius should grow");
    }

    @Test
    public void mergedObjectIsPlacedAtCenterOfMassFloat() {
        DataFloat data = new DataFloat(2);
        data.positionX[0] = 0;
        data.positionY[0] = 0;
        data.positionX[1] = 2;
        data.positionY[1] = 2;
        data.velocityX[1] = 4;
        data.velocityY[1] = -4;
        data.mass[0] = 3;
        data.mass[1] = 1;
        data.radius[0] = 1.5f;
        data.radius[1] = 1.5f;
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicFloat(data, true, 1).runOnCpu();

        assertFalse(data.deleted[0]);
        assertTrue(data.deleted[1], "Smaller object should be deleted");
        assertEquals(data.positionX[0], 0.5f, 0.0f, "Center of mass X");
        assertEquals(data.positionY[0], 0.5f, 0.0f, "Center of mass Y");
        assertEquals(data.velocityX[0], 1.0f, 0.0f, "Momentum conserving velocity X");
        assertEquals(data.velocityY[0], -1.0f, 0.0f, "Momentum conserving velocity Y");
        assertEquals(data.mass[0], 4.0f, 0.0f, "Total mass");
        assertTrue(data.radius[0] > 1.5f, "Radius should grow");
    }

    @Test
    public void mergedObjectIsPlacedAtCenterOfMassAP() {
        DataAP data = new DataAP(2);
        data.positionX[0] = ZERO;
        data.positionY[0] = ZERO;
        data.positionX[1] = New.num(2);
        data.positionY[1] = New.num(2);
        data.velocityX[0] = ZERO;
        data.velocityY[0] = ZERO;
        data.velocityX[1] = New.num(4);
        data.velocityY[1] = New.num(-4);
        data.mass[0] = New.num(3);
        data.mass[1] = New.num(1);
        data.radius[0] = New.num("1.5");
        data.radius[1] = New.num("1.5");
        data.copyToReadOnly(true);

        new ProcessCollisionsLogicAP(data, true, New.num(1)).runOnSingleThread();

        assertFalse(data.deleted[0]);
        assertTrue(data.deleted[1], "Smaller object should be deleted");
        assertTrue(areEqual(data.positionX[0], New.num("0.5"), PRECISION - 2), "Center of mass X: " + data.positionX[0]);
        assertTrue(areEqual(data.positionY[0], New.num("0.5"), PRECISION - 2), "Center of mass Y: " + data.positionY[0]);
        assertTrue(areEqual(data.velocityX[0], New.num(1), PRECISION - 2), "Velocity X: " + data.velocityX[0]);
        assertTrue(areEqual(data.velocityY[0], New.num(-1), PRECISION - 2), "Velocity Y: " + data.velocityY[0]);
        assertTrue(areEqual(data.mass[0], New.num(4), PRECISION - 2), "Total mass: " + data.mass[0]);
        assertTrue(data.radius[0].compareTo(New.num("1.5")) > 0, "Radius should grow");
    }
}
