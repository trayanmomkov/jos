package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.io.ReaderWriter;
import info.trekto.jos.visualization.Visualizer;

/**
 * Sorry - no time for better design :/
 * Contains instances of main application classes.
 *
 * @author Trayan Momkov
 * C from Container
 */
public class C {
    public static SimulationImpl simulation;
    public static SimulationProperties prop;
    public static ReaderWriter io;
    public static MainForm mainForm;
    public static boolean hasToStop;
    public static String endText;
    public static Visualizer visualizer;
}
