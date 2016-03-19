/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;

import java.awt.Color;


/**
 * Container for three objects of type byte.
 * Used for representing the color of {@link SimulationObject}.
 * Immutable.
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class TripleInt {
    private final int r;
    private final int g;
    private final int b;

    /**
     * @param r
     * @param g
     * @param b
     */
    public TripleInt(int r, int g, int b) {
        super();
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public TripleInt(TripleInt tripleInt) {
        super();
        this.r = tripleInt.getR();
        this.g = tripleInt.getG();
        this.b = tripleInt.getB();
    }

    public Color toColor() {
        return new Color(r, g, b);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }
}
