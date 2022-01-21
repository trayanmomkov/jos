package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

import java.util.List;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface ImmutableSimulationObject {
    Number getX();

    Number getY();

    Number getZ();

    TripleNumber getSpeed();

    Number getRadius();

    TripleInt getColor();

    Number getMass();

    String getId();

    /** This must be used only for visualization purposes. Trajectory list consumes memory and CPU. */
    List<TripleNumber> getTrajectory();
}
