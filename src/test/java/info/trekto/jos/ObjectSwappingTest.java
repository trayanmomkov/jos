/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.visualization.Visualizer;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Trayan Momkov
 * @date 18 Mar 2016
 */
public class ObjectSwappingTest {
    private static final Logger logger = LoggerFactory.getLogger(ObjectSwappingTest.class);

    @Test
    public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException, FileNotFoundException {
            
        Container.readerWriter = new JsonReaderWriter();
        Container.simulation = new SimulationForkJoinImpl();

        Container.properties = Container.readerWriter.readProperties("src/test/resources/PSC_5_10_objects_Java2D_RUN");
        Container.properties.createNumberFactory();
        Container.readerWriter.initWriter(Container.properties, "src/test/resources/PSC_5_10_objects_Java2D_RUN");
        Visualizer visualizer = new VisualizerImpl();
        Container.simulation.addObserver(visualizer);
        Container.simulationLogic = new SimulationLogicImpl();
            
            
        Container.properties.setNumberOfObjects(10);
        Container.properties.setNumberOfIterations(10);

        Class clazz = Container.simulation.getClass();

        Method initMethod = clazz.getDeclaredMethod("init");
        Method doIterationMethod = clazz.getDeclaredMethod("doIteration");

        initMethod.setAccessible(true);
        doIterationMethod.setAccessible(true);
        initMethod.invoke(Container.simulation);

        List<Integer> originalObjectsIds;
        List<Integer> auxiliaryObjectsIds;

        /** Get ids of first (original) objects */
        originalObjectsIds = getObjectsIds(Container.simulation.getObjects());

        Field auxiliaryObjectsField = clazz.getDeclaredField("auxiliaryObjects");
        auxiliaryObjectsField.setAccessible(true);

        List<SimulationObject> auxiliaryObjects = (List<SimulationObject>) auxiliaryObjectsField.get(Container.simulation);

        /** Get ids of auxiliary objects */
        auxiliaryObjectsIds = getObjectsIds(auxiliaryObjects);

        /** Do iterations and check whether objects in the two lists swap */
        for (long i = 0; i < Container.properties.getNumberOfIterations(); i++) {
            logger.info("\nIteration " + i);
            doIterationMethod.invoke(Container.simulation);

            if (i % 2 == 0) {
                List<Integer> actualObjects = getObjectsIds(Container.simulation.getObjects());
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField.get(Container
                                                                                                         .simulation));
                assertTrue(originalObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(originalObjectsIds));
            } else {
                List<Integer> actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField
                        .get(Container.simulation));
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds(Container.simulation.getObjects());
                assertTrue(originalObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(originalObjectsIds));
            }
        }
    }

    private List<Integer> getObjectsIds(List<SimulationObject> objects) {
        List<Integer> objectsIds = new ArrayList<>();
        for (Object element : objects) {
            SimulationObject simulationObject = (SimulationObject) element;
            // logger.info("obj id: " + System.identityHashCode(simulationObject));
            objectsIds.add(System.identityHashCode(simulationObject));
        }
        return objectsIds;
    }
}
