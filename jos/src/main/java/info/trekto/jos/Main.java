/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import visualization.Visualizer;
import visualization.java2dgraphics.VisualizerImpl;


/**
 * @author Trayan Momkov
 */
public class Main {

    /**
     * @param args
     * @throws SimulationException
     */
    public static void main(String[] args) throws SimulationException {
        Container.setSimulation(new SimulationImpl());

        Visualizer visualizer = new VisualizerImpl();

        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.setNumberType(NumberType.DOUBLE);
        // simulationProperties.setNumberType(NumberType.FLOAT);
        //        simulationProperties.setNumberType(NumberType.BIG_DECIMAL);
        simulationProperties.setFormatVersion1Writer(
                new FormatVersion1ReaderWriter(args[0]));
        simulationProperties.getFormatVersion1Writer().readProperties(simulationProperties);

        Container.getSimulation().setProperties(simulationProperties);
        Container.getSimulation().addObserver(visualizer);
        Container.setSimulationLogic(new SimulationLogicImpl());
        Container.getSimulation().startSimulation();
    }
}
