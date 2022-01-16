package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;

import java.util.List;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface ImmutableSimulationObject {
    double getX();

    double getY();

    double getZ();

    TripleNumber getSpeed();

    double getRadius();

    TripleInt getColor();

    double getMass();

    boolean isMotionless();

    String getLabel();

    /** This must be used only for visualization purposes. Trajectory list consumes memory and CPU. */
    List<TripleNumber> getTrajectory();
}
