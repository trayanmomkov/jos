package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;

import java.util.List;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface SimulationObject extends ImmutableSimulationObject {
    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setSpeed(TripleNumber speed);

    void setRadius(double radius);

    void setColor(TripleInt color);

    void setMass(double mass);

    void setMotionless(boolean motionless);

    void setLabel(String label);

    /** This must be used only for visualization purposes. Trajectory list consumes memory and CPU. */
    void setTrajectory(List<TripleNumber> trajectory);
}
