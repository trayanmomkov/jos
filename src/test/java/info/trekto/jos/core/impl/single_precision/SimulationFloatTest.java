package info.trekto.jos.core.impl.single_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createFloatSimulation;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;

public class SimulationFloatTest {
    public static final int PRECISION = 8;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void testCalculateTotalMass() throws SimulationException {
        assertEquals(createFloatSimulation(createObject(1, 1, 3, 1)).calculateTotalMass(), 3);
    }

    @Test
    public void testCalculateTotalMass2() throws SimulationException {
        SimulationFloat simulation = createFloatSimulation(
                createObject(1, 1, 1, 0.5f),
                createObject(4, 4, 2, 0.5f),
                createObject(10, 10, 3, 0.5f),
                createObject(20, 20, 4, 0.5f));
        simulation.getData().deleted[1] = true;
        simulation.getData().deleted[3] = true;
        assertEquals(simulation.calculateTotalMass(), 4);
    }

    @DataProvider(name = "momentum")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {0, 0, 3, 0},
                {1, 0, 3, 3},
                {0, 1, 3, 3},
                {0.5f, 0.5f, 3, 3},
                {1, 1, 3, 6},
                {2, 3, 3, 15},
                {2, -3, 3, 15}
        };
    }

    @Test(dataProvider = "momentum")
    public void testCalculateTotalMomentum(float velX, float velY, float mass, float expected) throws SimulationException {
        assertEquals(createFloatSimulation(createObject(1, 1, velX, velY, mass, 1)).calculateTotalMomentum(), expected);
    }

    @Test
    public void testCalculateTotalMomentum2() throws SimulationException {
        SimulationFloat simulation = createFloatSimulation(
                createObject(1, 1, 1, 1, 3, 1),
                createObject(4, 4, 2, 1, 3, 1),
                createObject(10, 10, 2, -3, 4, 1),
                createObject(20, 20, 1, 1, 4, 1));
        simulation.getData().deleted[1] = true;
        simulation.getData().deleted[3] = true;
        assertEquals(simulation.calculateTotalMomentum(), 26);
    }
}