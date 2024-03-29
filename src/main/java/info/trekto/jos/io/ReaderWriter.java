package info.trekto.jos.io;

import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface ReaderWriter {
    SimulationProperties readPropertiesAndCreateNumberFactory(String inputFilePath) throws FileNotFoundException;

    void writeProperties(SimulationProperties properties, String outputFilePath);

    void appendObjectsToFile(List<SimulationObject> objects, SimulationProperties properties, long currentIterationNumber);

    void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, double[] positionX, double[] positionY,
                             double[] positionZ, double[] velocityX, double[] velocityY, double[] velocityZ, double[] mass, double[] radius,
                             String[] id, int[] color, boolean[] deleted, double[] accelerationX, double[] accelerationY, double[] accelerationZ);

    boolean moveToNextIteration() throws IOException;

    void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, float[] positionX, float[] positionY,
                             float[] positionZ, float[] velocityX, float[] velocityY, float[] velocityZ, float[] mass, float[] radius,
                             String[] id, int[] color, boolean[] deleted, float[] accelerationX, float[] accelerationY, float[] accelerationZ);

    void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, Number[] positionX, Number[] positionY,
                             Number[] positionZ, Number[] velocityX, Number[] velocityY, Number[] velocityZ, Number[] mass, Number[] radius,
                             String[] id, int[] color, boolean[] deleted, Number[] accelerationX, Number[] accelerationY, Number[] accelerationZ);

    void endFile();

    SimulationProperties readPropertiesForPlaying(String inputFile) throws IOException;

    boolean hasMoreIterations();

    Iteration readNextIteration() throws IOException;
}
