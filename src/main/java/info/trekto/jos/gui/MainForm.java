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
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.isNullOrBlank;
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
    private JPanel playingPanel;
    private JPanel browsePropertiesPanel;
    private JTextField fontSize;
    private JCheckBox showObjectIDsCheckBox;
    private JCheckBox showTrailCheckBox;
    private JTextField trailSizeTextField;
    private JLabel fontSizeLabel;
    private JLabel trailSizeTextLabel;
    private ButtonGroup buttonGroup;
    private List<Component> runningComponents;
    private List<Component> playingComponents;
    private File playFile;

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
            if (!isNullOrBlank(numberOfObjectsTextField.getText())) {
                C.prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }
        });

        numberOfIterationsTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(numberOfIterationsTextField.getText())) {
                C.prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }
        });

        secondsPerIterationTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(secondsPerIterationTextField.getText())) {
                C.prop.setSecondsPerIteration(Double.parseDouble(secondsPerIterationTextField.getText()));
            }
        });

        saveToFileCheckBox.addActionListener(actionEvent -> {
            outputFileLabel.setEnabled(saveToFileCheckBox.isSelected());
            outputFileTextField.setEnabled(saveToFileCheckBox.isSelected());
            C.prop.setSaveToFile(saveToFileCheckBox.isSelected());
        });

        precisionTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(precisionTextField.getText())) {
                C.prop.setPrecision(Integer.parseInt(precisionTextField.getText()));
            }
        });

        scaleTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(scaleTextField.getText())) {
                C.prop.setScale(Integer.parseInt(scaleTextField.getText()));
            }
        });

        realTimeVisualizationCheckBox.addActionListener(actionEvent -> C.prop.setRealTimeVisualization(realTimeVisualizationCheckBox.isSelected()));
        bounceFromScreenWallsCheckBox.addActionListener(actionEvent -> C.prop.setBounceFromWalls(bounceFromScreenWallsCheckBox.isSelected()));

        playingSpeedTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(playingSpeedTextField.getText().replace("-", ""))) {
                C.prop.setPlayingSpeed(Integer.parseInt(playingSpeedTextField.getText()));
            }
        });

        outputFileTextField.getDocument().addUndoableEditListener(actionEvent -> {
            if (!isNullOrBlank(outputFileTextField.getText())) {
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

        runningComponents = Arrays.asList(
                numberOfIterationsTextField, secondsPerIterationTextField, browseButton, saveToFileCheckBox, outputFileTextField,
                realTimeVisualizationCheckBox, numberOfObjectsTextField, initialObjectsTable, numberOfIterationsLabel,
                startButton, savePropertiesButton, inputFilePathLabel, simulationPropertiesPanel, numberOfObjectsLabel,
                secondsPerIterationLabel, numberTypeLabel, interactingLawLabel, outputFileLabel, numberTypeDropdown, lawDropdown,
                initialObjectsTable, initialObjectsPanel, generateObjectsButton);

        playingComponents = Arrays.asList(playingSpeedTextField, playFileLabel, playFromLabel, browsePlayingFileButton, playButton);

        generateObjectsButton.addActionListener(actionEvent -> {
            SimulationProperties prop = new SimulationProperties();
            if (!isNullOrBlank(numberOfObjectsTextField.getText())) {
                prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }

            if (!isNullOrBlank(numberOfIterationsTextField.getText())) {
                prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }

            if (!isNullOrBlank(secondsPerIterationTextField.getText())) {
                prop.setSecondsPerIteration(Double.parseDouble(secondsPerIterationTextField.getText()));
            }
            prop.setRealTimeVisualization(realTimeVisualizationCheckBox.isSelected());
            prop.setSaveToFile(saveToFileCheckBox.isSelected());

            new Thread(() -> {
                try {
                    SimulationGenerator.generateObjects(prop, this);
                } catch (Exception ex) {
                    String message = "Error during object generation.";
                    error(logger, message, ex);
                    C.visualizer.closeWindow();
                    showError(mainPanel, message + " " + ex.getMessage());
                } finally {
                    onVisualizationWindowClosed();
                }
            }).start();

        });

        browsePlayingFileButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("GZipped JSON file", "gz"));
            int option = fileChooser.showOpenDialog(mainPanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                playFile = fileChooser.getSelectedFile();
                playFileLabel.setText(playFile.getAbsolutePath());
                try {
                    SimulationImpl.initForPlaying(playFile.getAbsolutePath());
                    refreshProperties(C.prop);
                } catch (IOException e) {
                    showError(mainPanel, "Cannot read ZIP file.", e);
                }
            }
        });

        playingSpeedLabel.setLabelFor(playingSpeedTextField);

        playButton.addActionListener(actionEvent -> {
            stopButton.setEnabled(true);
            play();
        });

        showObjectIDsCheckBox.addActionListener(actionEvent -> {
            fontSize.setEnabled(showObjectIDsCheckBox.isSelected());
            fontSizeLabel.setEnabled(showObjectIDsCheckBox.isSelected());
        });
        showTrailCheckBox.addActionListener(actionEvent -> {
            trailSizeTextField.setEnabled(showTrailCheckBox.isSelected());
            trailSizeTextLabel.setEnabled(showTrailCheckBox.isSelected());
        });
        outputFileTextField.addActionListener(actionEvent -> C.prop.setOutputFile(outputFileTextField.getText()));
        numberOfObjectsTextField.addActionListener(actionEvent -> {
            if (!isNullOrBlank(numberOfObjectsTextField.getText())) {
                C.prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }
        });
        numberOfIterationsTextField.addActionListener(actionEvent -> {
            if (!isNullOrBlank(numberOfIterationsTextField.getText())) {
                C.prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }
        });
        secondsPerIterationTextField.addActionListener(actionEvent -> {
            if (!isNullOrBlank(secondsPerIterationTextField.getText())) {
                C.prop.setSecondsPerIteration(Integer.parseInt(secondsPerIterationTextField.getText()));
            }
        });
    }

    private void enableRunning(boolean enable) {
        runningComponents.forEach(c -> c.setEnabled(enable));
        playingComponents.forEach(c -> c.setEnabled(!enable));
    }

    private void play() {
        new Thread(() -> {
            try {
                C.simulation.playSimulation(playFile.getAbsolutePath());
            } catch (Exception ex) {
                String message = "Error during playing.";
                error(logger, message, ex);
                C.visualizer.closeWindow();
                showError(mainPanel, message + " " + ex.getMessage());
            } finally {
                onVisualizationWindowClosed();
            }
        }).start();
    }

    private void start() {
        if (C.prop != null && C.prop.getInitialObjects() != null) {
            new Thread(() -> {
                try {
                    if (C.prop.isRealTimeVisualization()) {
                        C.visualizer = new VisualizerImpl();
                    }
                    C.simulation.startSimulation();
                } catch (SimulationException ex) {
                    String message = "Error during simulation.";
                    error(logger, message, ex);
                    C.visualizer.closeWindow();
                    showError(mainPanel, message + " " + ex.getMessage());
                } catch (ArithmeticException ex) {
                    if (ex.getMessage().contains("zero")) {
                        String message = "Operation with zero. Please increase the precision and try again.";
                        error(logger, message, ex);
                        C.visualizer.closeWindow();
                        showError(mainPanel, message + " " + ex.getMessage());
                    } else {
                        String message = "Arithmetic exception.";
                        error(logger, message, ex);
                        C.visualizer.closeWindow();
                        showError(mainPanel, message + " " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    String message = "Unexpected exception.";
                    error(logger, message, ex);
                    C.visualizer.closeWindow();
                    showError(mainPanel, message + " " + ex.getMessage());
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
        if (runningRadioButton.isSelected()) {
            startButton.setEnabled(true);
        }
        if (runningRadioButton.isSelected()) {
            runningComponents.forEach(c -> c.setEnabled(true));
        } else {
            playingComponents.forEach(c -> c.setEnabled(true));
        }
        runningRadioButton.setEnabled(true);
        playRadioButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public int getFontSize() {
        if (!isNullOrBlank(fontSize.getText().replace("-", ""))) {
            return Integer.parseInt(fontSize.getText());
        } else {
            return 48;
        }
    }

    public boolean isShowIds() {
        return showObjectIDsCheckBox.isSelected();
    }

    public boolean isShowTrail() {
        return showTrailCheckBox.isSelected();
    }

    public int getTrailSize() {
        if (!isNullOrBlank(trailSizeTextField.getText().replace("-", ""))) {
            return Integer.parseInt(trailSizeTextField.getText());
        } else {
            return 500;
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        simulationPropertiesPanel = new JPanel();
        simulationPropertiesPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        simulationPropertiesPanel.setEnabled(true);
        mainPanel.add(simulationPropertiesPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        simulationPropertiesPanel.setBorder(BorderFactory.createTitledBorder(null, "Simulation properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        simulationPropertiesPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        numberOfIterationsLabel = new JLabel();
        numberOfIterationsLabel.setText("Number of iterations");
        panel1.add(numberOfIterationsLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfIterationsTextField = new JTextField();
        numberOfIterationsTextField.setText("0");
        panel1.add(numberOfIterationsTextField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        secondsPerIterationLabel = new JLabel();
        secondsPerIterationLabel.setText("Seconds per iteration");
        panel1.add(secondsPerIterationLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        secondsPerIterationTextField = new JTextField();
        secondsPerIterationTextField.setText("0.01");
        panel1.add(secondsPerIterationTextField, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        numberTypeLabel = new JLabel();
        numberTypeLabel.setText("Number type");
        panel1.add(numberTypeLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        interactingLawLabel = new JLabel();
        interactingLawLabel.setText("Interacting law");
        panel1.add(interactingLawLabel, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfObjectsLabel = new JLabel();
        numberOfObjectsLabel.setText("Number of objects");
        panel1.add(numberOfObjectsLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfObjectsTextField = new JTextField();
        numberOfObjectsTextField.setText("20");
        panel1.add(numberOfObjectsTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        numberTypeDropdown = new JLabel();
        numberTypeDropdown.setText("DOUBLE");
        panel1.add(numberTypeDropdown, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lawDropdown = new JLabel();
        lawDropdown.setText("NEWTON_LAW_OF_GRAVITATION");
        panel1.add(lawDropdown, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        simulationPropertiesPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        realTimeVisualizationCheckBox = new JCheckBox();
        realTimeVisualizationCheckBox.setHorizontalTextPosition(10);
        realTimeVisualizationCheckBox.setSelected(true);
        realTimeVisualizationCheckBox.setText("Real time visualization");
        panel2.add(realTimeVisualizationCheckBox, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bounceFromScreenWallsCheckBox = new JCheckBox();
        bounceFromScreenWallsCheckBox.setEnabled(false);
        bounceFromScreenWallsCheckBox.setHideActionText(true);
        bounceFromScreenWallsCheckBox.setHorizontalAlignment(10);
        bounceFromScreenWallsCheckBox.setHorizontalTextPosition(10);
        bounceFromScreenWallsCheckBox.setText("Bounce from screen walls");
        panel2.add(bounceFromScreenWallsCheckBox, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        precisionLabel = new JLabel();
        precisionLabel.setEnabled(false);
        precisionLabel.setText("Precision");
        panel2.add(precisionLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        precisionTextField = new JTextField();
        precisionTextField.setEnabled(false);
        precisionTextField.setText("");
        panel2.add(precisionTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scaleLabel = new JLabel();
        scaleLabel.setEnabled(false);
        scaleLabel.setText("Scale");
        panel2.add(scaleLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaleTextField = new JTextField();
        scaleTextField.setEnabled(false);
        panel2.add(scaleTextField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        generateObjectsButton = new JButton();
        generateObjectsButton.setText("Generate objects");
        panel2.add(generateObjectsButton, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputFileTextField = new JTextField();
        outputFileTextField.setEnabled(false);
        simulationPropertiesPanel.add(outputFileTextField, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        outputFileLabel = new JLabel();
        outputFileLabel.setEnabled(false);
        outputFileLabel.setText("Output file");
        simulationPropertiesPanel.add(outputFileLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveToFileCheckBox = new JCheckBox();
        saveToFileCheckBox.setHorizontalTextPosition(10);
        saveToFileCheckBox.setText("Save to file");
        simulationPropertiesPanel.add(saveToFileCheckBox, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browsePropertiesPanel = new JPanel();
        browsePropertiesPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        simulationPropertiesPanel.add(browsePropertiesPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        browseButton = new JButton();
        browseButton.setText("Browse");
        browsePropertiesPanel.add(browseButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        browsePropertiesPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        savePropertiesButton = new JButton();
        savePropertiesButton.setText("Save properties");
        browsePropertiesPanel.add(savePropertiesButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        inputFilePathLabel = new JLabel();
        inputFilePathLabel.setText("");
        browsePropertiesPanel.add(inputFilePathLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setToolTipText("");
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Output conosle", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        consoleTextArea = new JTextArea();
        Font consoleTextAreaFont = this.$$$getFont$$$(null, -1, -1, consoleTextArea.getFont());
        if (consoleTextAreaFont != null) {
            consoleTextArea.setFont(consoleTextAreaFont);
        }
        consoleTextArea.setRows(30);
        consoleTextArea.setText("");
        consoleTextArea.setWrapStyleWord(false);
        scrollPane1.setViewportView(consoleTextArea);
        initialObjectsPanel = new JScrollPane();
        mainPanel.add(initialObjectsPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        initialObjectsPanel.setBorder(BorderFactory.createTitledBorder(null, "Initial objects", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        initialObjectsTable = new JTable();
        initialObjectsTable.setEnabled(true);
        initialObjectsPanel.setViewportView(initialObjectsTable);
        playingPanel = new JPanel();
        playingPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 12, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(playingPanel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        playRadioButton = new JRadioButton();
        playRadioButton.setText("Play simulation");
        playingPanel.add(playRadioButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playButton = new JButton();
        playButton.setEnabled(false);
        playButton.setText("Play");
        playingPanel.add(playButton, new com.intellij.uiDesigner.core.GridConstraints(0, 11, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playingSpeedLabel = new JLabel();
        playingSpeedLabel.setEnabled(true);
        playingSpeedLabel.setText("Playing speed");
        playingPanel.add(playingSpeedLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 9, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playingSpeedTextField = new JTextField();
        playingSpeedTextField.setColumns(3);
        playingPanel.add(playingSpeedTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 10, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playFromLabel = new JLabel();
        playFromLabel.setEnabled(false);
        playFromLabel.setText("Play from:");
        playingPanel.add(playFromLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        browsePlayingFileButton = new JButton();
        browsePlayingFileButton.setEnabled(false);
        browsePlayingFileButton.setText("Browse");
        playingPanel.add(browsePlayingFileButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playFileLabel = new JLabel();
        playFileLabel.setEnabled(false);
        playFileLabel.setText("");
        playingPanel.add(playFileLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 10, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontSize = new JTextField();
        fontSize.setColumns(2);
        fontSize.setEnabled(false);
        fontSize.setText("32");
        playingPanel.add(fontSize, new com.intellij.uiDesigner.core.GridConstraints(0, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showObjectIDsCheckBox = new JCheckBox();
        showObjectIDsCheckBox.setHorizontalAlignment(11);
        showObjectIDsCheckBox.setHorizontalTextPosition(10);
        showObjectIDsCheckBox.setText("Object IDs");
        playingPanel.add(showObjectIDsCheckBox, new com.intellij.uiDesigner.core.GridConstraints(0, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontSizeLabel = new JLabel();
        fontSizeLabel.setEnabled(false);
        fontSizeLabel.setText("Font size:");
        playingPanel.add(fontSizeLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showTrailCheckBox = new JCheckBox();
        showTrailCheckBox.setHorizontalTextPosition(10);
        showTrailCheckBox.setText("Trail");
        playingPanel.add(showTrailCheckBox, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        trailSizeTextLabel = new JLabel();
        trailSizeTextLabel.setEnabled(false);
        trailSizeTextLabel.setText("Trail size:");
        playingPanel.add(trailSizeTextLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        trailSizeTextField = new JTextField();
        trailSizeTextField.setColumns(4);
        trailSizeTextField.setEnabled(false);
        trailSizeTextField.setText("500");
        playingPanel.add(trailSizeTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runningRadioButton = new JRadioButton();
        runningRadioButton.setSelected(true);
        runningRadioButton.setText("Run simulation");
        panel3.add(runningRadioButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setText("Start");
        panel3.add(startButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopButton = new JButton();
        stopButton.setEnabled(false);
        stopButton.setText("Stop");
        panel3.add(stopButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel3.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) {
            return null;
        }
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
