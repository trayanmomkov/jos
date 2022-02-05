package info.trekto.jos.util;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.formulas.CommonFormulas;
import info.trekto.jos.model.SimulationObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static info.trekto.jos.core.impl.SimulationLogicImpl.calculateDistance;
import static info.trekto.jos.formulas.ScientificConstants.*;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class Utils {
    public static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final int LAST_N_INTERVALS = 3;

    private static final List<Long> lastIterationsCount = new ArrayList<>();
    private static final List<Long> lastTime = new ArrayList<>();
    public static final DateFormat df = new SimpleDateFormat("HH-mm-ss.SSS");

    public static void deepCopy(double[] src, double[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void deepCopy(int[] src, int[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void deepCopy(boolean[] src, boolean[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void printConfiguration(SimulationProperties properties) {
        if (!properties.isSaveToFile()) {
            warn(logger, "NOT SAVING TO FILE!");
        }
        info(logger, "JRE version: " + System.getProperty("java.specification.version"));
        info(logger, "JVM  implementation name: " + System.getProperty("java.vm.name"));
        info(logger, "Free memory (Mbytes): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        info(logger, "Maximum memory (Mbytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (1024 * 1024)));

        /* Total memory currently available to the JVM */
        info(logger, "Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory() / (1024 * 1024));

        info(logger, "OS name: " + System.getProperty("os.name"));
        info(logger, "OS version: " + System.getProperty("os.version"));
        info(logger, "OS architecture: " + System.getProperty("os.arch"));
        info(logger, "Host machine native word size: " + System.getProperty("sun.arch.data.model"));

        info(logger, "Number of cores: " + CORES);
        info(logger, "Precision: " + properties.getPrecision());
        info(logger, "Scale: " + properties.getScale());
        info(logger, "Number of objects: " + properties.getNumberOfObjects());
        info(logger, "Number of iterations: " + properties.getNumberOfIterations());
        info(logger, "'Number' implementation: double");
    }

    public static void showRemainingTime(long i, long startTime, long numberOfIterations, int numberOfObjects) {
        if (i == 0) {
            return;
        }

        if (lastIterationsCount.size() == LAST_N_INTERVALS) {
            lastIterationsCount.remove(0);
            lastTime.remove(0);
        }
        lastIterationsCount.add(i);
        lastTime.add(System.nanoTime());

        long time = lastTime.get(lastTime.size() - 1) - lastTime.get(0);
        double iterations = lastIterationsCount.get(lastIterationsCount.size() - 1) - lastIterationsCount.get(0);

        double averageTimePerIteration;
        if (time == 0) {
            long elapsed = (System.nanoTime() - startTime);
            averageTimePerIteration = Math.round(elapsed / (double) i / (double) NANOSECONDS_IN_ONE_MILLISECOND);
        } else {
            averageTimePerIteration = time / iterations / (double) NANOSECONDS_IN_ONE_MILLISECOND;
        }

        long remainingIterations = numberOfIterations - i;
        long remainingTime = Math.round(remainingIterations * averageTimePerIteration);
        if (remainingTime < 0) {
            remainingTime = 0;
        }

        String remainingString = "Iteration " + i
                + ", elapsed time: " + nanoToHumanReadable(System.nanoTime() - startTime)
                + ", objects: " + numberOfObjects
                + (numberOfIterations < 1 ? "" : ", remaining time: " + milliToHumanReadable(remainingTime));

        info(logger, remainingString);
    }

    public static String nanoToHumanReadable(long nanoseconds) {
        return milliToHumanReadable(Math.round(nanoseconds / (double) NANOSECONDS_IN_ONE_MILLISECOND));
    }

    public static String secondsToHumanReadable(double seconds) {
        return milliToHumanReadable(Math.round(seconds * MILLISECONDS_IN_ONE_SECOND));
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

    public static boolean collisionExists(double[] positionX, double[] positionY, double[] radius) {
        for (int i = 0; i < positionX.length; i++) {
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }
                // distance between centres
                double distance = calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);

                if (distance < radius[i] + radius[j]) {
                    info(logger, String.format("Collision between object A(x:%f, y:%f, r:%f) and B(x:%f, y:%f, r:%f)",
                                              positionX[i], positionY[i], radius[i], positionX[j], positionY[j], radius[j]));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean collisionExists(List<SimulationObject> objects) {
        for (SimulationObject object : objects) {
            for (SimulationObject object1 : objects) {
                if (object == object1) {
                    continue;
                }
                // distance between centres
                double distance = CommonFormulas.calculateDistance(object, object1);

                if (distance < object.getRadius() + object1.getRadius()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean duplicateIdExists(String[] id) {
        Set<String> ids = new HashSet<>();
        for (String s : id) {
            if (!ids.add(s)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    private static void append(String message) {
    if (C.mainForm != null) {
            C.mainForm.appendMessage(df.format(new Date()) + " " + message);
        }
    }
    
    public static void error(Logger logger, String s) {
        logger.error(s);
        append("███ ERROR: " + s);
    }
    
    public static void info(Logger logger, String s) {
        logger.info(s);
        append("INFO: " + s);
    }
    
    public static void warn(Logger logger, String s) {
        logger.warn(s);
        append("█ WARN: " + s);
    }
    
    public static void error(Logger logger, String s, Throwable tr) {
        logger.error(s, tr);
        append("███ ERROR: " + s + " - " + tr.getMessage());
    }
    
    public static void warn(Logger logger, String s, Throwable tr) {
        logger.warn(s, tr);
        append("█ WARN: " + s + " - " + tr.getMessage());
    }
    
    public static void info(Logger logger, String s, Throwable tr) {
        logger.info(s, tr);
        append("INFO: " + s + " - " + tr.getMessage());
    }
}
