/**
 * 
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.FormatVersion1ReaderWriter;


/**
 * @author Trayan Momkov
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Container.setSimulation(new SimulationImpl());
        Container.setSimulationLogic(new SimulationLogicImpl());

        SimulationProperties simulationProperties = new SimulationProperties();
        // simulationProperties.setNumberOfObjects(10);
        // simulationProperties.setNumberOfIterations(500);
        simulationProperties.setFormatVersion1Writer(
                new FormatVersion1ReaderWriter("/media/Data/Projects/v7.2.3.1/simulations/PSC_5_RUN"));
        // simulationProperties.getFormatVersion1Writer().readObjectFromFile();
        Container.getSimulation().setProperties(simulationProperties);
        Container.getSimulation().startSimulation();
    }
}
