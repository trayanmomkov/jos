package info.trekto.jos.core.impl.arbitrary_precision;

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

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/* The soft contact in arbitrary precision must give the same run as in double. */
public class SoftContactAPTest {

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, 32);
    }

    @Test
    public void sameRunAsDouble() throws SimulationException {
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(-6.0, 0.0, 1.0, 0.0, 1.0e6, 1.0));
        objects.add(createObject(6.0, 0.3, -1.0, 0.0, 1.5e6, 1.0));   // heavier than the left one: the two get different shares of the push
        objects.add(createObject(0.0, 9.0, 0.0, -0.5, 1.0e8, 2.0));

        SimulationAP arbitrary = new SimulationAP(properties(objects));
        SimulationDouble primitive = new SimulationDouble(properties(objects), null);
        arbitrary.init(false);
        primitive.init(false);
        assertTrue(arbitrary.isSoftContactActive(), "Soft contact should be active");
        for (int i = 1; i <= 30; i++) {
            arbitrary.doIteration(false, i);
            primitive.doIteration(false, i);
        }

        DataAP a = arbitrary.getData();
        DataDouble d = (DataDouble) primitive.getData();
        for (int i = 0; i < objects.size(); i++) {
            assertEquals(a.positionX[i].doubleValue(), d.positionX[i], 1e-9);
            assertEquals(a.positionY[i].doubleValue(), d.positionY[i], 1e-9);
            assertEquals(a.velocityX[i].doubleValue(), d.velocityX[i], 1e-9);
            assertEquals(a.velocityY[i].doubleValue(), d.velocityY[i], 1e-9);
        }
        /* The two small objects met and bounced back. */
        assertTrue(a.velocityX[0].doubleValue() < 0, "The left object should move back: " + a.velocityX[0]);
        assertTrue(a.velocityX[1].doubleValue() > 0, "The right object should move back: " + a.velocityX[1]);
    }

    private SimulationProperties properties(List<SimulationObject> objects) {
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(ARBITRARY_PRECISION);
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
