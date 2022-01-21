package info.trekto.jos.model;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface SimulationObject extends ImmutableSimulationObject {
    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setSpeedX(double speedX);
    
    void setSpeedY(double speedY);
    
    void setSpeedZ(double speedZ);
    
    void setRadius(double radius);

    void setColor(int color);

    void setMass(double mass);

    void setId(String id);
}
