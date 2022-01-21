package info.trekto.jos.util;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.formulas.CommonFormulas;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.numbers.Number;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static info.trekto.jos.formulas.ScientificConstants.*;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class Utils {
    public static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);

    public static List<SimulationObject> deepCopy(List<SimulationObject> src) {
        ArrayList<SimulationObject> dst = new ArrayList<>();
        for (SimulationObject element : src) {
            dst.add(new SimulationObjectImpl(element));
        }
        return dst;
    }

    public static void printConfiguration(SimulationProperties properties) {
        if (!properties.isSaveToFile()) {
            logger.info("███ NOT SAVING TO FILE! ███");
        }
        logger.info("JRE version: " + System.getProperty("java.specification.version"));
        logger.info("JVM  implementation name: " + System.getProperty("java.vm.name"));
        logger.info("Free memory (Mbytes): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        logger.info("Maximum memory (Mbytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (1024 * 1024)));

        /* Total memory currently available to the JVM */
        logger.info("Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory() / (1024 * 1024));

        logger.info("OS name: " + System.getProperty("os.name"));
        logger.info("OS version: " + System.getProperty("os.version"));
        logger.info("OS architecture: " + System.getProperty("os.arch"));
        logger.info("Host machine native word size: " + System.getProperty("sun.arch.data.model"));

        logger.info("Number of cores: " + CORES);
        logger.info("Precision: " + properties.getPrecision());
        logger.info("Scale: " + properties.getScale());
        logger.info("Number of objects: " + properties.getNumberOfObjects());
        logger.info("Number of iterations: " + properties.getNumberOfIterations());
        logger.info("'Number' implementation: " + properties.getNumberType());
    }

    public static String showRemainingTime(int i, long elapsedNanoseconds, long numberOfIterations) {
        return showRemainingTime(i, elapsedNanoseconds, numberOfIterations, 0);
    }

    public static String showRemainingTime(int i, long elapsedNanoseconds, long numberOfIterations, int numberOfObjects) {
        if (i == 0) {
            return "";
        }
        long elapsed = Math.round(elapsedNanoseconds / (double) NANOSECONDS_IN_ONE_MILLISECOND);
        double timePerIteration = elapsed / (double) i;
        long remainingIterations = numberOfIterations - i;
        long remainingTime = Math.round(remainingIterations * timePerIteration);

        String remainingString = "Iteration " + i
                + ", elapsed time: " + nanoToHumanReadable(elapsedNanoseconds)
                + ", objects: " + numberOfObjects
                + ", remaining time: " + milliToHumanReadable(remainingTime);

        if (C.mainForm != null) {
            C.mainForm.appendMessage(remainingString);
        }
        logger.info(remainingString);
        return remainingString;
    }

    public static String nanoToHumanReadable(long nanoseconds) {
        return milliToHumanReadable(Math.round(nanoseconds / (double) NANOSECONDS_IN_ONE_MILLISECOND));
    }

    public static String milliToHumanReadable(long milliseconds) {
        long days = milliseconds / MILLI_IN_DAY;
        long hours = (milliseconds - (days * MILLI_IN_DAY)) / MILLI_IN_HOUR;
        long minutes = (milliseconds - (days * MILLI_IN_DAY + hours * MILLI_IN_HOUR)) / MILLI_IN_MINUTE;
        long seconds = (milliseconds - (days * MILLI_IN_DAY + hours * MILLI_IN_HOUR + minutes * MILLI_IN_MINUTE)) / MILLISECONDS_IN_ONE_SECOND;
        return (days > 0 ? days + " d. " : "")
                + (hours > 0 ? hours + " h. " : "")
                + (minutes > 0 ? minutes + " m. " : "")
                + seconds + " s.";
    }

    public static boolean collisionExists(List<SimulationObject> objects) {
        for (SimulationObject object : objects) {
            for (SimulationObject object1 : objects) {
                if (object == object1) {
                    continue;
                }
                // distance between centres
                Number distance = CommonFormulas.calculateDistance(object, object1);

                if (distance.compareTo(object.getRadius().add(object1.getRadius())) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean duplicateIdExists(List<SimulationObject> objects) {
        Set<String> ids = new HashSet<>();
        for (SimulationObject object : objects) {
            if (!ids.add(object.getId())) {
                return true;
            }
        }
        return false;
    }
}
