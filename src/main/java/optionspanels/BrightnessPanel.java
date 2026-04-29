package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class BrightnessPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private int[][][] originalMatrix;
    private JSlider brightnessSlider;

    public BrightnessPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        this.originalMatrix = photoPanel.getImageMatrix();
        parentPanel.saveUndoState(originalMatrix);

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Change brightness:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        brightnessSlider = new JSlider(JSlider.HORIZONTAL, -255, 255, 0);
        brightnessSlider.setMajorTickSpacing(85);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, brightnessSlider.getPreferredSize().height));

        // Live preview for offset
        brightnessSlider.addChangeListener(e -> {
            int offset = brightnessSlider.getValue();
            int[][][] newMatrix = ImageProcessor.applyBrightnessOffset(originalMatrix, offset);
            photoPanel.setImageMatrix(newMatrix);
            parentPanel.updateHistogram();
        });

        JLabel rangeLabel = new JLabel("Extend brightness range to (N1, N2):");
        rangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel rangeInputsPanel = new JPanel();
        rangeInputsPanel.setLayout(new BoxLayout(rangeInputsPanel, BoxLayout.X_AXIS));
        rangeInputsPanel.setMaximumSize(new Dimension(300, 30));

        JSpinner n1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        JSpinner n2Spinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));

        rangeInputsPanel.add(new JLabel("N1: "));
        rangeInputsPanel.add(n1Spinner);
        rangeInputsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        rangeInputsPanel.add(new JLabel("N2: "));
        rangeInputsPanel.add(n2Spinner);

        JButton applyRangeBtn = new JButton("Apply Range");
        applyRangeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyRangeBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int n1 = (int) n1Spinner.getValue();
            int n2 = (int) n2Spinner.getValue();

            int[][][] newMatrix = ImageProcessor.applyBrightnessRange(photoPanel.getImageMatrix(), n1, n2);
            if (newMatrix != null) {
                photoPanel.setImageMatrix(newMatrix);
                originalMatrix = newMatrix;
                brightnessSlider.setValue(0);
            }

            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(10));
        this.add(brightnessSlider);
        this.add(Box.createVerticalStrut(40));
        this.add(rangeLabel);
        this.add(Box.createVerticalStrut(10));
        this.add(rangeInputsPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(applyRangeBtn);
        this.add(Box.createVerticalGlue());
    }
}