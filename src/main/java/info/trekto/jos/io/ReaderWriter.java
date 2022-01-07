package info.trekto.jos.io;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.model.SimulationObject;

import java.nio.charset.Charset;
import java.util.List;

public interface ReaderWriter {
    SimulationProperties readProperties(String inputFilePath);

    SimulationProperties readProperties(String inputFilePath, Charset charset);

    void appendObjectsToFile(List<SimulationObject> objects);

    void endFile();

    void initReaderAndWriter(String inputFilePath, SimulationProperties properties, Charset charset);

    void initReaderAndWriter(String inputFilePath, SimulationProperties properties);
}
