package info.trekto.jos.core.impl;

import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.util.Utils;

import java.util.ArrayList;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:48
 */
public class SimulationProperties {
    private int numberOfIterations;

    private int nanoSecondsPerIteration;

    private int numberOfObjects;

    private String outputFile;

    private FormatVersion1ReaderWriter formatVersion1ReaderWriter;

    private int numberOfThreads = Utils.CORES;

    public boolean isInfiniteSimulation() {
        return numberOfIterations == -1;
    }

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
    public FormatVersion1ReaderWriter getFormatVersion1Writer() {
        return formatVersion1ReaderWriter;
    }

    public void setFormatVersion1Writer(FormatVersion1ReaderWriter formatVersion1ReaderWriter) {
        this.formatVersion1ReaderWriter = formatVersion1ReaderWriter;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public int getNanoSecondsPerIteration() {
        return nanoSecondsPerIteration;
    }

    public void setNanoSecondsPerIteration(int nanoSecondsPerIteration) {
        this.nanoSecondsPerIteration = nanoSecondsPerIteration;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
