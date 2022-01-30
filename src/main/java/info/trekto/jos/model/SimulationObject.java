package info.trekto.jos.model;

import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface SimulationObject extends ImmutableSimulationObject {
    void setX(Number x);

    void setY(Number y);

    void setZ(Number z);

    void setSpeed(TripleNumber speed);

    void setRadius(Number radius);

    void setColor(int color);

    void setMass(Number mass);

    void setId(String id);
}
