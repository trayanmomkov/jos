package info.trekto.jos.core.model.impl;

import info.trekto.jos.core.model.SimulationObject;

import java.awt.*;


/**
 * Container for three objects of type byte.
 * Used for representing the color of {@link SimulationObject}.
 * Immutable.
 *
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class TripleInt {
    private final int r;
    private final int g;
    private final int b;

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
