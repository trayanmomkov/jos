package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.NumberFactory.NumberType;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.core.numbers.impl.ApfloatNumberImpl.ap;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;

/* An arbitrary precision run must calculate with the selected precision, from the first to the last iteration. */
public class ArbitraryPrecisionDigitsTest {
    private static final int PRECISION = 32;

    /* "Number type" was changed after the objects were made. */
    @Test
    public void objectsOfAnotherNumberTypeGetTheSelectedOne() throws SimulationException {
        createNumberFactory(DOUBLE, PRECISION);
        List<SimulationObject> objects = planetAndParticles(2, false);
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
        SimulationAP simulation = new SimulationAP(properties(objects, false, "0.005"));

        run(simulation, 5);

        assertEquals(digits(simulation), PRECISION);
    }

    /* "Precision" was changed after the objects were made. */
    @Test
    public void objectsOfAnotherPrecisionGetTheSelectedOne() throws SimulationException {
        createNumberFactory(ARBITRARY_PRECISION, 16);
        List<SimulationObject> objects = planetAndParticles(2, false);
        createNumberFactory(ARBITRARY_PRECISION, 40);
        SimulationAP simulation = new SimulationAP(properties(objects, false, "0.005"));

        run(simulation, 5);

        assertEquals(digits(simulation), 40);
    }

    /* A planet and a square block of particles above it. With the soft contact the particles touch. */
    private static List<SimulationObject> planetAndParticles(int side, boolean softContact) {
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(0.0, 0.0, 0.0, 0.0, 5e13, 20.0));
        double spacing = softContact ? 1.95 : 2.6;
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                objects.add(createObject(spacing * (i - (side - 1) / 2.0), 21.2 + spacing * j, 0.1 * (j + 1), -0.2 * (i + 1), 1e6, 1.0));
            }
        }
        if (softContact) {
            objects.add(createObject(-60.0, 30.0, 6.0, -1.0, 1e7, 1.5));
        }
        return objects;
    }

    private static SimulationProperties properties(List<SimulationObject> objects, boolean softContact, String secondsPerIteration) {
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(NumberType.ARBITRARY_PRECISION);
        properties.setNumberOfObjects(objects.size());
        properties.setInitialObjects(objects);
        properties.setSecondsPerIteration(New.num(secondsPerIteration));
        properties.setCoefficientOfRestitution(New.num("0.5"));
        properties.setMinDistance(New.num("0.8"));
        properties.setMergeOnCollision(false);
        properties.setSoftContact(softContact);
        return properties;
    }

    private static void run(SimulationAP simulation, int iterations) throws SimulationException {
        simulation.init(false);
        for (int i = 1; i <= iterations; i++) {
            simulation.doIteration(false, i);
        }
    }

    /* The fewest digits which a position of a particle has. */
    private static long digits(SimulationAP simulation) {
        DataAP data = simulation.getData();
        long digits = Long.MAX_VALUE;
        for (int i = 1; i < data.n; i++) {
            digits = Math.min(digits, Math.min(ap(data.positionX[i]).precision(), ap(data.positionY[i]).precision()));
        }
        return digits;
    }
}
