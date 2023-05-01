package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createDoubleSimulation;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;

public class SimulationDoubleTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void testCalculateTotalMass() throws SimulationException {
        assertEquals(createDoubleSimulation(createObject(1d, 1, 3, 1)).calculateTotalMass(), 3);
    }

    @Test
    public void testCalculateTotalMass2() throws SimulationException {
        SimulationDouble simulation = createDoubleSimulation(
                createObject(1d, 1, 1, 0.5d),
                createObject(4d, 4, 2, 0.5d),
                createObject(10d, 10, 3, 0.5d),
                createObject(20d, 20, 4, 0.5d));
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
                {0.5d, 0.5d, 3, 3},
                {1, 1, 3, 6},
                {2, 3, 3, 15},
                {2, -3, 3, 15}
        };
    }

    @Test(dataProvider = "momentum")
    public void testCalculateTotalMomentum(double velX, double velY, double mass, double expected) throws SimulationException {
        assertEquals(createDoubleSimulation(createObject(1, 1, velX, velY, mass, 1)).calculateTotalMomentum(), expected);
    }

    @Test
    public void testCalculateTotalMomentum2() throws SimulationException {
        SimulationDouble simulation = createDoubleSimulation(
                createObject(1d, 1, 1, 1, 3, 1),
                createObject(4d, 4, 2, 1, 3, 1),
                createObject(10d, 10, 2, -3, 4, 1),
                createObject(20d, 20, 1, 1, 4, 1));
        simulation.getData().deleted[1] = true;
        simulation.getData().deleted[3] = true;
        assertEquals(simulation.calculateTotalMomentum(), 26);
    }
}