package info.trekto.jos;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator.InteractingLaw;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.numbers.NumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

import static info.trekto.jos.numbers.NumberFactory.NumberType.*;

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

        Simulation simulation = new SimulationForkJoinImpl();
        benchmark.runBenchmark(simulation, FLOAT, inputFileName);
        benchmark.runBenchmark(simulation, DOUBLE, inputFileName);
        benchmark.runBenchmark(simulation, BIG_DECIMAL, inputFileName);
    }

    private void runBenchmark(Simulation simulation, NumberFactory.NumberType numberType, String inputFileName) throws SimulationException {

        C.io = new JsonReaderWriter();
        C.simulation = simulation;

        try {
            C.prop = C.io.readProperties(inputFileName);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
            return;
        }
        C.simulationLogic = new SimulationLogicImpl();

        C.prop.setNumberType(numberType);
        C.prop.setSaveToFile(false);
        C.prop.setInteractingLaw(InteractingLaw.NEWTON_LAW_OF_GRAVITATION);

        C.simulationLogic = new SimulationLogicImpl();

        long durationInNanoseconds = C.simulation.startSimulation();

        logger.info(C.prop.getPrecision() + "\t" + C.prop.getNumberType() + "\t" + (durationInNanoseconds / 1000000) + " ms");
    }
}
