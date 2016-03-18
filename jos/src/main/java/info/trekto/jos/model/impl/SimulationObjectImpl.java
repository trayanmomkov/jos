/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.Number;

import java.util.List;

/**
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class SimulationObjectImpl implements SimulationObject {
    Number x;
    Number y;
    TripleNumber speed;
    Number radius;
    TripleInt color;
    Number mass;

    /** Whether the object is static */
    boolean motionless = false;
    String label;

    /** Array with points which object passed through. */
    List<TripleNumber> trajectory;

    public Number getX() {
        return x;
    }

    public void setX(Number x) {
        this.x = x;
    }


    public Number getY() {
        return y;
    }


    public void setY(Number y) {
        this.y = y;
    }


    public TripleNumber getSpeed() {
        return speed;
    }


    public void setSpeed(TripleNumber speed) {
        this.speed = speed;
    }


    public Number getRadius() {
        return radius;
    }


    public void setRadius(Number radius) {
        this.radius = radius;
    }


    public TripleInt getColor() {
        return color;
    }


    public void setColor(TripleInt color) {
        this.color = color;
    }


    public Number getMass() {
        return mass;
    }


    public void setMass(Number mass) {
        this.mass = mass;
    }


    public boolean isMotionless() {
        return motionless;
    }


    public void setMotionless(boolean motionless) {
        this.motionless = motionless;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    public List<TripleNumber> getTrajectory() {
        return trajectory;
    }


    public void setTrajectory(List<TripleNumber> trajectory) {
        this.trajectory = trajectory;
    }

    /**
     * The magnitude of speed which is a 3D vector.
     */
    public Number calculateSpeedMagnitude() {
        return speed.getX().pow(2).add(speed.getY().pow(2)).add(speed.getZ().pow(2)).sqrt();
    }
}
