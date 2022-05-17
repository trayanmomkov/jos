package info.trekto.jos.core.model;

import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface SimulationObject extends ImmutableSimulationObject {
    void setX(Number x);

    void setY(Number y);

    void setZ(Number z);

    void setVelocity(TripleNumber velocity);

    void setAcceleration(TripleNumber acceleration);

    void setRadius(Number radius);

    void setColor(int color);

    void setMass(Number mass);

    void setId(String id);
}
