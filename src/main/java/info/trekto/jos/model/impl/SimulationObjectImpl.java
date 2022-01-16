package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class SimulationObjectImpl implements SimulationObject {

    double x;
    double y;
    double z;
    TripleNumber speed;
    double radius;
    TripleInt color;
    double mass;

    /* Whether the object is static */
    boolean motionless = false;
    String label;

    /* Array with points through which object passed. */
    List<TripleNumber> trajectory;

    public SimulationObjectImpl() {
        this.x = New.ZERO;
        this.y = New.ZERO;
        this.z = New.ZERO;
        color = new TripleInt(0, 0, 255);
    }

    public SimulationObjectImpl(SimulationObject simulationObject) {
        this.color = simulationObject.getColor();
        this.label = simulationObject.getLabel();
        this.mass = simulationObject.getMass();
        this.motionless = simulationObject.isMotionless();
        this.radius = simulationObject.getRadius();
        this.speed = simulationObject.getSpeed();
        this.trajectory = (simulationObject.getTrajectory() == null) ? null : new ArrayList<>(simulationObject.getTrajectory());
        this.x = simulationObject.getX();
        this.y = simulationObject.getY();
        this.z = simulationObject.getZ();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
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
    public double getMass() {
        return mass;
    }

    @Override
    public void setMass(double mass) {
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
