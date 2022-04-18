package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.gui.MainForm;

public interface SimulationGenerator {
    void generateObjects(SimulationProperties prop, MainForm mainForm);
}
