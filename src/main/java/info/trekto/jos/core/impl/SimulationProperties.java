package info.trekto.jos.core.impl;

import info.trekto.jos.core.ForceCalculator.InteractingLaw;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory.NumberType;

import java.util.List;

import static info.trekto.jos.core.ForceCalculator.InteractingLaw.NEWTON_LAW_OF_GRAVITATION;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public class SimulationProperties {
    private long numberOfIterations;
    private Number secondsPerIteration;
    private String outputFile;
    private boolean saveToFile = false;
    private NumberType numberType = NumberType.DOUBLE;
    private InteractingLaw interactingLaw = NEWTON_LAW_OF_GRAVITATION;
    private boolean realTimeVisualization = false;
    private int playingSpeed = 0;
    private boolean bounceFromWalls;
    private int saveEveryNthIteration = 1;

    /* Java {@link ArrayList} is limited to Integer.MAX_VALUE */
    private int numberOfObjects;

    /* BigDecimal precision */
    private int precision = 16;

    /* BigDecimal scale */
    private int scale = 16;

    private List<SimulationObject> initialObjects;

    public SimulationProperties() {
    }

    public boolean isInfiniteSimulation() {
        return numberOfIterations == 0;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
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

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }

    public NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;
    }

    public InteractingLaw getInteractingLaw() {
        return interactingLaw;
    }

    public void setInteractingLaw(InteractingLaw interactingLaw) {
        this.interactingLaw = interactingLaw;
    }

    public boolean isRealTimeVisualization() {
        return realTimeVisualization;
    }

    public void setRealTimeVisualization(boolean realTimeVisualization) {
        this.realTimeVisualization = realTimeVisualization;
    }

    public int getPlayingSpeed() {
        return playingSpeed;
    }

    public void setPlayingSpeed(int playingSpeed) {
        this.playingSpeed = playingSpeed;
    }

    public List<SimulationObject> getInitialObjects() {
        return initialObjects;
    }

    public void setInitialObjects(List<SimulationObject> initialObjects) {
        this.initialObjects = initialObjects;
    }

    public Number getSecondsPerIteration() {
        return secondsPerIteration;
    }

    public void setSecondsPerIteration(Number secondsPerIteration) {
        this.secondsPerIteration = secondsPerIteration;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isBounceFromWalls() {
        return bounceFromWalls;
    }

    public void setBounceFromWalls(boolean bounceFromWalls) {
        this.bounceFromWalls = bounceFromWalls;
    }

    public int getSaveEveryNthIteration() {
        return saveEveryNthIteration;
    }

    public void setSaveEveryNthIteration(int saveEveryNthIteration) {
        this.saveEveryNthIteration = saveEveryNthIteration;
    }
}
