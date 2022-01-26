package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static info.trekto.jos.util.Utils.error;

/**
 * @author Trayan Momkov
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SimulationException {
        if (args.length == 0) {
            error(logger, "Missing input file. Please pass it as program argument.");
            return;
        }

        SimulationImpl.init(args[0]);

        if (C.prop.isRealTimeVisualization()) {
            C.visualizer = new VisualizerImpl();
        }

        try {
            C.simulation.startSimulation();
        } catch (ArithmeticException ex) {
            if (ex.getMessage().contains("zero")) {
                String message = "Operation with zero. Please increase the precision and try again. " + ex.getMessage();
                error(logger, message, ex);
            } else {
                throw ex;
            }
        }
    }
}
