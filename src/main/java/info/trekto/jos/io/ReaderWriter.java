package info.trekto.jos.io;

import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ReaderWriter {
    SimulationProperties readProperties(String inputFilePath) throws FileNotFoundException;

    void writeProperties(SimulationProperties properties, String outputFilePath);

    void appendObjectsToFile();

    void endFile();

    SimulationProperties readPropertiesForPlaying(String inputFile) throws IOException;

    boolean hasMoreIterations();

    Iteration readNextIteration() throws IOException;
}
