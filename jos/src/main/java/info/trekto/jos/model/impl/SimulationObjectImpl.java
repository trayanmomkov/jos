/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;

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

    @Override
    public Number getX() {
        return x;
    }

    @Override
    public void setX(Number x) {
        this.x = x;
    }

    @Override
    public Number getY() {
        return y;
    }

    @Override
    public void setY(Number y) {
        this.y = y;
    }

    @Override
    public TripleNumber getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(TripleNumber speed) {
        this.speed = speed;
    }

    @Override
    public Number getRadius() {
        return radius;
    }

    @Override
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    @Override
    public TripleInt getColor() {
        return color;
    }

    @Override
    public void setColor(TripleInt color) {
        this.color = color;
    }

    @Override
    public Number getMass() {
        return mass;
    }

    @Override
    public void setMass(Number mass) {
        this.mass = mass;
    }

    @Override
    public boolean isMotionless() {
        return motionless;
    }

    @Override
    public void setMotionless(boolean motionless) {
        this.motionless = motionless;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public List<TripleNumber> getTrajectory() {
        return trajectory;
    }

    @Override
    public void setTrajectory(List<TripleNumber> trajectory) {
        this.trajectory = trajectory;
    }
}
