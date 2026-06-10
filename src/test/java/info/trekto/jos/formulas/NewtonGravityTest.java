package info.trekto.jos.formulas;

import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.impl.arbitrary_precision.MoveObjectsLogicAP;
import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.MoveObjectsLogicDouble;
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

public class NewtonGravityTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void calculateForceShouldCalculateCorrectly() {
        /* F = G * m1 * m2 / r^2 = 6.6743E-11 * 2E10 * 1E10 / 100^2 = 1334860 N */
        assertEquals(MoveObjectsLogicDouble.calculateForce(2e10, 1e10, 100), 1.33486e6, 1e-3);
        assertEquals(MoveObjectsLogicFloat.calculateForce(2e10f, 1e10f, 100), 1.33486e6f, 1f);

        MoveObjectsLogicAP logic = new MoveObjectsLogicAP(new DataAP(1), ONE, ONE, 0, 0);
        assertTrue(areEqual(logic.calculateForce(New.num(2e10), New.num(1e10), New.num(100)),
                            New.num("1334860"), PRECISION - 2));
    }

    @Test
    public void calculateForceAsVectorShouldCalculateCorrectly() {
        /* Two equal masses at (0, 0) and (3, 4): distance 5, force direction (3/5, 4/5). */
        double m = 1e10;
        DataDouble data = new DataDouble(2);
        data.positionX[1] = 3;
        data.positionY[1] = 4;
        data.mass[0] = m;
        data.mass[1] = m;
        data.copyToReadOnly(true);

        new MoveObjectsLogicDouble(data, 1, 1, 0, 0).runOnCpu();

        /* With dt = 1 and initial velocity 0 the velocity equals the acceleration: F * direction / m. */
        double force = MoveObjectsLogicDouble.calculateForce(m, m, 5);
        assertEquals(data.velocityX[0], force * 3 / 5 / m, 0.0, "Velocity along X");
        assertEquals(data.velocityY[0], force * 4 / 5 / m, 0.0, "Velocity along Y");

        /* The other object accelerates in the opposite direction: the total momentum stays zero. */
        assertEquals(m * data.velocityX[0] + m * data.velocityX[1], 0.0, 0.0, "Momentum X");
        assertEquals(m * data.velocityY[0] + m * data.velocityY[1], 0.0, 0.0, "Momentum Y");
    }
}
