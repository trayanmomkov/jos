package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;

import static java.awt.Color.BLUE;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class SimulationObjectImpl implements SimulationObject {

    private Number x;
    private Number y;
    private Number z;
    private TripleNumber speed;
    private Number radius;
    private int color;
    private Number mass;

    /* Whether the object is static */
    boolean motionless = false;
    String id;

    public SimulationObjectImpl() {
        this.x = New.ZERO;
        this.y = New.ZERO;
        this.z = New.ZERO;
        color = BLUE.getRGB();
    }

    public SimulationObjectImpl(SimulationObject simulationObject) {
        this.color = simulationObject.getColor();
        this.id = simulationObject.getId();
        this.mass = simulationObject.getMass();
        this.radius = simulationObject.getRadius();
        this.speed = simulationObject.getSpeed();
        this.x = simulationObject.getX();
        this.y = simulationObject.getY();
        this.z = simulationObject.getZ();
    }

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
    public Number getZ() {
        return z;
    }

    @Override
    public void setZ(Number z) {
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
    public Number getRadius() {
        return radius;
    }

    @Override
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int color) {
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
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
