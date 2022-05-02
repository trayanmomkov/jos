package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationGenerator;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.numbers.New;
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
import java.util.Date;
import java.util.Properties;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.isNullOrBlank;
import static java.awt.Color.PINK;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * @author Trayan Momkov
 */
public enum Controller {
    C;

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    public static final String PROGRAM_NAME = "JOS - arbitrary precision version";

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
            logger.error("Cannot load properties and/or icon image.", e);
        }

        if (isNullOrBlank(applicationProperties.getProperty("version"))) {
            applicationProperties.setProperty("version", "Unknown");
        }

        mainForm.setAboutMessage("JOS\n\nv. " + applicationProperties.getProperty("version") + "\narbitrary precision\n\nAuthor: Trayan Momkov\n2022");
        mainForm.setNumberTypeMessage("DOUBLE - Double precision. Fast. (Uses GPU if possible)\n"
                                              + "FLOAT - Single precision. Fastest. (Uses GPU if possible)\n"
                                              + "APFLOAT - Arbitrary precision. Fast.\n"
                                              + "BIG_DECIMAL - Arbitrary precision. Slow. Pi up to 10k digits.");
        mainForm.init();
        C.setMainForm(mainForm);

        C.appendMessage("Controls:");
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
    }

    private Simulation createSimulation(SimulationProperties properties) {
        Simulation simulation;
        switch (properties.getNumberType()) {
            case DOUBLE:
                simulation = new SimulationDouble(properties);
                break;
            case FLOAT:
            default:
                simulation = new SimulationAP(properties);
        }

        if (properties.isSaveToFile()) {
            readerWriter = new JsonReaderWriter();
        }
        return simulation;
    }

    public SimulationProperties loadProperties(String inputFile) {
        readerWriter = new JsonReaderWriter();
        SimulationProperties properties = null;
        try {
            properties = readerWriter.readPropertiesAndCreateNumberFactory(inputFile);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot read properties file.", e);
        } catch (NumberFormatException e) {
            error(logger, "Not a valid number.", e);
        }
        return properties;
    }

    public SimulationProperties loadPropertiesForPlaying(String inputFile) {
        readerWriter = new JsonReaderWriter();
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
            appendMessage(Utils.df.format(new Date()) + " " + message);
        }
    }

    public void play() {
        simulation = createSimulation(fetchPropertiesFromGuiAndCreateNumberFactory());
        paused = false;
        new Thread(() -> {
            try {
                if (simulation != null && playFile != null) {
                    hasToStop = false;
                    simulation.playSimulation(playFile.getAbsolutePath());
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
        if (simulation != null && simulation.getProperties() != null && simulation.getProperties().getInitialObjects() != null) {
            new Thread(() -> {
                try {
                    if (simulation.getProperties().isRealTimeVisualization()) {
                        visualizer = new VisualizerImpl(simulation.getProperties());
                    }
                    if (readerWriter == null && simulation.getProperties().isSaveToFile()) {
                        readerWriter = new JsonReaderWriter();
                    }
                    simulation.startSimulation();
                } catch (SimulationException ex) {
                    String message = "Error during simulation.";
                    error(logger, message, ex);
                    visualizer.closeWindow();
                    showError(message + " " + ex.getMessage());
                } catch (ArithmeticException ex) {
                    if (ex.getMessage().contains("zero")) {
                        String message = "Operation with zero. Please increase the precision and try again.";
                        error(logger, message, ex);
                        visualizer.closeWindow();
                        showError(message + " " + ex.getMessage());
                    } else {
                        String message = "Arithmetic exception.";
                        error(logger, message, ex);
                        visualizer.closeWindow();
                        showError(message + " " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    String message = "Unexpected exception.";
                    error(logger, message, ex);
                    visualizer.closeWindow();
                    showError(message + " " + ex.getMessage());
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

    private SimulationProperties fetchPropertiesFromGuiAndCreateNumberFactory() {
        SimulationProperties properties = new SimulationProperties();

        if (!isNullOrBlank(gui.getScaleTextField().getText())) {
            properties.setScale(Integer.parseInt(gui.getScaleTextField().getText()));
        }

        if (!isNullOrBlank(gui.getPrecisionTextField().getText())) {
            properties.setPrecision(Integer.parseInt(gui.getPrecisionTextField().getText()));
        }

        properties.setNumberType(NumberFactory.NumberType.valueOf(String.valueOf(gui.getNumberTypeComboBox().getSelectedItem())));

        createNumberFactory(properties.getNumberType(), properties.getPrecision(), properties.getScale());

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
        
        if (!isNullOrBlank(gui.getPlayingSpeedTextField().getText().replace("-", ""))) {
            properties.setPlayingSpeed(Integer.parseInt(gui.getPlayingSpeedTextField().getText()));
        }

        if (!isNullOrBlank(gui.getSaveEveryNthIterationTextField().getText())) {
            properties.setSaveEveryNthIteration(Integer.parseInt(gui.getSaveEveryNthIterationTextField().getText()));
        }

        properties.setBounceFromWalls(gui.getBounceFromScreenWallsCheckBox().isSelected());
        properties.setRealTimeVisualization(gui.getRealTimeVisualizationCheckBox().isSelected());
        properties.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(String.valueOf(gui.getInteractingLawComboBox().getSelectedItem())));
    }

    public void onVisualizationWindowClosed() {
        C.setPaused(false);
        if (gui.getRunningRadioButton().isSelected()) {
            gui.getStartButton().setEnabled(true);
        }
        if (gui.getRunningRadioButton().isSelected()) {
            gui.getRunningComponents().forEach(c -> c.setEnabled(true));
            gui.getSavingToFileComponents().forEach(c -> c.setEnabled(gui.getSaveToFileCheckBox().isSelected()));
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
    }

    public void setPause(boolean paused) {
        gui.getPauseButton().setText(paused ? "Unpause" : "Pause");
    }

    public void enableRunning(boolean enable) {
        gui.getRunningComponents().forEach(c -> c.setEnabled(enable));
        gui.getPlayingComponents().forEach(c -> c.setEnabled(!enable));
        gui.getSavingToFileComponents().forEach(c -> c.setEnabled(enable && gui.getSaveToFileCheckBox().isSelected()));
    }

    public void refreshProperties(SimulationProperties prop) {
        gui.getNumberOfObjectsTextField().setText(String.valueOf(prop.getNumberOfObjects()));
        gui.getNumberOfIterationsTextField().setText(String.valueOf(prop.getNumberOfIterations()));
        gui.getSecondsPerIterationTextField().setText(String.valueOf(prop.getSecondsPerIteration()));
        gui.getNumberTypeComboBox().setSelectedItem(prop.getNumberType());
        gui.getInteractingLawComboBox().setSelectedItem(prop.getInteractingLaw());
        gui.getSaveToFileCheckBox().setSelected(prop.isSaveToFile());
        gui.getSaveEveryNthIterationTextField().setText(String.valueOf(prop.getSaveEveryNthIteration()));
        gui.getOutputFileTextField().setText(prop.getOutputFile());
        gui.getPrecisionTextField().setText(String.valueOf(prop.getPrecision()));
        gui.getScaleTextField().setText(String.valueOf(prop.getScale()));
        gui.getRealTimeVisualizationCheckBox().setSelected(prop.isRealTimeVisualization());
        gui.getBounceFromScreenWallsCheckBox().setSelected(prop.isBounceFromWalls());
        gui.getPlayingSpeedTextField().setText(String.valueOf(prop.getPlayingSpeed()));

        ((InitialObjectsTableModelAndListener) gui.getInitialObjectsTable().getModel()).setInitialObjects(prop.getInitialObjects());
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

    public void appendMessage(String message) {
        gui.getConsoleTextArea().append(message + "\n");
        gui.getConsoleTextArea().setCaretPosition(gui.getConsoleTextArea().getDocument().getLength());
    }

    public int getFontSize() {
        if (!isNullOrBlank(gui.getFontSize().getText().replace("-", ""))) {
            return Integer.parseInt(gui.getFontSize().getText());
        } else {
            return 48;
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
                SimulationGenerator.generateObjects(properties);
                refreshProperties(properties);
                unHighlightGenerateObjectButton();
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

    private void highlightGenerateObjectsButton() {
        if (defaultButtonColor == null) {
            defaultButtonColor = gui.getGenerateObjectsButton().getBackground();
        }
        gui.getGenerateObjectsButton().setBackground(PINK);
    }

    private void unHighlightGenerateObjectButton() {
        gui.getGenerateObjectsButton().setBackground(defaultButtonColor != null ? defaultButtonColor : new Color(238, 238, 238));
    }

    public void scaleTextFieldEvent() {
        if (!isNullOrBlank(gui.getScaleTextField().getText())) {
            highlightGenerateObjectsButton();
        }
    }

    public void precisionTextFieldEvent() {
        if (!isNullOrBlank(gui.getPrecisionTextField().getText())) {
            highlightGenerateObjectsButton();
        }
    }

    public void numberTypeComboBoxEvent(ActionEvent actionEvent) {
        if (actionEvent.getModifiers() != 0) {
            highlightGenerateObjectsButton();
        }
    }

    public void savePropertiesButtonEvent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(gui.getInputFilePathLabel().getText()));
        int userSelection = fileChooser.showSaveDialog(gui.getMainPanel());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            SimulationProperties properties = fetchPropertiesFromGuiAndCreateNumberFactory();
            readerWriter.writeProperties(properties, fileToSave.getAbsolutePath());

            /* Reopen just saved file.
             * This ensures the simulation is the same as opened from the file,
             * and can help us to detect a bug (when after saving the properties suddenly changes). */
            gui.getInputFilePathLabel().setText(fileToSave.getAbsolutePath());
            properties = loadProperties(fileToSave.getAbsolutePath());
            refreshProperties(properties);
        }
    }

    public void browseButtonEvent() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON file", "json"));
        int option = fileChooser.showOpenDialog(gui.getMainPanel());
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            gui.getInputFilePathLabel().setText(file.getAbsolutePath());
            refreshProperties(loadProperties(file.getAbsolutePath()));
            unHighlightGenerateObjectButton();
            saveToFileCheckBoxEvent();
        }
    }

    public void secondsPerIterationTextFieldEvent() {
    }

    public void numberOfIterationsTextFieldEvent() {
    }

    public void numberOfObjectsTextFieldEvent() {
        if (!isNullOrBlank(gui.getNumberOfObjectsTextField().getText())) {
            highlightGenerateObjectsButton();
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

    public void bounceFromScreenWallsCheckBoxEvent() {
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
}
