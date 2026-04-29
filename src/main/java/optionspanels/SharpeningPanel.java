package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class SharpeningPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;

    private JComboBox<String> algorithmBox;
    private JPanel typePanel;
    private JPanel logPanel;
    private JPanel sigmaPanel;
    private JCheckBox logBox;

    public SharpeningPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Sharpening Filters:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // algorithm selection
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algoPanel.setMaximumSize(new Dimension(350, 40));
        algorithmBox = new JComboBox<>(new String[]{"Laplacian", "Unsharp Masking"});
        algoPanel.add(new JLabel("Algorithm: "));
        algoPanel.add(algorithmBox);

        // laplacian type
        typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setMaximumSize(new Dimension(350, 40));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Standard", "Strong"});
        typePanel.add(new JLabel("Laplacian Type: "));
        typePanel.add(typeBox);

        // LoG checkbox
        logPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logPanel.setMaximumSize(new Dimension(350, 40));
        logBox = new JCheckBox("Use Laplacian of Gaussian (LoG)");
        logPanel.add(logBox);

        // sigma for gaussian blur
        sigmaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sigmaPanel.setMaximumSize(new Dimension(350, 40));
        JSpinner sigmaSpinner = new JSpinner(new SpinnerNumberModel(1.4, 0.1, 10.0, 0.1));
        sigmaPanel.add(new JLabel("Blur Sigma (\u03C3): "));
        sigmaPanel.add(sigmaSpinner);

        // strength slider
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        strengthPanel.setMaximumSize(new Dimension(350, 50));
        JSlider strengthSlider = new JSlider(0, 100, 50); // 0.0 to 1.0
        JLabel strengthVal = new JLabel("0.50");
        strengthSlider.addChangeListener(e -> strengthVal.setText(String.format("%.2f", strengthSlider.getValue() / 100.0)));
        strengthPanel.add(new JLabel("Strength: "));
        strengthPanel.add(strengthSlider);
        strengthPanel.add(strengthVal);

        // edge thresholding
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.setMaximumSize(new Dimension(350, 40));
        JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        thresholdPanel.add(new JLabel("Edge Threshold: "));
        thresholdPanel.add(thresholdSpinner);

        // options are only visible when their parent option is chosen
        algorithmBox.addActionListener(e -> updateVisibility());
        logBox.addActionListener(e -> updateVisibility());
        updateVisibility();

        // apply button
        JButton applyBtn = new JButton("Apply Sharpening");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());

            int[][][] newMatrix = ImageProcessor.applyAdvancedSharpening(
                    photoPanel.getImageMatrix(),
                    (String) algorithmBox.getSelectedItem(),
                    (String) typeBox.getSelectedItem(),
                    logBox.isSelected(),
                    (Double) sigmaSpinner.getValue(),
                    strengthSlider.getValue() / 100.0,
                    (Integer) thresholdSpinner.getValue(),
                    parentPanel.getBoundaryMode()
            );
            photoPanel.setImageMatrix(newMatrix);

            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(15));
        this.add(algoPanel);
        this.add(typePanel);
        this.add(logPanel);
        this.add(sigmaPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(strengthPanel);
        this.add(thresholdPanel);
        this.add(Box.createVerticalStrut(20));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }

    private void updateVisibility() {
        boolean isLaplacian = algorithmBox.getSelectedItem().equals("Laplacian");
        boolean useLoG = logBox.isSelected();

        typePanel.setVisible(isLaplacian);
        logPanel.setVisible(isLaplacian);
        sigmaPanel.setVisible(!isLaplacian || useLoG);

        this.revalidate();
        this.repaint();

        parentPanel.updateHistogram();
    }
}