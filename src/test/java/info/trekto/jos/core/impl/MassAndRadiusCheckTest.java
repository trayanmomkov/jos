package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.impl.single_precision.SimulationFloat;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.NumberFactory.NumberType;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.FLOAT;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/** A scene with an object without mass or without radius must be refused at start, by all three simulations. */
public class MassAndRadiusCheckTest {
    private static final int PRECISION = 16;

    @Test
    public void objectWithoutMassIsRefused() {
        assertRefusedByAllSimulations(0, 5);
    }

    @Test
    public void objectWithoutRadiusIsRefused() {
        assertRefusedByAllSimulations(1000, 0);
    }

    @Test
    public void negativeMassIsRefused() {
        assertRefusedByAllSimulations(-1000, 5);
    }

    @Test
    public void objectsWithMassAndRadiusAreAccepted() throws SimulationException {
        createNumberFactory(DOUBLE, PRECISION);
        new SimulationAP(properties(DOUBLE, 1000, 5)).init(false);
    }

    /* The second object of the scene has the given mass and radius. */
    private static void assertRefusedByAllSimulations(double mass, double radius) {
        createNumberFactory(DOUBLE, PRECISION);
        assertRefused(new SimulationAP(properties(DOUBLE, mass, radius)));
        assertRefused(new SimulationDouble(properties(DOUBLE, mass, radius), null));
        createNumberFactory(FLOAT, PRECISION);
        assertRefused(new SimulationFloat(properties(FLOAT, mass, radius), null));
    }

    private static void assertRefused(Simulation simulation) {
        try {
            simulation.init(false);
            fail(simulation.getClass().getSimpleName() + " accepted the scene");
        } catch (SimulationException e) {
            assertTrue(e.getMessage().contains("'second'"), "The message must name the object: " + e.getMessage());
        }
    }

    private static SimulationProperties properties(NumberType numberType, double mass, double radius) {
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(0, 0, 1000, 5));
        SimulationObject second = createObject(100, 0, mass, radius);
        second.setId("second");
        objects.add(second);

        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(numberType);
        properties.setPrecision(PRECISION);
        properties.setNumberOfObjects(objects.size());
        properties.setInitialObjects(objects);
        properties.setSecondsPerIteration(New.num("0.01"));
        properties.setCoefficientOfRestitution(New.num("0.8"));
        properties.setMinDistance(New.num("1"));
        return properties;
    }
}
