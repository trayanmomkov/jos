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
    private JScrollPane consolePanel;
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

        initialObjectsPanel.setSize(initialObjectsPanel.getWidth(), initialObjectsPanel.getHeight() * 2);
    }

    private void enableRunning(boolean enable) {
        runningComponents.forEach(c -> c.setEnabled(enable));
        playingComponents.forEach(c -> c.setEnabled(!enable));
    }

    private void play() {
        new Thread(() -> {
            try {
                if (C.simulation != null && playFile != null) {
                    C.simulation.playSimulation(playFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                String message = "Error during playing.";
                error(logger, message, ex);
                if (C.visualizer != null) {
                    C.visualizer.closeWindow();
                }
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
        mainPanel.setLayout(new GridBagLayout());
        simulationPropertiesPanel = new JPanel();
        simulationPropertiesPanel.setLayout(new GridBagLayout());
        simulationPropertiesPanel.setEnabled(true);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(simulationPropertiesPanel, gbc);
        simulationPropertiesPanel.setBorder(BorderFactory.createTitledBorder(null, "Simulation properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        simulationPropertiesPanel.add(panel1, gbc);
        numberOfIterationsLabel = new JLabel();
        numberOfIterationsLabel.setText("Number of iterations");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(numberOfIterationsLabel, gbc);
        numberOfIterationsTextField = new JTextField();
        numberOfIterationsTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(numberOfIterationsTextField, gbc);
        secondsPerIterationLabel = new JLabel();
        secondsPerIterationLabel.setText("Seconds per iteration");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(secondsPerIterationLabel, gbc);
        secondsPerIterationTextField = new JTextField();
        secondsPerIterationTextField.setText("0.01");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(secondsPerIterationTextField, gbc);
        numberTypeLabel = new JLabel();
        numberTypeLabel.setText("Number type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(numberTypeLabel, gbc);
        interactingLawLabel = new JLabel();
        interactingLawLabel.setText("Interacting law");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(interactingLawLabel, gbc);
        numberOfObjectsLabel = new JLabel();
        numberOfObjectsLabel.setText("Number of objects");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(numberOfObjectsLabel, gbc);
        numberOfObjectsTextField = new JTextField();
        numberOfObjectsTextField.setText("20");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(numberOfObjectsTextField, gbc);
        numberTypeDropdown = new JLabel();
        numberTypeDropdown.setText("DOUBLE");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(numberTypeDropdown, gbc);
        lawDropdown = new JLabel();
        lawDropdown.setText("NEWTON_LAW_OF_GRAVITATION");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lawDropdown, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        simulationPropertiesPanel.add(panel2, gbc);
        realTimeVisualizationCheckBox = new JCheckBox();
        realTimeVisualizationCheckBox.setHorizontalTextPosition(10);
        realTimeVisualizationCheckBox.setSelected(true);
        realTimeVisualizationCheckBox.setText("Real time visualization");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(realTimeVisualizationCheckBox, gbc);
        bounceFromScreenWallsCheckBox = new JCheckBox();
        bounceFromScreenWallsCheckBox.setEnabled(false);
        bounceFromScreenWallsCheckBox.setHideActionText(true);
        bounceFromScreenWallsCheckBox.setHorizontalAlignment(10);
        bounceFromScreenWallsCheckBox.setHorizontalTextPosition(10);
        bounceFromScreenWallsCheckBox.setText("Bounce from screen walls");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(bounceFromScreenWallsCheckBox, gbc);
        precisionLabel = new JLabel();
        precisionLabel.setEnabled(false);
        precisionLabel.setText("Precision");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(precisionLabel, gbc);
        precisionTextField = new JTextField();
        precisionTextField.setEnabled(false);
        precisionTextField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(precisionTextField, gbc);
        scaleLabel = new JLabel();
        scaleLabel.setEnabled(false);
        scaleLabel.setText("Scale");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(scaleLabel, gbc);
        scaleTextField = new JTextField();
        scaleTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(scaleTextField, gbc);
        generateObjectsButton = new JButton();
        generateObjectsButton.setText("Generate objects");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(generateObjectsButton, gbc);
        outputFileTextField = new JTextField();
        outputFileTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        simulationPropertiesPanel.add(outputFileTextField, gbc);
        outputFileLabel = new JLabel();
        outputFileLabel.setEnabled(false);
        outputFileLabel.setText("Output file");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(outputFileLabel, gbc);
        saveToFileCheckBox = new JCheckBox();
        saveToFileCheckBox.setHorizontalTextPosition(10);
        saveToFileCheckBox.setText("Save to file");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        simulationPropertiesPanel.add(saveToFileCheckBox, gbc);
        browsePropertiesPanel = new JPanel();
        browsePropertiesPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        simulationPropertiesPanel.add(browsePropertiesPanel, gbc);
        browseButton = new JButton();
        browseButton.setText("Browse");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        browsePropertiesPanel.add(browseButton, gbc);
        savePropertiesButton = new JButton();
        savePropertiesButton.setText("Save properties");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        browsePropertiesPanel.add(savePropertiesButton, gbc);
        inputFilePathLabel = new JLabel();
        inputFilePathLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 2, 0, 0);
        browsePropertiesPanel.add(inputFilePathLabel, gbc);
        consolePanel = new JScrollPane();
        consolePanel.setToolTipText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 0, 2);
        mainPanel.add(consolePanel, gbc);
        consolePanel.setBorder(BorderFactory.createTitledBorder(null, "Output conosle", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        consoleTextArea = new JTextArea();
        Font consoleTextAreaFont = this.$$$getFont$$$(null, -1, -1, consoleTextArea.getFont());
        if (consoleTextAreaFont != null) {
            consoleTextArea.setFont(consoleTextAreaFont);
        }
        consoleTextArea.setRows(20);
        consoleTextArea.setText("");
        consoleTextArea.setWrapStyleWord(false);
        consolePanel.setViewportView(consoleTextArea);
        initialObjectsPanel = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 0, 0);
        mainPanel.add(initialObjectsPanel, gbc);
        initialObjectsPanel.setBorder(BorderFactory.createTitledBorder(null, "Initial objects", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        initialObjectsTable = new JTable();
        initialObjectsTable.setEnabled(true);
        initialObjectsPanel.setViewportView(initialObjectsTable);
        playingPanel = new JPanel();
        playingPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 0, 0);
        mainPanel.add(playingPanel, gbc);
        playRadioButton = new JRadioButton();
        playRadioButton.setText("Play simulation");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        playingPanel.add(playRadioButton, gbc);
        playButton = new JButton();
        playButton.setEnabled(false);
        playButton.setText("Play");
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 2);
        playingPanel.add(playButton, gbc);
        playingSpeedLabel = new JLabel();
        playingSpeedLabel.setEnabled(true);
        playingSpeedLabel.setText("Playing speed");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(playingSpeedLabel, gbc);
        playingSpeedTextField = new JTextField();
        playingSpeedTextField.setColumns(3);
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(playingSpeedTextField, gbc);
        playFromLabel = new JLabel();
        playFromLabel.setEnabled(false);
        playFromLabel.setText("Play from:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(playFromLabel, gbc);
        browsePlayingFileButton = new JButton();
        browsePlayingFileButton.setEnabled(false);
        browsePlayingFileButton.setText("Browse");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(browsePlayingFileButton, gbc);
        playFileLabel = new JLabel();
        playFileLabel.setEnabled(false);
        playFileLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        playingPanel.add(playFileLabel, gbc);
        fontSize = new JTextField();
        fontSize.setColumns(2);
        fontSize.setEnabled(false);
        fontSize.setText("32");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(fontSize, gbc);
        showObjectIDsCheckBox = new JCheckBox();
        showObjectIDsCheckBox.setHorizontalAlignment(11);
        showObjectIDsCheckBox.setHorizontalTextPosition(10);
        showObjectIDsCheckBox.setText("Object IDs");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(showObjectIDsCheckBox, gbc);
        fontSizeLabel = new JLabel();
        fontSizeLabel.setEnabled(false);
        fontSizeLabel.setText("Font size:");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(fontSizeLabel, gbc);
        showTrailCheckBox = new JCheckBox();
        showTrailCheckBox.setHorizontalTextPosition(10);
        showTrailCheckBox.setText("Trail");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(showTrailCheckBox, gbc);
        trailSizeTextLabel = new JLabel();
        trailSizeTextLabel.setEnabled(false);
        trailSizeTextLabel.setText("Trail size:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        playingPanel.add(trailSizeTextLabel, gbc);
        trailSizeTextField = new JTextField();
        trailSizeTextField.setColumns(4);
        trailSizeTextField.setEnabled(false);
        trailSizeTextField.setText("500");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(trailSizeTextField, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(panel3, gbc);
        startButton = new JButton();
        startButton.setText("Start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        panel3.add(startButton, gbc);
        stopButton = new JButton();
        stopButton.setEnabled(false);
        stopButton.setText("Stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(stopButton, gbc);
        runningRadioButton = new JRadioButton();
        runningRadioButton.setSelected(true);
        runningRadioButton.setText("Run simulation");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel3.add(runningRadioButton, gbc);
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
