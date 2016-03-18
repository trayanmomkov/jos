package info.trekto.jos.core.impl;

import info.trekto.jos.io.FormatVersion1Writer;

import java.util.ArrayList;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:48
 */
public class SimulationProperties {
    private int numberOfIterations;

    private int numberOfObjects;

    private FormatVersion1Writer formatVersion1Writer;

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

    /**
     * Returns fireWrite for saving objects in a file.
     * @return
     */
    public FormatVersion1Writer getFormatVersion1Writer() {
        return formatVersion1Writer;
    }

    public void setFormatVersion1Writer(FormatVersion1Writer formatVersion1Writer) {
        this.formatVersion1Writer = formatVersion1Writer;
    }
}
