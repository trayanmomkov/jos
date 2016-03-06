/**
 * 
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;


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

        Container.getSimulation().setProperties(new SimulationProperties());
        Container.getSimulation().startSimulation();
    }
}
