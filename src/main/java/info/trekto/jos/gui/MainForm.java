package info.trekto.jos.gui;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.util.SimulationGenerator;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainForm {
    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);
    public static final String PROGRAM_NAME = "JOS";

    private JButton browseButton;
    private JTextField numberOfIterationsTextField;
    private JTextField secondsPerIterationTextField;
    private JCheckBox saveToFileCheckBox;
    private JTextField outputFileTextField;
    private JCheckBox realTimeVisualizationCheckBox;
    private JTextField playingSpeedTextField;
    private JCheckBox bounceFromScreenWallsCheckBox;
    private JTextField numberOfObjectsTextField;
    private JTextField precisionTextField;
    private JTextField scaleTextField;
    private JTable initialObjectsTable;
    private JTextArea consoleTextArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton savePropertiesButton;
    private JPanel mainPanel;
    private JLabel inputFilePathLabel;
    private JRadioButton runningRadioButton;
    private JRadioButton playRadioButton;
    private JButton browsePlayingFileButton;
    private JLabel playFileLabel;
    private JPanel simulationPropertiesPanel;
    private JLabel numberOfObjectsLabel;
    private JLabel secondsPerIterationLabel;
    private JLabel numberTypeLabel;
    private JLabel interactingLawLabel;
    private JLabel outputFileLabel;
    private JLabel precisionLabel;
    private JLabel scaleLabel;
    private JLabel numberTypeDropdown;
    private JLabel lawDropdown;
    private JLabel numberOfIterationsLabel;
    private JLabel playFromLabel;
    private JLabel playingSpeedLabel;
    private JButton playButton;
    private JScrollPane initialObjectsPanel;
    private JButton generateObjectsButton;
    private ButtonGroup buttonGroup;
    private List<Component> runningComponents;
    private List<Component> playingComponents;

    public MainForm() {
        initialObjectsTable.setModel(new InitialObjectsTableModelAndListener(this));
        browseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON file", "json"));
            int option = fileChooser.showOpenDialog(mainPanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                inputFilePathLabel.setText(file.getAbsolutePath());
                SimulationImpl.init(file.getAbsolutePath());
                refreshProperties(C.prop);
            }
        });

        savePropertiesButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(inputFilePathLabel.getText()));
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                C.io.writeProperties(C.prop, fileToSave.getAbsolutePath());

                /* Reopen just saved file */
                inputFilePathLabel.setText(fileToSave.getAbsolutePath());
                SimulationImpl.init(fileToSave.getAbsolutePath());
                refreshProperties(C.prop);
            }
        });

        numberOfObjectsTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!numberOfObjectsTextField.getText().isBlank()) {
                C.prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }
        });

        numberOfIterationsTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!numberOfIterationsTextField.getText().isBlank()) {
                C.prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }
        });

        secondsPerIterationTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!secondsPerIterationTextField.getText().isBlank()) {
                C.prop.setSecondsPerIteration(Double.parseDouble(secondsPerIterationTextField.getText()));
            }
        });

        saveToFileCheckBox.addActionListener(actionEvent -> C.prop.setSaveToFile(saveToFileCheckBox.isSelected()));

        precisionTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!precisionTextField.getText().isBlank()) {
                C.prop.setPrecision(Integer.parseInt(precisionTextField.getText()));
            }
        });

        scaleTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!scaleTextField.getText().isBlank()) {
                C.prop.setScale(Integer.parseInt(scaleTextField.getText()));
            }
        });

        realTimeVisualizationCheckBox.addActionListener(actionEvent -> C.prop.setRealTimeVisualization(realTimeVisualizationCheckBox.isSelected()));
        bounceFromScreenWallsCheckBox.addActionListener(actionEvent -> C.prop.setBounceFromWalls(bounceFromScreenWallsCheckBox.isSelected()));

        playingSpeedTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!playingSpeedTextField.getText().isBlank()) {
                C.prop.setPlayingSpeed(Integer.parseInt(playingSpeedTextField.getText()));
            }
        });

        outputFileTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!outputFileTextField.getText().isBlank()) {
                C.prop.setOutputFile(outputFileTextField.getText());
            }
        });

        startButton.addActionListener(actionEvent -> start());
        stopButton.addActionListener(actionEvent -> {
            if (C.simulation != null && C.simulation.running) {
                C.hasToStop = true;
            }
        });

        appendMessage("Controls:");
        appendMessage("\tExit: Esc");
        appendMessage("\tZoom in: +");
        appendMessage("\tZoom out: -");
        appendMessage("\tMove up: ↑");
        appendMessage("\tMove down: ↓");
        appendMessage("\tMove right: →");
        appendMessage("\tMove left: ←");

        buttonGroup = new ButtonGroup();
        buttonGroup.add(runningRadioButton);
        buttonGroup.add(playRadioButton);

        runningRadioButton.addActionListener(actionEvent -> enableRunning(true));
        playRadioButton.addActionListener(actionEvent -> enableRunning(false));

        runningComponents = new ArrayList<>(List.of(
                numberOfIterationsTextField, secondsPerIterationTextField, browseButton, saveToFileCheckBox, outputFileTextField,
                realTimeVisualizationCheckBox, numberOfObjectsTextField, initialObjectsTable, numberOfIterationsLabel,
                startButton, stopButton, savePropertiesButton, inputFilePathLabel, simulationPropertiesPanel, numberOfObjectsLabel,
                secondsPerIterationLabel, numberTypeLabel, interactingLawLabel, outputFileLabel, numberTypeDropdown, lawDropdown,
                initialObjectsTable, initialObjectsPanel, generateObjectsButton));

        playingComponents = new ArrayList<>(List.of(playingSpeedTextField, playFileLabel, playFromLabel, browsePlayingFileButton,
                                                    playingSpeedLabel, playingSpeedTextField, playButton));

        generateObjectsButton.addActionListener(actionEvent -> {
            SimulationProperties prop = new SimulationProperties();
            if (!numberOfObjectsTextField.getText().isBlank()) {
                prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }

            if (!numberOfIterationsTextField.getText().isBlank()) {
                prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }

            if (!secondsPerIterationTextField.getText().isBlank()) {
                prop.setSecondsPerIteration(Double.parseDouble(secondsPerIterationTextField.getText()));
            }
            prop.setRealTimeVisualization(realTimeVisualizationCheckBox.isSelected());
            prop.setSaveToFile(saveToFileCheckBox.isSelected());
            
            new Thread(() -> SimulationGenerator.generateObjects(prop, this)).start();
        });
    }

    private void enableRunning(boolean enable) {
        runningComponents.forEach(c -> c.setEnabled(enable));
        playingComponents.forEach(c -> c.setEnabled(!enable));
    }

    private void start() {
        if (C.prop != null && C.prop.getInitialObjects() != null) {
            new Thread(() -> {
                try {
                    if (C.prop.isRealTimeVisualization()) {
                        C.visualizer = new VisualizerImpl();
                    }
                    C.simulation.startSimulation();
                } catch (SimulationException e) {
                    showError(mainPanel, "Error during simulation.", e);
                    C.visualizer.closeWindow();
                } catch (ArithmeticException ex) {
                    if (ex.getMessage().contains("zero")) {
                        String message = "Operation with zero. Please increase the precision and try again. " + ex.getMessage();
                        logger.error(message, ex);
                        appendMessage(message);
                        C.visualizer.closeWindow();
                    } else {
                        throw ex;
                    }
                } finally {
                    onVisualizationWindowClosed();
                }
            }).start();
            startButton.setEnabled(false);
            runningComponents.forEach(c -> c.setEnabled(false));
            playingComponents.forEach(c -> c.setEnabled(false));
            runningRadioButton.setEnabled(false);
            playRadioButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    public void refreshProperties(SimulationProperties prop) {
        numberOfObjectsTextField.setText(String.valueOf(prop.getNumberOfObjects()));
        numberOfIterationsTextField.setText(String.valueOf(prop.getNumberOfIterations()));
        secondsPerIterationTextField.setText(String.valueOf(prop.getSecondsPerIteration()));
        saveToFileCheckBox.setSelected(prop.isSaveToFile());
        outputFileTextField.setText(prop.getOutputFile());
        precisionTextField.setText(String.valueOf(prop.getPrecision()));
        scaleTextField.setText(String.valueOf(prop.getScale()));
        realTimeVisualizationCheckBox.setSelected(prop.isRealTimeVisualization());
        bounceFromScreenWallsCheckBox.setSelected(false);
        playingSpeedTextField.setText(String.valueOf(prop.getPlayingSpeed()));

        ((InitialObjectsTableModelAndListener) initialObjectsTable.getModel()).setRowCount(0);
        for (SimulationObject initialObject : prop.getInitialObjects()) {
            ((InitialObjectsTableModelAndListener) initialObjectsTable.getModel()).addRow(initialObject);
        }
    }

    private void showError(Component parent, String message, Exception exception) {
        showError(parent, message + " " + exception.getMessage());
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", ERROR_MESSAGE);
    }

    private void showWarn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        MainForm mainForm = new MainForm();
        C.mainForm = mainForm;
        C.prop = new SimulationProperties();
        jFrame.setContentPane(mainForm.mainPanel);
        jFrame.setTitle(PROGRAM_NAME);
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null); // Center of the screen
        jFrame.setVisible(true);
    }

    public void appendMessage(String message) {
        consoleTextArea.append(message + "\n");
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
    }

    public void onVisualizationWindowClosed() {
        startButton.setEnabled(true);
        if (runningRadioButton.isSelected()) {
            runningComponents.forEach(c -> c.setEnabled(true));
        } else {
            playingComponents.forEach(c -> c.setEnabled(true));
        }
        runningRadioButton.setEnabled(true);
        playRadioButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}
