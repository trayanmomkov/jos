/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.Number;


/**
 * Container for three objects of type {@link Number}.
 * Used for representing a point in three dimensional space. This point itself may represent a speed of
 * {@link SimulationObject}.
 * Immutable.
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class TripleNumber {
    private final Number x;
    private final Number y;
    private final Number z;

    /**
     * @param x
     * @param y
     * @param z
     */
    public TripleNumber(Number x, Number y, Number z) {
        super();
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
