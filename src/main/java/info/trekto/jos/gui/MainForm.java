package info.trekto.jos.gui;

import info.trekto.jos.C;
import info.trekto.jos.Main;
import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator.InteractingLaw;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.visualization.Visualizer;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainForm {
    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);

    private JButton browseButton;
    private JTextField numberOfIterationsTextField;
    private JTextField secondsPerIterationTextField;
    private JCheckBox saveToFileCheckBox;
    private JTextField outputFileTextField;
    private JComboBox<NumberType> numberTypeComboBox;
    private JComboBox interactingLawComboBox;
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

    public MainForm() {
        initialObjectsTable.setModel(new InitialObjectsTableModelAndListener(this));
        browseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON file", "json"));
            int option = fileChooser.showOpenDialog(mainPanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                inputFilePathLabel.setText(file.getAbsolutePath());
                Main.init(file.getAbsolutePath());
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
                Main.init(fileToSave.getAbsolutePath());
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
                C.prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
            }
        });

        Vector<NumberType> comboBoxItems = new Vector<>();
        Collections.addAll(comboBoxItems, NumberType.values());
        numberTypeComboBox.setModel(new DefaultComboBoxModel<>(comboBoxItems));

        numberTypeComboBox.addActionListener(
                actionEvent -> {
                    if (actionEvent.getModifiers() != 0) {
                        C.prop.setNumberType(NumberType.valueOf(String.valueOf(numberTypeComboBox.getSelectedItem())));
//                    createNumberFactory(C.prop.getNumberType(), C.prop.getPrecision(), C.prop.getScale());
//                    C.prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
//                    ((InitialObjectsTableModelAndListener) initialObjectsTable.getModel()).refreshInitialObjects();
                        showWarn(mainPanel, "You have to save properties, number type change to take effect.");
                    }
                });

        interactingLawComboBox.addActionListener(
                actionEvent -> C.prop.setInteractingLaw(InteractingLaw.valueOf(String.valueOf(interactingLawComboBox.getSelectedItem()))));


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
        stopButton.addActionListener(actionEvent -> C.hasToStop = true);

        appendMessage("Controls:");
        appendMessage("\tExit: Esc");
        appendMessage("\tZoom in: +");
        appendMessage("\tZoom out: -");
        appendMessage("\tMove up: ↑");
        appendMessage("\tMove down: ↓");
        appendMessage("\tMove right: →");
        appendMessage("\tMove left: ←");
    }

    private void start() {
        C.simulation = new SimulationForkJoinImpl();
        new Thread(() -> {

            C.simulationLogic = new SimulationLogicImpl();
            C.simulation.removeAllSubscribers();
            if (C.prop.isRealTimeVisualization()) {
                Visualizer visualizer = new VisualizerImpl();
                C.simulation.subscribe(visualizer);
            }
            try {
                C.simulation.startSimulation();
            } catch (SimulationException e) {
                showError(mainPanel, "Error during simulation.", e);
            } catch (ArithmeticException ex) {
                if (ex.getMessage().contains("zero")) {
                    String message = "Operation with zero. Please increase the precision and try again. " + ex.getMessage();
                    logger.error(message, ex);
                    if (C.mainForm != null) {
                        C.mainForm.appendMessage(message);
                    }
                } else {
                    throw ex;
                }
            }

        }).start();
    }

    void refreshProperties(SimulationProperties prop) {
        numberOfObjectsTextField.setText(String.valueOf(prop.getNumberOfObjects()));
        numberOfIterationsTextField.setText(String.valueOf(prop.getNumberOfIterations()));
        secondsPerIterationTextField.setText(String.valueOf(prop.getSecondsPerIteration()));
        numberTypeComboBox.setSelectedItem(prop.getNumberType());
        interactingLawComboBox.setSelectedItem(prop.getInteractingLaw());
        saveToFileCheckBox.setSelected(prop.isSaveToFile());
        outputFileTextField.setText(prop.getOutputFile());
        precisionTextField.setText(String.valueOf(prop.getPrecision()));
        scaleTextField.setText(String.valueOf(prop.getScale()));
        realTimeVisualizationCheckBox.setSelected(prop.isRealTimeVisualization());
        bounceFromScreenWallsCheckBox.setSelected(prop.isBounceFromWalls());
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
        jFrame.setContentPane(mainForm.mainPanel);
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void appendMessage(String message) {
        consoleTextArea.append(message + "\n");
    }
}
