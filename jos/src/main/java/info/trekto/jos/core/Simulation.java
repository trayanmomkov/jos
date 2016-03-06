package info.trekto.jos.core;

import java.util.List;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:52:10
 */
public interface Simulation {

    void setProperties(SimulationProperties simulationProperties);

    SimulationProperties getProperties();

    void startSimulation();

    public List<SimulationObject> getObjects();

    public void setObjects(List<SimulationObject> objects);

    public List<SimulationObject> getAuxiliaryObjects();

    public void setAuxiliaryObjects(List<SimulationObject> auxiliaryObjects);

    public List<SimulationObject> getObjectsForRemoval();

    public void setObjectsForRemoval(List<SimulationObject> objectsForRemoval);
}
