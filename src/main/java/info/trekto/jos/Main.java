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

    /**
     * @param args
     * @throws SimulationException
     */
    public static void main(String[] args) throws SimulationException {
        Container.setProperties(new SimulationProperties());
//        Container.setSimulation(new SimulationImpl());
        Container.setSimulation(new SimulationForkJoinImpl());

        //        Container.getProperties().setBenchmarkMode(true);
        Container.getProperties().setNumberType(NumberType.DOUBLE);
        // simulationProperties.setNumberType(NumberType.FLOAT);
        //        simulationProperties.setNumberType(NumberType.BIG_DECIMAL);
        
        if (args.length == 0) {
            System.err.println("Missing input file. Please pass it as program argument.");
            return;
        }
        Container.getProperties().setFormatVersion1Writer(new FormatVersion1ReaderWriter(args[0]));
        Container.getProperties().getFormatVersion1Writer().readProperties(Container.getProperties());

        Visualizer visualizer = new VisualizerImpl();
        Container.getSimulation().addObserver(visualizer);
        Container.setSimulationLogic(new SimulationLogicImpl());
        Container.getSimulation().startSimulation();
    }
}
