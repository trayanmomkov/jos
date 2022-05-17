package info.trekto.jos.core.model;

import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface ImmutableSimulationObject {
    Number getX();

    Number getY();

    Number getZ();

    TripleNumber getVelocity();

    TripleNumber getAcceleration();

    Number getRadius();

    int getColor();

    Number getMass();

    String getId();
}
