package info.trekto.jos.core.model.impl;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.Objects;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

/**
 * Container for three objects of type {@link Number}. Used for representing a point in three-dimensional space.
 * This point itself may represent a velocity of {@link SimulationObject}.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TripleNumber)) {
            return false;
        }
        TripleNumber that = (TripleNumber) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
