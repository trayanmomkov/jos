package info.trekto.jos.core.model.impl;

import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.TRIPLE_ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static java.awt.Color.BLUE;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class SimulationObjectImpl implements SimulationObject {
    public static final int DEFAULT_COLOR = BLUE.getRGB();
    public static final int DEFAULT_COLOR_SIMPLIFIED = BLUE.getBlue();

    private Number x;
    private Number y;
    private Number z;
    private TripleNumber velocity;
    private TripleNumber acceleration;
    private Number radius;
    private int color;
    private Number mass;

    /* Whether the object is static */
    boolean motionless = false;
    String id;

    public SimulationObjectImpl() {
        this.x = ZERO;
        this.y = ZERO;
        this.z = ZERO;
        this.velocity = TRIPLE_ZERO;
        this.acceleration = TRIPLE_ZERO;
        color = DEFAULT_COLOR;
    }

    public SimulationObjectImpl(ImmutableSimulationObject simulationObject) {
        this.color = simulationObject.getColor();
        this.id = simulationObject.getId();
        this.mass = simulationObject.getMass();
        this.radius = simulationObject.getRadius();
        this.velocity = simulationObject.getVelocity();
        this.acceleration = simulationObject.getAcceleration();
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
    public TripleNumber getVelocity() {
        return velocity;
    }

    @Override
    public TripleNumber getAcceleration() {
        return acceleration;
    }

    @Override
    public void setVelocity(TripleNumber velocity) {
        this.velocity = velocity;
    }

    @Override
    public void setAcceleration(TripleNumber acceleration) {
        this.acceleration = acceleration;
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

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", velocity=" + velocity +
                '}';
    }

    /**
     * Do not change! {@link SimulationObjectImpl} is used as HashMap key.
     * This method is created only to show that usage of Object's equals is intentional.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /**
     * Do not change! {@link SimulationObjectImpl} is used as HashMap key.
     * This method is created only to show that usage of Object's hashCode is intentional.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
