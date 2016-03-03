/**
 * 
 */
package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;

import java.util.List;

/**
 * @author Trayan Momkov
 * @date 19 Aug 2015
 */
public interface SimulationObject {
    Number getX();

    void setX(Number x);

    Number getY();

    void setY(Number y);

    TripleNumber getSpeed();

    void setSpeed(TripleNumber speed);

    Number getRadius();

    void setRadius(Number radius);

    TripleInt getColor();

    void setColor(TripleInt color);

    Number getMass();

    void setMass(Number mass);

    boolean isMotionless();

    void setMotionless(boolean motionless);

    String getLabel();

    void setLabel(String label);

    List<TripleNumber> getTrajectory();

    void setTrajectory(List<TripleNumber> trajectory);
}
