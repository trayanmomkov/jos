package info.trekto.jos.gui;

import info.trekto.jos.core.ForceCalculator;
import info.trekto.jos.core.numbers.NumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static info.trekto.jos.core.Controller.C;

public class MainForm {
    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);
    private static final String PLAYING_SPEED_TIP = "If x < 0: every iteration sleep x milliseconds; If x >= 0: visualize every x milliseconds";
    private String aboutMessage;
    private String numberTypeMessage;
    private BufferedImage icon;

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
    private List<Component> runningComponents;
    private List<Component> playingComponents;
    private List<Component> savingToFileComponents;

    public void init() {
        scaleTextField.addActionListener(actionEvent -> C.scaleTextFieldEvent());
        precisionTextField.addActionListener(actionEvent -> C.precisionTextFieldEvent());
        initialObjectsTable.setModel(new InitialObjectsTableModelAndListener());
        browseButton.addActionListener(actionEvent -> C.browseButtonEvent());
        savePropertiesButton.addActionListener(actionEvent -> C.savePropertiesButtonEvent());
        numberOfObjectsTextField.getDocument().addUndoableEditListener(actionEvent -> C.numberOfObjectsTextFieldEvent());
        numberOfIterationsTextField.getDocument().addUndoableEditListener(actionEvent -> C.numberOfIterationsTextFieldEvent());
        secondsPerIterationTextField.getDocument().addUndoableEditListener(actionEvent -> C.secondsPerIterationTextFieldEvent());

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

        numberTypeComboBox.addActionListener(C::numberTypeComboBoxEvent);
        interactingLawComboBox.addActionListener(actionEvent -> C.interactingLawComboBoxEvent());
        precisionTextField.getDocument().addUndoableEditListener(actionEvent -> C.precisionTextFieldEvent());
        scaleTextField.getDocument().addUndoableEditListener(actionEvent -> C.scaleTextFieldEvent());
        realTimeVisualizationCheckBox.addActionListener(actionEvent -> C.realTimeVisualizationCheckBoxEvent());
        bounceFromScreenWallsCheckBox.addActionListener(actionEvent -> C.bounceFromScreenWallsCheckBoxEvent());
        playingSpeedTextField.getDocument().addUndoableEditListener(actionEvent -> C.playingSpeedTextFieldEvent());
        outputFileTextField.getDocument().addUndoableEditListener(actionEvent -> C.outputFileTextFieldEvent());
        startButton.addActionListener(actionEvent -> C.start());
        stopButton.addActionListener(actionEvent -> C.stopButtonEvent());
        pauseButton.addActionListener(actionEvent -> C.pauseButtonEvent());

        buttonGroup = new ButtonGroup();
        buttonGroup.add(runningRadioButton);
        buttonGroup.add(playRadioButton);

        runningRadioButton.addActionListener(actionEvent -> C.enableRunning(true));
        playRadioButton.addActionListener(actionEvent -> C.enableRunning(false));

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

        saveToFileCheckBox.addActionListener(actionEvent -> C.saveToFileCheckBoxEvent());
        generateObjectsButton.addActionListener(actionEvent -> C.generateObjectButtonEvent());
        browsePlayingFileButton.addActionListener(actionEvent -> C.browsePlayingFileButtonEvent());
        playingSpeedLabel.setLabelFor(playingSpeedTextField);
        playButton.addActionListener(actionEvent -> C.play());
        showObjectIDsCheckBox.addActionListener(actionEvent -> C.showObjectIDsCheckBoxEvent());
        showTrailCheckBox.addActionListener(actionEvent -> C.showTrailCheckBoxEvent());
        outputFileTextField.addActionListener(actionEvent -> C.outputFileTextFieldEvent());
        numberOfObjectsTextField.addActionListener(actionEvent -> C.numberOfObjectsTextFieldEvent());
        numberOfIterationsTextField.addActionListener(actionEvent -> C.numberOfIterationsTextFieldEvent());
        secondsPerIterationTextField.addActionListener(actionEvent -> C.secondsPerIterationTextFieldEvent());
        initialObjectsPanel.setSize(initialObjectsPanel.getWidth(), initialObjectsPanel.getHeight() * 2);

        aboutLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        playingSpeedLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, PLAYING_SPEED_TIP, "Playing speed", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        numberTypeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, numberTypeMessage, "Number type", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        numberTypeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playingSpeedLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /////////////////////////////////////////


    public void setAboutMessage(String aboutMessage) {
        this.aboutMessage = aboutMessage;
    }

    public void setIcon(BufferedImage icon) {
        this.icon = icon;
    }

    public String getAboutMessage() {
        return aboutMessage;
    }

    public BufferedImage getIcon() {
        return icon;
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public JTextField getNumberOfIterationsTextField() {
        return numberOfIterationsTextField;
    }

    public JTextField getSecondsPerIterationTextField() {
        return secondsPerIterationTextField;
    }

    public JCheckBox getSaveToFileCheckBox() {
        return saveToFileCheckBox;
    }

    public JTextField getOutputFileTextField() {
        return outputFileTextField;
    }

    public JCheckBox getRealTimeVisualizationCheckBox() {
        return realTimeVisualizationCheckBox;
    }

    public JTextField getPlayingSpeedTextField() {
        return playingSpeedTextField;
    }

    public JCheckBox getBounceFromScreenWallsCheckBox() {
        return bounceFromScreenWallsCheckBox;
    }

    public JTextField getNumberOfObjectsTextField() {
        return numberOfObjectsTextField;
    }

    public JTextField getPrecisionTextField() {
        return precisionTextField;
    }

    public JTextField getScaleTextField() {
        return scaleTextField;
    }

    public JTable getInitialObjectsTable() {
        return initialObjectsTable;
    }

    public JTextArea getConsoleTextArea() {
        return consoleTextArea;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JButton getSavePropertiesButton() {
        return savePropertiesButton;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JLabel getInputFilePathLabel() {
        return inputFilePathLabel;
    }

    public JRadioButton getRunningRadioButton() {
        return runningRadioButton;
    }

    public JRadioButton getPlayRadioButton() {
        return playRadioButton;
    }

    public JButton getBrowsePlayingFileButton() {
        return browsePlayingFileButton;
    }

    public JLabel getPlayFileLabel() {
        return playFileLabel;
    }

    public JPanel getSimulationPropertiesPanel() {
        return simulationPropertiesPanel;
    }

    public JLabel getNumberOfObjectsLabel() {
        return numberOfObjectsLabel;
    }

    public JLabel getSecondsPerIterationLabel() {
        return secondsPerIterationLabel;
    }

    public JLabel getNumberTypeLabel() {
        return numberTypeLabel;
    }

    public JLabel getInteractingLawLabel() {
        return interactingLawLabel;
    }

    public JLabel getOutputFileLabel() {
        return outputFileLabel;
    }

    public JLabel getPrecisionLabel() {
        return precisionLabel;
    }

    public JLabel getScaleLabel() {
        return scaleLabel;
    }

    public JLabel getNumberOfIterationsLabel() {
        return numberOfIterationsLabel;
    }

    public JLabel getPlayFromLabel() {
        return playFromLabel;
    }

    public JLabel getPlayingSpeedLabel() {
        return playingSpeedLabel;
    }

    public JButton getPlayButton() {
        return playButton;
    }

    public JScrollPane getInitialObjectsPanel() {
        return initialObjectsPanel;
    }

    public JButton getGenerateObjectsButton() {
        return generateObjectsButton;
    }

    public JPanel getPlayingPanel() {
        return playingPanel;
    }

    public JPanel getBrowsePropertiesPanel() {
        return browsePropertiesPanel;
    }

    public JTextField getFontSize() {
        return fontSize;
    }

    public JCheckBox getShowObjectIDsCheckBox() {
        return showObjectIDsCheckBox;
    }

    public JCheckBox getShowTrailCheckBox() {
        return showTrailCheckBox;
    }

    public JTextField getTrailSizeTextField() {
        return trailSizeTextField;
    }

    public JLabel getFontSizeLabel() {
        return fontSizeLabel;
    }

    public JLabel getTrailSizeTextLabel() {
        return trailSizeTextLabel;
    }

    public JScrollPane getConsolePanel() {
        return consolePanel;
    }

    public JLabel getAboutLabel() {
        return aboutLabel;
    }

    public JComboBox getNumberTypeComboBox() {
        return numberTypeComboBox;
    }

    public JComboBox getInteractingLawComboBox() {
        return interactingLawComboBox;
    }

    public JCheckBox getShowTimeAndIterationCheckBox() {
        return showTimeAndIterationCheckBox;
    }

    public JTextField getSaveEveryNthIterationTextField() {
        return saveEveryNthIterationTextField;
    }

    public JLabel getSaveEveryNthIterationLabel1() {
        return saveEveryNthIterationLabel1;
    }

    public JLabel getSaveEveryNthIterationLabel2() {
        return saveEveryNthIterationLabel2;
    }

    public JButton getPauseButton() {
        return pauseButton;
    }

    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    public List<Component> getRunningComponents() {
        return runningComponents;
    }

    public List<Component> getPlayingComponents() {
        return playingComponents;
    }

    public List<Component> getSavingToFileComponents() {
        return savingToFileComponents;
    }

    public String getNumberTypeMessage() {
        return numberTypeMessage;
    }

    public void setNumberTypeMessage(String numberTypeMessage) {
        this.numberTypeMessage = numberTypeMessage;
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
        numberTypeLabel.setForeground(new Color(-16776961));
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
