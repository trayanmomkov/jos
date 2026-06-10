package info.trekto.jos.formulas;

import info.trekto.jos.core.impl.arbitrary_precision.ProcessCollisionsLogicAP;
import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.MoveObjectsLogicDouble;
import info.trekto.jos.core.impl.double_precision.ProcessCollisionsLogicDouble;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.PI;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.RATIO_FOUR_THREE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CommonFormulasTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void calculateDistanceShouldCalculateCorrectly() {
        /* 3-4-5 triangle */
        assertEquals(new ProcessCollisionsLogicDouble(1, false, 1).calculateDistance(0, 0, 3, 4), 5.0, 0.0);
        assertEquals(new ProcessCollisionsLogicFloat(1, false, 1).calculateDistance(0, 0, 3, 4), 5.0f, 0.0f);
        assertTrue(areEqual(new ProcessCollisionsLogicAP(1, false, ONE).calculateDistance(ZERO, ZERO, New.num(3), New.num(4)),
                            New.num(5), PRECISION - 2));
    }

    @Test
    public void calculateVolumeFromRadiusShouldCalculateCorrectly() {
        /* V = 4/3 * pi * r^3 */
        ProcessCollisionsLogicDouble logic = new ProcessCollisionsLogicDouble(1, false, 1);
        assertEquals(logic.calculateVolumeFromRadius(2), 4 / 3.0 * Math.PI * 8, 0.0);
        /* Documented behavior: zero radius gives the smallest positive volume instead of zero. */
        assertEquals(logic.calculateVolumeFromRadius(0), Double.MIN_VALUE, 0.0);

        assertTrue(areEqual(ProcessCollisionsLogicAP.calculateVolumeFromRadius(New.num(2)),
                            RATIO_FOUR_THREE.multiply(PI).multiply(New.num(8)), PRECISION - 2));
    }

    @Test
    public void calculateRadiusFromVolumeShouldCalculateCorrectly() {
        ProcessCollisionsLogicDouble logic = new ProcessCollisionsLogicDouble(1, false, 1);
        assertEquals(logic.calculateRadiusFromVolume(4 / 3.0 * Math.PI * 8), 2.0, 1e-12);
        /* Round trip */
        assertEquals(logic.calculateRadiusFromVolume(logic.calculateVolumeFromRadius(1.7)), 1.7, 1e-12);
        assertEquals(logic.calculateRadiusFromVolume(0), Double.MIN_VALUE, 0.0);

        assertTrue(areEqual(ProcessCollisionsLogicAP.calculateRadiusFromVolume(
                                    ProcessCollisionsLogicAP.calculateVolumeFromRadius(New.num(2))),
                            New.num(2), PRECISION - 2));
    }

    @Test
    public void calculateAccelerationShouldCalculateCorrectly() {
        /* a = F / m1 = G * m2 / d^2 = 6.6743E-11 * 1E10 / 100^2 = 6.6743E-5 m/s^2.
         * With dt = 1 and initial velocity 0 the velocity after one iteration equals the acceleration. */
        DataDouble data = new DataDouble(2);
        data.positionX[1] = 100;
        data.mass[0] = 1;
        data.mass[1] = 1e10;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 0, 0).runOnCpu();

        assertEquals(data.velocityX[0], MoveObjectsLogicDouble.calculateForce(1, 1e10, 100) * 100 / 100 / 1, 0.0);
        assertEquals(data.velocityX[0], 6.6743e-5, 1e-12, "Physical value of the acceleration");
        assertEquals(data.velocityY[0], 0.0, 0.0, "No force along Y");
    }
}
