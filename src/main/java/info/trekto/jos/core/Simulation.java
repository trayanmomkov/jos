package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.formulas.ForceCalculator;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;

import java.io.IOException;
import java.util.List;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public interface Simulation {

    void startSimulation() throws SimulationException;

    List<SimulationObject> getObjects();

    List<SimulationObject> getAuxiliaryObjects();

    long getCurrentIterationNumber();

    ForceCalculator getForceCalculator();
    
    boolean isRunning();

    void init(SimulationProperties prop);

    void initForPlaying(String absolutePath) throws IOException;

    void playSimulation(String absolutePath);

    void init(String absolutePath);
    
    void setPaused(boolean paused);
    
    boolean isPaused();

    void switchPause();

    SimulationLogic getSimulationLogic();

    void setSimulationLogic(SimulationLogic simulationLogic);

    SimulationProperties getProperties();

    void setProperties(SimulationProperties properties);
}
