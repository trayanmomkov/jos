package info.trekto.jos.core.impl;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:42
 */
public class SimulationLogicImpl implements SimulationLogic {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void calculateNewValues(Simulation simulation, List<SimulationObject> targetObjects) {
        logger.warn("calculateNewValues() for thred: " + Thread.currentThread().getName() + " Target objects: "
                + targetObjects);
        for (Iterator iterator = targetObjects.iterator(); iterator.hasNext();) {
            SimulationObject simulationObject = (SimulationObject) iterator.next();
            simulationObject.setX(New.num(10).multiply(simulationObject.getX()));
            simulationObject.setY(New.num(10).multiply(simulationObject.getY()));
        }
    }

}
