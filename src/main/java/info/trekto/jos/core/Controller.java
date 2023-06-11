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
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * @author Trayan Momkov
 */
public enum Controller {
    C;

    public static final int CPU_DEFAULT_THRESHOLD = 384;
    public static final int DEFAULT_MIN_DISTANCE = 10;
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

    public static void main(String[] args) {
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
                                              
        mainForm.setMinDistanceMessage("If distance < minDistance, minDistane will be used when calculating acceleration.");
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
        NumberFactory.NumberType numberType = properties.getNumberType();
        int n = properties.getNumberOfObjects();

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
        simulation = createSimulation(fetchPropertiesFromGuiAndCreateNumberFactory(), CPU);
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

    private void handleException(Exception ex, String message) {
        error(logger, message, ex);
        visualizer.closeWindow();
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
        properties.setMergeOnCollision(gui.getMergeObjectsWhenCollideCheckBox().isSelected());
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
            setCorVisibility();
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
    }

    public void refreshProperties(SimulationProperties prop) {
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
        gui.getMergeObjectsWhenCollideCheckBox().setSelected(prop.isMergeOnCollision());
        gui.getCorTextField().setText(String.valueOf(prop.getCoefficientOfRestitution()));
        gui.getMinDistanceField().setText(String.valueOf(prop.getMinDistance()));
        gui.getScaleField().setText(String.valueOf(prop.getScale()));
        gui.setBackgroundColor(prop.getBackgroundColor());

        ((InitialObjectsTableModelAndListener) gui.getInitialObjectsTable().getModel()).setInitialObjects(prop.getInitialObjects());

        calculateAverageSize();
        setPrecisionFieldVisibility();
        setExecutionModeFieldVisibilityAndValue();
        setCpuGpuThresholdVisibility();
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
        gui.getConsoleTextArea().setCaretPosition(gui.getConsoleTextArea().getDocument().getLength());
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
        }
    }

    public void generateObjectButtonEvent() {
        SimulationProperties properties = fetchPropertiesFromGuiAndCreateNumberFactory();

        new Thread(() -> {
            try {
                SimulationGenerator.generateObjects(properties, true);
                refreshProperties(properties);
                unHighlightButtons(gui.getGenerateObjectsButton());
            } catch (Exception ex) {
                String message = "Error during object generation.";
                error(logger, message, ex);
                showError(message + " " + ex.getMessage());
            }
        }).start();
    }

    public void saveToFileCheckBoxEvent() {
        gui.getSavingToFileComponents().forEach(c -> c.setEnabled(gui.getSaveToFileCheckBox().isSelected()));
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

    public void precisionTextFieldEvent() {
        if (!isNullOrBlank(gui.getPrecisionTextField().getText())) {
            highlightButton(gui.getGenerateObjectsButton());
            calculateAverageSize();
        }
    }

    public void numberTypeComboBoxEvent(ActionEvent actionEvent) {
        if (actionEvent.getModifiers() != 0) {
            setPrecisionFieldVisibility();
            setExecutionModeFieldVisibilityAndValue();
            setCpuGpuThresholdVisibility();
            highlightButton(gui.getGenerateObjectsButton());
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
            unHighlightButtons(gui.getGenerateObjectsButton(), gui.getDetectCpuGpuThresholdButton());
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
            unHighlightButtons(gui.getGenerateObjectsButton());
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
        if (!isNullOrBlank(gui.getNumberOfObjectsTextField().getText())) {
            highlightButton(gui.getGenerateObjectsButton());
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

    public void mergeObjectsWhenCollideCheckBoxEvent() {
        setCorVisibility();
        setMinDistanceVisibility();
    }

    private void setCorVisibility() {
        gui.getCorLabel().setEnabled(!gui.getMergeObjectsWhenCollideCheckBox().isSelected());
        gui.getCorTextField().setEnabled(!gui.getMergeObjectsWhenCollideCheckBox().isSelected());
    }

    private void setMinDistanceVisibility() {
        gui.getMinDistanceLabel().setEnabled(!gui.getMergeObjectsWhenCollideCheckBox().isSelected());
        gui.getMinDistanceField().setEnabled(!gui.getMergeObjectsWhenCollideCheckBox().isSelected());
    }
    
    public void updateScaleLabel(double scale) {
        gui.getScaleField().setText(String.valueOf(scale));
        gui.getScaleField().setCaretPosition(0); 
    }
}
