package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.formulas.ScientificConstants;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.numbers.New.*;
import static info.trekto.jos.core.numbers.New.RATIO_FOUR_THREE;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class SimulationLogicImpl implements SimulationLogic {

    @Override
    public void calculateNewValues(Simulation simulation, int fromIndex, int toIndex) {
        Iterator<SimulationObject> newObjectsIterator = simulation.getAuxiliaryObjects().subList(fromIndex, toIndex).iterator();

        /* We should not change oldObject. We can change only newObject. */
        for (ImmutableSimulationObject oldObject : simulation.getObjects().subList(fromIndex, toIndex)) {
            SimulationObject newObject = newObjectsIterator.next();

            /* Calculate acceleration */
            TripleNumber acceleration = new TripleNumber();
            for (ImmutableSimulationObject tempObject : simulation.getObjects()) {
                if (tempObject == oldObject) {
                    continue;
                }
                /* Calculate force */
                Number distance = calculateDistance(oldObject, tempObject);
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

            /* Move objects */
            moveObject(oldObject, newObject);
        }
    }

    @Override
    public void processCollisions(Simulation simulation) {
        List<SimulationObject> forRemoval = new ArrayList<>();
        for (SimulationObject newObject : simulation.getAuxiliaryObjects()) {
            if (forRemoval.contains(newObject)) {
                continue;
            }
            for (SimulationObject tempObject : simulation.getAuxiliaryObjects()) {
                if (tempObject == newObject || forRemoval.contains(tempObject)) {
                    continue;
                }
                Number distance = calculateDistance(newObject, tempObject);
                if (distance.compareTo(tempObject.getRadius().add(newObject.getRadius())) < 0) {    // if collide
                    SimulationObject bigger;
                    SimulationObject smaller;
                    if (newObject.getMass().compareTo(tempObject.getMass()) < 0) {
                        smaller = newObject;
                        bigger = tempObject;
                    } else {
                        smaller = tempObject;
                        bigger = newObject;
                    }
                    forRemoval.add(smaller);

                    /* Objects merging */
                    /* Speed */
                    bigger.setSpeed(calculateSpeedOnMerging(smaller, bigger));

                    /* Position */
                    TripleNumber position = calculatePosition(smaller, bigger);
                    bigger.setX(position.getX());
                    bigger.setY(position.getY());
                    bigger.setZ(position.getZ());

                    /* Color */
                    bigger.setColor(calculateColor(smaller, bigger));


                    /* Volume (radius) */
                    bigger.setRadius(calculateRadiusBasedOnNewVolumeAndDensity(smaller, bigger));

                    /* Mass */
                    bigger.setMass(bigger.getMass().add(smaller.getMass()));

                    if (newObject == smaller) {
                        /* If the current object is deleted one, stop processing it further. */
                        break;
                    }
                }
            }
        }
        simulation.getAuxiliaryObjects().removeAll(forRemoval);
    }

    private int calculateColor(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        double bigV = calculateVolumeFromRadius(bigger.getRadius()).doubleValue();
        double smallV = calculateVolumeFromRadius(smaller.getRadius()).doubleValue();
        long r = Math.round((new Color(bigger.getColor()).getRed() * bigV + new Color(smaller.getColor()).getRed() * smallV) / (bigV + smallV));
        long g = Math.round((new Color(bigger.getColor()).getGreen() * bigV + new Color(smaller.getColor()).getGreen() * smallV) / (bigV + smallV));
        long b = Math.round((new Color(bigger.getColor()).getBlue() * bigV + new Color(smaller.getColor()).getBlue() * smallV) / (bigV + smallV));

        return new Color((int) r, (int) g, (int) b).getRGB();
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public Number calculateRadiusBasedOnNewVolumeAndDensity(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        Number smallVolume = calculateVolumeFromRadius(smaller.getRadius());
        Number smallDensity = smaller.getMass().divide(smallVolume);
        Number bigVolume = calculateVolumeFromRadius(bigger.getRadius());
        Number bigDensity = bigger.getMass().divide(bigVolume);
        Number newMass = bigger.getMass().add(smaller.getMass());

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be, 
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        Number newDensity = (smallDensity.multiply(smaller.getMass()).add((bigDensity.multiply(bigger.getMass()))).divide(newMass));
        Number newVolume = newMass.divide(newDensity);

        return calculateRadiusFromVolume(newVolume);
    }

    private static TripleNumber calculatePosition(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        Number distanceX = bigger.getX().subtract(smaller.getX());
        Number distanceY = bigger.getY().subtract(smaller.getY());
        Number distanceZ = bigger.getZ().subtract(smaller.getZ());

        Number massRatio = smaller.getMass().divide(bigger.getMass());
        Number x = bigger.getX().subtract(distanceX.multiply(massRatio).divide(TWO));
        Number y = bigger.getY().subtract(distanceY.multiply(massRatio).divide(TWO));
        Number z = bigger.getZ().subtract(distanceZ.multiply(massRatio).divide(TWO));

        return new TripleNumber(x, y, z);
    }

    private static TripleNumber calculateSpeedOnMerging(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        TripleNumber totalImpulse = new TripleNumber(
                smaller.getSpeed().getX().multiply(smaller.getMass()).add(bigger.getSpeed().getX().multiply(bigger.getMass())),
                smaller.getSpeed().getY().multiply(smaller.getMass()).add(bigger.getSpeed().getY().multiply(bigger.getMass())),
                smaller.getSpeed().getZ().multiply(smaller.getMass()).add(bigger.getSpeed().getZ().multiply(bigger.getMass())));
        Number totalMass = bigger.getMass().add(smaller.getMass());

        return new TripleNumber(totalImpulse.getX().divide(totalMass),
                                totalImpulse.getY().divide(totalMass),
                                totalImpulse.getZ().divide(totalMass));
    }

    private void bounceFromWalls(SimulationObject newObject) {
        if (C.visualizer != null) {
            int width = C.visualizer.getVisualizationPanel().getWidth();
            int height = C.visualizer.getVisualizationPanel().getHeight();

            if (newObject.getX().add(newObject.getRadius()).doubleValue() > width / 2.0
                    || newObject.getX().subtract(newObject.getRadius()).doubleValue() < -width / 2.0) {
                TripleNumber speed = new TripleNumber(newObject.getSpeed().getX().negate(),
                                                      newObject.getSpeed().getY(),
                                                      newObject.getSpeed().getZ());
                newObject.setSpeed(speed);
            }

            if (newObject.getY().add(newObject.getRadius()).doubleValue() > height / 2.0
                    || newObject.getY().subtract(newObject.getRadius()).doubleValue() < -height / 2.0) {
                TripleNumber speed = new TripleNumber(newObject.getSpeed().getX(),
                                                      newObject.getSpeed().getY().negate(),
                                                      newObject.getSpeed().getZ());
                newObject.setSpeed(speed);
            }
        }
    }

    private void moveObject(ImmutableSimulationObject oldObject, SimulationObject newObject) {
        // members[i]->x = members[i]->x + members[i]->speed.x * simulationProperties.secondsPerCycle;
        newObject.setX(newObject.getX().add(newObject.getSpeed().getX().multiply(C.prop.getSecondsPerIteration())));
        newObject.setY(newObject.getY().add(newObject.getSpeed().getY().multiply(C.prop.getSecondsPerIteration())));
        newObject.setZ(newObject.getZ().add(newObject.getSpeed().getZ().multiply(C.prop.getSecondsPerIteration())));
    }

    private TripleNumber calculateSpeed(ImmutableSimulationObject object, TripleNumber acceleration) {
        // members[i]->speed.x += a.x * simulationProperties.secondsPerCycle;//* t;
        Number speedX = object.getSpeed().getX().add(acceleration.getX().multiply(C.prop.getSecondsPerIteration()));
        Number speedY = object.getSpeed().getY().add(acceleration.getY().multiply(C.prop.getSecondsPerIteration()));
        Number speedZ = object.getSpeed().getZ().add(acceleration.getZ().multiply(C.prop.getSecondsPerIteration()));

        return new TripleNumber(speedX, speedY, speedZ);
    }

    @Override
    public Number calculateDistance(ImmutableSimulationObject object1, ImmutableSimulationObject object2) {
        Number x = object2.getX().subtract(object1.getX());
        Number y = object2.getY().subtract(object1.getY());
        Number z = object2.getZ().subtract(object1.getZ());
        return (x.multiply(x).add(y.multiply(y)).add(z.multiply(z))).sqrt();
    }

    @Override
    public Number calculateVolumeFromRadius(Number radius) {
        // V = 4/3 * pi * r^3
        return RATIO_FOUR_THREE.multiply(ScientificConstants.PI).multiply(radius.pow(3));
    }

    public Number calculateRadiusFromVolume(Number volume) {
        // V = 4/3 * pi * r^3
        return IGNORED.cbrt(volume.divide(RATIO_FOUR_THREE.multiply(ScientificConstants.PI)));
    }

    public TripleNumber calculateAcceleration(ImmutableSimulationObject object, TripleNumber acceleration, TripleNumber force) {
        // ax = Fx / m
        Number newAccelerationX = acceleration.getX().add(force.getX().divide(object.getMass()));
        Number newAccelerationY = acceleration.getY().add(force.getY().divide(object.getMass()));
        Number newAccelerationZ = acceleration.getZ().add(force.getZ().divide(object.getMass()));
        return new TripleNumber(newAccelerationX, newAccelerationY, newAccelerationZ);
    }
}
