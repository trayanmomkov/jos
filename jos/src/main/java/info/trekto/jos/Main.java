/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.io.FormatVersion1ReaderWriter;


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


        SimulationProperties simulationProperties = new SimulationProperties();
        // simulationProperties.setNumberOfObjects(10);
        // simulationProperties.setNumberOfIterations(500);
        simulationProperties.setFormatVersion1Writer(
                new FormatVersion1ReaderWriter(args[0]));
        simulationProperties.getFormatVersion1Writer().readProperties(simulationProperties);
        // simulationProperties.getFormatVersion1Writer().readObjectFromFile();
        simulationProperties.setBenchmarkMode(true);

        Container.getSimulation().setProperties(simulationProperties);
        Container.setSimulationLogic(new SimulationLogicImpl());
        Container.getSimulation().startSimulation();

    }
}
