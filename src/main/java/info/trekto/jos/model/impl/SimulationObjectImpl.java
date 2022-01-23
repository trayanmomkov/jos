package info.trekto.jos.model.impl;

import info.trekto.jos.C;
import info.trekto.jos.model.SimulationObject;

import static java.awt.Color.BLUE;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class SimulationObjectImpl implements SimulationObject {

    private double x;
    private double y;
    private double z;
    private double speedX;
    private double speedY;
    private double speedZ;
    private double radius;
    private int color;
    private double mass;
    private String id;

    public SimulationObjectImpl() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        color = BLUE.getRGB();
    }

    public SimulationObjectImpl(SimulationObject simulationObject) {
        this.color = simulationObject.getColor();
        this.id = simulationObject.getId();
        this.mass = simulationObject.getMass();
        this.radius = simulationObject.getRadius();
        this.speedX = simulationObject.getSpeedX();
        this.speedY = simulationObject.getSpeedY();
        this.speedZ = simulationObject.getSpeedZ();
        this.x = simulationObject.getX();
        this.y = simulationObject.getY();
        this.z = simulationObject.getZ();
    }

    public SimulationObjectImpl(int i) {
        this.color = C.simulation.simulationLogicKernel.color[i];
        this.id = C.simulation.simulationLogicKernel.id[i];
        this.mass = C.simulation.simulationLogicKernel.mass[i];
        this.radius = C.simulation.simulationLogicKernel.radius[i];
        this.speedX = C.simulation.simulationLogicKernel.speedX[i];
        this.speedY = C.simulation.simulationLogicKernel.speedY[i];
        this.x = C.simulation.simulationLogicKernel.positionX[i];
        this.y = C.simulation.simulationLogicKernel.positionY[i];
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
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
    public double getMass() {
        return mass;
    }

    @Override
    public void setMass(double mass) {
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
    public double getSpeedX() {
        return speedX;
    }

    @Override
    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    @Override
    public double getSpeedY() {
        return speedY;
    }

    @Override
    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    @Override
    public double getSpeedZ() {
        return speedZ;
    }

    @Override
    public void setSpeedZ(double speedZ) {
        this.speedZ = speedZ;
    }
}
