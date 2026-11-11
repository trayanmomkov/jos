package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationGenerator;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.impl.single_precision.SimulationFloat;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;
import info.trekto.jos.gui.InitialObjectsTableModelAndListener;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.gui.Visualizer;
import info.trekto.jos.gui.java2dgraphics.VisualizerImpl;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.io.ReaderWriter;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Properties;

import static info.trekto.jos.core.CollisionMode.BOUNCE;
import static info.trekto.jos.core.CollisionMode.MERGE;
import static info.trekto.jos.core.CollisionMode.SOFT_CONTACT;
import static info.trekto.jos.core.ExecutionMode.AUTO;
import static info.trekto.jos.core.ExecutionMode.CPU;
import static info.trekto.jos.core.ExecutionMode.GPU;
import static info.trekto.jos.core.GpuChecker.checkGpu;
import static info.trekto.jos.core.GpuChecker.findCpuThreshold;
import static info.trekto.jos.core.GpuChecker.gpuDoubleAvailable;
import static info.trekto.jos.core.GpuChecker.gpuFloatAvailable;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.FLOAT;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.gui.java2dgraphics.VisualizationPanel.DEFAULT_FONT_SIZE;
import static info.trekto.jos.util.Utils.calculateAverageFileSize;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.isNullOrBlank;
import static info.trekto.jos.util.Utils.isNumeric;
import static info.trekto.jos.util.Utils.warn;
import static java.awt.Color.PINK;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * @author Trayan Momkov
 */
public enum Controller {
    C;

    public static final int CPU_DEFAULT_THRESHOLD = 384;
    public static final int DEFAULT_MIN_DISTANCE = 10;
    public static final int DEFAULT_BARNES_HUT_THETA = 0;
    public static final double DEFAULT_SCALE = 1;
    public static final String JSON_FILE_EXTENSION = ".json";
    public static final String JSON_GZIP_FILE_EXTENSION = ".json.gz";
    int cpuThreshold;
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    public static final String PROGRAM_NAME = "JOS";

    private Simulation simulation;
    private MainForm gui;
    private Visualizer visualizer;
    private ReaderWriter readerWriter;
    private SimulationGenerator simulationGenerator;

    private boolean running = false;
    private boolean paused = false;
    private boolean hasToStop;
    private String endText;
    private File playFile;
    private Color defaultButtonColor;
    private boolean hasToStopCpuGpuMeasuring = false;

    /* PNG-frame export during playback. exportDir != null means an export is active for this playback. */
    private File exportDir;
    private int exportWidth;
    private int exportHeight;
    private int exportFrameCounter;

    /* The settings with which the current objects were generated or loaded.
     * The "Generate objects" button is highlighted when the settings in the GUI differ from them. */
    private boolean generationSnapshotTaken = false;
    private String snapshotNumberOfObjects;
    private NumberFactory.NumberType snapshotNumberType;
    private String snapshotPrecision;
    /* True while refreshProperties() fills the GUI, to tell its changes from the ones made by the user. */
    private boolean refreshingProperties = false;

    public static void main(String[] args) {
        if (args.length > 0 && "--headless".equals(args[0])) {
            runHeadless(args);
            return;
        }
        Properties applicationProperties = new Properties();
        JFrame jFrame = new JFrame();
        MainForm mainForm = new MainForm();

        try {
            applicationProperties.load(Controller.class.getClassLoader().getResourceAsStream("application.properties"));
            BufferedImage icon = ImageIO.read(Controller.class.getClassLoader().getResource("jos-icon.png"));
            mainForm.setIcon(icon);
            jFrame.setIconImage(icon);
        } catch (Exception e) {
            error(logger, "Cannot load properties and/or icon image.", e);
        }

        if (isNullOrBlank(applicationProperties.getProperty("version"))) {
            applicationProperties.setProperty("version", "Unknown");
        }

        mainForm.setAboutMessage("JOS - v. " + applicationProperties.getProperty("version") + "\n\nAuthor: Trayan Momkov\n2023");
        mainForm.setNumberTypeMessage("DOUBLE - Double precision. Fast. (Uses GPU if possible)\n"
                                              + "FLOAT - Single precision. Fastest. (Uses GPU if possible)\n"
                                              + "ARBITRARY_PRECISION - Arbitrary precision.");
                                              
        mainForm.setMinDistanceMessage("If two objects are closer than the minimum distance, "
                                               + "the minimum distance is used when calculating the acceleration.");
        mainForm.setBarnesHutThetaMessage("Barnes-Hut approximation of the gravity. Applies on the CPU and the GPU.\n"
                                                  + "0: exact brute force calculation.\n"
                                                  + "Bigger values: faster but less accurate. Typical value: 0.5 - 0.7.\n"
                                                  + "Theta > 0 also speeds up the collision detection using the same tree\n"
                                                  + "(the detection itself stays exact).\n"
                                                  + "On the GPU the approximation pays off for very large numbers of objects\n"
                                                  + "(tens of thousands); below that the exact GPU kernels are usually faster.\n"
                                                  + "Note: with theta > 0 the total momentum is not exactly conserved.");
        mainForm.setCpuGpuThresholdMessage("If the objects are fewer than this threshold\n"
                                                   + "the execution will continue on the CPU.");
        mainForm.init();
        C.setMainForm(mainForm);

        C.appendMessage("Controls (Not fully implemented!):");
        C.appendMessage("\tExit: Esc");
        C.appendMessage("\tZoom in: +");
        C.appendMessage("\tZoom out: -");
        C.appendMessage("\tMove up: ↑");
        C.appendMessage("\tMove down: ↓");
        C.appendMessage("\tMove right: →");
        C.appendMessage("\tMove left: ←");
        C.appendMessage("\tSwitch trails: t");

        jFrame.setContentPane(mainForm.getMainPanel());
        jFrame.setTitle(PROGRAM_NAME);
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null); // Center of the screen
        jFrame.setVisible(true);
        
        checkGpu();
        
        C.calculateAverageSize();
        C.setPrecisionFieldVisibility();
        C.setExecutionModeFieldVisibilityAndValue();
        C.setCpuGpuThresholdVisibility();
        C.setReaderWriter(new JsonReaderWriter());
        C.refreshGenerateObjectsButtonState();
    }

    /**
     * Runs a simulation without GUI and always saves it to a file:
     * java -jar jos.jar --headless input.json [--iterations N] [--save-every N] [--output FILE]
     * The default output is the input file plus ".gz". With 0 iterations it runs until it is stopped (Ctrl+C).
     */
    private static void runHeadless(String[] args) {
        System.setProperty("java.awt.headless", "true");
        String input = null, output = null;
        Integer saveEvery = null;
        Long iterations = null;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--iterations": iterations = Long.parseLong(args[++i]); break;
                case "--save-every": saveEvery = Integer.parseInt(args[++i]); break;
                case "--output":     output = args[++i]; break;
                default:
                    if (args[i].startsWith("--")) { System.err.println("Unknown option: " + args[i]); System.exit(2); }
                    else if (input == null) { input = args[i]; }   // first positional = the scene file
                    else { System.err.println("Unexpected argument: " + args[i]); System.exit(2); }
            }
        }
        if (input == null) {
            System.err.println("Usage: --headless <input.json> [--iterations N] [--save-every N] [--output FILE]");
            System.exit(2);
        }
        if (iterations != null && iterations < 0) {
            System.err.println("--iterations must be 0 (run until stopped) or more, got " + iterations);
            System.exit(2);
        }
        if (saveEvery != null && saveEvery < 1) {
            System.err.println("--save-every must be at least 1, got " + saveEvery);
            System.exit(2);
        }

        C.setReaderWriter(new JsonReaderWriter());
        try {
            checkGpu();                                    // probe GPU availability - no display needed
        } catch (Throwable t) {
            error(logger, "GPU check failed; continuing on the CPU.", t);
        }
        SimulationProperties properties = C.loadProperties(input);
        if (properties == null) {
            System.err.println("Could not load scene: " + input);
            System.exit(1);
        }

        /* Headless is always: run a simulation, no real-time visualization, always save to file. */
        properties.setRealTimeVisualization(false);
        properties.setSaveToFile(true);
        if (iterations != null) {
            properties.setNumberOfIterations(iterations);
        }
        if (saveEvery != null) {
            properties.setSaveEveryNthIteration(saveEvery);
        }
        /* Output file: --output, else the scene's own outputFile, else auto-generate (input + ".gz", like the GUI). */
        if (isNullOrBlank(output)) {
            output = properties.getOutputFile();
        }
        if (isNullOrBlank(output)) {
            output = input + ".gz";
        }
        properties.setOutputFile(output);

        final Simulation simulation = createSimulation(properties, AUTO);
        C.setSimulation(simulation);
        info(logger, "Headless run: " + input
                + " | " + (properties.isInfiniteSimulation() ? "infinite - stop with Ctrl+C"
                                                             : properties.getNumberOfIterations() + " iterations")
                + " | save every " + properties.getSaveEveryNthIteration()
                + " | " + properties.getNumberType() + (properties.isSoftContact() ? " | soft contact" : "")
                + " | -> " + output);

        /* The simulation runs in its own thread, so Ctrl+C can stop it and the output file is closed properly. */
        final boolean[] failed = {false};
        Thread simThread = new Thread(() -> {
            try {
                simulation.startSimulation();
            } catch (Exception e) {
                error(logger, "Headless simulation failed.", e);
                failed[0] = true;
            }
        }, "headless-sim");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            C.setHasToStop(true);                          // break the loop; its stop path closes the output file
            try {
                simThread.join();                          // wait for the flush before the JVM halts
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }));
        simThread.start();
        try {
            simThread.join();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        info(logger, "Headless run finished. Output: " + output);
        System.exit(failed[0] ? 1 : 0);                    // Aparapi / AWT threads can linger; exit cleanly
    }

    public int getCpuThreshold() {
        return cpuThreshold;
    }

    public void setCpuThreshold(int cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }

    Simulation createSimulation(SimulationProperties properties) {
        return createSimulation(properties, getSelectedExecutionMode());
    }

    /* This method is static for testing purposes */
    static Simulation createSimulation(SimulationProperties properties, ExecutionMode executionMode) {
        return createSimulation(properties, executionMode, false);
    }

    /* Playing a saved file needs a CpuSimulation, so the soft contact is not used for it. */
    static Simulation createSimulation(SimulationProperties properties, ExecutionMode executionMode, boolean forPlayback) {
        NumberFactory.NumberType numberType = properties.getNumberType();
        int n = properties.getNumberOfObjects();

        /* The arrays are filled with all initial objects, so their count is the real number of objects. */
        if (properties.getInitialObjects() != null && !properties.getInitialObjects().isEmpty()
                && properties.getInitialObjects().size() != n) {
            n = properties.getInitialObjects().size();
            properties.setNumberOfObjects(n);
        }

        /* The soft contact exists only in the float and double simulations. Float runs it on the GPU,
         * double on the CPU cores, because double is slow on most video cards. */
        if (!forPlayback && properties.useSoftContact()) {
            if (numberType == FLOAT) {
                return new SimulationFloat(properties, null);
            }
            return new SimulationDouble(properties, null);
        }

        if (numberType == ARBITRARY_PRECISION
                || executionMode == CPU
                || (numberType == DOUBLE && !gpuDoubleAvailable)
                || (numberType == FLOAT && !gpuFloatAvailable)
                || (executionMode == AUTO && n <= C.cpuThreshold)) {
            return new SimulationAP(properties);
        } else {
            if (numberType == DOUBLE) {
                return new SimulationDouble(properties, executionMode == GPU ? null : new SimulationAP(properties));
            } else {
                return new SimulationFloat(properties, executionMode == GPU ? null : new SimulationAP(properties));
            }
        }
    }

    public SimulationProperties loadProperties(String inputFile) {
        SimulationProperties properties = null;
        try {
            properties = readerWriter.readPropertiesAndCreateNumberFactory(inputFile);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot open properties file.", e);
        } catch (NumberFormatException e) {
            error(logger, "Not a valid number.", e);
        } catch (Exception e) {
            error(logger, "Problem while reading properties file.", e);
        }
        return properties;
    }

    public SimulationProperties loadPropertiesForPlaying(String inputFile) {
        SimulationProperties properties = null;
        try {
            properties = readerWriter.readPropertiesForPlaying(inputFile);
            properties.setRealTimeVisualization(true);
        } catch (IOException e) {
            error(logger, "Cannot read properties file.", e);
        }
        return properties;
    }

    public void append(String message) {
        if (gui != null) {
            appendMessage(Utils.DATE_FORMAT.format(new Date()) + " " + message);
        }
    }

    public void play() {
        simulation = createSimulation(fetchPropertiesFromGuiAndCreateNumberFactory(), CPU, true);
        paused = false;
        new Thread(() -> {
            try {
                if (simulation != null && playFile != null) {
                    hasToStop = false;
                    ((CpuSimulation) simulation).playSimulation(playFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                String message = "Error during playing.";
                error(logger, message, ex);
                if (visualizer != null) {
                    visualizer.closeWindow();
                }
                showError(message + " " + ex.getMessage());
            } finally {
                onVisualizationWindowClosed();
            }
        }).start();
        gui.getPlayingComponents().forEach(c -> c.setEnabled(false));
        gui.getStopButton().setEnabled(true);
        gui.getPauseButton().setEnabled(true);
    }

    public void start() {
        if (!readyToStart()) {
            return;
        }
        simulation = createSimulation(fetchPropertiesFromGuiAndCreateNumberFactory());
        paused = false;
        if (simulation.getProperties() != null && simulation.getProperties().getInitialObjects() != null) {
            new Thread(() -> {
                try {
                    if (simulation.getProperties().isRealTimeVisualization()) {
                        visualizer = new VisualizerImpl(simulation.getProperties());
                    }
                    simulation.startSimulation();
                } catch (SimulationException ex) {
                    handleException(ex, "Error during simulation.");
                } catch (ArithmeticException ex) {
                    if (ex.getMessage().contains("zero")) {
                        handleException(ex, "Operation with zero. Please increase the precision and try again.");
                    } else {
                        handleException(ex, "Arithmetic exception.");
                    }
                } catch (ClassCastException ex) {
                        handleException(ex, "It looks like number types are incompatible. Did you forget to push 'Generate objects' button?");
                } catch (Exception ex) {
                    handleException(ex, "Unexpected exception.");
                } finally {
                    onVisualizationWindowClosed();
                }
            }).start();
            gui.getStartButton().setEnabled(false);
            gui.getRunningComponents().forEach(c -> c.setEnabled(false));
            gui.getPlayingComponents().forEach(c -> c.setEnabled(false));
            gui.getSavingToFileComponents().forEach(c -> c.setEnabled(false));
            gui.getRunningRadioButton().setEnabled(false);
            gui.getPlayRadioButton().setEnabled(false);
            gui.getStopButton().setEnabled(true);
            gui.getPauseButton().setEnabled(true);
        }
    }

    /* Tells the user when there are no objects. Asks when a setting was changed after the objects were made. */
    private boolean readyToStart() {
        int objects = gui.getInitialObjectsTable().getRowCount();
        if (objects == 0) {
            showWarn("There are no objects. Push \"Generate objects\" or load a file.");
            return false;
        }
        String change = changedGenerationSetting();
        if (change == null) {
            return true;
        }
        String question = change + "\n"
                + "The table \"Initial objects\" still has the " + objects + " old objects,\n"
                + "because \"Generate objects\" was not pushed after the change.\n\n"
                + "Start the simulation with the " + objects + " old objects?";
        return JOptionPane.showConfirmDialog(gui.getMainPanel(), question, "Start", YES_NO_OPTION) == YES_OPTION;
    }

    private void handleException(Exception ex, String message) {
        error(logger, message, ex);
        /* There is no visualizer when the simulation runs without real time visualization. */
        if (visualizer != null) {
            visualizer.closeWindow();
        }
        showError(message + "\n" + ex.getMessage());
    }

    private SimulationProperties fetchPropertiesFromGuiAndCreateNumberFactory() {
        if (isNullOrBlank(gui.getCpuGpuThresholdField().getText()) || !isNumeric(gui.getCpuGpuThresholdField().getText())) {
            cpuThreshold = CPU_DEFAULT_THRESHOLD;
            gui.getCpuGpuThresholdField().setText(String.valueOf(cpuThreshold));
        } else {
            cpuThreshold = Integer.parseInt(gui.getCpuGpuThresholdField().getText());
        }

        SimulationProperties properties = new SimulationProperties();

        if (!isNullOrBlank(gui.getPrecisionTextField().getText())) {
            properties.setPrecision(Integer.parseInt(gui.getPrecisionTextField().getText()));
        }

        properties.setNumberType(getSelectedNumberType());

        createNumberFactory(properties.getNumberType(), properties.getPrecision());

        fetchPropertiesNotRelatedToNumberFactory(properties);
        return properties;
    }

    private void fetchPropertiesNotRelatedToNumberFactory(SimulationProperties properties) {
        if (!isNullOrBlank(gui.getNumberOfObjectsTextField().getText())) {
            properties.setNumberOfObjects(Integer.parseInt(gui.getNumberOfObjectsTextField().getText()));
        }

        properties.setInitialObjects(((InitialObjectsTableModelAndListener) gui.getInitialObjectsTable().getModel()).getInitialObjects());

        if (!isNullOrBlank(gui.getSecondsPerIterationTextField().getText())) {
            properties.setSecondsPerIteration(New.num(gui.getSecondsPerIterationTextField().getText()));
        }

        properties.setSaveToFile(gui.getSaveToFileCheckBox().isSelected());
        if (!isNullOrBlank(gui.getOutputFileTextField().getText())) {
            properties.setOutputFile(gui.getOutputFileTextField().getText());
        }

        if (!isNullOrBlank(gui.getNumberOfIterationsTextField().getText())) {
            properties.setNumberOfIterations(Integer.parseInt(gui.getNumberOfIterationsTextField().getText()));
        }
        
        properties.setSaveMass(gui.getSaveMassCheckBox().isSelected());
        properties.setSaveVelocity(gui.getSaveVelocityCheckBox().isSelected());
        properties.setSaveAcceleration(gui.getSaveAccelerationCheckBox().isSelected());

        if (!isNullOrBlank(gui.getPlayingSpeedTextField().getText().replace("-", ""))) {
            properties.setPlayingSpeed(Integer.parseInt(gui.getPlayingSpeedTextField().getText()));
        }

        if (!isNullOrBlank(gui.getSaveEveryNthIterationTextField().getText())) {
            properties.setSaveEveryNthIteration(Integer.parseInt(gui.getSaveEveryNthIterationTextField().getText()));
        }
        
        if (!isNullOrBlank(gui.getCorTextField().getText())) {
            Number cor = New.num(gui.getCorTextField().getText());
            if (cor.compareTo(ZERO) < 0) {
                cor = ZERO;
            }
            properties.setCoefficientOfRestitution(cor);
        }

        String minDistanceText = gui.getMinDistanceField().getText();
        if (isNullOrBlank(minDistanceText) || !isNumeric(minDistanceText) || New.num(minDistanceText).compareTo(ZERO) < 0) {
            properties.setMinDistance(New.num(DEFAULT_MIN_DISTANCE));
            gui.getMinDistanceField().setText(String.valueOf(DEFAULT_MIN_DISTANCE));
        } else {
            properties.setMinDistance(New.num(minDistanceText));
        }

        String barnesHutThetaText = gui.getBarnesHutThetaField().getText();
        if (isNullOrBlank(barnesHutThetaText) || !isNumeric(barnesHutThetaText) || New.num(barnesHutThetaText).compareTo(ZERO) < 0) {
            properties.setBarnesHutTheta(New.num(DEFAULT_BARNES_HUT_THETA));
            gui.getBarnesHutThetaField().setText(String.valueOf(DEFAULT_BARNES_HUT_THETA));
        } else {
            properties.setBarnesHutTheta(New.num(barnesHutThetaText));
        }

        String scaleText = gui.getScaleField().getText();
        if (isNullOrBlank(scaleText) || !isNumeric(scaleText) || Double.parseDouble(scaleText) <= 0) {
            properties.setScale(DEFAULT_SCALE);
            gui.getScaleField().setText(String.valueOf(DEFAULT_SCALE));
        } else {
            properties.setScale(Double.parseDouble(scaleText));
        }
        
        properties.setBackgroundColor(gui.getBackgroundColor());

        properties.setBounceFromScreenBorders(gui.getBounceFromScreenBordersCheckBox().isSelected());
        properties.setRealTimeVisualization(gui.getRealTimeVisualizationCheckBox().isSelected());
        properties.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(String.valueOf(gui.getInteractingLawComboBox().getSelectedItem())));
        selectedCollisionMode().applyTo(properties);
    }

    public void onVisualizationWindowClosed() {
        C.setPaused(false);
        if (gui.getRunningRadioButton().isSelected()) {
            gui.getStartButton().setEnabled(true);
        }
        if (gui.getRunningRadioButton().isSelected()) {
            gui.getRunningComponents().forEach(c -> c.setEnabled(true));
            gui.getSavingToFileComponents().forEach(c -> c.setEnabled(gui.getSaveToFileCheckBox().isSelected()));
            setPrecisionFieldVisibility();
            setExecutionModeFieldVisibilityAndValue();
            setCpuGpuThresholdVisibility();
            setCollisionFieldsEnabled();
            setMinDistanceVisibility();
        } else {
            gui.getPlayingComponents().forEach(c -> c.setEnabled(true));
        }
        gui.getRunningRadioButton().setEnabled(true);
        gui.getPlayRadioButton().setEnabled(true);
        gui.getStopButton().setEnabled(false);
        gui.getPauseButton().setEnabled(false);
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void setMainForm(MainForm mainForm) {
        this.gui = mainForm;
    }

    public void switchPause() {
        paused = !paused;
        setPause(paused);
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

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setHasToStop(boolean hasToStop) {
        this.hasToStop = hasToStop;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void switchTrail() {
        setShowTrail(!isShowTrail());
        if (!isShowTrail()) {
            visualizer.getTrails().clear();
        }
    }

    public void setPause(boolean paused) {
        gui.getPauseButton().setText(paused ? "Unpause" : "Pause");
    }

    public void enableRunning(boolean enable) {
        gui.getRunningComponents().forEach(c -> c.setEnabled(enable));
        gui.getPlayingComponents().forEach(c -> c.setEnabled(!enable));
        gui.getSavingToFileComponents().forEach(c -> c.setEnabled(enable && gui.getSaveToFileCheckBox().isSelected()));
        setPrecisionFieldVisibility();
        setExecutionModeFieldVisibilityAndValue();
        setCpuGpuThresholdVisibility();
        setCollisionFieldsEnabled();
        setMinDistanceVisibility();
        refreshGenerateObjectsButtonState();
    }

    public void refreshProperties(SimulationProperties prop) {
        refreshingProperties = true;
        try {
            gui.getNumberOfObjectsTextField().setText(String.valueOf(prop.getNumberOfObjects()));
            gui.getNumberOfIterationsTextField().setText(String.valueOf(prop.getNumberOfIterations()));
            gui.getSecondsPerIterationTextField().setText(String.valueOf(prop.getSecondsPerIteration()));
            gui.getNumberTypeComboBox().setSelectedItem(prop.getNumberType());
            gui.getInteractingLawComboBox().setSelectedItem(prop.getInteractingLaw());
            gui.getSaveToFileCheckBox().setSelected(prop.isSaveToFile());
            gui.getSaveEveryNthIterationTextField().setText(String.valueOf(prop.getSaveEveryNthIteration()));
            gui.getSaveMassCheckBox().setSelected(prop.isSaveMass());
            gui.getSaveVelocityCheckBox().setSelected(prop.isSaveVelocity());
            gui.getSaveAccelerationCheckBox().setSelected(prop.isSaveAcceleration());
            gui.getOutputFileTextField().setText(prop.getOutputFile());
            gui.getPrecisionTextField().setText(String.valueOf(prop.getPrecision()));
            gui.getRealTimeVisualizationCheckBox().setSelected(prop.isRealTimeVisualization());
            gui.getBounceFromScreenBordersCheckBox().setSelected(prop.isBounceFromScreenBorders());
            gui.getPlayingSpeedTextField().setText(String.valueOf(prop.getPlayingSpeed()));
            gui.getCollisionComboBox().setSelectedItem(CollisionMode.of(prop));
            gui.getCorTextField().setText(String.valueOf(prop.getCoefficientOfRestitution()));
            gui.getMinDistanceField().setText(String.valueOf(prop.getMinDistance()));
            gui.getBarnesHutThetaField().setText(String.valueOf(prop.getBarnesHutTheta()));
            gui.getScaleField().setText(String.valueOf(prop.getScale()));
            gui.setBackgroundColor(prop.getBackgroundColor());

            ((InitialObjectsTableModelAndListener) gui.getInitialObjectsTable().getModel()).setInitialObjects(prop.getInitialObjects());

            calculateAverageSize();
            setPrecisionFieldVisibility();
            setExecutionModeFieldVisibilityAndValue();
            setCpuGpuThresholdVisibility();
            setCollisionFieldsEnabled();
        } finally {
            refreshingProperties = false;
        }
    }

    private void showError(Component parent, String message, Exception exception) {
        showError(parent, message + " " + exception.getMessage());
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", ERROR_MESSAGE);
    }

    private void showError(String message, Exception exception) {
        showError(gui.getMainPanel(), message, exception);
    }

    private void showError(String message) {
        showError(gui.getMainPanel(), message);
    }

    public void showHtmlError(String message, Exception exception) {
        showHtmlError(message + "<p>" + exception.getMessage() + "</p>");
    }

    public void showHtmlError(String message) {
        JEditorPane ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setText(message);

        ep.setEditable(false);  //so it s not editable
        ep.setOpaque(false);    //so we don't see white background

        ep.addHyperlinkListener(event -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception ignored) {
                }
            }
        });

        JOptionPane.showMessageDialog(gui.getMainPanel(), ep, "Error", ERROR_MESSAGE);
    }

    private void showWarn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", WARNING_MESSAGE);
    }

    private void showWarn(String message) {
        showWarn(gui.getMainPanel(), message);
    }

    public void appendMessage(String message) {
        gui.getConsoleTextArea().append(message + "\n");
        if (gui.getAutoscrollCheckBox().isSelected()) {
            gui.scroll();
        }
    }

    public int getFontSize() {
        if (!isNullOrBlank(gui.getFontSize().getText().replace("-", ""))) {
            return Integer.parseInt(gui.getFontSize().getText());
        } else {
            return DEFAULT_FONT_SIZE;
        }
    }

    public boolean isShowIds() {
        return gui.getShowObjectIDsCheckBox().isSelected();
    }

    /** Whether the visualization should antialias (smooth) the drawn objects. Read by the renderer each
     * paint, so toggling the checkbox takes effect on the next frame. Off by default for speed. */
    public boolean isAntialiasing() {
        return gui != null && gui.getAntialiasingCheckBox().isSelected();
    }

    public boolean isExportFrames() {
        return gui != null && gui.getExportFramesCheckBox().isSelected();
    }

    /* Creates a new folder for the PNG frames next to the played file, named after the file and the resolution. */
    public void prepareExport() {
        exportDir = null;
        if (!isExportFrames() || playFile == null) {
            return;
        }
        String res = String.valueOf(gui.getExportResolutionComboBox().getSelectedItem());
        if (res.matches("\\d+x\\d+")) {                  // "WxH" - the current monitor's native resolution
            exportWidth = Integer.parseInt(res.substring(0, res.indexOf('x')));
            exportHeight = Integer.parseInt(res.substring(res.indexOf('x') + 1));
        } else if (res.startsWith("1440")) {
            exportWidth = 2560;
            exportHeight = 1440;
        } else if (res.startsWith("4")) {
            exportWidth = 3840;
            exportHeight = 2160;
        } else {
            exportWidth = 1920;
            exportHeight = 1080;
        }
        String resTag = res.replaceAll("[^A-Za-z0-9]+", "");   // folder-friendly (e.g. "Screen (1920x1080)" -> "Screen1920x1080")
        String base = playFile.getName().replaceFirst("\\.gz$", "").replaceFirst("\\.json$", "") + "-" + resTag;
        File parent = playFile.getParentFile();
        File dir = new File(parent, base);
        for (int suffix = 2; dir.exists(); suffix++) {
            dir = new File(parent, base + "-" + suffix);
        }
        dir.mkdirs();
        exportDir = dir;
        exportFrameCounter = 1;   // frame_000001.png upward, so ffmpeg's default start number works
        info(logger, "Exporting PNG frames " + exportWidth + "x" + exportHeight + " to: " + exportDir.getAbsolutePath()
                + " (assemble with: ffmpeg -framerate 60 -i frame_%06d.png -c:v libx264 -crf 18 -pix_fmt yuv420p out.mp4)");
    }

    /** Writes the currently visualized frame to a numbered PNG at the export resolution, if export is on. */
    public void exportFrameIfEnabled() {
        if (exportDir == null) {
            return;
        }
        try {
            BufferedImage image = getVisualizer().getVisualizationPanel().renderToImage(exportWidth, exportHeight);
            ImageIO.write(image, "png", new File(exportDir, String.format("frame_%06d.png", exportFrameCounter++)));
        } catch (Exception e) {
            error(logger, "Could not export frame " + exportFrameCounter, e);
        }
    }

    public boolean isShowTrail() {
        return gui.getShowTrailCheckBox().isSelected();
    }

    public void setShowTrail(boolean selected) {
        gui.getShowTrailCheckBox().setSelected(selected);
        showTrailCheckBoxEvent();
    }

    public int getTrailSize() {
        if (!isNullOrBlank(gui.getTrailSizeTextField().getText().replace("-", ""))) {
            return Integer.parseInt(gui.getTrailSizeTextField().getText());
        } else {
            return 500;
        }
    }

    public boolean getShowTimeAndIteration() {
        return gui.getShowTimeAndIterationCheckBox().isSelected();
    }

    public void browsePlayingFileButtonEvent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("GZipped JSON file", "gz"));
        int option = fileChooser.showOpenDialog(gui.getMainPanel());
        if (option == JFileChooser.APPROVE_OPTION) {
            playFile = fileChooser.getSelectedFile();
            gui.getPlayFileLabel().setText(playFile.getAbsolutePath());
            refreshProperties(loadPropertiesForPlaying(playFile.getAbsolutePath()));
            snapshotGenerationParams();
        }
    }

    public void generateObjectButtonEvent() {
        SimulationProperties properties = fetchPropertiesFromGuiAndCreateNumberFactory();

        new Thread(() -> {
            try {
                SimulationGenerator.generateObjects(properties, true);
                refreshProperties(properties);
                snapshotGenerationParams();
                refreshGenerateObjectsButtonState();
            } catch (Exception ex) {
                String message = "Error during object generation.";
                error(logger, message, ex);
                showError(message + " " + ex.getMessage());
            }
        }).start();
    }

    public void saveToFileCheckBoxEvent() {
        gui.getSavingToFileComponents().forEach(c -> c.setEnabled(gui.getSaveToFileCheckBox().isSelected()));
        /* When saving is switched on and no output file has been chosen yet, default it to the loaded
         * input file path with .gz appended (e.g. /path/scene.json -> /path/scene.json.gz). */
        if (gui.getSaveToFileCheckBox().isSelected() && isNullOrBlank(gui.getOutputFileTextField().getText())) {
            String inputFilePath = gui.getInputFilePathLabel().getText();
            if (!isNullOrBlank(inputFilePath)) {
                gui.getOutputFileTextField().setText(inputFilePath + ".gz");
            }
        }
    }

    public void pauseButtonEvent() {
        if (simulation != null) {
            switchPause();
        }
    }

    public void stopButtonEvent() {
        if (C.getSimulation() != null) {
            C.setPaused(false);
            if (C.isRunning()) {
                C.setHasToStop(true);
            }
        }
    }

    public void outputFileTextFieldEvent() {
    }

    public void playingSpeedTextFieldEvent() {
    }

    private void highlightButton(JButton button) {
        if (defaultButtonColor == null) {
            defaultButtonColor = gui.getGenerateObjectsButton().getBackground();
        }
        if (gui.getRunningRadioButton().isSelected()) {
            button.setBackground(PINK);
        }
    }

    private void unHighlightButtons(JButton... buttons) {
        for (JButton button : buttons) {
            button.setBackground(defaultButtonColor != null ? defaultButtonColor : new Color(238, 238, 238));
        }
    }

    /* Highlights the "Generate objects" button when the objects have to be generated (again). */
    private void refreshGenerateObjectsButtonState() {
        if (defaultButtonColor == null) {
            defaultButtonColor = gui.getGenerateObjectsButton().getBackground();
        }
        boolean dirty = gui.getRunningRadioButton().isSelected() && isGenerationDirty();
        gui.getGenerateObjectsButton().setBackground(
                dirty ? PINK : (defaultButtonColor != null ? defaultButtonColor : new Color(238, 238, 238)));
    }

    private boolean isGenerationDirty() {
        /* Without a snapshot nothing is generated or loaded yet - the user must generate objects first. */
        return !generationSnapshotTaken || changedGenerationSetting() != null;
    }

    /* A sentence about the setting which was changed after the objects were generated or loaded, or null. */
    private String changedGenerationSetting() {
        if (!generationSnapshotTaken) {
            return null;
        }
        NumberFactory.NumberType currentNumberType = getSelectedNumberType();
        if (currentNumberType != snapshotNumberType) {
            return "\"Number type\" was changed from " + snapshotNumberType + " to " + currentNumberType + ".";
        }
        String numberOfObjects = gui.getNumberOfObjectsTextField().getText().trim();
        if (!numberOfObjects.equals(snapshotNumberOfObjects)) {
            return "\"Number of objects\" was changed from " + snapshotNumberOfObjects + " to " + numberOfObjects + ".";
        }
        /* Precision influences generation only for arbitrary precision numbers. */
        String precision = gui.getPrecisionTextField().getText().trim();
        if (currentNumberType == ARBITRARY_PRECISION && !precision.equals(snapshotPrecision)) {
            return "\"Precision\" was changed from " + snapshotPrecision + " to " + precision + ".";
        }
        return null;
    }

    private void snapshotGenerationParams() {
        generationSnapshotTaken = true;
        snapshotNumberOfObjects = gui.getNumberOfObjectsTextField().getText().trim();
        snapshotNumberType = getSelectedNumberType();
        snapshotPrecision = gui.getPrecisionTextField().getText().trim();
    }

    public void precisionTextFieldEvent() {
        refreshGenerateObjectsButtonState();
        if (!isNullOrBlank(gui.getPrecisionTextField().getText())) {
            calculateAverageSize();
        }
    }

    public void numberTypeComboBoxEvent(ActionEvent actionEvent) {
        if (!refreshingProperties) {
            setPrecisionFieldVisibility();
            setExecutionModeFieldVisibilityAndValue();
            setCpuGpuThresholdVisibility();
            refreshGenerateObjectsButtonState();
            calculateAverageSize();
        }
    }

    public void savePropertiesButtonEvent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(gui.getInputFilePathLabel().getText()));
        int userSelection = fileChooser.showSaveDialog(gui.getMainPanel());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filename = fileToSave.getAbsolutePath();
            if (fileToSave.exists()) {
                String message = "File already exists: " + filename;
                warn(logger, message);
                showWarn(message);
                return;
            }
            SimulationProperties properties = fetchPropertiesFromGuiAndCreateNumberFactory();
            if (!filename.endsWith(JSON_FILE_EXTENSION)) {
                filename += JSON_FILE_EXTENSION;
            }
            readerWriter.writeProperties(properties, filename);

            /* Reopen just saved file.
             * This ensures the simulation is the same as opened from the file,
             * and can help us to detect a bug (when after saving the properties suddenly change). */
            gui.getInputFilePathLabel().setText(filename);
            properties = loadProperties(filename);
            refreshProperties(properties);
            unHighlightButtons(gui.getDetectCpuGpuThresholdButton());
            snapshotGenerationParams();
            refreshGenerateObjectsButtonState();
        }
    }

    public void browseButtonEvent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON file", "json"));
        int option = fileChooser.showOpenDialog(gui.getMainPanel());
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            gui.getInputFilePathLabel().setText(file.getAbsolutePath());
            String numberOfObjectsBefore = gui.getNumberOfObjectsTextField().getText();
            refreshProperties(loadProperties(file.getAbsolutePath()));
            snapshotGenerationParams();
            refreshGenerateObjectsButtonState();
            if (gui.getNumberOfObjectsTextField().getText().equals(numberOfObjectsBefore)
                    || getSelectedExecutionMode() != AUTO) {
                unHighlightButtons(gui.getDetectCpuGpuThresholdButton());
            }
            saveToFileCheckBoxEvent();
        }
    }

    public void secondsPerIterationTextFieldEvent() {
    }

    public void numberOfIterationsTextFieldEvent() {
        calculateAverageSize();
    }

    public void numberOfObjectsTextFieldEvent() {
        refreshGenerateObjectsButtonState();
        if (!isNullOrBlank(gui.getNumberOfObjectsTextField().getText())) {
            if (getSelectedExecutionMode() == AUTO) {
                highlightButton(gui.getDetectCpuGpuThresholdButton());
            }
            calculateAverageSize();
        }
    }

    public void showTrailCheckBoxEvent() {
        gui.getTrailSizeTextField().setEnabled(gui.getShowTrailCheckBox().isSelected());
        gui.getTrailSizeTextLabel().setEnabled(gui.getShowTrailCheckBox().isSelected());
    }

    public void showObjectIDsCheckBoxEvent() {
        gui.getFontSize().setEnabled(gui.getShowObjectIDsCheckBox().isSelected());
        gui.getFontSizeLabel().setEnabled(gui.getShowObjectIDsCheckBox().isSelected());
    }

    public void bounceFromScreenBordersCheckBoxEvent() {
    }

    public void realTimeVisualizationCheckBoxEvent() {
    }

    public void interactingLawComboBoxEvent() {
    }

    public String getEndText() {
        return endText;
    }

    public void setEndText(String endText) {
        this.endText = endText;
    }

    public Image getIcon() {
        return gui.getIcon();
    }

    public Visualizer createVisualizer(SimulationProperties properties) {
        return new VisualizerImpl(properties);
    }

    public SimulationObject createNewSimulationObject(SimulationObject o) {
        return new SimulationObjectImpl(o);
    }

    public SimulationObject createNewSimulationObject() {
        return new SimulationObjectImpl();
    }

    public void saveEveryNthIterationTextFieldEvent() {
        if (!isNullOrBlank(gui.getSaveEveryNthIterationTextField().getText())) {
            calculateAverageSize();
        }
    }

    private void calculateAverageSize() {
        gui.getAvgFileSize().setText(calculateAverageFileSize(
                gui.getNumberOfObjectsTextField().getText(),
                gui.getNumberOfIterationsTextField().getText(),
                String.valueOf(gui.getNumberTypeComboBox().getSelectedItem()),
                gui.getSaveEveryNthIterationTextField().getText(),
                gui.getPrecisionTextField().getText(),
                gui.getSaveMassCheckBox().isSelected(),
                gui.getSaveVelocityCheckBox().isSelected(),
                gui.getSaveAccelerationCheckBox().isSelected()));
    }
    
    private NumberFactory.NumberType getSelectedNumberType() {
        return NumberFactory.NumberType.valueOf(String.valueOf(gui.getNumberTypeComboBox().getSelectedItem()));
    }

    private void setPrecisionFieldVisibility() {
        if (getSelectedNumberType() == ARBITRARY_PRECISION) {
            gui.getPrecisionLabel().setEnabled(gui.getRunningRadioButton().isSelected());
            gui.getPrecisionTextField().setEnabled(gui.getRunningRadioButton().isSelected());
        } else {
            gui.getPrecisionLabel().setEnabled(false);
            gui.getPrecisionTextField().setEnabled(false);
        }
    }

    private void setCpuGpuThresholdVisibility() {
        if (getSelectedExecutionMode() == AUTO) {
            gui.getCpuGpuThresholdLabel().setEnabled(gui.getRunningRadioButton().isSelected());
            gui.getCpuGpuThresholdField().setEnabled(gui.getRunningRadioButton().isSelected());
            gui.getCpuGpuThresholdLabel2().setEnabled(gui.getRunningRadioButton().isSelected());
            gui.getDetectCpuGpuThresholdButton().setEnabled(gui.getRunningRadioButton().isSelected());
            if (isNullOrBlank(gui.getCpuGpuThresholdField().getText())) {
                gui.getCpuGpuThresholdField().setText(String.valueOf(CPU_DEFAULT_THRESHOLD));
            }
        } else {
            gui.getCpuGpuThresholdLabel().setEnabled(false);
            gui.getCpuGpuThresholdField().setEnabled(false);
            gui.getCpuGpuThresholdLabel2().setEnabled(false);
            gui.getDetectCpuGpuThresholdButton().setEnabled(false);
            unHighlightButtons(gui.getDetectCpuGpuThresholdButton());
        }
    }

    private void setExecutionModeFieldVisibilityAndValue() {
        final int CPU_ITEM_INDEX = 2;
        if (gpuFloatAvailable && gpuDoubleAvailable) {
            gui.getExecutionModeComboBox().setToolTipText(null);
        } else if (gpuFloatAvailable) {
            gui.getExecutionModeComboBox().setToolTipText("Double precision on GPU is not available. Try to restart the application/computer.");
        } else if (gpuDoubleAvailable) {
            gui.getExecutionModeComboBox().setToolTipText("Float precision on GPU is not available. Try to restart the application/computer.");
        } else {
            gui.getExecutionModeComboBox().setToolTipText("GPU is not compatible/available. Try to restart the application/computer.");
        }

        if (getSelectedNumberType() == ARBITRARY_PRECISION
                || (!gpuFloatAvailable && !gpuDoubleAvailable)
                || (getSelectedNumberType() == DOUBLE && !gpuDoubleAvailable)
                || (getSelectedNumberType() == FLOAT && !gpuFloatAvailable)) {
            gui.getExecutionModeLabel().setEnabled(false);
            gui.getExecutionModeComboBox().setEnabled(false);
            gui.getExecutionModeComboBox().setSelectedIndex(CPU_ITEM_INDEX);
        } else {
            gui.getExecutionModeLabel().setEnabled(gui.getRunningRadioButton().isSelected());
            gui.getExecutionModeComboBox().setEnabled(gui.getRunningRadioButton().isSelected());
        }
    }
    
    public ExecutionMode getSelectedExecutionMode() {
        if (gui == null) {                    // headless: no combo box - use a GPU if one is available, else CPU
            return (gpuFloatAvailable || gpuDoubleAvailable) ? AUTO : CPU;
        }
        return ((AbstractMap.SimpleEntry<ExecutionMode, String>) gui.getExecutionModeComboBox().getSelectedItem()).getKey();
    }

    public void detectCpuGpuThresholdButtonEvent() {
        SimulationProperties properties = fetchPropertiesFromGuiAndCreateNumberFactory();
        gui.getStartButton().setEnabled(false);
        gui.getRunningComponents().forEach(c -> c.setEnabled(false));
        gui.getPlayingComponents().forEach(c -> c.setEnabled(false));
        gui.getSavingToFileComponents().forEach(c -> c.setEnabled(false));
        gui.getRunningRadioButton().setEnabled(false);
        gui.getPlayRadioButton().setEnabled(false);
        
        String message = "Trying to detect CPU/GPU threshold.\nPlease wait...";
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> hasToStopCpuGpuMeasuring = true);
        JOptionPane messagePane = new JOptionPane(message, INFORMATION_MESSAGE, YES_OPTION, null, new Object[]{stopButton});
        final JDialog dialog = messagePane.createDialog(gui.getMainPanel(), "Measuring");

        new Thread(() -> {
            try {
                hasToStopCpuGpuMeasuring = false;
                int threshold = findCpuThreshold(properties);
                gui.getCpuGpuThresholdField().setText(String.valueOf(threshold));
                info(logger, "CPU/GPU threshold: " + threshold);
            } catch (Exception e) {
                warn(logger, "Cannot find CPU/GPU threshold. Will use default value: " + CPU_DEFAULT_THRESHOLD, e);
            } finally {
                dialog.dispose();
                onVisualizationWindowClosed();
            }
        }).start();
        
        dialog.setVisible(true);
        unHighlightButtons(gui.getDetectCpuGpuThresholdButton());
    }

    public void executionModeComboBoxEvent() {
        setCpuGpuThresholdVisibility();
    }

    public boolean isHasToStopCpuGpuMeasuring() {
        return hasToStopCpuGpuMeasuring;
    }

    public void cpuGpuThresholdFieldEvent() {
        unHighlightButtons(gui.getDetectCpuGpuThresholdButton());
    }

    public void saveMassCheckBoxEvent() {
        calculateAverageSize();
    }

    public void saveVelocityCheckBoxEvent() {
        calculateAverageSize();
    }

    public void saveAccelerationCheckBoxEvent() {
        calculateAverageSize();
    }

    public void collisionComboBoxEvent() {
        setCollisionFieldsEnabled();
        setMinDistanceVisibility();
    }

    private CollisionMode selectedCollisionMode() {
        return (CollisionMode) gui.getCollisionComboBox().getSelectedItem();
    }

    /* The coefficient of restitution is used only when the objects bounce. */
    private void setCollisionFieldsEnabled() {
        CollisionMode mode = selectedCollisionMode();
        boolean bounce = gui.getRunningRadioButton().isSelected() && (mode == BOUNCE || mode == SOFT_CONTACT);
        gui.getCorLabel().setEnabled(bounce);
        gui.getCorTextField().setEnabled(bounce);
    }

    private void setMinDistanceVisibility() {
        boolean enabled = gui.getRunningRadioButton().isSelected() && selectedCollisionMode() != MERGE;
        gui.getMinDistanceLabel().setEnabled(enabled);
        gui.getMinDistanceField().setEnabled(enabled);
    }
    
    public void updateScaleLabel(double scale) {
        gui.getScaleField().setText(String.valueOf(scale));
        gui.getScaleField().setCaretPosition(0); 
    }
}
