package optionspanels;

import core.ImageProcessor;
import core.FingerprintProcessor;
import core.MenuBar;
import core.OptionPanel;
import core.PhotoPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static core.ImageProcessor.getGaussianMask;

public class FingerprintPanel extends JPanel{
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private int[][][] originalMatrix;
    private OptionPanel.BoundaryMode boundaryMode;
    private int[] roiBoundaries;
    private int[][][] cleanMatrixForCrop;

    public FingerprintPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        this.originalMatrix = photoPanel.getImageMatrix();
        boundaryMode = parentPanel.getBoundaryMode();

        loadDefaultImage();

        buildUI();
    }

    private void loadDefaultImage() {
        try {
            BufferedImage img = ImageIO.read(new File("src/testImage.bmp"));
            originalMatrix = photoPanel.createImageMatrix(img);
            photoPanel.setImageMatrix(originalMatrix);
            photoPanel.setCurrentFilename("testImage");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not load default image.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Fingerprint analysis:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JButton choosePictureBtn = new JButton("0. Choose a picture");
        choosePictureBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton grayscaleBtn = new JButton("1. Convert to Grayscale");
        grayscaleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton brightnessBtn = new JButton("2. Change brightness range");
        brightnessBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton convolutionBtn = new JButton("3. Apply blur");
        convolutionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton binarizationBtn = new JButton("4. Apply binarization");
        binarizationBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton morphologyBtn = new JButton("5. Apply morphology operations");
        morphologyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel roiContainer = new JPanel();
        roiContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        roiContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        roiContainer.setMaximumSize(new Dimension(400, 40));

        JButton roiBtn = new JButton("6. Find the region of interest");
        JLabel thresholdLabel = new JLabel("Noise threshold:");
        JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 100, 1));

        roiContainer.add(roiBtn);
        roiContainer.add(Box.createHorizontalStrut(5));
        roiContainer.add(thresholdLabel);
        roiContainer.add(thresholdSpinner);

        JButton cropBtn = new JButton("7. Crop the image");
        cropBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel thinningContainer = new JPanel();
        thinningContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        thinningContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        thinningContainer.setMaximumSize(new Dimension(500, 40));

        JButton thinningBtn = new JButton("8. Apply thinning");
        JLabel algorithmLabel = new JLabel("Algorithm:");

        String[] algorithms = {"KMM", "K3M"};
        JComboBox<String> algorithmComboBox = new JComboBox<>(algorithms);

        thinningContainer.add(thinningBtn);
        thinningContainer.add(Box.createHorizontalStrut(5));
        thinningContainer.add(algorithmLabel);
        thinningContainer.add(algorithmComboBox);

        choosePictureBtn.addActionListener(e -> {
            File file = MenuBar.chooseImageFile(this, "Select First Fingerprint Image");
            if (file == null) return;

            try {
                BufferedImage img1 = ImageIO.read(file);

                originalMatrix = photoPanel.createImageMatrix(img1);

                photoPanel.setImageMatrix(originalMatrix);
                photoPanel.setCurrentFilename(file.getName());

                parentPanel.resetUndoAndRevertStates(originalMatrix);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading image file.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        grayscaleBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyGrayscale(photoPanel.getImageMatrix(), GrayscalePanel.GrayscaleOptions.LUMINANCE);
            photoPanel.setImageMatrix(newMatrix);
        });

        brightnessBtn.addActionListener(e -> {
            // changing the brightness range to (0, 255)
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyBrightnessRange(photoPanel.getImageMatrix(), 0, 255);
            photoPanel.setImageMatrix(newMatrix);
        });

        convolutionBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyConvolution(photoPanel.getImageMatrix(), getGaussianMask(0.6), boundaryMode);
            photoPanel.setImageMatrix(newMatrix);
        });

        binarizationBtn.addActionListener(e -> {
            // applying Bernsen with window size 9 and contrast limit 50 - we could still test the parameters
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyBernsen(photoPanel.getImageMatrix(), 9, 50, boundaryMode);
            photoPanel.setImageMatrix(newMatrix);
        });

        morphologyBtn.addActionListener(e -> {
            // only closing with a 3x3 cross for now, not sure if it could be better
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            boolean[][] horizontalLineMask = {
                    {false, true, false},
                    {true,  true,  true },
                    {false, true, false}
            };
            int[][][] newMatrix = ImageProcessor.applyClosing(photoPanel.getImageMatrix(), horizontalLineMask, boundaryMode);
            photoPanel.setImageMatrix(newMatrix);
            cleanMatrixForCrop = photoPanel.getImageMatrix();
        });

        roiBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int threshold = (Integer) thresholdSpinner.getValue();

            if (cleanMatrixForCrop == null) {
                JOptionPane.showMessageDialog(this, "Please apply morphology operations first", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            roiBoundaries = FingerprintProcessor.calculateROIBounds(ImageProcessor.getProjections(cleanMatrixForCrop), threshold);
            if (roiBoundaries != null) {
                int[][][] matrixWithBox = FingerprintProcessor.drawRedBox(cleanMatrixForCrop, roiBoundaries);
                photoPanel.setImageMatrix(matrixWithBox);
            }
        });

        cropBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            if (roiBoundaries == null || cleanMatrixForCrop == null) {
                JOptionPane.showMessageDialog(this, "Please find the ROI first", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int[][][] newMatrix = FingerprintProcessor.cropToROI(cleanMatrixForCrop, roiBoundaries);
            photoPanel.setImageMatrix(newMatrix);
            cleanMatrixForCrop = null;
            roiBoundaries = null;
        });

        thinningBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            String selectedAlgo = (String) algorithmComboBox.getSelectedItem();
            int[][][] currentMatrix = photoPanel.getImageMatrix();
            int[][][] newMatrix = currentMatrix;

            if ("KMM".equals(selectedAlgo)) {
                newMatrix = FingerprintProcessor.applyKMM(currentMatrix);
            } else if ("K3M".equals(selectedAlgo)) {
                newMatrix = FingerprintProcessor.applyK3M(currentMatrix);
            } else {
                // newMatrix = FingerprintProcessor.applyMorphologicalThinning(currentMatrix);
            }

            photoPanel.setImageMatrix(newMatrix);
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(choosePictureBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(grayscaleBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(brightnessBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(convolutionBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(binarizationBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(morphologyBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(roiContainer);
        this.add(Box.createVerticalStrut(20));
        this.add(cropBtn);
        this.add(Box.createVerticalStrut(20));
        this.add(thinningContainer);
        this.add(Box.createVerticalGlue());
    }
}
