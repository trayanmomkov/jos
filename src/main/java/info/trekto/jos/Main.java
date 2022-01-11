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

        Container.readerWriter = new JsonReaderWriter();
        Container.simulation = new SimulationForkJoinImpl();

        try {
            Container.properties = Container.readerWriter.readProperties(args[0]);
        } catch (FileNotFoundException e) {
            logger.error("Cannot read properties file.", e);
            return;
        }
        
        Container.properties.createNumberFactory();

        if (Container.properties.isRealTimeVisualization()) {
            Visualizer visualizer = new VisualizerImpl();
            Container.simulation.addObserver(visualizer);
        }
        Container.simulationLogic = new SimulationLogicImpl();
        Container.simulation.startSimulation();
    }
}
