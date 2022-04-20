package info.trekto.jos.core;


import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.numbers.Number;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public interface SimulationLogic {
    void calculateNewValues(int fromIndex, int toIndex);

    void processCollisions(Simulation simulation);

    Number calculateDistance(ImmutableSimulationObject object1, ImmutableSimulationObject object2);

    Number calculateVolumeFromRadius(Number radius);
}
