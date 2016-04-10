package info.trekto.jos.core.impl;

import java.util.Iterator;

import info.trekto.jos.Container;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.formulas.CommonFormulas;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:42
 */
public class SimulationLogicImpl implements SimulationLogic {
    // private Logger logger = LoggerFactory.getLogger(getClass());
    private Number secondsPerIteration;

    public SimulationLogicImpl() {
        super();
        secondsPerIteration = Container.getSimulation().getProperties().getSecondsPerIteration();
    }

    @Override
    // public void calculateNewValues(Simulation simulation, List<SimulationObject> targetObjects) {
    public void calculateNewValues(Simulation simulation, int fromIndex, int toIndex) {
        // logger.info("calculateNewValues() for thred: " + Thread.currentThread().getName() + " Target objects: "
        // + simulation.getObjects().subList(fromIndex, toIndex));

        Iterator<SimulationObject> targetAuxiliaryObjectsIterator = simulation.getAuxiliaryObjects()
                .subList(fromIndex, toIndex).iterator();
        for (SimulationObject currentSimulationObject : simulation.getObjects().subList(fromIndex, toIndex)) {
            SimulationObject simulationAuxiliaryObject = targetAuxiliaryObjectsIterator.next();

            /** Move objects */
            moveSimulationObjects(currentSimulationObject, simulationAuxiliaryObject);

            /** Calculate acceleration */
            TripleNumber acceleration = new TripleNumber(New.ZERO, New.ZERO, New.ZERO);
            for (SimulationObject tempSimulationObject : simulation.getObjects()) {
                if (tempSimulationObject == currentSimulationObject) {
                    continue;
                }
                /** Calculate force */
                TripleNumber force = simulation.getForceCalculator().caclulateForceAsVector(currentSimulationObject,
                        tempSimulationObject,
                        CommonFormulas.calculateDistance(currentSimulationObject, tempSimulationObject));

                /** Add to current acceleration */
                acceleration = addAcceleration(currentSimulationObject, acceleration, force);
            }

            /** Change speed */
            changeSpeed(currentSimulationObject, simulationAuxiliaryObject, acceleration);
        }
    }

    private void moveSimulationObjects(SimulationObject currentSimulationObject,
                                       SimulationObject simulationAuxiliaryObject) {
        // members[i]->x = members[i]->x + members[i]->speed.x * simulationProperties.secondsPerCycle;
        simulationAuxiliaryObject.setX(currentSimulationObject.getX().add(
                currentSimulationObject.getSpeed().getX().multiply(secondsPerIteration)));
        simulationAuxiliaryObject.setY(currentSimulationObject.getY().add(
                currentSimulationObject.getSpeed().getY().multiply(secondsPerIteration)));
        simulationAuxiliaryObject.setZ(currentSimulationObject.getZ().add(
                currentSimulationObject.getSpeed().getZ().multiply(secondsPerIteration)));
    }

    private TripleNumber addAcceleration(SimulationObject currentSimulationObject, TripleNumber oldAcceleration,
                                         TripleNumber force) {
        // ax = Fx / m
        Number accelerationX = oldAcceleration.getX().add(force.getX().divide(currentSimulationObject.getMass()));
        Number accelerationY = oldAcceleration.getY().add(force.getY().divide(currentSimulationObject.getMass()));
        Number accelerationZ = oldAcceleration.getZ().add(force.getZ().divide(currentSimulationObject.getMass()));
        return new TripleNumber(accelerationX, accelerationY, accelerationZ);
    }

    private void changeSpeed(SimulationObject currentSimulationObject, SimulationObject simulationAuxiliaryObject,
                             TripleNumber acceleration) {
        // members[i]->speed.x += a.x * simulationProperties.secondsPerCycle;//* t;
        Number speedX = currentSimulationObject.getSpeed().getX()
                .add(acceleration.getX().multiply(secondsPerIteration));
        Number speedY = currentSimulationObject.getSpeed().getY()
                .add(acceleration.getY().multiply(secondsPerIteration));
        Number speedZ = currentSimulationObject.getSpeed().getZ()
                .add(acceleration.getZ().multiply(secondsPerIteration));

        simulationAuxiliaryObject.setSpeed(new TripleNumber(speedX, speedY, speedZ));
    }
}
