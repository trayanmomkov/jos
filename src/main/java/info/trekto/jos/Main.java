/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
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

        Container.readerWriter = new FormatVersion1ReaderWriter();
        Container.simulation = new SimulationForkJoinImpl();

        Container.properties = Container.readerWriter.readProperties(args[0]);
        Container.readerWriter.initReaderAndWriter(args[0], Container.properties);
        Visualizer visualizer = new VisualizerImpl();
        Container.simulation.addObserver(visualizer);
        Container.simulationLogic = new SimulationLogicImpl();
        Container.simulation.startSimulation();
    }
}
