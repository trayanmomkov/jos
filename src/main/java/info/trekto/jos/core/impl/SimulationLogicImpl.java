package info.trekto.jos.core.impl;

import info.trekto.jos.C;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.formulas.CommonFormulas;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

import java.util.Iterator;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class SimulationLogicImpl implements SimulationLogic {
    @Override
    public void calculateNewValues(Simulation simulation, int fromIndex, int toIndex) {
        Iterator<SimulationObject> targetAuxiliaryObjectsIterator = simulation.getAuxiliaryObjects()
                .subList(fromIndex, toIndex).iterator();
        for (SimulationObject currentSimulationObject : simulation.getObjects().subList(fromIndex, toIndex)) {
            SimulationObject simulationAuxiliaryObject = targetAuxiliaryObjectsIterator.next();

            /* Move objects */
            moveSimulationObjects(currentSimulationObject, simulationAuxiliaryObject);

            /* Calculate acceleration */
            TripleNumber acceleration = new TripleNumber();
            for (SimulationObject tempSimulationObject : simulation.getObjects()) {
                if (tempSimulationObject == currentSimulationObject) {
                    continue;
                }
                /* Calculate force */
                Number distance = CommonFormulas.calculateDistance(currentSimulationObject, tempSimulationObject);
                TripleNumber force = simulation.getForceCalculator()
                        .calculateForceAsVector(currentSimulationObject, tempSimulationObject, distance);

                /* Add to current acceleration */
                acceleration = addAcceleration(currentSimulationObject, acceleration, force);
            }

            /* Change speed */
            changeSpeed(currentSimulationObject, simulationAuxiliaryObject, acceleration);
        }
    }

    private void moveSimulationObjects(SimulationObject currentSimulationObject,
                                       SimulationObject simulationAuxiliaryObject) {
        // members[i]->x = members[i]->x + members[i]->speed.x * simulationProperties.secondsPerCycle;
        simulationAuxiliaryObject.setX(currentSimulationObject.getX().add(
                currentSimulationObject.getSpeed().getX().multiply(C.prop.getSecondsPerIteration())));
        simulationAuxiliaryObject.setY(currentSimulationObject.getY().add(
                currentSimulationObject.getSpeed().getY().multiply(C.prop.getSecondsPerIteration())));
        simulationAuxiliaryObject.setZ(currentSimulationObject.getZ().add(
                currentSimulationObject.getSpeed().getZ().multiply(C.prop.getSecondsPerIteration())));
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
                .add(acceleration.getX().multiply(C.prop.getSecondsPerIteration()));
        Number speedY = currentSimulationObject.getSpeed().getY()
                .add(acceleration.getY().multiply(C.prop.getSecondsPerIteration()));
        Number speedZ = currentSimulationObject.getSpeed().getZ()
                .add(acceleration.getZ().multiply(C.prop.getSecondsPerIteration()));

        simulationAuxiliaryObject.setSpeed(new TripleNumber(speedX, speedY, speedZ));
    }
}
