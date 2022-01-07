package info.trekto.jos;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.ReaderWriter;

/**
 * Contains instances of main application classes.
 *
 * @author Trayan Momkov
 * @date 5 Mar 2016
 */
public class Container {
    public static SimulationLogic simulationLogic;
    public static Simulation simulation;
    public static SimulationProperties properties;
    public static ReaderWriter readerWriter;
}
