package info.trekto.jos.io;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.model.SimulationObject;

import java.io.FileNotFoundException;
import java.util.List;

public interface ReaderWriter {
    SimulationProperties readProperties(String inputFilePath) throws FileNotFoundException;

    void writeProperties(SimulationProperties properties, String outputFilePath);

    void appendObjectsToFile(List<SimulationObject> objects);

    void appendObjectsToFile();

    void endFile();
}
