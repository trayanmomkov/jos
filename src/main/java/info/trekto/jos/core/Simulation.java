package info.trekto.jos.core;

import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.model.SimulationObject;

import java.util.List;
import java.util.concurrent.Flow;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public interface Simulation extends Flow.Publisher<List<SimulationObject>> {

    long startSimulation() throws SimulationException;

    List<SimulationObject> getObjects();

    List<SimulationObject> getAuxiliaryObjects();

    int getCurrentIterationNumber();

    ForceCalculator getForceCalculator();
    
    List<Flow.Subscriber<? super List<SimulationObject>>> getSubscribers();
}
