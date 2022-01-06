package info.trekto.jos;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.impl.SimulationProperties;

/**
 * Contains instances of main application classes.
 *
 * @author Trayan Momkov
 * @date 5 Mar 2016
 */
public class Container {

    private static SimulationLogic simulationLogic;
    private static Simulation simulation;
    private static SimulationProperties properties;

    public static SimulationLogic getSimulationLogic() {
        return simulationLogic;
    }

    public static void setSimulationLogic(SimulationLogic simulationLogic) {
        Container.simulationLogic = simulationLogic;
    }

    public static Simulation getSimulation() {
        return simulation;
    }

    public static void setSimulation(Simulation simulation) {
        Container.simulation = simulation;
    }

    public static SimulationProperties getProperties() {
        return properties;
    }

    public static void setProperties(SimulationProperties properties) {
        Container.properties = properties;
    }
}
