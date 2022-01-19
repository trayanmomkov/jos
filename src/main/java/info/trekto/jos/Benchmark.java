package info.trekto.jos;

import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.JsonReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * @author Trayan Momkov
 * 31 Mar 2016
 */
public class Benchmark {
    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

    public static void main(String[] args) throws SimulationException {
        Benchmark benchmark = new Benchmark();
        if (args.length == 0) {
            System.err.println("Missing input file. Please pass it as program argument.");
            return;
        }
        String inputFileName = args[0];

        benchmark.runBenchmark(inputFileName);

//        32	BIG_DECIMAL	10873 ms
//        32	APFLOAT	752 ms
//        32	DOUBLE	24 ms
//        32	FLOAT	27 ms
    }

    private void runBenchmark(String inputFileName) throws SimulationException {

        C.io = new JsonReaderWriter();

        try {
            C.prop = C.io.readProperties(inputFileName);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
            return;
        }

        C.prop.setSaveToFile(false);

        long durationInNanoseconds = C.simulation.startSimulation();

        logger.info(C.prop.getPrecision() + "\tDOUBLE\t" + (durationInNanoseconds / 1000000) + " ms");
    }
}
