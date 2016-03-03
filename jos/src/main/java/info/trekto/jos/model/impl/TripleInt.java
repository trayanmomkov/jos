/**
 * 
 */
package info.trekto.jos.model.impl;

import info.trekto.jos.model.SimulationObject;

import java.awt.Color;


/**
 * Container for three objects of type byte.
 * Used for representing the color of {@link SimulationObject}.
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class TripleInt {
    private int r;
    private int g;
    private int b;

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

    public Color toColor() {
        return new Color(r, g, b);
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
