package info.trekto.jos.io;

import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface ReaderWriter {
    SimulationProperties readPropertiesAndCreateNumberFactory(String inputFilePath) throws FileNotFoundException;

    void writeProperties(SimulationProperties properties, String outputFilePath);

    void appendObjectsToFile(List<SimulationObject> objects, SimulationProperties properties, long currentIterationNumber);

    void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, double[] positionX, double[] positionY,
                             double[] positionZ, double[] velocityX, double[] velocityY, double[] velocityZ, double[] mass, double[] radius,
                             String[] id, int[] color, boolean[] deleted);

    void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, float[] positionX, float[] positionY,
                             float[] positionZ, float[] velocityX, float[] velocityY, float[] velocityZ, float[] mass, float[] radius,
                             String[] id, int[] color, boolean[] deleted);

    void endFile();

    SimulationProperties readPropertiesForPlaying(String inputFile) throws IOException;

    boolean hasMoreIterations();

    Iteration readNextIteration() throws IOException;
}
