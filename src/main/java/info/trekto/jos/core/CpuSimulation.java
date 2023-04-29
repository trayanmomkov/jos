package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.List;

public interface CpuSimulation extends Simulation {

    List<SimulationObject> getObjects();

    List<SimulationObject> getAuxiliaryObjects();

    ForceCalculator getForceCalculator();

    void playSimulation(String absolutePath);

    void setProperties(SimulationProperties properties);

    Number calculateDistance(ImmutableSimulationObject object, ImmutableSimulationObject object1);

    boolean isCollisionExists();

    void upCollisionExists();

    void initSwitchingFromGpu(List<SimulationObject> currentObjects);
}
