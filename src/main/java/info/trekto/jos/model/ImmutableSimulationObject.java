package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

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

    int getColor();

    Number getMass();

    String getId();
}
