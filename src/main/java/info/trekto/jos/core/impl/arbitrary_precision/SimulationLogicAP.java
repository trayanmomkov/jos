package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

import java.awt.*;
import java.util.List;
import java.util.*;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.*;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class SimulationLogicAP implements SimulationLogic {
    private final Simulation simulation;

    public SimulationLogicAP(Simulation simulation) {
        this.simulation = simulation;
    }

    public void calculateNewValues(int fromIndex, int toIndex) {
        Iterator<SimulationObject> newObjectsIterator = simulation.getAuxiliaryObjects().subList(fromIndex, toIndex).iterator();

        /* We should not change oldObject. We can change only newObject. */
        for (ImmutableSimulationObject oldObject : simulation.getObjects().subList(fromIndex, toIndex)) {
            SimulationObject newObject = newObjectsIterator.next();

            /* Speed is scalar, velocity is vector. Velocity = speed + direction. */

            /* Time T passed */

            /* Calculate acceleration */
            /* For the time T, forces accelerated the objects (changed their velocities).
             * Forces are calculated having the positions of the objects at the beginning of the period,
             * and these forces are applied for time T. */
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

            /* Move objects */
            /* For the time T, velocity moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            moveObject(newObject);

            /* Change velocity */
            /* For the time T, accelerations changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            newObject.setVelocity(calculateVelocity(oldObject));
            
            /* Change the acceleration */
            newObject.setAcceleration(acceleration);

            /* Bounce from screen borders */
            /* Only change the direction of the velocity */
            if (simulation.getProperties().isBounceFromScreenBorders()) {
                bounceFromScreenBorders(newObject);
            }
        }
    }

    public void processCollisions(Simulation simulation) {
        boolean mergeOnCollision = simulation.getProperties().isMergeOnCollision();
        List<ImmutableSimulationObject> forRemoval = null;
        Set<Map.Entry<SimulationObject, SimulationObject>> processedElasticCollision = null;
        if (mergeOnCollision) {
            forRemoval = new ArrayList<>();
        } else {
            processedElasticCollision = new HashSet<>();
        }
        for (SimulationObject newObject : simulation.getAuxiliaryObjects()) {
            if (mergeOnCollision && forRemoval.contains(newObject)) {
                continue;
            }
            for (SimulationObject tempObject : simulation.getAuxiliaryObjects()) {
                if (tempObject == newObject) {
                    continue;
                }

                if (mergeOnCollision) {
                    if (forRemoval.contains(tempObject)) {
                        continue;
                    }
                } else if (processedElasticCollision.contains(new AbstractMap.SimpleEntry<>(tempObject, newObject))) {
                    continue;
                }

                Number distance = calculateDistance(newObject, tempObject);
                if (distance.compareTo(tempObject.getRadius().add(newObject.getRadius())) <= 0) {    // if collide
                    if (!mergeOnCollision) {
                        processTwoDimensionalCollision(newObject, tempObject, simulation.getProperties().getCoefficientOfRestitution());
                        processedElasticCollision.add(new AbstractMap.SimpleEntry<>(tempObject, newObject));
                        processedElasticCollision.add(new AbstractMap.SimpleEntry<>(newObject, tempObject));
                    } else {
                        SimulationObject bigger;
                        ImmutableSimulationObject smaller;
                        if (newObject.getMass().compareTo(tempObject.getMass()) < 0) {
                            smaller = newObject;
                            bigger = tempObject;
                        } else {
                            smaller = tempObject;
                            bigger = newObject;
                        }
                        forRemoval.add(smaller);

                        /* Objects merging */
                        /* Velocity */
                        bigger.setVelocity(calculateVelocityOnMerging(smaller, bigger));

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
        }
        if (mergeOnCollision) {
            simulation.getAuxiliaryObjects().removeAll(forRemoval);
        }
    }

    private int calculateColor(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        double bigMass = bigger.getMass().doubleValue();
        double smallMass = smaller.getMass().doubleValue();
        double totalMass = bigMass + smallMass;
        long r = Math.round((new Color(bigger.getColor()).getRed() * bigMass + new Color(smaller.getColor()).getRed() * smallMass) / totalMass);
        long g = Math.round((new Color(bigger.getColor()).getGreen() * bigMass + new Color(smaller.getColor()).getGreen() * smallMass) / totalMass);
        long b = Math.round((new Color(bigger.getColor()).getBlue() * bigMass + new Color(smaller.getColor()).getBlue() * smallMass) / totalMass);

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

    private TripleNumber calculatePosition(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        Number distanceX = bigger.getX().subtract(smaller.getX());
        Number distanceY = bigger.getY().subtract(smaller.getY());
        Number distanceZ = bigger.getZ().subtract(smaller.getZ());

        Number massRatio = smaller.getMass().divide(bigger.getMass());
        Number x = bigger.getX().subtract(distanceX.multiply(massRatio).divide(TWO));
        Number y = bigger.getY().subtract(distanceY.multiply(massRatio).divide(TWO));
        Number z = bigger.getZ().subtract(distanceZ.multiply(massRatio).divide(TWO));

        return new TripleNumber(x, y, z);
    }

    private static TripleNumber calculateVelocityOnMerging(ImmutableSimulationObject smaller, ImmutableSimulationObject bigger) {
        TripleNumber totalImpulse = new TripleNumber(
                smaller.getVelocity().getX().multiply(smaller.getMass()).add(bigger.getVelocity().getX().multiply(bigger.getMass())),
                smaller.getVelocity().getY().multiply(smaller.getMass()).add(bigger.getVelocity().getY().multiply(bigger.getMass())),
                smaller.getVelocity().getZ().multiply(smaller.getMass()).add(bigger.getVelocity().getZ().multiply(bigger.getMass())));
        Number totalMass = bigger.getMass().add(smaller.getMass());

        return new TripleNumber(totalImpulse.getX().divide(totalMass),
                                totalImpulse.getY().divide(totalMass),
                                totalImpulse.getZ().divide(totalMass));
    }

    private void bounceFromScreenBorders(SimulationObject newObject) {
        if (C.getVisualizer() != null) {
            int width = C.getVisualizer().getVisualizationPanel().getWidth();
            int height = C.getVisualizer().getVisualizationPanel().getHeight();

            if (newObject.getX().add(newObject.getRadius()).doubleValue() >= width / 2.0
                    || newObject.getX().subtract(newObject.getRadius()).doubleValue() <= -width / 2.0) {
                TripleNumber velocity = new TripleNumber(newObject.getVelocity().getX().negate(),
                                                      newObject.getVelocity().getY(),
                                                      newObject.getVelocity().getZ());
                newObject.setVelocity(velocity);
            }

            if (newObject.getY().add(newObject.getRadius()).doubleValue() >= height / 2.0
                    || newObject.getY().subtract(newObject.getRadius()).doubleValue() <= -height / 2.0) {
                TripleNumber velocity = new TripleNumber(newObject.getVelocity().getX(),
                                                      newObject.getVelocity().getY().negate(),
                                                      newObject.getVelocity().getZ());
                newObject.setVelocity(velocity);
            }
        }
    }

    private void moveObject(SimulationObject newObject) {
        // members[i]->x = members[i]->x + members[i]->speed.x * simulationProperties.secondsPerCycle;
        newObject.setX(newObject.getX().add(newObject.getVelocity().getX().multiply(simulation.getProperties().getSecondsPerIteration())));
        newObject.setY(newObject.getY().add(newObject.getVelocity().getY().multiply(simulation.getProperties().getSecondsPerIteration())));
        newObject.setZ(newObject.getZ().add(newObject.getVelocity().getZ().multiply(simulation.getProperties().getSecondsPerIteration())));
    }

    private TripleNumber calculateVelocity(ImmutableSimulationObject object) {
        // members[i]->speed.x += a.x * simulationProperties.secondsPerCycle;//* t;
        Number velX = object.getVelocity().getX().add(object.getAcceleration().getX().multiply(simulation.getProperties().getSecondsPerIteration()));
        Number velY = object.getVelocity().getY().add(object.getAcceleration().getY().multiply(simulation.getProperties().getSecondsPerIteration()));
        Number velZ = object.getVelocity().getZ().add(object.getAcceleration().getZ().multiply(simulation.getProperties().getSecondsPerIteration()));

        return new TripleNumber(velX, velY, velZ);
    }

    public Number calculateDistance(ImmutableSimulationObject object1, ImmutableSimulationObject object2) {
        Number x = object2.getX().subtract(object1.getX());
        Number y = object2.getY().subtract(object1.getY());
        Number z = object2.getZ().subtract(object1.getZ());
        return (x.multiply(x).add(y.multiply(y)).add(z.multiply(z))).sqrt();
    }

    public static Number calculateVolumeFromRadius(Number radius) {
        // V = 4/3 * pi * r^3
        return RATIO_FOUR_THREE.multiply(PI).multiply(radius.pow(3));
    }

    public static Number calculateRadiusFromVolume(Number volume) {
        // V = 4/3 * pi * r^3
        return IGNORED.cbrt(volume.divide(RATIO_FOUR_THREE.multiply(PI)));
    }

    public TripleNumber calculateAcceleration(ImmutableSimulationObject object, TripleNumber acceleration, TripleNumber force) {
        // ax = Fx / m
        Number newAccelerationX = acceleration.getX().add(force.getX().divide(object.getMass()));
        Number newAccelerationY = acceleration.getY().add(force.getY().divide(object.getMass()));
        Number newAccelerationZ = acceleration.getZ().add(force.getZ().divide(object.getMass()));
        return new TripleNumber(newAccelerationX, newAccelerationY, newAccelerationZ);
    }
    
    public void processTwoDimensionalCollision(SimulationObject o1, SimulationObject o2, Number cor) {
        Number v1x = o1.getVelocity().getX();
        Number v1y = o1.getVelocity().getY();
        Number v2x = o2.getVelocity().getX();
        Number v2y = o2.getVelocity().getY();
        
        Number o1x = o1.getX();
        Number o1y = o1.getY();
        Number o2x = o2.getX();
        Number o2y = o2.getY();
        
        Number o1m = o1.getMass();
        Number o2m = o2.getMass();
        
        // v'1y = v1y - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1y, o1x, o2y, o2x) * (o1y-o2y)
        // v'2x = v2x - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2x, o2y, o1x, o1y) * (o2x-o1x)
        // v'2y = v2y - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2y, o2x, o1y, o1x) * (o2y-o1y)
        // v'1x = v1x - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        Number o1NewVelocityX = calculateVelocity(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m, cor);
        Number o1NewVelocityY = calculateVelocity(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m, cor);
        Number o2NewVelocityX = calculateVelocity(v2x, v2y, v1x, v1y, o2x, o2y, o1x, o1y, o2m, o1m, cor);
        Number o2NewVelocityY = calculateVelocity(v2y, v2x, v1y, v1x, o2y, o2x, o1y, o1x, o2m, o1m, cor);
        
        o1.setVelocity(new TripleNumber(o1NewVelocityX, o1NewVelocityY, ZERO));
        o2.setVelocity(new TripleNumber(o2NewVelocityX, o2NewVelocityY, ZERO));
    }

    private Number calculateVelocity(Number v1x, Number v1y, Number v2x, Number v2y,
                                     Number o1x, Number o1y, Number o2x, Number o2y, Number o1m, Number o2m, Number cor) {
        // v'1x = v1x - 2*o2m/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return v1x.subtract(
                (cor.multiply(o2m).add(o2m))
                        .divide(o1m.add(o2m))
                        .multiply(dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y))
                        .divide(dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y))
                        .multiply(o1x.subtract(o2x))
        );
    }

    private Number dotProduct2D(Number ax, Number ay, Number bx, Number by, Number cx, Number cy, Number dx, Number dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax.subtract(bx)).multiply(cx.subtract(dx)).add(ay.subtract(by).multiply(cy.subtract(dy)));
    }
}
