/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;


/**
 * Container for three objects of type {@link Number}.
 * Used for representing a point in three dimensional space. This point itself may represent a speed of
 * {@link SimulationObject}.
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class TripleNumber {
    private Number x;
    private Number y;
    private Number z;

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

    public void setX(Number x) {
        this.x = x;
    }

    public Number getY() {
        return y;
    }

    public void setY(Number y) {
        this.y = y;
    }

    public Number getZ() {
        return z;
    }

    public void setZ(Number z) {
        this.z = z;
    }
}
