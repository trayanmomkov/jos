package info.trekto.jos.core;

import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.model.SimulationObject;

import java.util.List;
import java.util.Observer;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:52:10
 */
public interface Simulation {

    long startSimulation() throws SimulationException;

    List<SimulationObject> getObjects();

    List<SimulationObject> getAuxiliaryObjects();

    int getCurrentIterationNumber();

    ForceCalculator getForceCalculator();

    void addObserver(Observer o);
}
