package info.trekto.jos.core.impl;

import info.trekto.jos.core.ForceCalculator.InteractingLaw;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;
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
    private boolean bounceFromScreenBorders;
    private int saveEveryNthIteration = 1;
    private boolean saveMass = false;
    private boolean saveVelocity = false;
    private boolean saveAcceleration = false;
    private boolean mergeOnCollision = true;
    private Number coefficientOfRestitution;
    private Number minDistance;

    /* Java {@link ArrayList} is limited to Integer.MAX_VALUE */
    private int numberOfObjects;

    private int precision = NumberFactory.DEFAULT_PRECISION;

    private List<SimulationObject> initialObjects;

    public SimulationProperties() {
    }

    public SimulationProperties(SimulationProperties properties) {
        this.numberOfIterations = properties.numberOfIterations;
        this.secondsPerIteration = properties.secondsPerIteration;
        this.outputFile = properties.outputFile;
        this.saveToFile = properties.saveToFile;
        this.numberType = properties.numberType;
        this.interactingLaw = properties.interactingLaw;
        this.realTimeVisualization = properties.realTimeVisualization;
        this.playingSpeed = properties.playingSpeed;
        this.bounceFromScreenBorders = properties.bounceFromScreenBorders;
        this.saveEveryNthIteration = properties.saveEveryNthIteration;
        this.numberOfObjects = properties.numberOfObjects;
        this.precision = properties.precision;
        this.initialObjects = properties.initialObjects;
        this.saveMass = properties.saveMass;
        this.saveVelocity = properties.saveVelocity;
        this.saveAcceleration = properties.saveAcceleration;
        this.mergeOnCollision = properties.mergeOnCollision;
        this.coefficientOfRestitution = properties.coefficientOfRestitution;
        this.minDistance = properties.minDistance;
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

    public boolean isBounceFromScreenBorders() {
        return bounceFromScreenBorders;
    }

    public void setBounceFromScreenBorders(boolean bounceFromScreenBorders) {
        this.bounceFromScreenBorders = bounceFromScreenBorders;
    }

    public int getSaveEveryNthIteration() {
        return saveEveryNthIteration;
    }

    public void setSaveEveryNthIteration(int saveEveryNthIteration) {
        this.saveEveryNthIteration = saveEveryNthIteration;
    }

    public boolean isSaveMass() {
        return saveMass;
    }

    public void setSaveMass(boolean saveMass) {
        this.saveMass = saveMass;
    }

    public boolean isSaveVelocity() {
        return saveVelocity;
    }

    public void setSaveVelocity(boolean saveVelocity) {
        this.saveVelocity = saveVelocity;
    }

    public boolean isSaveAcceleration() {
        return saveAcceleration;
    }

    public void setSaveAcceleration(boolean saveAcceleration) {
        this.saveAcceleration = saveAcceleration;
    }

    public boolean isMergeOnCollision() {
        return mergeOnCollision;
    }

    public void setMergeOnCollision(boolean mergeOnCollision) {
        this.mergeOnCollision = mergeOnCollision;
    }

    public Number getCoefficientOfRestitution() {
        return coefficientOfRestitution;
    }

    public void setCoefficientOfRestitution(Number coefficientOfRestitution) {
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    public Number getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(Number minDistance) {
        this.minDistance = minDistance;
    }
}
