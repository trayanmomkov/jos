/**
 * 
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.FormatVersion1Writer;


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
        simulationProperties.setFormatVersion1Writer(new FormatVersion1Writer("/home/bibo/Desktop/simulation.out"));
        Container.getSimulation().setProperties(simulationProperties);
        Container.getSimulation().startSimulation();
    }
}
