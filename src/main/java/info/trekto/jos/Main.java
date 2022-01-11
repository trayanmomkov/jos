package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.visualization.Visualizer;
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

        C.io = new JsonReaderWriter();
        C.simulation = new SimulationForkJoinImpl();

        try {
            C.prop = C.io.readProperties(args[0]);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
            return;
        }

        C.prop.createNumberFactory();

        if (C.prop.isRealTimeVisualization()) {
            Visualizer visualizer = new VisualizerImpl();
            C.simulation.addObserver(visualizer);
        }
        C.simulationLogic = new SimulationLogicImpl();
        C.simulation.startSimulation();
    }
}
