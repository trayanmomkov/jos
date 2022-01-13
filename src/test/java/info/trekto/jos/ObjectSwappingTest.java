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
 * 18 Mar 2016
 */
public class ObjectSwappingTest {
    private static final Logger logger = LoggerFactory.getLogger(ObjectSwappingTest.class);

    @Test
    public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException, FileNotFoundException {

        C.io = new JsonReaderWriter();
        C.simulation = new SimulationForkJoinImpl();

        C.prop = C.io.readProperties("src/test/resources/PSC_5_10_objects_Java2D_RUN.json");
//        createNumberFactory(NumberFactory.NumberType.BIG_DECIMAL, 32, 16);
        Visualizer visualizer = new VisualizerImpl();
        C.simulation.subscribe(visualizer);
        C.simulationLogic = new SimulationLogicImpl();


        C.prop.setNumberOfObjects(10);
        C.prop.setNumberOfIterations(10);

        Class clazz = C.simulation.getClass();

        Method initMethod = clazz.getDeclaredMethod("init");
        Method doIterationMethod = clazz.getDeclaredMethod("doIteration");

        initMethod.setAccessible(true);
        doIterationMethod.setAccessible(true);
        initMethod.invoke(C.simulation);

        List<Integer> originalObjectsIds;
        List<Integer> auxiliaryObjectsIds;

        /** Get ids of first (original) objects */
        originalObjectsIds = getObjectsIds(C.simulation.getObjects());

        Field auxiliaryObjectsField = clazz.getDeclaredField("auxiliaryObjects");
        auxiliaryObjectsField.setAccessible(true);

        List<SimulationObject> auxiliaryObjects = (List<SimulationObject>) auxiliaryObjectsField.get(C.simulation);

        /** Get ids of auxiliary objects */
        auxiliaryObjectsIds = getObjectsIds(auxiliaryObjects);

        /** Do iterations and check whether objects in the two lists swap */
        for (long i = 0; i < C.prop.getNumberOfIterations(); i++) {
            logger.info("\nIteration " + i);
            doIterationMethod.invoke(C.simulation);

            if (i % 2 == 0) {
                List<Integer> actualObjects = getObjectsIds(C.simulation.getObjects());
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField.get(C
                                                                                                         .simulation));
                assertTrue(originalObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(originalObjectsIds));
            } else {
                List<Integer> actualObjects = getObjectsIds((List<SimulationObject>) auxiliaryObjectsField
                        .get(C.simulation));
                assertTrue(auxiliaryObjectsIds.containsAll(actualObjects));
                assertTrue(actualObjects.containsAll(auxiliaryObjectsIds));

                actualObjects = getObjectsIds(C.simulation.getObjects());
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
