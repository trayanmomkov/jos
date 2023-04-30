package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.DataAP;

public interface CpuSimulation extends Simulation {

    DataAP getData();
    
    void playSimulation(String inputFile);

    void setProperties(SimulationProperties properties);

    void setDataAndInitializeLogic(DataAP data);
}
