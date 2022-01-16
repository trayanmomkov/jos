package info.trekto.jos.gui;

import info.trekto.jos.C;
import info.trekto.jos.Main;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator.InteractingLaw;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.visualization.Visualizer;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainForm {
    private JTextField inputFileTextField;
    private JButton browseButton;
    private JTextField numberOfIterationsTextField;
    private JTextField secondsPerIterationTextField;
    private JCheckBox saveToFileCheckBox;
    private JTextField outputFileTextField;
    private JComboBox numberTypeComboBox;
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

    public MainForm() {
        initialObjectsTable.setModel(new InitialObjectsTableModelAndListener(this));
        browseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(mainPanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                inputFileTextField.setText(file.getAbsolutePath());
                Main.init(file.getAbsolutePath());
                refreshProperties(C.prop);
            }
        });

        savePropertiesButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(inputFileTextField.getText()));
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                C.io.writeProperties(C.prop, fileToSave.getAbsolutePath());
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

        numberTypeComboBox.addActionListener(
                actionEvent -> C.prop.setNumberType(NumberType.valueOf(String.valueOf(numberTypeComboBox.getSelectedItem()))));

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
        if (C.simulation != null) {
            new Thread(() -> {
                C.simulation.removeAllSubscribers();
                if (C.prop.isRealTimeVisualization()) {
                    Visualizer visualizer = new VisualizerImpl();
                    C.simulation.subscribe(visualizer);
                }
                try {
                    C.simulation.startSimulation();
                } catch (SimulationException e) {
                    showError(mainPanel, "Error during simulation.", e);
                }
            }).start();
        }
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
