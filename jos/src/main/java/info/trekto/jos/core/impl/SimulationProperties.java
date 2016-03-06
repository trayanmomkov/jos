package info.trekto.jos.core.impl;

import java.util.ArrayList;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:48
 */
public class SimulationProperties {
    private int numberOfIterations;

    private int numberOfObjects;

    /**
     * Call {@link #getNumberOfObjects}. Returns number of objects.
     * @return
     */
    public int getN() {
        return numberOfObjects;
    }

    /**
     * Returns number of objects.
     * @return
     */
    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    /** Java {@link ArrayList} is limited to Integer.MAX_VALUE */
    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }
}
