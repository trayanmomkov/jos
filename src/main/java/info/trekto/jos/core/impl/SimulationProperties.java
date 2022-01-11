package info.trekto.jos.core.impl;

import info.trekto.jos.exceptions.SimulationRuntimeException;
import info.trekto.jos.formulas.ForceCalculator.InteractingLaw;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.numbers.NumberFactoryProxy;
import info.trekto.jos.numbers.impl.BigDecimalNumberFactory;
import info.trekto.jos.numbers.impl.DoubleNumberFactory;
import info.trekto.jos.numbers.impl.FloatNumberFactory;
import info.trekto.jos.util.Utils;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.formulas.ForceCalculator.InteractingLaw.NEWTON_LAW_OF_GRAVITATION;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:48
 */
public class SimulationProperties {
    private long numberOfIterations;

    private long nanoSecondsPerIteration;
    private double secondsPerIteration;

    private long numberOfObjects;

    private String outputFile;

    private boolean saveToFile = false;

    private NumberType numberType;
    private InteractingLaw interactingLaw = NEWTON_LAW_OF_GRAVITATION;
    private Integer precision;

    private boolean realTimeVisualization = false;

    private int playingSpeed = 1;

    private List<SimulationObject> initialObjects;

    public SimulationProperties() {
    }

    public boolean isInfiniteSimulation() {
        return numberOfIterations == -1;
    }

    /**
     * Returns number of objects.
     *
     * @return
     */
    public long getNumberOfObjects() {
        return numberOfObjects;
    }

    /**
     * Java {@link ArrayList} is limited to Integer.MAX_VALUE
     */
    public void setNumberOfObjects(long numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public long getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public long getNanoSecondsPerIteration() {
        return nanoSecondsPerIteration;
    }

    public void setNanoSecondsPerIteration(long nanoSecondsPerIteration) {
        this.nanoSecondsPerIteration = nanoSecondsPerIteration;
        this.secondsPerIteration = nanoSecondsPerIteration / 1000000000.0;
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }

    public double getSecondsPerIteration() {
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
    }

    /**
     * @return the forceCalculatorType
     */
    public InteractingLaw getInteractingLaw() {
        return interactingLaw;
    }

    /**
     * @param interactingLaw the forceCalculatorType to set
     */
    public void setInteractingLaw(InteractingLaw interactingLaw) {
        this.interactingLaw = interactingLaw;
    }

    public int getPrecision() {
        return precision;
    }

    /**
     * Number of digits to be used for an operation; results are rounded to this precision
     *
     * @param precision
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * @return the realTimeVisualization
     */
    public boolean isRealTimeVisualization() {
        return realTimeVisualization;
    }

    /**
     * @param realTimeVisualization the realTimeVisualization to set
     */
    public void setRealTimeVisualization(boolean realTimeVisualization) {
        this.realTimeVisualization = realTimeVisualization;
    }

    /**
     * @return the playingSpeed
     */
    public int getPlayingSpeed() {
        return playingSpeed;
    }

    /**
     * @param playingSpeed the playingSpeed to set
     */
    public void setPlayingSpeed(int playingSpeed) {
        this.playingSpeed = playingSpeed;
    }

    public void createNumberFactory() {
        switch (numberType) {
            case FLOAT:
                NumberFactoryProxy.setFactory(new FloatNumberFactory());
                break;
            case DOUBLE:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
            case BIG_DECIMAL:
                if (precision == null) {
                    throw new SimulationRuntimeException("Precision is not set!");
                }
                NumberFactoryProxy.setFactory(new BigDecimalNumberFactory(new MathContext(precision)));
                break;
            default:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
        }
    }

    public List<SimulationObject> getInitialObjects() {
        return initialObjects;
    }

    public void setInitialObjects(List<SimulationObject> initialObjects) {
        this.initialObjects = initialObjects;
    }
}
