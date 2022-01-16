package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;

/**
 * Container for three objects of type {@link Number}. Used for representing a point in three-dimensional space.
 * This point itself may represent a speed of {@link SimulationObject}.
 * Immutable.
 *
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class TripleNumber {

    private final double x;
    private final double y;
    private final double z;

    public TripleNumber() {
        this.x = New.ZERO;
        this.y = New.ZERO;
        this.z = New.ZERO;
    }

    public TripleNumber(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
