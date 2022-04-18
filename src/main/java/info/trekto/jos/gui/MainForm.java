package info.trekto.jos.gui;

import info.trekto.jos.core.C;
import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.formulas.ForceCalculator;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.NumberFactory;
import info.trekto.jos.core.SimulationGenerator;
import info.trekto.jos.gui.java2dgraphics.VisualizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.isNullOrBlank;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainForm {
    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);
    public static final String PROGRAM_NAME = "JOS - arbitrary precision version";
    private static final String PLAYING_SPEED_TIP = "If x < 0: every iteration sleep x milliseconds; If x >= 0: visualize every x milliseconds";
    private static String ABOUT_MESSAGE;
    public static BufferedImage icon;
    public static Properties properties;

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
    private JLabel aboutLabel;
    private JComboBox numberTypeComboBox;
    private JComboBox interactingLawComboBox;
    private JCheckBox showTimeAndIterationCheckBox;
    private JTextField saveEveryNthIterationTextField;
    private JLabel saveEveryNthIterationLabel1;
    private JLabel saveEveryNthIterationLabel2;
    private JButton pauseButton;
    private ButtonGroup buttonGroup;
    private final List<Component> runningComponents;
    private final List<Component> playingComponents;
    private final List<Component> savingToFileComponents;
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
                C.simulation = new SimulationForkJoinImpl();
                C.simulationLogic = new SimulationLogicImpl();
                C.simulation.init(file.getAbsolutePath());
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
                C.simulation.init(fileToSave.getAbsolutePath());
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
                C.prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
            }
        });

        {
            Vector<NumberFactory.NumberType> comboBoxItems = new Vector<>();
            Collections.addAll(comboBoxItems, NumberFactory.NumberType.values());
            numberTypeComboBox.setModel(new DefaultComboBoxModel<>(comboBoxItems));
        }

        {
            Vector<ForceCalculator.InteractingLaw> comboBoxItems = new Vector<>();
            Collections.addAll(comboBoxItems, ForceCalculator.InteractingLaw.values());
            interactingLawComboBox.setModel(new DefaultComboBoxModel<>(comboBoxItems));
        }

        numberTypeComboBox.addActionListener(
                actionEvent -> {
                    if (actionEvent.getModifiers() != 0) {
                        C.prop.setNumberType(NumberFactory.NumberType.valueOf(String.valueOf(numberTypeComboBox.getSelectedItem())));
//                    createNumberFactory(C.prop.getNumberType(), C.prop.getPrecision(), C.prop.getScale());
//                    C.prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
//                    ((InitialObjectsTableModelAndListener) initialObjectsTable.getModel()).refreshInitialObjects();
                        showWarn(mainPanel, "You have to save properties, number type change to take effect.");
                    }
                });

        interactingLawComboBox.addActionListener(
                actionEvent -> C.prop.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(String.valueOf(interactingLawComboBox.getSelectedItem()))));

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
            if (C.simulation != null) {
                C.simulation.setPaused(false);
                if (C.simulation.isRunning()) {
                    C.hasToStop = true;
                }
            }
        });

        pauseButton.addActionListener(actionEvent -> {
            if (C.simulation != null) {
                switchPause();
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
        appendMessage("\tSwitch trails: t");

        buttonGroup = new ButtonGroup();
        buttonGroup.add(runningRadioButton);
        buttonGroup.add(playRadioButton);

        runningRadioButton.addActionListener(actionEvent -> enableRunning(true));
        playRadioButton.addActionListener(actionEvent -> enableRunning(false));

        runningComponents = Arrays.asList(
                numberOfIterationsTextField, secondsPerIterationTextField, browseButton, saveToFileCheckBox, outputFileTextField,
                realTimeVisualizationCheckBox, numberOfObjectsTextField, initialObjectsTable, numberOfIterationsLabel,
                startButton, savePropertiesButton, inputFilePathLabel, simulationPropertiesPanel, numberOfObjectsLabel,
                secondsPerIterationLabel, numberTypeLabel, interactingLawLabel, outputFileLabel, precisionLabel,
                initialObjectsTable, initialObjectsPanel, generateObjectsButton, numberTypeComboBox, precisionTextField,
                precisionTextField, scaleLabel, scaleTextField);

        playingComponents = Arrays.asList(playFileLabel, playFromLabel, browsePlayingFileButton, playButton);

        savingToFileComponents = Arrays.asList(outputFileLabel, outputFileTextField, saveEveryNthIterationLabel1, saveEveryNthIterationTextField,
                                               saveEveryNthIterationLabel2);

        saveToFileCheckBox.addActionListener(actionEvent -> {
            savingToFileComponents.forEach(c -> c.setEnabled(saveToFileCheckBox.isSelected()));
            C.prop.setSaveToFile(saveToFileCheckBox.isSelected());
        });

        generateObjectsButton.addActionListener(actionEvent -> {
            createNumberFactory(
                    NumberFactory.NumberType.valueOf(numberTypeComboBox.getSelectedItem().toString()),
                    Integer.parseInt(precisionTextField.getText()), Integer.parseInt(scaleTextField.getText()));
            SimulationProperties prop = new SimulationProperties();
            if (!isNullOrBlank(numberOfObjectsTextField.getText())) {
                prop.setNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()));
            }

            if (!isNullOrBlank(numberOfIterationsTextField.getText())) {
                prop.setNumberOfIterations(Integer.parseInt(numberOfIterationsTextField.getText()));
            }

            if (!isNullOrBlank(secondsPerIterationTextField.getText())) {
                prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
            }
            prop.setRealTimeVisualization(realTimeVisualizationCheckBox.isSelected());
            prop.setSaveToFile(saveToFileCheckBox.isSelected());
            prop.setNumberType(NumberFactory.NumberType.valueOf(numberTypeComboBox.getSelectedItem().toString()));
            prop.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(interactingLawComboBox.getSelectedItem().toString()));
            prop.setScale(Integer.parseInt(scaleTextField.getText()));
            prop.setPrecision(Integer.parseInt(precisionTextField.getText()));

            C.simulation = new SimulationForkJoinImpl();
            C.simulationLogic = new SimulationLogicImpl();

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
                    C.simulation = new SimulationForkJoinImpl();
                    C.simulationLogic = new SimulationLogicImpl();
                    C.simulation.initForPlaying(playFile.getAbsolutePath());
                    refreshProperties(C.prop);
                } catch (IOException e) {
                    showError(mainPanel, "Cannot read ZIP file.", e);
                }
            }
        });

        playingSpeedLabel.setLabelFor(playingSpeedTextField);

        playButton.addActionListener(actionEvent -> play());

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
                C.prop.setSecondsPerIteration(New.num(secondsPerIterationTextField.getText()));
            }
        });

        initialObjectsPanel.setSize(initialObjectsPanel.getWidth(), initialObjectsPanel.getHeight() * 2);

        aboutLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, ABOUT_MESSAGE, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        playingSpeedLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, PLAYING_SPEED_TIP, "Playing speed", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playingSpeedLabel.setToolTipText(PLAYING_SPEED_TIP);
        playingSpeedTextField.setToolTipText(PLAYING_SPEED_TIP);
    }

    public void switchPause() {
        C.simulation.setPaused(!C.simulation.isPaused());
        pauseButton.setText(C.simulation.isPaused() ? "Unpause" : "Pause");
    }

    private void enableRunning(boolean enable) {
        runningComponents.forEach(c -> c.setEnabled(enable));
        playingComponents.forEach(c -> c.setEnabled(!enable));
        savingToFileComponents.forEach(c -> c.setEnabled(enable && saveToFileCheckBox.isSelected()));
    }

    private void play() {
        C.simulation.setPaused(false);
        new Thread(() -> {
            try {
                if (C.simulation != null && playFile != null) {
                    C.hasToStop = false;
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
        playingComponents.forEach(c -> c.setEnabled(false));
        stopButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }

    private void start() {
        C.simulation.setPaused(false);
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
            savingToFileComponents.forEach(c -> c.setEnabled(false));
            runningRadioButton.setEnabled(false);
            playRadioButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
        }
    }

    public void refreshProperties(SimulationProperties prop) {
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
        properties = new Properties();
        JFrame jFrame = new JFrame();

        try {
            properties.load(MainForm.class.getClassLoader().getResourceAsStream("application.properties"));
            icon = ImageIO.read(MainForm.class.getClassLoader().getResource("jos-icon.png"));
            jFrame.setIconImage(icon);
        } catch (Exception e) {
            logger.error("Cannot load properties and/or icon image.", e);
        }

        if (isNullOrBlank(properties.getProperty("version"))) {
            properties.setProperty("version", "Unknown");
        }

        ABOUT_MESSAGE = "JOS\n\nv. " + properties.getProperty("version") + "\narbitrary precision\n\nAuthor: Trayan Momkov\n2022";

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
        C.simulation.setPaused(false);
        if (runningRadioButton.isSelected()) {
            startButton.setEnabled(true);
        }
        if (runningRadioButton.isSelected()) {
            runningComponents.forEach(c -> c.setEnabled(true));
            savingToFileComponents.forEach(c -> c.setEnabled(saveToFileCheckBox.isSelected()));
        } else {
            playingComponents.forEach(c -> c.setEnabled(true));
        }
        runningRadioButton.setEnabled(true);
        playRadioButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
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

    public void setShowTrail(boolean selected) {
        showTrailCheckBox.setSelected(selected);
    }

    public int getTrailSize() {
        if (!isNullOrBlank(trailSizeTextField.getText().replace("-", ""))) {
            return Integer.parseInt(trailSizeTextField.getText());
        } else {
            return 500;
        }
    }

    public boolean getShowTimeAndIteration() {
        return showTimeAndIterationCheckBox.isSelected();
    }

    public int getSaveEveryNthIteration() {
        return Integer.parseInt(saveEveryNthIterationTextField.getText());
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
        gbc.gridwidth = 2;
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
        numberTypeComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(numberTypeComboBox, gbc);
        interactingLawComboBox = new JComboBox();
        interactingLawComboBox.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(interactingLawComboBox, gbc);
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
        precisionLabel.setEnabled(true);
        precisionLabel.setText("Precision");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(precisionLabel, gbc);
        precisionTextField = new JTextField();
        precisionTextField.setEnabled(true);
        precisionTextField.setText("16");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(precisionTextField, gbc);
        scaleLabel = new JLabel();
        scaleLabel.setEnabled(true);
        scaleLabel.setText("Scale");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel2.add(scaleLabel, gbc);
        scaleTextField = new JTextField();
        scaleTextField.setEnabled(true);
        scaleTextField.setText("16");
        scaleTextField.setToolTipText("Scale is used by BigDecimal. ApFloat uses precision only.");
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
        aboutLabel = new JLabel();
        aboutLabel.setForeground(new Color(-16776961));
        aboutLabel.setText("About");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(aboutLabel, gbc);
        saveToFileCheckBox = new JCheckBox();
        saveToFileCheckBox.setHorizontalTextPosition(10);
        saveToFileCheckBox.setText("Save to file");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        simulationPropertiesPanel.add(saveToFileCheckBox, gbc);
        browsePropertiesPanel = new JPanel();
        browsePropertiesPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
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
        outputFileLabel = new JLabel();
        outputFileLabel.setEnabled(false);
        outputFileLabel.setText("Output file");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(outputFileLabel, gbc);
        outputFileTextField = new JTextField();
        outputFileTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        simulationPropertiesPanel.add(outputFileTextField, gbc);
        saveEveryNthIterationLabel1 = new JLabel();
        saveEveryNthIterationLabel1.setEnabled(false);
        saveEveryNthIterationLabel1.setText("Save every");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 1);
        simulationPropertiesPanel.add(saveEveryNthIterationLabel1, gbc);
        saveEveryNthIterationTextField = new JTextField();
        saveEveryNthIterationTextField.setColumns(4);
        saveEveryNthIterationTextField.setEnabled(false);
        saveEveryNthIterationTextField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        simulationPropertiesPanel.add(saveEveryNthIterationTextField, gbc);
        saveEveryNthIterationLabel2 = new JLabel();
        saveEveryNthIterationLabel2.setEnabled(false);
        saveEveryNthIterationLabel2.setText("-th iteration");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(saveEveryNthIterationLabel2, gbc);
        consolePanel = new JScrollPane();
        consolePanel.setToolTipText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
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
        gbc.gridwidth = 2;
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
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 0, 0);
        mainPanel.add(playingPanel, gbc);
        playingSpeedLabel = new JLabel();
        playingSpeedLabel.setEnabled(true);
        playingSpeedLabel.setForeground(new Color(-16776961));
        playingSpeedLabel.setText("Playing speed?");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(playingSpeedLabel, gbc);
        playingSpeedTextField = new JTextField();
        playingSpeedTextField.setColumns(3);
        playingSpeedTextField.setEnabled(true);
        playingSpeedTextField.setText("0");
        playingSpeedTextField.setToolTipText("");
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
        playFileLabel = new JLabel();
        playFileLabel.setEnabled(false);
        playFileLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 9;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        playingPanel.add(playFileLabel, gbc);
        fontSize = new JTextField();
        fontSize.setColumns(2);
        fontSize.setEnabled(false);
        fontSize.setText("24");
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
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        playingPanel.add(panel3, gbc);
        playRadioButton = new JRadioButton();
        playRadioButton.setText("Play simulation");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(playRadioButton, gbc);
        playButton = new JButton();
        playButton.setEnabled(false);
        playButton.setText("Play ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        panel3.add(playButton, gbc);
        browsePlayingFileButton = new JButton();
        browsePlayingFileButton.setEnabled(false);
        browsePlayingFileButton.setText("Browse");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(browsePlayingFileButton, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(panel4, gbc);
        startButton = new JButton();
        startButton.setText("Start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        panel4.add(startButton, gbc);
        stopButton = new JButton();
        stopButton.setEnabled(false);
        stopButton.setText("Stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(stopButton, gbc);
        runningRadioButton = new JRadioButton();
        runningRadioButton.setSelected(true);
        runningRadioButton.setText("Run simulation");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel4.add(runningRadioButton, gbc);
        pauseButton = new JButton();
        pauseButton.setEnabled(false);
        pauseButton.setText("Pause");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel4.add(pauseButton, gbc);
        showTimeAndIterationCheckBox = new JCheckBox();
        showTimeAndIterationCheckBox.setHorizontalAlignment(10);
        showTimeAndIterationCheckBox.setHorizontalTextPosition(10);
        showTimeAndIterationCheckBox.setSelected(true);
        showTimeAndIterationCheckBox.setText("Show time and iteration");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(showTimeAndIterationCheckBox, gbc);
        outputFileLabel.setLabelFor(outputFileTextField);
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
