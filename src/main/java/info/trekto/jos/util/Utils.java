package info.trekto.jos.util;

import info.trekto.jos.core.impl.SimulationProperties;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:54:43
 */
public class Utils {
    public static final int CORES = Runtime.getRuntime().availableProcessors();

    // public static final int CORES = 1;

    public static void log(Object... objects) {
        for (Object object : objects) {
            log(object.toString());
        }
    }

    public static void log(String... messages) {
        for (String message : messages) {
            System.out.println(message);
        }
    }

    public static void printConfiguration(SimulationProperties properties) {
        if (properties.isBenchmarkMode() || !properties.isSaveToFile()) {
            log("███ NOT SAVING TO FILE! ███");
        }
        log("JRE version: " + System.getProperty("java.specification.version"));
        log("JVM  implementation name: " + System.getProperty("java.vm.name"));
        // log("JVM implementation version: " + System.getProperty("java.vm.version"));
        // log("The Name of JIT compiler to use: " + System.getProperty("java.compiler"));

        log("Free memory (Mbytes): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        log("Maximum memory (Mbytes): "
                + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (1024 * 1024)));

        /* Total memory currently available to the JVM */
        log("Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory()
                / (1024 * 1024));

        log("OS name: " + System.getProperty("os.name"));
        log("OS version: " + System.getProperty("os.version"));
        log("OS architecture: " + System.getProperty("os.arch"));
        log("Host machine native word size: " + System.getProperty("sun.arch.data.model"));

        log("Number of cores: " + CORES);
        log("Precision (number of digits to be used): " + properties.getPrecision());
        log("Number of runnig threads: " + properties.getNumberOfThreads());
        log("Number of objects: " + properties.getNumberOfObjects());
        log("Number of iterations: " + properties.getNumberOfIterations());
        log("'Number' implementation: " + properties.getNumberType());
        log("Writer buffer size: " + properties.getWriterBufferSize());
    }
}
