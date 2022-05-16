package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.double_precision.SimulationLogicDouble;
import info.trekto.jos.core.impl.single_precision.SimulationLogicFloat;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.*;
import static info.trekto.jos.core.numbers.impl.ApfloatNumberImpl.ap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ElasticCollision1DTest {
    public static final int PRECISION = 16;
    public static final int DOUBLE_PRECISION = 16;
    public static final int SINGLE_PRECISION = 8;
    private static final String IMPL = "SimulationLogic impl: ";
    ImmutableSimulationObject o;

    public static String error(SimulationLogic simulationLogic) {
        return IMPL + simulationLogic.getClass().getSimpleName();
    }

    public static boolean equals(Number n1, Number n2, long precision) {
        long equalDigits = ap(n1).equalDigits(ap(n2));
        if (equalDigits < precision) {
            System.out.println("Number of equal digits: " + equalDigits + " (" + n1 + ", " + n2 + ")");
            return false;
        }
        return true;
    }

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);

        SimulationObject om = new SimulationObjectImpl();
        om.setMass(ONE);
        om.setSpeed(new TripleNumber(ONE, ZERO, ZERO));
        om.setRadius(ONE);
        om.setX(MINUS_ONE);
        om.setY(ZERO);
        om.setZ(ZERO);
        o = om;
    }

    @DataProvider(name = "logic_implementations")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {new SimulationLogicAP(new SimulationAP(new SimulationProperties())), PRECISION - 2},
                {new SimulationLogicDouble(2, 1, 0, 0), DOUBLE_PRECISION - 1},
                {new SimulationLogicFloat(2, 1, 0, 0), SINGLE_PRECISION - 1}
        };
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassAndSpeedOppositeDirection(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), o2Speed, error(simulationLogic));
        assertEquals(o2.getSpeed(), o.getSpeed(), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsZeroComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setSpeed(TRIPLE_ZERO);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), TRIPLE_ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed(), o.getSpeed(), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsZeroComingFromRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setSpeed(TRIPLE_ZERO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), o2Speed, error(simulationLogic));
        assertEquals(o2.getSpeed(), TRIPLE_ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoRightSlowerGoRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(o1.getSpeed().getX().divide(TWO), ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), new TripleNumber(ONE.divide(TWO), ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getSpeed(), new TripleNumber(ONE, ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoLeftSlowerGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setSpeed(new TripleNumber(MINUS_ONE.divide(TWO), ZERO, ZERO));
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), new TripleNumber(MINUS_ONE, ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getSpeed(), new TripleNumber(MINUS_ONE.divide(TWO), ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoRightSlowerGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(o1.getSpeed().getX().divide(TWO).negate(), ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), o2Speed, error(simulationLogic));
        assertEquals(o2.getSpeed(), o.getSpeed(), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoLeftSlowerGoRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        TripleNumber o1Speed = new TripleNumber(ONE.divide(TWO), ZERO, ZERO);
        o1.setSpeed(o1Speed);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), o2Speed, error(simulationLogic));
        assertEquals(o2.getSpeed(), o1Speed, error(simulationLogic));
    }

    // Mass ############################

    @Test(dataProvider = "logic_implementations")
    public void equalSpeedOnesMassIsHalfTheOtherHeavierGoRightLighterGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertTrue(equals(o1.getSpeed().getX(), ONE.divide(THREE).negate(), precision), error(simulationLogic));
        assertEquals(o1.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o1.getSpeed().getZ(), ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), ONE.add(ONE.divide(THREE).multiply(TWO)), precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassOnesSpeedIsZeroHeavierComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setSpeed(TRIPLE_ZERO);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertTrue(equals(o1.getSpeed().getX(), ONE.divide(THREE), precision), error(simulationLogic));
        assertEquals(o1.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o1.getSpeed().getZ(), ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), ONE.add(ONE.divide(THREE)), precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedHeavierAndFasterComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        TripleNumber o1Speed = new TripleNumber(TWO, ZERO, ZERO);
        o1.setSpeed(o1Speed);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertEquals(o1.getSpeed(), TRIPLE_ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), THREE, precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedHeavierComingFromLeftFasterComingFromRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Speed = new TripleNumber(TWO.negate(), ZERO, ZERO);
        o2.setSpeed(o2Speed);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertTrue(equals(o1.getSpeed().getX(), MINUS_ONE, precision), error(simulationLogic));
        assertEquals(o1.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o1.getSpeed().getZ(), ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), TWO, precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedOneDirectionHeavierLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        TripleNumber o1Speed = new TripleNumber(TWO, ZERO, ZERO);
        o1.setSpeed(o1Speed);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertTrue(equals(o1.getSpeed().getX(), ONE.add(ONE.divide(THREE)), precision), error(simulationLogic));
        assertEquals(o1.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o1.getSpeed().getZ(), ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), TWO.add(ONE.divide(THREE)), precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedOneDirectionHeavierRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        TripleNumber o1Speed = new TripleNumber(TWO, ZERO, ZERO);
        o1.setSpeed(o1Speed);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setMass(TWO);

        simulationLogic.processTwoDimensionalCollision(o1, o2);

        assertTrue(equals(o1.getSpeed().getX(), ONE.subtract(ONE.divide(THREE)), precision), error(simulationLogic));
        assertEquals(o1.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o1.getSpeed().getZ(), ZERO, error(simulationLogic));

        assertTrue(equals(o2.getSpeed().getX(), TWO.subtract(ONE.divide(THREE)), precision), error(simulationLogic));
        assertEquals(o2.getSpeed().getY(), ZERO, error(simulationLogic));
        assertEquals(o2.getSpeed().getZ(), ZERO, error(simulationLogic));
    }
}