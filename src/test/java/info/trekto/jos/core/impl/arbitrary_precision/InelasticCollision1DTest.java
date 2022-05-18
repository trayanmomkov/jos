package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.double_precision.SimulationLogicDouble;
import info.trekto.jos.core.impl.single_precision.SimulationLogicFloat;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
 
public class InelasticCollision1DTest {
    public static final int PRECISION = 16;
    public static final int DOUBLE_PRECISION = 16;
    public static final int SINGLE_PRECISION = 8;
    private static final String IMPL = "SimulationLogic impl: ";
    private ImmutableSimulationObject o;
    private static Number cor;
    private static SimulationProperties properties;

    public static String error(SimulationLogic simulationLogic) {
        return IMPL + simulationLogic.getClass().getSimpleName();
    }

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);

        SimulationObject om = new SimulationObjectImpl();
        om.setMass(ONE);
        om.setVelocity(new TripleNumber(ONE, ZERO, ZERO));
        om.setRadius(ONE);
        om.setX(MINUS_ONE);
        om.setY(ZERO);
        om.setZ(ZERO);
        o = om;
        
        cor = New.num("0.75");
        properties = new SimulationProperties();
        properties.setCoefficientOfRestitution(cor);
    }

    @DataProvider(name = "logic_implementations")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {new SimulationLogicAP(new SimulationAP(properties)), PRECISION - 2},
                {new SimulationLogicDouble(2, 1, 0, 0, false,
                                           properties.getCoefficientOfRestitution().doubleValue()),
                        DOUBLE_PRECISION - 1},
                {new SimulationLogicFloat(2, 1, 0, 0, false,
                                          properties.getCoefficientOfRestitution().floatValue()),
                        SINGLE_PRECISION - 1}
        };
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassAndSpeedOppositeDirection(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertEquals(o1.getVelocity(), new TripleNumber(New.num("-0.75"), ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getVelocity(), new TripleNumber(New.num("0.75"), ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsZeroComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setVelocity(TRIPLE_ZERO);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("0.125"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("0.875"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsZeroComingFromRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setVelocity(TRIPLE_ZERO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("-0.875"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("-0.125"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoRightSlowerGoRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setVelocity(new TripleNumber(TWO, ZERO, ZERO));
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

//        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("1.125"), ZERO, ZERO), precision), error(simulationLogic));
//        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("1.875"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoLeftSlowerGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setVelocity(new TripleNumber(MINUS_ONE.divide(TWO), ZERO, ZERO));
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

//        assertEquals(o1.getVelocity(), new TripleNumber(MINUS_ONE, ZERO, ZERO), error(simulationLogic));
//        assertEquals(o2.getVelocity(), new TripleNumber(MINUS_ONE.divide(TWO), ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void equalMassOnesSpeedIsHalfTheOtherFasterGoRightSlowerGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setVelocity(new TripleNumber(TWO, ZERO, ZERO));
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setVelocity(new TripleNumber(MINUS_ONE, ZERO, ZERO));

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("-0.625"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("1.625"), ZERO, ZERO), precision), error(simulationLogic));
    }

    // Mass ############################

    @Test(dataProvider = "logic_implementations")
    public void equalSpeedOnesMassIsHalfTheOtherHeavierGoRightLighterGoLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("-0.166666666666666"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("1.333333333333333"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassOnesSpeedIsZeroHeavierComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setVelocity(TRIPLE_ZERO);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("0.416666666666666"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("1.166666666666666"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedHeavierAndFasterComingFromLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        TripleNumber o1Velocity = new TripleNumber(TWO, ZERO, ZERO);
        o1.setVelocity(o1Velocity);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(MINUS_ONE, ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

//        assertEquals(o1.getVelocity(), TRIPLE_ZERO, error(simulationLogic));
//
//        assertTrue(areEqual(o2.getVelocity().getX(), THREE, precision), error(simulationLogic));
//        assertEquals(o2.getVelocity().getY(), ZERO, error(simulationLogic));
//        assertEquals(o2.getVelocity().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedHeavierComingFromLeftFasterComingFromRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        TripleNumber o2Velocity = new TripleNumber(TWO.negate(), ZERO, ZERO);
        o2.setVelocity(o2Velocity);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

        assertTrue(areEqual(o1.getVelocity(), new TripleNumber(New.num("-0.75"), ZERO, ZERO), precision), error(simulationLogic));
        assertTrue(areEqual(o2.getVelocity(), new TripleNumber(New.num("1.5"), ZERO, ZERO), precision), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedOneDirectionHeavierLeft(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setMass(TWO);
        TripleNumber o1Velocity = new TripleNumber(TWO, ZERO, ZERO);
        o1.setVelocity(o1Velocity);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

//        assertTrue(areEqual(o1.getVelocity().getX(), ONE.add(ONE.divide(THREE)), precision), error(simulationLogic));
//        assertEquals(o1.getVelocity().getY(), ZERO, error(simulationLogic));
//        assertEquals(o1.getVelocity().getZ(), ZERO, error(simulationLogic));
//
//        assertTrue(areEqual(o2.getVelocity().getX(), TWO.add(ONE.divide(THREE)), precision), error(simulationLogic));
//        assertEquals(o2.getVelocity().getY(), ZERO, error(simulationLogic));
//        assertEquals(o2.getVelocity().getZ(), ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void diffMassDiffSpeedOneDirectionHeavierRight(SimulationLogic simulationLogic, int precision) {
        SimulationObject o1 = new SimulationObjectImpl(o);
        TripleNumber o1Velocity = new TripleNumber(TWO, ZERO, ZERO);
        o1.setVelocity(o1Velocity);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ONE);
        o2.setMass(TWO);

        simulationLogic.processTwoDimensionalCollision(o1, o2, cor);

//        assertTrue(areEqual(o1.getVelocity().getX(), ONE.subtract(ONE.divide(THREE)), precision), error(simulationLogic));
//        assertEquals(o1.getVelocity().getY(), ZERO, error(simulationLogic));
//        assertEquals(o1.getVelocity().getZ(), ZERO, error(simulationLogic));
//
//        assertTrue(areEqual(o2.getVelocity().getX(), TWO.subtract(ONE.divide(THREE)), precision), error(simulationLogic));
//        assertEquals(o2.getVelocity().getY(), ZERO, error(simulationLogic));
//        assertEquals(o2.getVelocity().getZ(), ZERO, error(simulationLogic));
    }
}