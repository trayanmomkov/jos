package info.trekto.jos.util;

import info.trekto.jos.core.impl.SimulationProperties;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:54:43
 */
public class Utils {
    public static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void printConfiguration(SimulationProperties properties) {
        if (properties.isBenchmarkMode() || !properties.isSaveToFile()) {
            logger.info("███ NOT SAVING TO FILE! ███");
        }
        logger.info("JRE version: " + System.getProperty("java.specification.version"));
        logger.info("JVM  implementation name: " + System.getProperty("java.vm.name"));
        // logger.info("JVM implementation version: " + System.getProperty("java.vm.version"));
        // logger.info("The Name of JIT compiler to use: " + System.getProperty("java.compiler"));

        logger.info("Free memory (Mbytes): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        logger.info("Maximum memory (Mbytes): "
                            + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (1024 * 1024)));

        /* Total memory currently available to the JVM */
        logger.info("Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory()
                / (1024 * 1024));

        logger.info("OS name: " + System.getProperty("os.name"));
        logger.info("OS version: " + System.getProperty("os.version"));
        logger.info("OS architecture: " + System.getProperty("os.arch"));
        logger.info("Host machine native word size: " + System.getProperty("sun.arch.data.model"));

        logger.info("Number of cores: " + CORES);
        logger.info("Precision (number of digits to be used): " + properties.getPrecision());
        logger.info("Number of runnig threads: " + properties.getNumberOfThreads());
        logger.info("Number of objects: " + properties.getNumberOfObjects());
        logger.info("Number of iterations: " + properties.getNumberOfIterations());
        logger.info("'Number' implementation: " + properties.getNumberType());
        logger.info("Writer buffer size: " + properties.getWriterBufferSize());
    }
}
