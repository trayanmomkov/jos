package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.FOUR;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.HALF;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.TEN;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.THREE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.TWO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createObject;
import static info.trekto.jos.util.UtilsTest.createAPSimulation;
import static org.testng.Assert.assertEquals;

public class SimulationAPTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void testCalculateTotalMass() throws SimulationException {
        assertEquals(createAPSimulation(createObject(ONE, ONE, THREE, ONE)).calculateTotalMass(), THREE.doubleValue());
    }

    @Test
    public void testCalculateTotalMass2() throws SimulationException {
        SimulationAP simulation = createAPSimulation(
                createObject(ONE, ONE, ONE, HALF),
                createObject(FOUR, FOUR, TWO, HALF),
                createObject(TEN, TEN, THREE, HALF),
                createObject(TEN.add(TEN), TEN.add(TEN), FOUR, HALF));
        simulation.getData().deleted[1] = true;
        simulation.getData().deleted[3] = true;
        assertEquals(simulation.calculateTotalMass(), FOUR.doubleValue());
    }

    @DataProvider(name = "momentum")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {ZERO, ZERO, THREE, ZERO},
                {ONE, ZERO, THREE, THREE},
                {ZERO, ONE, THREE, THREE},
                {HALF, HALF, THREE, THREE},
                {ONE, ONE, THREE, New.num(6)},
                {TWO, THREE, THREE, New.num(15)},
                {TWO, THREE.negate(), THREE, New.num(15)}
        };
    }

    @Test(dataProvider = "momentum")
    public void testCalculateTotalMomentum(Number velX, Number velY, Number mass, Number expected) throws SimulationException {
        assertEquals(createAPSimulation(createObject(ONE, ONE, velX, velY, mass, ONE)).calculateTotalMomentum(), expected.doubleValue());
    }

    @Test
    public void testCalculateTotalMomentum2() throws SimulationException {
        SimulationAP simulation = createAPSimulation(
                createObject(ONE, ONE, ONE, ONE, THREE, ONE),
                createObject(FOUR, FOUR, TWO, ONE, THREE, ONE),
                createObject(TEN, TEN, TWO, THREE.negate(), FOUR, ONE),
                createObject(TEN.add(TEN), TEN.add(TEN), ONE, ONE, FOUR, ONE));
        simulation.getData().deleted[1] = true;
        simulation.getData().deleted[3] = true;
        assertEquals(simulation.calculateTotalMomentum(), New.num(26).doubleValue());
    }
}