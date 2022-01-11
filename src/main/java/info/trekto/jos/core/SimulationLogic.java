package info.trekto.jos.core;


import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:20
 */
public interface SimulationLogic {
    // void calculateNewValues(Simulation simulation, List<SimulationObject> targetObjects);
    void calculateNewValues(Simulation simulation, int fromIndex, int toIndex);
    void calculateNewValues(Simulation simulation, SimulationObject simulationObject, SimulationObject simulationAuxiliaryObject);
}
