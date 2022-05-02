package info.trekto.jos.core.model.impl;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

/**
 * Container for three objects of type {@link Number}. Used for representing a point in three-dimensional space.
 * This point itself may represent a speed of {@link SimulationObject}.
 * Immutable.
 *
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class TripleNumber {

    private final Number x;
    private final Number y;
    private final Number z;

    public TripleNumber() {
        this.x = ZERO;
        this.y = ZERO;
        this.z = ZERO;
    }

    public TripleNumber(Number x, Number y, Number z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Number getX() {
        return x;
    }

    public Number getY() {
        return y;
    }

    public Number getZ() {
        return z;
    }
}
