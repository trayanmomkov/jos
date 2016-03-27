package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:42
 */
public class SimulationLogicImpl implements SimulationLogic {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    // public void calculateNewValues(Simulation simulation, List<SimulationObject> targetObjects) {
    public void calculateNewValues(Simulation simulation, int fromIndex, int toIndex) {
        // logger.info("calculateNewValues() for thred: " + Thread.currentThread().getName() + " Target objects: "
        // + simulation.getObjects().subList(fromIndex, toIndex));

        Iterator targetAuxiliaryObjectsIterator = simulation.getAuxiliaryObjects().subList(fromIndex, toIndex)
                .iterator();
        for (Iterator targetObjectsIterator = simulation.getObjects().subList(fromIndex, toIndex)
                .iterator(); targetObjectsIterator
                .hasNext();) {
            SimulationObject simulationObject = (SimulationObject) targetObjectsIterator.next();
            SimulationObject simulationAuxiliaryObject = (SimulationObject) targetAuxiliaryObjectsIterator.next();
            for (Iterator allObjectsIterator = simulation.getObjects().iterator(); allObjectsIterator.hasNext();) {
                SimulationObject simulationObject2 = (SimulationObject) allObjectsIterator.next();

            }
            simulationAuxiliaryObject.setX(simulationObject.getX().add(New.num(10000)));
            // simulationAuxiliaryObject.setY(simulationObject.getY().multiply(New.num(10)));
            // simulationAuxiliaryObject.setSpeed(new TripleNumber(
            // simulationObject.getSpeed().getX().add(New.num(2)),
            // New.num(0),
            // New.num(0))
            // );
        }
    }

}
