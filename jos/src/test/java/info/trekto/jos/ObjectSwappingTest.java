/**
 * 
 */
package info.trekto.jos;

import static org.junit.Assert.assertTrue;
import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.model.SimulationObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author Trayan Momkov
 * @date 18 Mar 2016
 */
public class ObjectSwappingTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        Container.setSimulation(new SimulationImpl());
        Container.setSimulationLogic(new SimulationLogicImpl());

        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.setNumberOfObjects(10);
        simulationProperties.setNumberOfIterations(10);
        simulationProperties.setFormatVersion1Writer(new FormatVersion1ReaderWriter("simulation",
                "simulation.out"));
        Container.getSimulation().setProperties(simulationProperties);

        Class clazz = Container.getSimulation().getClass();

        Method initMethod = clazz.getDeclaredMethod("init");
        Method doIterationMethod = clazz.getDeclaredMethod("doIteration");
        // logger.info(initMethod.getName() + " is " + Modifier.toString(initMethod.getModifiers()));

        initMethod.setAccessible(true);
        doIterationMethod.setAccessible(true);
        initMethod.invoke(Container.getSimulation());

        List<Integer> originalObjectsIds = new ArrayList<>();
        List<Integer> auxiliaryObjectsIds = new ArrayList<>();

        /** Get ids of first (original) objects */
        originalObjectsIds = getObjectsIds(Container.getSimulation().getObjects());

        Field auxiliaryObjectsField = clazz.getDeclaredField("auxiliaryObjects");
        auxiliaryObjectsField.setAccessible(true);

        List<SimulationObject> auxiliaryObjects = (List<SimulationObject>) auxiliaryObjectsField.get(Container
                .getSimulation());

        /** Get ids of auxiliary objects */
        auxiliaryObjectsIds = getObjectsIds(auxiliaryObjects);

        /** Do iterations and check whether objects in the two lists swap */
        for (long i = 0; i < simulationProperties.getNumberOfIterations(); i++) {
            logger.info("\nIteration " + i);
            doIterationMethod.invoke(Container.getSimulation());

            if (i % 2 == 0) {
                List<Integer> actualObjects = getObjectsIds(Container.getSimulation().getObjects());
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField.get(Container
                        .getSimulation()));
                assertTrue(originalObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(originalObjectsIds));
            } else {
                List<Integer> actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField
                        .get(Container.getSimulation()));
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds(Container.getSimulation().getObjects());
                assertTrue(originalObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(originalObjectsIds));
            }
        }
    }

    private List<Integer> getObjectsIds(List<SimulationObject> objects) {
        List<Integer> objectsIds = new ArrayList<>();
        for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
            SimulationObject simulationObject = (SimulationObject) iterator.next();
            // logger.info("obj id: " + System.identityHashCode(simulationObject));
            objectsIds.add(System.identityHashCode(simulationObject));
        }
        return objectsIds;
    }
}
