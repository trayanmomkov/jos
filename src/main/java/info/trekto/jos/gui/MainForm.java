package info.trekto.jos.gui;

import info.trekto.jos.core.ExecutionMode;
import info.trekto.jos.core.ForceCalculator;
import info.trekto.jos.core.GpuChecker;
import info.trekto.jos.core.numbers.NumberFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.ExecutionMode.AUTO;
import static info.trekto.jos.core.ExecutionMode.CPU;
import static info.trekto.jos.core.ExecutionMode.GPU;
import static info.trekto.jos.util.Utils.CORES;
import static info.trekto.jos.util.Utils.colorToString;
import static info.trekto.jos.util.Utils.invertColor;
import static info.trekto.jos.util.Utils.stringToColor;

public class MainForm {
    private static final String PLAYING_SPEED_TIP = "If x < 0: every iteration sleep x milliseconds; If x >= 0: visualize every x milliseconds";
    private String aboutMessage;
    private String minDistanceMessage;
    private String numberTypeMessage;
    private BufferedImage icon;

    private JButton browseButton;
    private JTextField numberOfIterationsTextField;
    private JTextField secondsPerIterationTextField;
    private JCheckBox saveToFileCheckBox;
    private JTextField outputFileTextField;
    private JCheckBox realTimeVisualizationCheckBox;
    private JTextField playingSpeedTextField;
    private JCheckBox bounceFromScreenBordersCheckBox;
    private JTextField numberOfObjectsTextField;
    private JTextField precisionTextField;
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
    private JLabel avgFileSize;
    private JComboBox executionModeComboBox;
    private JLabel executionModeLabel;
    private JButton detectCpuGpuThresholdButton;
    private JTextField cpuGpuThresholdField;
    private JLabel cpuGpuThresholdLabel;
    private JLabel cpuGpuThresholdLabel2;
    private JCheckBox saveMassCheckBox;
    private JCheckBox saveVelocityCheckBox;
    private JCheckBox saveAccelerationCheckBox;
    private JCheckBox mergeObjectsWhenCollideCheckBox;
    private JTextField corTextField;
    private JLabel corLabel;
    private JLabel minDistanceLabel;
    private JTextField minDistanceField;
    private JTextField scaleField;
    private JTextField backgroundColorField;
    private JCheckBox autoscrollCheckBox;
    private JCheckBox roundCheckBox;
    private ButtonGroup buttonGroup;
    private List<Component> runningComponents;
    private List<Component> playingComponents;
    private List<Component> savingToFileComponents;
    private String cpuGpuThresholdMessage;

    public void init() {
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

        {
            Vector<AbstractMap.SimpleEntry<ExecutionMode, String>> model = new Vector();
            model.addElement(new AbstractMap.SimpleEntry<>(AUTO, "AUTO - Dynamically switch. Best perf."));
            model.addElement(new AbstractMap.SimpleEntry<>(GPU, "GPU - Runs on video card if possible"));
            model.addElement(new AbstractMap.SimpleEntry<>(CPU, "CPU"));

            executionModeComboBox.setModel(new DefaultComboBoxModel<>(model));
            executionModeComboBox.setRenderer(new ExecutionModeRenderer());
        }

        cpuGpuThresholdField.setText(String.valueOf(CORES));

        numberTypeComboBox.addActionListener(C::numberTypeComboBoxEvent);
        interactingLawComboBox.addActionListener(actionEvent -> C.interactingLawComboBoxEvent());
        precisionTextField.getDocument().addUndoableEditListener(actionEvent -> C.precisionTextFieldEvent());
        realTimeVisualizationCheckBox.addActionListener(actionEvent -> C.realTimeVisualizationCheckBoxEvent());
        bounceFromScreenBordersCheckBox.addActionListener(actionEvent -> C.bounceFromScreenBordersCheckBoxEvent());
        playingSpeedTextField.getDocument().addUndoableEditListener(actionEvent -> C.playingSpeedTextFieldEvent());
        outputFileTextField.getDocument().addUndoableEditListener(actionEvent -> C.outputFileTextFieldEvent());
        cpuGpuThresholdField.getDocument().addUndoableEditListener(actionEvent -> C.cpuGpuThresholdFieldEvent());
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
                precisionTextField, executionModeLabel, executionModeComboBox, cpuGpuThresholdLabel, cpuGpuThresholdField,
                cpuGpuThresholdLabel2, detectCpuGpuThresholdButton, bounceFromScreenBordersCheckBox, mergeObjectsWhenCollideCheckBox,
                corLabel, corTextField);

        playingComponents = Arrays.asList(playFileLabel, playFromLabel, browsePlayingFileButton, playButton);

        savingToFileComponents = Arrays.asList(outputFileLabel, outputFileTextField, saveEveryNthIterationLabel1, saveEveryNthIterationTextField,
                                               saveEveryNthIterationLabel2, avgFileSize, saveMassCheckBox, saveVelocityCheckBox,
                                               saveAccelerationCheckBox);

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
        detectCpuGpuThresholdButton.addActionListener(actionEvent -> C.detectCpuGpuThresholdButtonEvent());
        saveMassCheckBox.addActionListener(actionEvent -> C.saveMassCheckBoxEvent());
        saveVelocityCheckBox.addActionListener(actionEvent -> C.saveVelocityCheckBoxEvent());
        saveAccelerationCheckBox.addActionListener(actionEvent -> C.saveAccelerationCheckBoxEvent());
        mergeObjectsWhenCollideCheckBox.addActionListener(actionEvent -> C.mergeObjectsWhenCollideCheckBoxEvent());
        executionModeComboBox.addActionListener(actionEvent -> {
            C.executionModeComboBoxEvent();
            roundNumberOfObjects();
        });

        saveEveryNthIterationTextField.getDocument().addUndoableEditListener(actionEvent -> C.saveEveryNthIterationTextFieldEvent());

        initialObjectsPanel.setSize(initialObjectsPanel.getWidth(), initialObjectsPanel.getHeight() * 2);

        aboutLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        minDistanceLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, minDistanceMessage, "Minimum distane", JOptionPane.INFORMATION_MESSAGE);
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

        cpuGpuThresholdLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(mainPanel, cpuGpuThresholdMessage, "CPU/GPU threshold", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        numberTypeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playingSpeedLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cpuGpuThresholdLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backgroundColorField.addActionListener(e -> {
            backgroundColorField.setBackground(stringToColor(backgroundColorField.getText()));
            backgroundColorField.setForeground(invertColor(stringToColor(backgroundColorField.getText())));
        });

        backgroundColorField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Color chosenColor = JColorChooser.showDialog(null, "Choose a color", backgroundColorField.getBackground());
                if (chosenColor != null) {
                    backgroundColorField.setBackground(chosenColor);
                    backgroundColorField.setText(colorToString(chosenColor));
                    backgroundColorField.setForeground(invertColor(chosenColor));

                }
            }
        });

        backgroundColorField.setBackground(stringToColor(backgroundColorField.getText()));
        autoscrollCheckBox.addActionListener(actionEvent -> {
            if (autoscrollCheckBox.isSelected()) {
                scroll();
            }
        });

        numberOfObjectsTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                roundNumberOfObjects();
            }
        });

        roundCheckBox.addActionListener(actionEvent -> roundNumberOfObjects());
    }

    private void roundNumberOfObjects() {
        if (roundCheckBox.isSelected()) {
            numberOfObjectsTextField.setText(String.valueOf(
                    GpuChecker.roundNumberOfObjects(Integer.parseInt(numberOfObjectsTextField.getText()))));
        }
    }

    public void scroll() {
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
    }

    /////////////////////////////////////////

    private static class ExecutionModeRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(((Map.Entry<ExecutionMode, String>) value).getValue());
            return this;
        }
    }


    public void setAboutMessage(String aboutMessage) {
        this.aboutMessage = aboutMessage;
    }

    public void setMinDistanceMessage(String message) {
        this.minDistanceMessage = message;
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

    public JCheckBox getBounceFromScreenBordersCheckBox() {
        return bounceFromScreenBordersCheckBox;
    }

    public JTextField getNumberOfObjectsTextField() {
        return numberOfObjectsTextField;
    }

    public JTextField getPrecisionTextField() {
        return precisionTextField;
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

    public JLabel getAvgFileSize() {
        return avgFileSize;
    }

    public JComboBox getExecutionModeComboBox() {
        return executionModeComboBox;
    }

    public JLabel getExecutionModeLabel() {
        return executionModeLabel;
    }

    public JButton getDetectCpuGpuThresholdButton() {
        return detectCpuGpuThresholdButton;
    }

    public JTextField getCpuGpuThresholdField() {
        return cpuGpuThresholdField;
    }

    public JLabel getCpuGpuThresholdLabel() {
        return cpuGpuThresholdLabel;
    }

    public JLabel getCpuGpuThresholdLabel2() {
        return cpuGpuThresholdLabel2;
    }

    public void setCpuGpuThresholdMessage(String cpuGpuThresholdMessage) {
        this.cpuGpuThresholdMessage = cpuGpuThresholdMessage;
    }

    public JCheckBox getSaveMassCheckBox() {
        return saveMassCheckBox;
    }

    public JCheckBox getSaveVelocityCheckBox() {
        return saveVelocityCheckBox;
    }

    public JCheckBox getSaveAccelerationCheckBox() {
        return saveAccelerationCheckBox;
    }

    public JCheckBox getMergeObjectsWhenCollideCheckBox() {
        return mergeObjectsWhenCollideCheckBox;
    }

    public JTextField getCorTextField() {
        return corTextField;
    }

    public JTextField getMinDistanceField() {
        return minDistanceField;
    }

    public JLabel getMinDistanceLabel() {
        return minDistanceLabel;
    }

    public JLabel getCorLabel() {
        return corLabel;
    }

    public JTextField getScaleField() {
        return scaleField;
    }

    public Color getBackgroundColor() {
        return backgroundColorField.getBackground();
    }

    public void setBackgroundColor(Color backgroundColor) {
        backgroundColorField.setBackground(backgroundColor);
        backgroundColorField.setText(colorToString(backgroundColor));
    }

    public JCheckBox getAutoscrollCheckBox() {
        return autoscrollCheckBox;
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
        panel1.setVisible(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
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
        numberOfIterationsTextField.setColumns(12);
        numberOfIterationsTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
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
        secondsPerIterationTextField.setColumns(12);
        secondsPerIterationTextField.setText("0.005");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(secondsPerIterationTextField, gbc);
        interactingLawLabel = new JLabel();
        interactingLawLabel.setText("Interacting law");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
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
        numberOfObjectsTextField.setColumns(12);
        numberOfObjectsTextField.setText("100");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(numberOfObjectsTextField, gbc);
        interactingLawComboBox = new JComboBox();
        interactingLawComboBox.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(interactingLawComboBox, gbc);
        realTimeVisualizationCheckBox = new JCheckBox();
        realTimeVisualizationCheckBox.setHorizontalTextPosition(10);
        realTimeVisualizationCheckBox.setSelected(true);
        realTimeVisualizationCheckBox.setText("Real time visualization");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel1.add(realTimeVisualizationCheckBox, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Background color");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        panel1.add(label1, gbc);
        backgroundColorField = new JTextField();
        backgroundColorField.setColumns(12);
        backgroundColorField.setEditable(false);
        backgroundColorField.setText("FFFFFF");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(backgroundColorField, gbc);
        roundCheckBox = new JCheckBox();
        roundCheckBox.setForeground(new Color(-16776961));
        roundCheckBox.setSelected(true);
        roundCheckBox.setText("Round");
        roundCheckBox.setToolTipText("Round to number of cores for better performance");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(roundCheckBox, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        simulationPropertiesPanel.add(panel2, gbc);
        generateObjectsButton = new JButton();
        generateObjectsButton.setText("Generate objects");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(generateObjectsButton, gbc);
        aboutLabel = new JLabel();
        aboutLabel.setForeground(new Color(-16776961));
        aboutLabel.setText("About");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel2.add(aboutLabel, gbc);
        numberTypeLabel = new JLabel();
        numberTypeLabel.setForeground(new Color(-16776961));
        numberTypeLabel.setText("Number type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel2.add(numberTypeLabel, gbc);
        numberTypeComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(numberTypeComboBox, gbc);
        executionModeLabel = new JLabel();
        executionModeLabel.setText("Execution mode");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel2.add(executionModeLabel, gbc);
        executionModeComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(executionModeComboBox, gbc);
        precisionLabel = new JLabel();
        precisionLabel.setEnabled(true);
        precisionLabel.setText("Precision");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel2.add(precisionLabel, gbc);
        precisionTextField = new JTextField();
        precisionTextField.setEnabled(true);
        precisionTextField.setText("16");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(precisionTextField, gbc);
        cpuGpuThresholdLabel = new JLabel();
        cpuGpuThresholdLabel.setForeground(new Color(-16772097));
        cpuGpuThresholdLabel.setText("CPU/GPU threshold");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 5);
        panel2.add(cpuGpuThresholdLabel, gbc);
        detectCpuGpuThresholdButton = new JButton();
        detectCpuGpuThresholdButton.setText("Detect threshold");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(detectCpuGpuThresholdButton, gbc);
        cpuGpuThresholdField = new JTextField();
        cpuGpuThresholdField.setColumns(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(cpuGpuThresholdField, gbc);
        cpuGpuThresholdLabel2 = new JLabel();
        cpuGpuThresholdLabel2.setText("objects");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(cpuGpuThresholdLabel2, gbc);
        corTextField = new JTextField();
        corTextField.setColumns(3);
        corTextField.setEnabled(false);
        corTextField.setText("0.5");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(corTextField, gbc);
        bounceFromScreenBordersCheckBox = new JCheckBox();
        bounceFromScreenBordersCheckBox.setEnabled(true);
        bounceFromScreenBordersCheckBox.setHideActionText(true);
        bounceFromScreenBordersCheckBox.setHorizontalAlignment(10);
        bounceFromScreenBordersCheckBox.setHorizontalTextPosition(10);
        bounceFromScreenBordersCheckBox.setSelected(false);
        bounceFromScreenBordersCheckBox.setText("Bounce from screen borders");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(bounceFromScreenBordersCheckBox, gbc);
        corLabel = new JLabel();
        corLabel.setEnabled(false);
        corLabel.setText("Coef. of restitution");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 7, 0, 0);
        panel2.add(corLabel, gbc);
        mergeObjectsWhenCollideCheckBox = new JCheckBox();
        mergeObjectsWhenCollideCheckBox.setHorizontalTextPosition(10);
        mergeObjectsWhenCollideCheckBox.setSelected(true);
        mergeObjectsWhenCollideCheckBox.setText("Merge on collision");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(mergeObjectsWhenCollideCheckBox, gbc);
        minDistanceField = new JTextField();
        minDistanceField.setColumns(3);
        minDistanceField.setEditable(true);
        minDistanceField.setEnabled(false);
        minDistanceField.setText("10");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(minDistanceField, gbc);
        minDistanceLabel = new JLabel();
        minDistanceLabel.setEnabled(false);
        minDistanceLabel.setForeground(new Color(-16776961));
        minDistanceLabel.setText("Min. dist.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 4, 0, 5);
        panel2.add(minDistanceLabel, gbc);
        saveToFileCheckBox = new JCheckBox();
        saveToFileCheckBox.setHorizontalTextPosition(10);
        saveToFileCheckBox.setText("Save to file");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(saveToFileCheckBox, gbc);
        saveEveryNthIterationLabel1 = new JLabel();
        saveEveryNthIterationLabel1.setEnabled(false);
        saveEveryNthIterationLabel1.setText("Save every");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        simulationPropertiesPanel.add(saveEveryNthIterationLabel1, gbc);
        browseButton = new JButton();
        browseButton.setHorizontalAlignment(0);
        browseButton.setText("Browse");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(browseButton, gbc);
        inputFilePathLabel = new JLabel();
        inputFilePathLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 0, 0);
        simulationPropertiesPanel.add(inputFilePathLabel, gbc);
        outputFileTextField = new JTextField();
        outputFileTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 2);
        simulationPropertiesPanel.add(outputFileTextField, gbc);
        saveEveryNthIterationTextField = new JTextField();
        saveEveryNthIterationTextField.setColumns(4);
        saveEveryNthIterationTextField.setEnabled(false);
        saveEveryNthIterationTextField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(saveEveryNthIterationTextField, gbc);
        saveMassCheckBox = new JCheckBox();
        saveMassCheckBox.setEnabled(false);
        saveMassCheckBox.setHorizontalTextPosition(10);
        saveMassCheckBox.setText("Save mass");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(saveMassCheckBox, gbc);
        saveVelocityCheckBox = new JCheckBox();
        saveVelocityCheckBox.setEnabled(false);
        saveVelocityCheckBox.setHorizontalTextPosition(10);
        saveVelocityCheckBox.setText("Save velocity");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        simulationPropertiesPanel.add(saveVelocityCheckBox, gbc);
        saveAccelerationCheckBox = new JCheckBox();
        saveAccelerationCheckBox.setEnabled(false);
        saveAccelerationCheckBox.setHorizontalTextPosition(10);
        saveAccelerationCheckBox.setText("Save acceleration");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        simulationPropertiesPanel.add(saveAccelerationCheckBox, gbc);
        outputFileLabel = new JLabel();
        outputFileLabel.setEnabled(false);
        outputFileLabel.setText("Output file");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 8, 0, 0);
        simulationPropertiesPanel.add(outputFileLabel, gbc);
        savePropertiesButton = new JButton();
        savePropertiesButton.setText("Save properties");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        simulationPropertiesPanel.add(savePropertiesButton, gbc);
        saveEveryNthIterationLabel2 = new JLabel();
        saveEveryNthIterationLabel2.setEnabled(false);
        saveEveryNthIterationLabel2.setText("-th iteration");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        simulationPropertiesPanel.add(saveEveryNthIterationLabel2, gbc);
        avgFileSize = new JLabel();
        avgFileSize.setEnabled(false);
        avgFileSize.setText("Average file size: ---");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        simulationPropertiesPanel.add(avgFileSize, gbc);
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
        consoleTextArea.setEditable(false);
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
        playingSpeedLabel.setText("Playing speed");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
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
        gbc.gridx = 11;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
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
        gbc.gridwidth = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        playingPanel.add(playFileLabel, gbc);
        fontSize = new JTextField();
        fontSize.setColumns(2);
        fontSize.setEnabled(false);
        fontSize.setText("18");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(fontSize, gbc);
        showObjectIDsCheckBox = new JCheckBox();
        showObjectIDsCheckBox.setHorizontalAlignment(11);
        showObjectIDsCheckBox.setHorizontalTextPosition(10);
        showObjectIDsCheckBox.setText("Object IDs");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(showObjectIDsCheckBox, gbc);
        fontSizeLabel = new JLabel();
        fontSizeLabel.setEnabled(false);
        fontSizeLabel.setText("Font size:");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 0);
        playingPanel.add(fontSizeLabel, gbc);
        showTrailCheckBox = new JCheckBox();
        showTrailCheckBox.setHorizontalTextPosition(10);
        showTrailCheckBox.setText("Trail");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(showTrailCheckBox, gbc);
        trailSizeTextLabel = new JLabel();
        trailSizeTextLabel.setEnabled(false);
        trailSizeTextLabel.setText("Trail size:");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        playingPanel.add(trailSizeTextLabel, gbc);
        trailSizeTextField = new JTextField();
        trailSizeTextField.setColumns(4);
        trailSizeTextField.setEnabled(false);
        trailSizeTextField.setText("500");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
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
        final JLabel label2 = new JLabel();
        label2.setText("Scale");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 10.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 4);
        playingPanel.add(label2, gbc);
        scaleField = new JTextField();
        scaleField.setColumns(4);
        scaleField.setHorizontalAlignment(10);
        scaleField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        playingPanel.add(scaleField, gbc);
        autoscrollCheckBox = new JCheckBox();
        autoscrollCheckBox.setHorizontalTextPosition(10);
        autoscrollCheckBox.setSelected(true);
        autoscrollCheckBox.setText("Autoscroll");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        playingPanel.add(autoscrollCheckBox, gbc);
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
        numberTypeLabel.setLabelFor(numberTypeComboBox);
        executionModeLabel.setLabelFor(executionModeComboBox);
        cpuGpuThresholdLabel.setLabelFor(cpuGpuThresholdField);
        cpuGpuThresholdLabel2.setLabelFor(cpuGpuThresholdField);
        corLabel.setLabelFor(corTextField);
        minDistanceLabel.setLabelFor(cpuGpuThresholdField);
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
