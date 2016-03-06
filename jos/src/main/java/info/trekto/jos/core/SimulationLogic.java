package info.trekto.jos.core;

import java.util.List;

import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:20
 */
public interface SimulationLogic {
    void calculateNewValues(Simulation simulation, List<SimulationObject> targetObjects);
}
