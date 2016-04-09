package info.trekto.jos.core.impl;

import java.util.ArrayList;

import info.trekto.jos.formulas.ForceCalculator.ForceCalculatorType;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.numbers.NumberFactoryProxy;
import info.trekto.jos.numbers.impl.BigDecimalNumberFactory;
import info.trekto.jos.numbers.impl.DoubleNumberFactory;
import info.trekto.jos.numbers.impl.FloatNumberFactory;
import info.trekto.jos.util.Utils;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:48
 */
public class SimulationProperties {
    private int numberOfIterations;

    private int nanoSecondsPerIteration;
    private Number secondsPerIteration;

    private int numberOfObjects;

    private String outputFile;

    private FormatVersion1ReaderWriter formatVersion1ReaderWriter;

    private int numberOfThreads = Utils.CORES;

    private boolean saveToFile = false;

    private boolean benchmarkMode = false;

    private NumberType numberType;
    private ForceCalculatorType forceCalculatorType;

    private int writerBufferSize = 0;

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
        this.secondsPerIteration = New.num(nanoSecondsPerIteration).divide(New.BILLION);
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }

    public boolean isBenchmarkMode() {
        return benchmarkMode;
    }

    public void setBenchmarkMode(boolean benchmarkMode) {
        this.benchmarkMode = benchmarkMode;
    }

    public int getWriterBufferSize() {
        return writerBufferSize;
    }

    public void setWriterBufferSize(int writerBufferSize) {
        this.writerBufferSize = writerBufferSize;
    }

    public Number getSecondsPerIteration() {
        return secondsPerIteration;
    }

    /**
     * @return the numberType
     */
    public NumberType getNumberType() {
        return numberType;
    }

    /**
     * @param numberType the numberType to set
     */
    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;

        switch (numberType) {
            case FLOAT:
                NumberFactoryProxy.setFactory(new FloatNumberFactory());
                break;
            case DOUBLE:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
            case BIG_DECIMAL:
                NumberFactoryProxy.setFactory(new BigDecimalNumberFactory());
                break;
            default:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
        }
    }

    /**
     * @return the forceCalculatorType
     */
    public ForceCalculatorType getForceCalculatorType() {
        return forceCalculatorType;
    }

    /**
     * @param forceCalculatorType the forceCalculatorType to set
     */
    public void setForceCalculatorType(ForceCalculatorType forceCalculatorType) {
        this.forceCalculatorType = forceCalculatorType;
    }
}
