package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class BlurringPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;

    public BlurringPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Blurring Filters:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel typePanel = new JPanel();
        typePanel.setMaximumSize(new Dimension(300, 40));
        String[] blurTypes = {"Box Blur", "Gaussian Blur"};
        JComboBox<String> typeComboBox = new JComboBox<>(blurTypes);
        typePanel.add(new JLabel("Type: "));
        typePanel.add(typeComboBox);

        JPanel cardsPanel = new JPanel(new CardLayout());
        cardsPanel.setMaximumSize(new Dimension(300, 80));

        // box blur panel
        JPanel boxPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(3, 3, 21, 2));
        JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        boxPanel.add(new JLabel("Mask Size (NxN): "));
        boxPanel.add(sizeSpinner);
        boxPanel.add(new JLabel("Center Weight: "));
        boxPanel.add(weightSpinner);

        // gaussian blur panel
        JPanel gaussianPanel = new JPanel();
        JSpinner sigmaSpinner = new JSpinner(new SpinnerNumberModel(1.4, 0.1, 10.0, 0.1));
        gaussianPanel.add(new JLabel("Sigma (\u03C3): "));
        gaussianPanel.add(sigmaSpinner);

        cardsPanel.add(boxPanel, "Box Blur");
        cardsPanel.add(gaussianPanel, "Gaussian Blur");

        typeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) (cardsPanel.getLayout());
            cl.show(cardsPanel, (String) typeComboBox.getSelectedItem());
        });

        JButton applyBtn = new JButton("Apply");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            String selectedType = (String) typeComboBox.getSelectedItem();
            double[][] mask = null;

            if ("Box Blur".equals(selectedType)) {
                mask = ImageProcessor.getAveragingMask((int) sizeSpinner.getValue(), (int) weightSpinner.getValue());
            } else if ("Gaussian Blur".equals(selectedType)) {
                mask = ImageProcessor.getGaussianMask((double) sigmaSpinner.getValue());
            }

            if (mask != null) {
                int[][][] newMatrix = ImageProcessor.applyConvolution(
                        photoPanel.getImageMatrix(), mask, parentPanel.getBoundaryMode());
                photoPanel.setImageMatrix(newMatrix);
            }

            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(typePanel);
        this.add(Box.createVerticalStrut(10));
        this.add(cardsPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }
}