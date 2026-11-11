package info.trekto.jos.core.impl.single_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.double_precision.DataDouble;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.FLOAT;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/* The soft contact in float must give the same run as in double. */
public class SoftContactFloatTest {
    private static final double TOLERANCE = 0.01;

    @BeforeClass
    public void init() {
        createNumberFactory(FLOAT, 8);
    }

    @Test
    public void sameRunAsDouble() throws SimulationException {
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(-6.0, 0.0, 1.0, 0.0, 1.0e6, 1.0));
        objects.add(createObject(6.0, 0.3, -1.0, 0.0, 1.5e6, 1.0));   // heavier than the left one: the two get different shares of the push
        objects.add(createObject(0.0, 9.0, 0.0, -0.5, 1.0e8, 2.0));

        SimulationFloat single = new SimulationFloat(properties(objects), null);
        SimulationDouble primitive = new SimulationDouble(properties(objects), null);
        single.init(false);
        primitive.init(false);
        assertTrue(single.isSoftContactActive(), "Soft contact should be active");
        for (int i = 1; i <= 30; i++) {
            single.doIteration(false, i);
            primitive.doIteration(false, i);
        }

        DataFloat f = (DataFloat) single.getData();
        DataDouble d = (DataDouble) primitive.getData();
        for (int i = 0; i < objects.size(); i++) {
            assertEquals(f.positionX[i], d.positionX[i], TOLERANCE);
            assertEquals(f.positionY[i], d.positionY[i], TOLERANCE);
            assertEquals(f.velocityX[i], d.velocityX[i], TOLERANCE);
            assertEquals(f.velocityY[i], d.velocityY[i], TOLERANCE);
        }
        /* The two small objects met and bounced back. */
        assertTrue(f.velocityX[0] < 0, "The left object should move back: " + f.velocityX[0]);
        assertTrue(f.velocityX[1] > 0, "The right object should move back: " + f.velocityX[1]);
    }

    private SimulationProperties properties(List<SimulationObject> objects) {
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(FLOAT);
        properties.setCoefficientOfRestitution(New.num(0.5));
        properties.setMinDistance(New.num(1.0));
        properties.setSecondsPerIteration(New.num(0.5));
        properties.setScale(1.0);
        properties.setMergeOnCollision(false);
        properties.setSoftContact(true);
        properties.setNumberOfObjects(objects.size());
        properties.setInitialObjects(objects);
        return properties;
    }
}
