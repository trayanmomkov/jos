package info.trekto.jos;

import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * @author Trayan Momkov
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SimulationException {
        if (args.length == 0) {
            System.err.println("Missing input file. Please pass it as program argument.");
            return;
        }

        String inputFile = args[0];
        init(inputFile);
        if (C.prop.isRealTimeVisualization()) {
            C.visualizer = new VisualizerImpl();
        }
        C.simulation.startSimulation();
    }
    
    public static void init(String inputFile) {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readProperties(inputFile);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
        }
    }
}
