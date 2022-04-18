package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.io.ReaderWriter;
import info.trekto.jos.gui.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 */
public enum Controller {
    C;

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private Simulation simulation;
    public static SimulationProperties prop;
    private Visualizer visualizer;
    private ReaderWriter readerWriter;
    public static MainForm mainForm;
    
    private boolean hasToStop;
    private String endText;

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public Visualizer getVisualizer() {
        return visualizer;
    }

    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }

    public ReaderWriter getReaderWriter() {
        return readerWriter;
    }

    public void setReaderWriter(ReaderWriter readerWriter) {
        this.readerWriter = readerWriter;
    }

    public boolean hasToStop() {
        return hasToStop;
    }

    public void setHasToStop(boolean hasToStop) {
        this.hasToStop = hasToStop;
    }

    public String getEndText() {
        return endText;
    }

    public void setEndText(String endText) {
        this.endText = endText;
    }
}
