package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.io.ReaderWriter;
import info.trekto.jos.gui.Visualizer;

/**
 * Sorry - no time for better design :/
 * Contains instances of main application classes.
 *
 * @author Trayan Momkov
 * C from Container
 */
public enum Controller {
    C;
    public static SimulationLogic simulationLogic;
    public static Simulation simulation;
    public static SimulationProperties prop;
    public static ReaderWriter io;
    public static MainForm mainForm;
    public static boolean hasToStop;
    public static String endText;
    public static Visualizer visualizer;
}
