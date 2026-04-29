package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class ContrastPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private int[][][] originalMatrix;

    public ContrastPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        this.originalMatrix = photoPanel.getImageMatrix();
        parentPanel.saveUndoState(originalMatrix);

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Gamma correction:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel valueLabel = new JLabel("Gamma factor: 1.0");
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JSlider gammaSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
        gammaSlider.setMajorTickSpacing(10);
        gammaSlider.setPaintTicks(true);
        gammaSlider.setPaintLabels(true);

        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        for (int i = 0; i <= 50; i += 10) labelTable.put(i, new JLabel(String.valueOf(i / 10)));
        gammaSlider.setLabelTable(labelTable);
        gammaSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, gammaSlider.getPreferredSize().height));

        // Live preview for Gamma
        gammaSlider.addChangeListener(e -> {
            double gammaValue = gammaSlider.getValue() / 10.0;
            valueLabel.setText(String.format("Gamma factor: %.1f", gammaValue));
            int[][][] newMatrix = ImageProcessor.applyContrastPower(originalMatrix, gammaValue);
            if (newMatrix != null) photoPanel.setImageMatrix(newMatrix);
            parentPanel.updateHistogram();
        });

        JButton applyGammaBtn = new JButton("Apply Gamma");
        applyGammaBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyGammaBtn.addActionListener(e -> {
            originalMatrix = photoPanel.getImageMatrix();
            gammaSlider.setValue(10);
            parentPanel.updateHistogram();
        });

        JLabel logLabel = new JLabel("Log correction:");
        logLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton applyLogBtn = new JButton("Apply Log");
        applyLogBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyLogBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyContrastLog(photoPanel.getImageMatrix());
            if (newMatrix != null) {
                photoPanel.setImageMatrix(newMatrix);
                originalMatrix = newMatrix;
                gammaSlider.setValue(10);
                parentPanel.updateHistogram();
            }
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(15));
        this.add(valueLabel);
        this.add(Box.createVerticalStrut(10));
        this.add(gammaSlider);
        this.add(Box.createVerticalStrut(10));
        this.add(applyGammaBtn);
        this.add(Box.createVerticalStrut(40));
        this.add(logLabel);
        this.add(Box.createVerticalStrut(10));
        this.add(applyLogBtn);
        this.add(Box.createVerticalGlue());
    }
}