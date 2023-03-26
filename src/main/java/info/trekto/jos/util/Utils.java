package info.trekto.jos.util;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.NumberFactory.NumberType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.ExecutionMode.AUTO;
import static info.trekto.jos.core.ExecutionMode.GPU;
import static info.trekto.jos.core.numbers.NumberFactory.DEFAULT_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static java.lang.Double.parseDouble;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class Utils {
    public static final long NANOSECONDS_IN_ONE_SECOND = 1000 * 1000 * 1000;
    public static final long MILLISECONDS_IN_ONE_SECOND = 1000;
    public static final long MILLI_IN_MINUTE = 60 * MILLISECONDS_IN_ONE_SECOND;
    public static final long MILLI_IN_HOUR = 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    public static final long MILLI_IN_DAY = 24 * 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    public static final long NANOSECONDS_IN_ONE_MILLISECOND = 1000 * 1000;
    
    private static final int ONE_DOUBLE_OBJECT_SIZE_BYTES = 443; // 299
    private static final int ONE_FLOAT_OBJECT_SIZE_BYTES = 373; // 247
    private static final int ONE_AP_OBJECT_SIZE_WITHOUT_REAL_NUMBERS_BYTES = 188;
    private static final double COMPRESSION_RATIO = 7;
    private static final double BYTES_IN_MIB = 1024 * 1024;
    public static final String NA = "---";

    public static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final int LAST_N_INTERVALS = 3;

    private static final List<Long> lastIterationsCount = new ArrayList<>();
    private static final List<Long> lastTime = new ArrayList<>();
    public static final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    public static List<SimulationObject> deepCopy(List<SimulationObject> src) {
        ArrayList<SimulationObject> dst = new ArrayList<>();
        for (SimulationObject element : src) {
            dst.add(C.createNewSimulationObject(element));
        }
        return dst;
    }

    public static void deepCopy(double[] src, double[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void deepCopy(float[] src, float[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void deepCopy(int[] src, int[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void deepCopy(boolean[] src, boolean[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
    }

    public static void printConfiguration(Simulation simulation) {
        SimulationProperties properties = simulation.getProperties();
        if (!properties.isSaveToFile()) {
            warn(logger, "NOT SAVING TO FILE!");
        }
        info(logger, "JRE version: " + System.getProperty("java.specification.version"));
        info(logger, "JVM  implementation name: " + System.getProperty("java.vm.name"));

        /* Total memory currently available to the JVM */
        info(logger, "Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory() / (1024 * 1024));

        info(logger, "OS name: " + System.getProperty("os.name"));
        info(logger, "OS version: " + System.getProperty("os.version"));
        info(logger, "OS architecture: " + System.getProperty("os.arch"));
        info(logger, "Host machine native word size: " + System.getProperty("sun.arch.data.model"));
        info(logger, "Number of cores: " + CORES);

        if ((C.getSelectedExecutionMode() == AUTO || C.getSelectedExecutionMode() == GPU)
                && Device.bestGPU().getType() == Device.TYPE.GPU) {
            OpenCLDevice openCLDevice = (OpenCLDevice) Device.bestGPU();
            info(logger, "GPU Vendor: " + openCLDevice.getOpenCLPlatform().getVendor());
            info(logger, "GPU Name: " + openCLDevice.getName());
            info(logger, "GPU Platform: " + openCLDevice.getOpenCLPlatform().getVersion());
            info(logger, "GPU Max work group size: " + openCLDevice.getMaxWorkGroupSize());
        }

        info(logger, "Precision: " + properties.getPrecision());
        info(logger, "Number of objects: " + properties.getNumberOfObjects());
        info(logger, "Number of iterations: " + properties.getNumberOfIterations());
        info(logger, "'Number' implementation: " + properties.getNumberType());
        info(logger, "'Simulation' implementation: " + simulation.getClass().getSimpleName());
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
                + (numberOfIterations < 1 ? "" : ", remaining time: " + (remainingTime >= 1 ? milliToHumanReadable(remainingTime) : NA));

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
        milliseconds = milliseconds - (days * MILLI_IN_DAY + hours * MILLI_IN_HOUR + minutes * MILLI_IN_MINUTE + seconds * MILLISECONDS_IN_ONE_SECOND);
        return (days > 0 ? days + " d. " : "")
                + (hours > 0 ? hours + " h. " : "")
                + (minutes > 0 ? minutes + " m. " : "")
                + seconds + "." + milliseconds + " s.";
    }

    public static String calculateAverageFileSize(String numberOfObjectsString, String numberOfIterationsString, String numberTypeString,
                                                  String saveEveryNthIterationString, String precisionString, boolean saveMass,
                                                  boolean saveVelocity, boolean saveAcceleration) {
        int numberOfObjects;
        long numberOfIterations = 0;
        NumberType numberType = DOUBLE;
        int saveEveryNthIteration = 1;
        int precision = DEFAULT_PRECISION;
        
        if (isNumeric(numberOfObjectsString)) {
            numberOfObjects = (int)Math.round(parseDouble(numberOfObjectsString));
        } else {
            return NA;
        }
        
        if (isNumeric(numberOfIterationsString)) {
            numberOfIterations = (int)Math.round(parseDouble(numberOfIterationsString));
        }
        
        try {
            numberType = NumberType.valueOf(numberTypeString);
        } catch (IllegalArgumentException ignored) {}
        
        if (isNumeric(saveEveryNthIterationString)) {
            saveEveryNthIteration = (int)Math.round(parseDouble(saveEveryNthIterationString));
        }
        
        if (isNumeric(precisionString)) {
            precision = (int)Math.round(parseDouble(precisionString));
        }

        double massLineBytes;
        double oneVelocityLineBytes;
        double oneAccelerationLineBytes;
        double bytesPerObject = 0;
        
        switch (numberType) {
            case FLOAT:
                massLineBytes = !saveMass ? 40 : 0;
                oneVelocityLineBytes = !saveVelocity ? 33 : 0;
                oneAccelerationLineBytes = !saveAcceleration ? 37 : 0;
                bytesPerObject = ONE_FLOAT_OBJECT_SIZE_BYTES - massLineBytes - 3 * oneVelocityLineBytes - 3 * oneAccelerationLineBytes;
                break;
            case DOUBLE:
                massLineBytes = !saveMass ? 31 : 0;
                oneVelocityLineBytes = !saveVelocity ? 42 : 0;
                oneAccelerationLineBytes = !saveAcceleration ? 45 : 0;
                bytesPerObject = ONE_DOUBLE_OBJECT_SIZE_BYTES - massLineBytes - 3 * oneVelocityLineBytes - 3 * oneAccelerationLineBytes;
                break;
            case ARBITRARY_PRECISION:
                // Z-coordinate is excluded for position, velocity and acceleration
                double realNumbersInOneObject = 3 + (saveMass ? 1 : 0) + (saveVelocity ? 2 : 0) + (saveAcceleration ? 2 : 0);
                bytesPerObject = ONE_AP_OBJECT_SIZE_WITHOUT_REAL_NUMBERS_BYTES + realNumbersInOneObject * (precision + 1);  // 1 for the dot (.)
                break;
        }
        
        int cycleHeadBytes = 63;
        double bytesPerCycle = cycleHeadBytes + numberOfObjects * bytesPerObject;
        double bytesPer1000Iterations = 1000 / (double)saveEveryNthIteration * bytesPerCycle;

        double headBytes = 473 + bytesPerCycle;
        double tailBytes = 14;
        double totalBytes = headBytes;
        
        if (numberOfIterations < 1) {
            totalBytes += bytesPer1000Iterations;
        } else {
            totalBytes += bytesPer1000Iterations * (numberOfIterations / 1000.0);
        }
        
        totalBytes += tailBytes;
        
        double totalBytesCompressed = totalBytes / COMPRESSION_RATIO;
        String totalMiB = String.valueOf(round(totalBytes / BYTES_IN_MIB, 1));
        String totalMiBCompressed = String.valueOf(round(totalBytesCompressed / BYTES_IN_MIB, 1));
        return "~ " + totalMiBCompressed + " MiB (" + totalMiB + " uncompressed)" + (numberOfIterations < 1 ? " per 1000 iterations" : "");
    }

    public static boolean isNumeric(String str) {
        try {
            parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static void error(Logger logger, String s) {
        logger.error(s);
        C.append("███ ERROR: " + s);
    }

    public static void info(Logger logger, String s) {
        logger.info(s);
        C.append("INFO: " + s);
    }

    public static void warn(Logger logger, String s) {
        logger.warn(s);
        C.append("█ WARN: " + s);
    }

    public static void error(Logger logger, String s, Throwable tr) {
        logger.error(s, tr);
        C.append("███ ERROR: " + s + " - " + tr.getMessage());
    }

    public static void warn(Logger logger, String s, Throwable tr) {
        logger.warn(s, tr);
        C.append("█ WARN: " + s + " - " + tr.getMessage());
    }

    public static void info(Logger logger, String s, Throwable tr) {
        logger.info(s, tr);
        C.append("INFO: " + s + " - " + tr.getMessage());
    }
}
