package info.trekto.jos.core;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

public interface SimulationLogic {
    void processTwoDimensionalCollision(SimulationObject o1, SimulationObject o2, Number cor);
}
