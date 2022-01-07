/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.visualization.Visualizer;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;

/**
 * @author Trayan Momkov
 */
public class Main {
    public static void main(String[] args) throws SimulationException {
        if (args.length == 0) {
            System.err.println("Missing input file. Please pass it as program argument.");
            return;
        }
        Container.setProperties(FormatVersion1ReaderWriter.readProperties(args[0]));
        Container.setSimulation(new SimulationForkJoinImpl());
        Visualizer visualizer = new VisualizerImpl();
        Container.getSimulation().addObserver(visualizer);
        Container.setSimulationLogic(new SimulationLogicImpl());
        Container.getSimulation().startSimulation();
    }
}
