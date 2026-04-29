package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class BinarizationPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private int[][][] originalMatrix;
    private boolean isGrayscaleApplied = false;

    private JComboBox<String> typeComboBox;

    public BinarizationPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        this.originalMatrix = photoPanel.getImageMatrix();
        parentPanel.saveUndoState(originalMatrix);

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Binarization:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JButton grayscaleBtn = new JButton("Convert to Grayscale");
        grayscaleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel typePanel = new JPanel();
        typePanel.setMaximumSize(new Dimension(300, 40));
        String[] options = {"Otsu", "Niblack", "Bernsen", "Multi-Otsu", "Custom multi-threshold", "Custom threshold"};
        typeComboBox = new JComboBox<>(options);
        typePanel.add(new JLabel("Method: "));
        typePanel.add(typeComboBox);

        JPanel cardsPanel = new JPanel(new CardLayout());
        cardsPanel.setMaximumSize(new Dimension(300, 80));

        // Otsu
        JPanel otsuPanel = new JPanel();

        // Niblack
        JPanel niblackPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JSpinner niblackWinSpinner = new JSpinner(new SpinnerNumberModel(11, 3, 99, 2));
        JSpinner niblackKSpinner = new JSpinner(new SpinnerNumberModel(-0.2, -1.0, 1.0, 0.1));
        niblackPanel.add(new JLabel("Window Size: "));
        niblackPanel.add(niblackWinSpinner);
        niblackPanel.add(new JLabel("k parameter: "));
        niblackPanel.add(niblackKSpinner);

        // Bernsen
        JPanel bernsenPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JSpinner bernsenWinSpinner = new JSpinner(new SpinnerNumberModel(11, 3, 99, 2));
        JSpinner bernsenContrastSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 255, 1));
        bernsenPanel.add(new JLabel("Window Size: "));
        bernsenPanel.add(bernsenWinSpinner);
        bernsenPanel.add(new JLabel("Contrast Limit: "));
        bernsenPanel.add(bernsenContrastSpinner);

        // Multi-Otsu
        JPanel multiOtsuPanel = new JPanel();
        JSpinner multiOtsuClasses = new JSpinner(new SpinnerNumberModel(3, 3, 5, 1));
        multiOtsuPanel.add(new JLabel("Classes: "));
        multiOtsuPanel.add(multiOtsuClasses);

        // Custom multi-threshold
        JPanel multiCustomPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JSpinner t1Spinner = new JSpinner(new SpinnerNumberModel(85, 0, 255, 1));
        JSpinner t2Spinner = new JSpinner(new SpinnerNumberModel(170, 0, 255, 1));
        multiCustomPanel.add(new JLabel("Threshold 1: "));
        multiCustomPanel.add(t1Spinner);
        multiCustomPanel.add(new JLabel("Threshold 2: "));
        multiCustomPanel.add(t2Spinner);

        // Custom threshold
        JPanel customPanel = new JPanel();
        JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(128, 0, 255, 1));
        customPanel.add(new JLabel("Threshold: "));
        customPanel.add(thresholdSpinner);

        cardsPanel.add(otsuPanel, "Otsu");
        cardsPanel.add(niblackPanel, "Niblack");
        cardsPanel.add(bernsenPanel, "Bernsen");
        cardsPanel.add(multiOtsuPanel, "Multi-Otsu");
        cardsPanel.add(multiCustomPanel, "Custom multi-threshold");
        cardsPanel.add(customPanel, "Custom threshold");

        typeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) (cardsPanel.getLayout());
            cl.show(cardsPanel, (String) typeComboBox.getSelectedItem());
        });

        JButton applyBinarizationBtn = new JButton("Apply");
        applyBinarizationBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBinarizationBtn.setEnabled(false);

        grayscaleBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyGrayscale(photoPanel.getImageMatrix(), GrayscalePanel.GrayscaleOptions.LUMINANCE);
            photoPanel.setImageMatrix(newMatrix);
            originalMatrix = newMatrix;
            isGrayscaleApplied = true;

            applyBinarizationBtn.setEnabled(true);

            parentPanel.updateHistogram();
        });

        applyBinarizationBtn.addActionListener(e -> {
            if (!isGrayscaleApplied) return;
            parentPanel.saveUndoState(photoPanel.getImageMatrix());

            String selectedMethod = (String) typeComboBox.getSelectedItem();
            int[][][] currentMatrix = photoPanel.getImageMatrix();
            int[][][] newMatrix = null;

            if ("Otsu".equals(selectedMethod)) {
                newMatrix = ImageProcessor.applyOtsu(currentMatrix);
            }
            else if ("Niblack".equals(selectedMethod)) {
                int windowSize = (Integer) niblackWinSpinner.getValue();
                double k = (Double) niblackKSpinner.getValue();
                newMatrix = ImageProcessor.applyNiblack(currentMatrix, windowSize, k, parentPanel.getBoundaryMode());
            }
            else if ("Bernsen".equals(selectedMethod)) {
                int windowSize = (Integer) bernsenWinSpinner.getValue();
                int contrastLimit = (Integer) bernsenContrastSpinner.getValue();
                newMatrix = ImageProcessor.applyBernsen(currentMatrix, windowSize, contrastLimit, parentPanel.getBoundaryMode());
            }
            else if ("Multi-Otsu".equals(selectedMethod)) {
                int classes = (Integer) multiOtsuClasses.getValue();
                newMatrix = ImageProcessor.applyMultiOtsu(currentMatrix, classes);
            }
            else if ("Custom multi-threshold".equals(selectedMethod)) {
                int t1 = (Integer) t1Spinner.getValue();
                int t2 = (Integer) t2Spinner.getValue();
                newMatrix = ImageProcessor.applySegmentation(currentMatrix, t1, t2);
            }
            else if ("Custom threshold".equals(selectedMethod)) {
                int t = (int) thresholdSpinner.getValue();
                newMatrix = ImageProcessor.applySegmentation(currentMatrix, t);
            }

            if (newMatrix != null) {
                photoPanel.setImageMatrix(newMatrix);
                originalMatrix = newMatrix;
                parentPanel.updateHistogram();
            }
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(grayscaleBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(typePanel);
        this.add(cardsPanel);
        this.add(Box.createVerticalStrut(15));
        this.add(applyBinarizationBtn);
        this.add(Box.createVerticalGlue());
    }
}