package info.trekto.jos.core.impl;

import info.trekto.jos.C;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.model.ImmutableSimulationObject;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static info.trekto.jos.formulas.CommonFormulas.*;
import static info.trekto.jos.numbers.New.*;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class SimulationLogicImpl implements SimulationLogic {
    static Set<SimulationObject> objectsForRemoval;

    @Override
    public void calculateNewValues(Simulation simulation, int fromIndex, int toIndex) {
        Set<ImmutableSimulationObject> oldObjectsForRemoval = new HashSet<>();

        Iterator<SimulationObject> newObjectsIterator = simulation.getAuxiliaryObjects().subList(fromIndex, toIndex).iterator();

        /* We should not change oldObject. We can change only newObject. */
        for (ImmutableSimulationObject oldObject : simulation.getObjects().subList(fromIndex, toIndex)) {
            SimulationObject newObject = newObjectsIterator.next();

            /* Move objects */
            moveObject(oldObject, newObject);

            /* Calculate acceleration */
            TripleNumber acceleration = new TripleNumber();
            for (ImmutableSimulationObject tempObject : simulation.getObjects()) {
                if (tempObject == oldObject) {
                    continue;
                }
                /* Calculate force */
                double distance = calculateDistance(oldObject, tempObject);
                TripleNumber force = simulation.getForceCalculator().calculateForceAsVector(oldObject, tempObject, distance);

                /* Add to current acceleration */
                acceleration = calculateAcceleration(oldObject, acceleration, force);
            }

            /* Change speed */
            newObject.setSpeed(calculateSpeed(oldObject, acceleration));

            /* Bounce from walls */
            if (C.prop.isBounceFromWalls()) {
                bounceFromWalls(newObject);
            }

            /* Collision and merging */
            processCollisions(oldObject, newObject, simulation.getObjects(), oldObjectsForRemoval);
        }
    }

    private void processCollisions(ImmutableSimulationObject oldObject, SimulationObject newObject,
                                   List<SimulationObject> oldObjects, Set<ImmutableSimulationObject> oldObjectsForRemoval) {
        if (oldObjectsForRemoval.contains(oldObject)) {
            /* The collision is already processed. Current newObject was smaller thus it was added in oldObjectsForRemoval. */
            objectsForRemoval.add(newObject);
            return;
        }

        for (ImmutableSimulationObject tempOldObject : oldObjects) {
            if (tempOldObject == oldObject) {
                continue;
            }
            double distance = calculateDistance(tempOldObject, newObject);
            if (distance < tempOldObject.getRadius() + newObject.getRadius()) {    // if collide
                if (newObject.getRadius() < tempOldObject.getRadius()) {
                    /*The collision will be processed
                     * when we process the other colliding object (which is the bigger one). */
                    objectsForRemoval.add(newObject);
                    return;
                }

                /* Bounce off each other */
//            if(!simulationProperties.absorbtion) {
//                calculateImpulseAfterCollision(i, j);
//            } esle {

                /* Objects merging */
                SimulationObject bigger = newObject;
                ImmutableSimulationObject smaller = tempOldObject;
                oldObjectsForRemoval.add(smaller);

                /* Speed */
                bigger.setSpeed(calculateSpeedOnMerging(smaller, bigger));

                /* Position */
                TripleNumber position = calculatePosition(smaller, bigger);
                bigger.setX(position.getX());
                bigger.setY(position.getY());
                bigger.setZ(position.getZ());

                /* Mass */
                bigger.setMass(bigger.getMass() + smaller.getMass());

                /* Volume (radius) */
                bigger.setRadius(calculateRadiusBasedOnNewVolume(smaller, bigger));

                /* Color */
                bigger.setColor(calculateColor(smaller, bigger));
//            }
            }
        }
    }

    private TripleInt calculateColor(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        double bigVolume = calculateVolumeFromRadius(bigger.getRadius());
        double smallVolume = calculateVolumeFromRadius(bigger.getRadius());
        int r = (int) Math.round((bigger.getColor().getR() * bigVolume + smaller.getColor().getR() * smallVolume) / (bigVolume + smallVolume));
        int g = (int) Math.round((bigger.getColor().getG() * bigVolume + smaller.getColor().getG() * smallVolume) / (bigVolume + smallVolume));
        int b = (int) Math.round((bigger.getColor().getB() * bigVolume + smaller.getColor().getB() * smallVolume) / (bigVolume + smallVolume));

        return new TripleInt(r, g, b);
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public static double calculateRadiusBasedOnNewVolume(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        double smallVolume = calculateVolumeFromRadius(smaller.getRadius());
        double bigVolume = calculateVolumeFromRadius(bigger.getRadius());
        return calculateRadiusFromVolume(bigVolume + smallVolume);
    }

    private TripleNumber calculatePosition(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        // TODO What about Z ?!
        double distanceX = bigger.getX() - smaller.getX();
        double distanceY = bigger.getY() - smaller.getY();

        double massRatio = smaller.getMass() / bigger.getMass();
        double x = bigger.getX() - distanceX * massRatio / TWO;
        double y = bigger.getY() - distanceY * massRatio / TWO;

        return new TripleNumber(x, y, ZERO);
    }

    private TripleNumber calculateSpeedOnMerging(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        TripleNumber totalImpulse = new TripleNumber(
                smaller.getSpeed().getX() * smaller.getMass() + bigger.getSpeed().getX() * bigger.getMass(),
                smaller.getSpeed().getY() * smaller.getMass() + bigger.getSpeed().getY() * bigger.getMass(),
                smaller.getSpeed().getZ() * smaller.getMass() + bigger.getSpeed().getZ() * bigger.getMass());
        double totalMass = bigger.getMass() + smaller.getMass();

        return new TripleNumber(totalImpulse.getX() / totalMass,
                                totalImpulse.getY() / totalMass,
                                totalImpulse.getZ() / totalMass);
    }

    private void bounceFromWalls(SimulationObject newObject) {
        if (C.simulation.getSubscribers() != null && !C.simulation.getSubscribers().isEmpty()) {
            int width = ((VisualizerImpl) C.simulation.getSubscribers().get(0)).getVisualizationPanel().getWidth();
            int height = ((VisualizerImpl) C.simulation.getSubscribers().get(0)).getVisualizationPanel().getHeight();

            if (newObject.getX() + newObject.getRadius() > width / 2.0
                    || newObject.getX() - newObject.getRadius() < -width / 2.0) {
                TripleNumber speed = new TripleNumber(-newObject.getSpeed().getX(),
                                                      newObject.getSpeed().getY(),
                                                      newObject.getSpeed().getZ());
                newObject.setSpeed(speed);
            }

            if (newObject.getY() + newObject.getRadius() > height / 2.0
                    || newObject.getY() - newObject.getRadius() < -height / 2.0) {
                TripleNumber speed = new TripleNumber(newObject.getSpeed().getX(),
                                                      -newObject.getSpeed().getY(),
                                                      newObject.getSpeed().getZ());
                newObject.setSpeed(speed);
            }
        }
    }

    private void moveObject(ImmutableSimulationObject oldObject, SimulationObject newObject) {
        // members[i]->x = members[i]->x + members[i]->speed.x * simulationProperties.secondsPerCycle;
        newObject.setX(oldObject.getX() + oldObject.getSpeed().getX() * C.prop.getSecondsPerIteration());
        newObject.setY(oldObject.getY() + oldObject.getSpeed().getY() * C.prop.getSecondsPerIteration());
        newObject.setZ(oldObject.getZ() + oldObject.getSpeed().getZ() * C.prop.getSecondsPerIteration());
    }

    private TripleNumber calculateSpeed(ImmutableSimulationObject object, TripleNumber acceleration) {
        // members[i]->speed.x += a.x * simulationProperties.secondsPerCycle;//* t;
        double speedX = object.getSpeed().getX() + acceleration.getX() * C.prop.getSecondsPerIteration();
        double speedY = object.getSpeed().getY() + acceleration.getY() * C.prop.getSecondsPerIteration();
        double speedZ = object.getSpeed().getZ() + acceleration.getZ() * C.prop.getSecondsPerIteration();

        return new TripleNumber(speedX, speedY, speedZ);
    }
}
