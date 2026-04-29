package core;

import optionspanels.GrayscalePanel;

/**
 * Utility class that implements all image processing algorithms.
 */
public class ImageProcessor {

    // ==========================================================
    // PIXEL OPERATIONS
    // ==========================================================

    /**
     * Shifts the brightness of the image by adding a constant offset to every color channel.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param offset The value to add (-255 to 255).
     * @return A new 3D array with adjusted brightness.
     */
    public static int[][][] applyBrightnessOffset(int[][][] originalMatrix, int offset) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int newValue = originalMatrix[y][x][c] + offset;
                    newMatrix[y][x][c] = Math.min(Math.max(newValue, 0), 255);
                }
            }
        }
        return newMatrix;
    }

    /**
     * Stretches or compresses the image's brightness histogram to fit within a new range [N1, N2].
     *
     * @param originalMatrix The 3D array representing the image.
     * @param N1 The new minimum brightness value.
     * @param N2 The new maximum brightness value.
     * @return A new 3D array with the adjusted brightness range.
     */
    public static int[][][] applyBrightnessRange(int[][][] originalMatrix, int N1, int N2) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int J_min = 255, J_max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int val = originalMatrix[y][x][c];
                    if (val < J_min) J_min = val;
                    if (val > J_max) J_max = val;
                }
            }
        }

        if (J_max == J_min) { return originalMatrix; }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int originalValue = originalMatrix[y][x][c];
                    double ratio = (double) (originalValue - J_min) / (J_max - J_min);
                    int newValue = (int) Math.round(ratio * (N2 - N1)) + N1;
                    newMatrix[y][x][c] = Math.min(Math.max(newValue, 0), 255);
                }
            }
        }
        return newMatrix;
    }

    /**
     * Applies contrast correction.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param alpha The gamma factor.
     * @return A new 3D array with adjusted contrast.
     */
    public static int[][][] applyContrastPower(int[][][] originalMatrix, double alpha) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int J_max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int val = originalMatrix[y][x][c];
                    if (val > J_max) J_max = val;
                }
            }
        }

        if (J_max == 0) return originalMatrix;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int originalValue = originalMatrix[y][x][c];
                    double ratio = (double) (originalValue) / J_max;
                    int newValue = (int) Math.round(255 * Math.pow(ratio, alpha));
                    newMatrix[y][x][c] = Math.min(Math.max(newValue, 0), 255);
                }
            }
        }
        return newMatrix;
    }

    /**
     * Applies logarithmic contrast correction.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A new 3D array with logarithmic contrast applied.
     */
    public static int[][][] applyContrastLog(int[][][] originalMatrix) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int J_max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int val = originalMatrix[y][x][c];
                    if (val > J_max) J_max = val;
                }
            }
        }

        if (J_max == 0) return originalMatrix;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int originalValue = originalMatrix[y][x][c];
                    double ratio = Math.log(1 + originalValue) / Math.log(1 + J_max);
                    int newValue = (int) Math.round(255 * ratio);
                    newMatrix[y][x][c] = Math.min(Math.max(newValue, 0), 255);
                }
            }
        }
        return newMatrix;
    }

    /**
     * Converts an image to grayscale using the standard conversion algorithm selected via the provided option.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param option The chosen grayscale algorithm.
     * @return A new 3D array representing the grayscale image.
     */
    public static int[][][] applyGrayscale(int[][][] originalMatrix, GrayscalePanel.GrayscaleOptions option) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];

                int gray = 0;
                int min = Math.min(r, Math.min(g, b));
                int max = Math.max(r, Math.max(g, b));

                if (option == GrayscalePanel.GrayscaleOptions.AVERAGING) {
                    gray = Math.round((float) (r + g + b) / 3);
                } else if (option == GrayscalePanel.GrayscaleOptions.LUMINANCE) {
                    gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                } else if (option == GrayscalePanel.GrayscaleOptions.DESATURATION) {
                    gray = Math.round((float) (max + min) / 2);
                } else if (option == GrayscalePanel.GrayscaleOptions.DECOMPOSITION_MAX) {
                    gray = max;
                } else if (option == GrayscalePanel.GrayscaleOptions.DECOMPOSITION_MIN) {
                    gray = min;
                } else if (option == GrayscalePanel.GrayscaleOptions.SINGLE_RED) {
                    gray = r;
                } else if (option == GrayscalePanel.GrayscaleOptions.SINGLE_GREEN) {
                    gray = g;
                } else if (option == GrayscalePanel.GrayscaleOptions.SINGLE_BLUE) {
                    gray = b;
                }

                gray = Math.min(Math.max(gray, 0), 255);

                newMatrix[y][x][0] = gray;
                newMatrix[y][x][1] = gray;
                newMatrix[y][x][2] = gray;
            }
        }

        return newMatrix;
    }

    /**
     * Converts an image to grayscale using a quantization algorithm that limits the output
     * to a specific number of shades, optionally applying dithering.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param option The quantization option (Custom or Custom Dithered).
     * @param shades The number of grayscale shades to reduce the image to.
     * @return A new 3D array representing the quantized grayscale image.
     */
    public static int[][][] applyGrayscale(int[][][] originalMatrix, GrayscalePanel.GrayscaleOptions option, int shades) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        double factor = 255.0 / (shades - 1);
        int errorVal = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];

                int gray = 0;
                int avgVal = Math.round((float) (r + g + b) / 3);

                if (option == GrayscalePanel.GrayscaleOptions.CUSTOM) {
                    gray = (int) Math.round(Math.round(avgVal / factor) * factor);
                } else if (option == GrayscalePanel.GrayscaleOptions.CUSTOM_DITHERED) {
                    int tempGray = avgVal + errorVal;
                    gray = (int) Math.round(Math.round(tempGray / factor) * factor);
                    errorVal += avgVal - gray;
                }

                gray = Math.min(Math.max(gray, 0), 255);

                newMatrix[y][x][0] = gray;
                newMatrix[y][x][1] = gray;
                newMatrix[y][x][2] = gray;
            }
            errorVal = 0;
        }

        return newMatrix;
    }

    /**
     * Inverts the colors of the image.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A new 3D array representing the negative image.
     */
    public static int[][][] applyNegative(int[][][] originalMatrix) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newMatrix[y][x][0] = 255 - originalMatrix[y][x][0];
                newMatrix[y][x][1] = 255 - originalMatrix[y][x][1];
                newMatrix[y][x][2] = 255 - originalMatrix[y][x][2];
            }
        }
        return newMatrix;
    }

    // ==========================================================
    // BINARIZATION
    // ==========================================================

    /**
     * Segments an image into multiple brightness classes based on an array of thresholds.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param thresholds An array of integer thresholds to split the image by.
     * @return A segmented 3D array.
     */
    public static int[][][] applySegmentation(int[][][] originalMatrix, int... thresholds) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        java.util.Arrays.sort(thresholds);

        int classes = thresholds.length + 1;
        int[] colors = new int[classes];
        for (int i = 0; i < classes; i++) {
            colors[i] = (int) Math.round((255.0 / (classes - 1)) * i);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int val = originalMatrix[y][x][c];

                    int classIdx = classes - 1;
                    for (int i = 0; i < thresholds.length; i++) {
                        if (val <= thresholds[i]) {
                            classIdx = i;
                            break;
                        }
                    }
                    newMatrix[y][x][c] = colors[classIdx];
                }
            }
        }
        return newMatrix;
    }

    /**
     * Calculates the binarization thresholds for each pixel using Niblack's method, then applies segmentation.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param windowSize The window size for the sliding window.
     * @param k The sensitivity parameter.
     * @param boundaryMode The boundary handling rule for edges.
     * @return A binary 3D array (black and white).
     */
    public static int[][][] applyNiblack(int[][][] originalMatrix, int windowSize, double k, OptionPanel.BoundaryMode boundaryMode) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int offset = windowSize / 2;
        int N = windowSize * windowSize;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                long sum = 0;
                long sumSquares = 0;

                for (int wy = -offset; wy <= offset; wy++) {
                    for (int wx = -offset; wx <= offset; wx++) {
                        int px = x + wx;
                        int py = y + wy;

                        int[] rgb = getPixelWithBoundary(originalMatrix, px, py, width, height, boundaryMode);
                        int val = rgb[0];

                        sum += val;
                        sumSquares += (long) val * val;
                    }
                }

                double mean = (double) sum / N;

                double variance = ((double) sumSquares / N) - (mean * mean);
                if (variance < 0) variance = 0;
                double stdDev = Math.sqrt(variance);

                double T = mean + (k * stdDev);
                int originalValue = originalMatrix[y][x][0];
                int color = (originalValue <= T) ? 0 : 255;

                newMatrix[y][x][0] = color;
                newMatrix[y][x][1] = color;
                newMatrix[y][x][2] = color;
            }
        }

        return newMatrix;
    }

    /**
     * Calculates the binarization thresholds for each pixel using Bernsen's method, then applies segmentation.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param windowSize The window size for the sliding window.
     * @param contrastLimit The minimum local contrast required to apply dynamic thresholding.
     * @param boundaryMode The boundary handling rule for edges.
     * @return A binary 3D array (black and white).
     */
    public static int[][][] applyBernsen(int[][][] originalMatrix, int windowSize, int contrastLimit, OptionPanel.BoundaryMode boundaryMode) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int offset = windowSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int minVal = 255;
                int maxVal = 0;

                for (int wy = -offset; wy <= offset; wy++) {
                    for (int wx = -offset; wx <= offset; wx++) {
                        int px = x + wx;
                        int py = y + wy;

                        int[] rgb = getPixelWithBoundary(originalMatrix, px, py, width, height, boundaryMode);
                        int val = rgb[0];

                        if (val < minVal) minVal = val;
                        if (val > maxVal) maxVal = val;
                    }
                }

                int contrast = maxVal - minVal;
                int midPoint = (maxVal + minVal) / 2;
                int originalValue = originalMatrix[y][x][0];
                int color;

                if (contrast < contrastLimit) {
                    color = (midPoint >= 128) ? 255 : 0;
                } else {
                    color = (originalValue <= midPoint) ? 0 : 255;
                }

                newMatrix[y][x][0] = color;
                newMatrix[y][x][1] = color;
                newMatrix[y][x][2] = color;
            }
        }

        return newMatrix;
    }

    /**
     * Calculates the binarization threshold using Otsu's method, then applies segmentation.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A binary 3D array (black and white).
     */
    public static int[][][] applyOtsu(int[][][] originalMatrix) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int[] hist = new int[256];
        int N = height * width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];

                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                hist[gray]++;
            }
        }

        int bestThreshold = 0;
        double maxVar = 0.0;

        for (int t = 0; t < 256; t++) {
            int pixels0 = 0;
            int pixels1 = 0;
            long sum0 = 0;
            long sum1 = 0;

            for (int i = 0; i <= t; i++) {
                pixels0 += hist[i];
                sum0 += (long) i * hist[i];
            }
            for (int i = t+1; i < 256; i++) {
                pixels1 += hist[i];
                sum1 += (long) i * hist[i];
            }

            if (pixels0 == 0 || pixels1 == 0) continue;

            double w0 = (double) pixels0 / N;
            double w1 = (double) pixels1 / N;
            double avgBrightness0 = (double) sum0 / pixels0;
            double avgBrightness1 = (double) sum1 / pixels1;
            double var = w0 * w1 * Math.pow(avgBrightness0 - avgBrightness1, 2);

            if (var > maxVar) {
                maxVar = var;
                bestThreshold = t;
            }
        }

        return applySegmentation(originalMatrix, bestThreshold);
    }

    /**
     * A version of Otsu's method that finds multiple optimal thresholds
     * for segmenting an image into several color levels between black and white.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param classes The number of segments/classes to divide the image into.
     * @return A multi-level segmented 3D array.
     */
    public static int[][][] applyMultiOtsu(int[][][] originalMatrix, int classes) {
        if (originalMatrix == null || classes < 2) return originalMatrix;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int N = height * width;

        int[] hist = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];
                hist[(int)(0.299 * r + 0.587 * g + 0.114 * b)]++;
            }
        }

        double[] P = new double[256];
        double[] S = new double[256];
        for (int i = 0; i < 256; i++) {
            double p = (double) hist[i] / N;
            P[i] = (i == 0) ? p : P[i - 1] + p;
            S[i] = (i == 0) ? 0 : S[i - 1] + (i * p);
        }

        int numThresholds = classes - 1;
        int[] bestThresholds = new int[numThresholds];
        int[] currentThresholds = new int[numThresholds];
        double[] maxVar = new double[]{0.0};

        generateCombinations(0, 0, numThresholds, currentThresholds, bestThresholds, maxVar, P, S);

        return applySegmentation(originalMatrix, bestThresholds);
    }

    /**
     * A helper function to recursively generate all possible combinations of thresholds for multi-level
     * Otsu's method.
     *
     * @param step The current depth of the recursion.
     * @param startVal The starting pixel intensity value to check.
     * @param numThresholds The total number of thresholds needed (classes - 1).
     * @param current The array holding the currently evaluated threshold combination.
     * @param best The array holding the best threshold combination found so far.
     * @param maxVar A 1-element array holding the maximum variance found so far.
     * @param P The cumulative probability array of the image histogram.
     * @param S The cumulative mean array of the image histogram.
     */
    private static void generateCombinations(int step, int startVal, int numThresholds, int[] current, int[] best, double[] maxVar, double[] P, double[] S) {
        if (step == numThresholds) {
            double currentVar = 0.0;
            int lastT = -1;

            for (int i = 0; i <= numThresholds; i++) {
                int tStart = lastT + 1;
                int tEnd = (i == numThresholds) ? 255 : current[i];
                if (tStart > tEnd) return;

                double w = P[tEnd] - (tStart > 0 ? P[tStart - 1] : 0);
                if (w > 0) {
                    double m = (S[tEnd] - (tStart > 0 ? S[tStart - 1] : 0)) / w;
                    currentVar += w * m * m;
                }
                lastT = tEnd;
            }

            if (currentVar > maxVar[0]) {
                maxVar[0] = currentVar;
                System.arraycopy(current, 0, best, 0, numThresholds);
            }
            return;
        }

        for (int t = startVal; t < 256; t++) {
            current[step] = t;
            generateCombinations(step + 1, t + 1, numThresholds, current, best, maxVar, P, S);
        }
    }

    // ==========================================================
    // CONVOLUTION FILTERS
    // ==========================================================

    /**
     * Applies a convolution mask to a particular pixel.
     *
     * @param matrix The original 3D image array.
     * @param centerX The X coordinate of the target pixel.
     * @param centerY The Y coordinate of the target pixel.
     * @param mask The 2D array representing the convolution kernel.
     * @param mode The boundary handling rule for edges.
     * @return A double array [R, G, B] containing the un-normalized convolution sum.
     */
    private static double[] applyMaskToPixel(int[][][] matrix, int centerX, int centerY, double[][] mask, OptionPanel.BoundaryMode mode) {
        int maskSize = mask.length;
        int offset = maskSize / 2;
        int height = matrix.length;
        int width = matrix[0].length;

        double r = 0, g = 0, b = 0;

        for (int my = 0; my < maskSize; my++) {
            for (int mx = 0; mx < maskSize; mx++) {
                int pixelY = centerY + my - offset;
                int pixelX = centerX + mx - offset;

                int[] rgb = getPixelWithBoundary(matrix, pixelX, pixelY, width, height, mode);
                double weight = mask[my][mx];

                r += rgb[0] * weight;
                g += rgb[1] * weight;
                b += rgb[2] * weight;
            }
        }

        return new double[]{r, g, b};
    }

    /**
     * Applies a convolution mask to the entire image.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param mask The 2D kernel to apply.
     * @param currentBoundaryMode The boundary handling rule for edges.
     * @return A new 3D array representing the modified image.
     */
    public static int[][][] applyConvolution(int[][][] originalMatrix, double[][] mask, OptionPanel.BoundaryMode currentBoundaryMode) {
        if (originalMatrix == null || mask == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int maskSize = mask.length;
        int offset = maskSize / 2;

        int outHeight = (currentBoundaryMode == OptionPanel.BoundaryMode.CROP) ? height - 2 * offset : height;
        int outWidth = (currentBoundaryMode == OptionPanel.BoundaryMode.CROP) ? width - 2 * offset : width;

        if (outHeight <= 0 || outWidth <= 0) return originalMatrix;

        int[][][] newMatrix = new int[outHeight][outWidth][3];

        double weightSum = 0;
        for (int i = 0; i < maskSize; i++) {
            for (int j = 0; j < maskSize; j++) {
                weightSum += mask[i][j];
            }
        }
        if (weightSum == 0) weightSum = 1;

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {

                int origY = (currentBoundaryMode == OptionPanel.BoundaryMode.CROP) ? y + offset : y;
                int origX = (currentBoundaryMode == OptionPanel.BoundaryMode.CROP) ? x + offset : x;

                if (currentBoundaryMode == OptionPanel.BoundaryMode.KEEP_ORIGINAL) {
                    if (origY < offset || origY >= height - offset || origX < offset || origX >= width - offset) {
                        newMatrix[y][x][0] = originalMatrix[origY][origX][0];
                        newMatrix[y][x][1] = originalMatrix[origY][origX][1];
                        newMatrix[y][x][2] = originalMatrix[origY][origX][2];
                        continue;
                    }
                }

                double[] rgbResult = applyMaskToPixel(originalMatrix, origX, origY, mask, currentBoundaryMode);

                newMatrix[y][x][0] = Math.min(Math.max((int) Math.round(rgbResult[0] / weightSum), 0), 255);
                newMatrix[y][x][1] = Math.min(Math.max((int) Math.round(rgbResult[1] / weightSum), 0), 255);
                newMatrix[y][x][2] = Math.min(Math.max((int) Math.round(rgbResult[2] / weightSum), 0), 255);
            }
        }

        return newMatrix;
    }

    /**
     * Retrieves a pixel's RGB values (applying the selected boundary logic for pixels outside the image).
     *
     * @param matrix The original 3D image array.
     * @param x The requested X coordinate.
     * @param y The requested Y coordinate.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param mode The boundary handling rule (e.g., MIRROR, PAD_BLACK).
     * @return An integer array [R, G, B] of the fetched pixel.
     */
    private static int[] getPixelWithBoundary(int[][][] matrix, int x, int y, int width, int height, OptionPanel.BoundaryMode mode) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return matrix[y][x];
        }

        switch (mode) {
            case PAD_BLACK: return new int[]{0, 0, 0};
            case PAD_WHITE: return new int[]{255, 255, 255};
            case PAD_GRAY:  return new int[]{128, 128, 128};
            case MIRROR:
                int mx = x;
                if (mx < 0) mx = Math.abs(mx);
                if (mx >= width) mx = 2 * width - mx - 2;
                mx = Math.max(0, Math.min(mx, width - 1));

                int my = y;
                if (my < 0) my = Math.abs(my);
                if (my >= height) my = 2 * height - my - 2;
                my = Math.max(0, Math.min(my, height - 1));

                return matrix[my][mx];
            case REPLICATE:
            case CROP:
            case KEEP_ORIGINAL:
            default:
                int sx = Math.max(0, Math.min(x, width - 1));
                int sy = Math.max(0, Math.min(y, height - 1));
                return matrix[sy][sx];
        }
    }

    /**
     * Generates an NxN averaging (Box Blur) mask.
     *
     * @param size The dimensions of the mask (e.g., 3 for a 3x3 mask).
     * @param centerWeight The weight of the central pixel.
     * @return A 2D array representing the averaging mask.
     */
    public static double[][] getAveragingMask(int size, int centerWeight) {
        double[][] mask = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                mask[i][j] = 1.0;
            }
        }
        mask[size / 2][size / 2] = (double) centerWeight;
        return mask;
    }

    /**
     * Generates a Gaussian Blur mask based on a specified standard deviation.
     * The size of the mask is automatically calculated as ceil(6 * sigma).
     *
     * @param sigma The standard deviation for the Gaussian distribution.
     * @return A dynamically sized 2D array representing the Gaussian mask.
     */
    public static double[][] getGaussianMask(double sigma) {
        int size = (int) Math.ceil(6 * sigma);
        if (size % 2 == 0) size++;

        double[][] mask = new double[size][size];
        int offset = size / 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int cx = x - offset;
                int cy = y - offset;
                mask[y][x] = (1.0 / (2.0 * Math.PI * sigma * sigma)) * Math.exp(-(cx * cx + cy * cy) / (2.0 * sigma * sigma));
            }
        }
        return mask;
    }

    /**
     * Retrieves a standard 3x3 Laplacian sharpening mask.
     *
     * @param type The type of mask ("Strong" includes diagonals, otherwise standard orthogonal).
     * @return A 3x3 2D array representing the sharpening mask.
     */
    public static double[][] getSharpeningMask(String type) {
        if ("Strong".equals(type)) {
            return new double[][] {
                    {-1, -1, -1},
                    {-1,  9, -1},
                    {-1, -1, -1}
            };
        } else {
            return new double[][] {
                    { 0, -1,  0},
                    {-1,  5, -1},
                    { 0, -1,  0}
            };
        }
    }

    /**
     * Helper method to calculate the final pixel value for Laplacian sharpening.
     *
     * @param origVal The original color channel value of the pixel.
     * @param response The raw output from Laplacian mask.
     * @param strength The user-defined intensity multiplier.
     * @param threshold The minimum response required to apply the sharpening.
     * @return The clamped integer value (0-255) for the sharpened pixel.
     */
    private static int applySharpeningMath(int origVal, double response, double strength, int threshold) {
        if (Math.abs(response) >= threshold) {
            int finalVal = (int) Math.round(origVal + (strength * response));
            return Math.min(Math.max(finalVal, 0), 255);
        }
        return origVal;
    }

    /**
     * Applies advanced sharpening techniques (e.g. Unsharp Masking, Laplacian of Gaussian (LoG)).
     *
     * @param originalMatrix The 3D array representing the image.
     * @param mode The sharpening algorithm ("Unsharp Masking" or "Laplacian").
     * @param laplacianType The intensity of the Laplacian mask ("Standard" or "Strong").
     * @param useLoG True to apply a Gaussian blur before running the Laplacian filter.
     * @param sigma The standard deviation for the Gaussian blur.
     * @param strength A multiplier applied to the sharpened edges before merging with the original image.
     * @param threshold Edges below this threshold will be ignored to prevent sharpening noise.
     * @param boundaryMode The rule for edge handling.
     * @return A new 3D array representing the sharpened image.
     */
    public static int[][][] applyAdvancedSharpening(
            int[][][] originalMatrix,
            String mode,
            String laplacianType,
            boolean useLoG,
            double sigma,
            double strength,
            int threshold,
            OptionPanel.BoundaryMode boundaryMode) {

        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;

        // generate blurred image for LoG or unsharp
        int[][][] blurredMatrix = null;
        int gaussOffset = 0;
        if (mode.equals("Unsharp Masking") || (mode.equals("Laplacian") && useLoG)) {
            double[][] gaussMask = getGaussianMask(sigma);
            gaussOffset = gaussMask.length / 2;
            blurredMatrix = applyConvolution(originalMatrix, gaussMask, boundaryMode);
            if (blurredMatrix == null || blurredMatrix == originalMatrix) return originalMatrix;
        }

        // unsharp masking: output = original + strength * (original - blurred)
        if (mode.equals("Unsharp Masking")) {
            int outH = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? height - 2 * gaussOffset : height;
            int outW = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? width - 2 * gaussOffset : width;
            int[][][] newMatrix = new int[outH][outW][3];

            for (int y = 0; y < outH; y++) {
                for (int x = 0; x < outW; x++) {
                    int origY = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? y + gaussOffset : y;
                    int origX = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? x + gaussOffset : x;

                    for (int c = 0; c < 3; c++) {
                        int origVal = originalMatrix[origY][origX][c];
                        int blurVal = blurredMatrix[y][x][c];
                        double diff = origVal - blurVal;

                        // check threshold
                        if (Math.abs(diff) >= threshold) {
                            int finalVal = (int) Math.round(origVal + strength * diff);
                            newMatrix[y][x][c] = Math.min(Math.max(finalVal, 0), 255);
                        } else {
                            newMatrix[y][x][c] = origVal;
                        }
                    }
                }
            }
            return newMatrix;
        }

        // laplacian masking: output = original + strength * (laplacian mask output)
        if (mode.equals("Laplacian")) {
            int[][][] baseMatrix = useLoG ? blurredMatrix : originalMatrix;
            int baseH = baseMatrix.length;
            int baseW = baseMatrix[0].length;
            int lapOffset = 1;

            double[][] lapMask = laplacianType.equals("Strong")
                    ? new double[][] {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}}
                    : new double[][] {{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};

            int outH = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? baseH - 2 * lapOffset : baseH;
            int outW = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? baseW - 2 * lapOffset : baseW;
            if (outH <= 0 || outW <= 0) return originalMatrix;

            int[][][] newMatrix = new int[outH][outW][3];

            for (int y = 0; y < outH; y++) {
                for (int x = 0; x < outW; x++) {
                    int baseY = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? y + lapOffset : y;
                    int baseX = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? x + lapOffset : x;

                    int origY = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? baseY + gaussOffset : y;
                    int origX = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? baseX + gaussOffset : x;

                    if (boundaryMode == OptionPanel.BoundaryMode.KEEP_ORIGINAL) {
                        int totalOffset = lapOffset + gaussOffset;
                        if (origY < totalOffset || origY >= height - totalOffset || origX < totalOffset || origX >= width - totalOffset) {
                            newMatrix[y][x][0] = originalMatrix[origY][origX][0];
                            newMatrix[y][x][1] = originalMatrix[origY][origX][1];
                            newMatrix[y][x][2] = originalMatrix[origY][origX][2];
                            continue;
                        }
                    }

                    double rLap = 0, gLap = 0, bLap = 0;

                    // regular laplacian mask output
                    for (int my = 0; my < 3; my++) {
                        for (int mx = 0; mx < 3; mx++) {
                            int pixelY = baseY + my - lapOffset;
                            int pixelX = baseX + mx - lapOffset;
                            double weight = lapMask[my][mx];

                            int[] rgb = getPixelWithBoundary(baseMatrix, pixelX, pixelY, baseW, baseH, boundaryMode);
                            rLap += rgb[0] * weight;
                            gLap += rgb[1] * weight;
                            bLap += rgb[2] * weight;
                        }
                    }

                    int[] origRgb = originalMatrix[origY][origX];
                    newMatrix[y][x][0] = applySharpeningMath(origRgb[0], rLap, strength, threshold);
                    newMatrix[y][x][1] = applySharpeningMath(origRgb[1], gLap, strength, threshold);
                    newMatrix[y][x][2] = applySharpeningMath(origRgb[2], bLap, strength, threshold);
                }
            }
            return newMatrix;
        }

        return originalMatrix;
    }

    // ==========================================================
    // EDGE DETECTION
    // ==========================================================

    /**
     * Applies standard masks to detect edges based on image gradients.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param operator The specific mathematical mask to apply (Sobel, Prewitt, Scharr, etc.).
     * @param boundaryMode The rule for edge handling.
     * @return A new 3D array representing the edge magnitudes.
     */
    public static int[][][] applyEdgeDetection(int[][][] originalMatrix, String operator, OptionPanel.BoundaryMode boundaryMode) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int offset = 1;

        double[][][] masks;

        if (operator.equals("Roberts Cross")) {
            masks = new double[][][] {
                    { {0, 0, 0}, {0, 1, 0}, {0, 0, -1} },
                    { {0, 0, 0}, {0, 0, 1}, {0, -1, 0} }
            };
        } else if (operator.equals("Laplace")) {
            masks = new double[][][] {
                    { {0,  -1, 0}, { -1, 4,  -1}, {0,  -1, 0} }
            };
        } else if (operator.equals("Prewitt Compass")) {
            masks = new double[][][] {
                    { {-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1} },
                    { { 0, 1, 1}, {-1, 0, 1}, {-1,-1, 0} },
                    { { 1, 1, 1}, { 0, 0, 0}, {-1,-1,-1} },
                    { { 1, 1, 0}, { 1, 0,-1}, { 0,-1,-1} },
                    { { 1, 0,-1}, { 1, 0,-1}, { 1, 0,-1} },
                    { { 0,-1,-1}, { 1, 0,-1}, { 1, 1, 0} },
                    { {-1,-1,-1}, { 0, 0, 0}, { 1, 1, 1} },
                    { {-1,-1, 0}, {-1, 0, 1}, { 0, 1, 1} }
            };
        } else if (operator.equals("Sobel Compass")) {
            masks = new double[][][] {
                    { {-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1} },
                    { { 0, 1, 2}, {-1, 0, 1}, {-2,-1, 0} },
                    { { 1, 2, 1}, { 0, 0, 0}, {-1,-2,-1} },
                    { { 2, 1, 0}, { 1, 0,-1}, { 0,-1,-2} },
                    { { 1, 0,-1}, { 2, 0,-2}, { 1, 0,-1} },
                    { { 0,-1,-2}, { 1, 0,-1}, { 2, 1, 0} },
                    { {-1,-2,-1}, { 0, 0, 0}, { 1, 2, 1} },
                    { {-2,-1, 0}, {-1, 0, 1}, { 0, 1, 2} }
            };
        } else {
            // Sobel (default)
            masks = new double[][][] {
                    { {-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1} },
                    { {-1, -2, -1}, { 0, 0, 0}, { 1, 2, 1} }
            };
        }

        int outHeight = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? height - 2 * offset : height;
        int outWidth = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? width - 2 * offset : width;
        if (outHeight <= 0 || outWidth <= 0) return originalMatrix;

        int[][][] newMatrix = new int[outHeight][outWidth][3];

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {

                int origY = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? y + offset : y;
                int origX = (boundaryMode == OptionPanel.BoundaryMode.CROP) ? x + offset : x;

                if (boundaryMode == OptionPanel.BoundaryMode.KEEP_ORIGINAL) {
                    if (origY < offset || origY >= height - offset || origX < offset || origX >= width - offset) {
                        newMatrix[y][x][0] = originalMatrix[origY][origX][0];
                        newMatrix[y][x][1] = originalMatrix[origY][origX][1];
                        newMatrix[y][x][2] = originalMatrix[origY][origX][2];
                        continue;
                    }
                }

                double finalR = 0, finalG = 0, finalB = 0;

                if (masks.length == 1) {
                    // just result, no changes, no abs()
                    double[] res = applyMaskToPixel(originalMatrix, origX, origY, masks[0], boundaryMode);
                    finalR = res[0];
                    finalG = res[1];
                    finalB = res[2];

                } else if (masks.length == 2) {
                    // sqrt of sum of squares of mask results
                    double[] resX = applyMaskToPixel(originalMatrix, origX, origY, masks[0], boundaryMode);
                    double[] resY = applyMaskToPixel(originalMatrix, origX, origY, masks[1], boundaryMode);
                    finalR = Math.sqrt(resX[0] * resX[0] + resY[0] * resY[0]);
                    finalG = Math.sqrt(resX[1] * resX[1] + resY[1] * resY[1]);
                    finalB = Math.sqrt(resX[2] * resX[2] + resY[2] * resY[2]);

                } else {
                    // maximum absolute response among all masks
                    for (double[][] mask : masks) {
                        double[] res = applyMaskToPixel(originalMatrix, origX, origY, mask, boundaryMode);
                        finalR = Math.max(finalR, Math.abs(res[0]));
                        finalG = Math.max(finalG, Math.abs(res[1]));
                        finalB = Math.max(finalB, Math.abs(res[2]));
                    }
                }

                newMatrix[y][x][0] = Math.min(Math.max((int) Math.round(finalR), 0), 255);
                newMatrix[y][x][1] = Math.min(Math.max((int) Math.round(finalG), 0), 255);
                newMatrix[y][x][2] = Math.min(Math.max((int) Math.round(finalB), 0), 255);
            }
        }

        return newMatrix;
    }

    /**
     * Performs Canny Edge Detection.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param sigma The blur amount for the initial noise reduction pass.
     * @param lowThresh The lower threshold for edge certainty classification; edges below it are discarded.
     * @param highThresh The upper threshold for edge certainty classification; edges above it are strictly kept.
     * @param boundaryMode The rule for edge handling.
     * @return A binary 3D array showing thinned, tracked edges.
     */
    public static int[][][] applyCannyEdgeDetection(int[][][] originalMatrix, double sigma, int lowThresh, int highThresh, OptionPanel.BoundaryMode boundaryMode) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;

        // 1. gaussian blur & greyscale
        int[][][] grayMatrix = applyGrayscale(originalMatrix, GrayscalePanel.GrayscaleOptions.LUMINANCE);
        double[][] gaussMask = getGaussianMask(sigma);
        int[][][] blurredMatrix = applyConvolution(grayMatrix, gaussMask, boundaryMode);

        // 2. 2-direction sobel
        double[][] sobelX = { {-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1} };
        double[][] sobelY = { {-1, -2, -1}, { 0, 0, 0}, { 1, 2, 1} };

        double[][] magnitude = new double[height][width];
        int[][] angle = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // image is greyscale so we can only work on 1 channel
                double resX = applyMaskToPixel(blurredMatrix, x, y, sobelX, boundaryMode)[0];
                double resY = applyMaskToPixel(blurredMatrix, x, y, sobelY, boundaryMode)[0];

                magnitude[y][x] = Math.sqrt(resX * resX + resY * resY);

                double theta = Math.toDegrees(Math.atan2(resY, resX));
                if (theta < 0) theta += 180;

                // limit to 0, 45, 90, 135
                if ((theta >= 0 && theta < 22.5) || (theta >= 157.5 && theta <= 180)) {
                    angle[y][x] = 0; // horizontal (-)
                } else if (theta >= 22.5 && theta < 67.5) {
                    angle[y][x] = 45;  // diagonal (\)
                } else if (theta >= 67.5 && theta < 112.5) {
                    angle[y][x] = 90;  // vertical (|)
                } else if (theta >= 112.5 && theta < 157.5) {
                    angle[y][x] = 135; // diagonal (/)
                }
            }
        }

        // 3. non-maximum suppression
        double[][] nms = new double[height][width];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double q = 255, r = 255; // default: neighbors have edge

                switch (angle[y][x]) {
                    case 0:
                        q = magnitude[y][x + 1];
                        r = magnitude[y][x - 1];
                        break;
                    case 45:
                        q = magnitude[y + 1][x + 1];
                        r = magnitude[y - 1][x - 1];
                        break;
                    case 90:
                        q = magnitude[y + 1][x];
                        r = magnitude[y - 1][x];
                        break;
                    case 135:
                        q = magnitude[y + 1][x - 1];
                        r = magnitude[y - 1][x + 1];
                        break;
                }

                // keep current pixel only if stronger than both neighbors q and r
                if (magnitude[y][x] >= q && magnitude[y][x] >= r) {
                    nms[y][x] = magnitude[y][x];
                } else {
                    nms[y][x] = 0;
                }
            }
        }

        // 4. double thresholding
        int[][] edges = new int[height][width]; // 0 = non-edge, 1 = weak, 2 = strong

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (nms[y][x] >= highThresh) {
                    edges[y][x] = 2;
                } else if (nms[y][x] >= lowThresh) {
                    edges[y][x] = 1;
                } else {
                    edges[y][x] = 0;
                }
            }
        }

        // 5. edge tracking
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (edges[y][x] == 1) {
                        if (edges[y+1][x] == 2 || edges[y-1][x] == 2 ||
                                edges[y][x+1] == 2 || edges[y][x-1] == 2 ||
                                edges[y+1][x+1] == 2 || edges[y-1][x-1] == 2 ||
                                edges[y-1][x+1] == 2 || edges[y+1][x-1] == 2) {

                            edges[y][x] = 2;
                            changed = true;
                        }
                    }
                }
            }
        }

        // 6. convert edge matrix back into rgb
        int[][][] finalMatrix = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // strong edges become white, all rest is black
                int color = (edges[y][x] == 2) ? 255 : 0;
                finalMatrix[y][x][0] = color;
                finalMatrix[y][x][1] = color;
                finalMatrix[y][x][2] = color;
            }
        }

        return finalMatrix;
    }

    // ==========================================================
    // RGB & HSV
    // ==========================================================

    /**
     * Converts an RGB color to the HSV color space.
     *
     * @return A double array [Hue (0-360), Saturation (0-1), Value (0-255)].
     */
    public static double[] rgbToHsv(int r, int g, int b) {
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;

        double cMax = Math.max(rNorm, Math.max(gNorm, bNorm));
        double cMin = Math.min(rNorm, Math.min(gNorm, bNorm));
        double delta = cMax - cMin;

        double h = 0;
        double s = 0;
        double v = cMax * 255.0;

        if (delta == 0) {
            h = 0;
        } else if (cMax == rNorm) {
            h = 60 * (((gNorm - bNorm) / delta) % 6);
        } else if (cMax == gNorm) {
            h = 60 * (((bNorm - rNorm) / delta) + 2);
        } else if (cMax == bNorm) {
            h = 60 * (((rNorm - gNorm) / delta) + 4);
        }

        if (h < 0) {
            h += 360;
        }

        if (cMax > 0) {
            s = delta / cMax;
        } else {
            s = 0;
        }

        return new double[]{h, s, v};
    }

    /**
     * Converts an HSV color back to the RGB color space.
     *
     * @return An integer array [R, G, B] constrained to 0-255.
     */
    public static int[] hsvToRgb(double h, double s, double v) {
        double vNorm = v / 255.0;

        double c = vNorm * s;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = vNorm - c;

        double rPrime = 0, gPrime = 0, bPrime = 0;

        if (h >= 0 && h < 60) {
            rPrime = c; gPrime = x; bPrime = 0;
        } else if (h >= 60 && h < 120) {
            rPrime = x; gPrime = c; bPrime = 0;
        } else if (h >= 120 && h < 180) {
            rPrime = 0; gPrime = c; bPrime = x;
        } else if (h >= 180 && h < 240) {
            rPrime = 0; gPrime = x; bPrime = c;
        } else if (h >= 240 && h < 300) {
            rPrime = x; gPrime = 0; bPrime = c;
        } else if (h >= 300 && h < 360) {
            rPrime = c; gPrime = 0; bPrime = x;
        }

        int r = (int) Math.round((rPrime + m) * 255);
        int g = (int) Math.round((gPrime + m) * 255);
        int b = (int) Math.round((bPrime + m) * 255);

        r = Math.min(Math.max(r, 0), 255);
        g = Math.min(Math.max(g, 0), 255);
        b = Math.min(Math.max(b, 0), 255);

        return new int[]{r, g, b};
    }

    // ==========================================================
    // Histogram Equalization
    // ==========================================================

    /**
     * Equalizes the image histogram.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A new 3D array representing the equalized image.
     */
    public static int[][][] applyHistogramEqualization(int[][][] originalMatrix) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        double[][][] HSVMatrix = new double[height][width][3];
        int[] histogramV = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];

                HSVMatrix[y][x] = rgbToHsv(r, g, b);

                histogramV[(int)Math.round(HSVMatrix[y][x][2])]++;
            }
        }

        int[] cumHistV = new int[256];
        cumHistV[0] = histogramV[0];
        for (int i = 1; i < 256; i++) {
            cumHistV[i] = cumHistV[i-1] + histogramV[i];
        }

        int CDFMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cumHistV[i] > 0) {
                CDFMin = cumHistV[i];
                break;
            }
        }

        int[] LUT = new int[256];
        int N = width * height;
        float denominator = Math.max(1, N - CDFMin);

        for (int i = 0; i < 256; i++) {
            int newValue = Math.round(((cumHistV[i] - CDFMin) / denominator) * 255);
            LUT[i] = Math.min(Math.max(newValue, 0), 255);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double H = HSVMatrix[y][x][0];
                double S = HSVMatrix[y][x][1];
                int V = (int) Math.round(HSVMatrix[y][x][2]);

                int newV = LUT[V];
                newMatrix[y][x] = hsvToRgb(H, S, newV);
            }
        }

        return newMatrix;
    }

    // ==========================================================
    // Projections
    // ==========================================================

    /**
     * Calculates the horizontal and vertical projections of black pixels in an image.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A 2D array containing [verticalProjectionArray, horizontalProjectionArray].
     */
    public static int[][] getProjections(int[][][] originalMatrix) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;

        int[] verticalProj = new int[width];
        int[] horizontalProj = new int[height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (originalMatrix[y][x][0] == 0) {
                    verticalProj[x]++;
                    horizontalProj[y]++;
                }
            }
        }

        return new int[][] {verticalProj, horizontalProj};
    }

    // ==========================================================
    // MORPHOLOGY OPERATIONS
    // ==========================================================

    /**
     * Applies a morphological operation (erosion or dilation) to the image based on the specified structuring element.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param structuralElement The 2D boolean array representing the active cells of the structuring element.
     * @param mode The boundary handling rule for edges.
     * @param min A boolean flag determining the operation type: true to perform erosion (calculating the local minimum),
     *           or false to perform dilation (calculating the local maximum).
     * @return A new 3D array representing the modified color image.
     */
    private static int[][][] applyMorphology(int[][][] originalMatrix, boolean[][] structuralElement, OptionPanel.BoundaryMode mode, boolean min) {
        if (originalMatrix == null) return null;

        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int windowSize = structuralElement.length;
        int offset = windowSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int minVal = 255;
                int maxVal = 0;

                for (int wy = -offset; wy <= offset; wy++) {
                    for (int wx = -offset; wx <= offset; wx++) {

                        if (structuralElement[wy + offset][wx + offset]) {
                            int px = x + wx;
                            int py = y + wy;

                            int[] rgb = getPixelWithBoundary(originalMatrix, px, py, width, height, mode);
                            int v = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));

                            if (v < minVal) {
                                minVal = v;
                            }
                            if (v > maxVal) {
                                maxVal = v;
                            }
                        }
                    }
                }

                int r = originalMatrix[y][x][0];
                int g = originalMatrix[y][x][1];
                int b = originalMatrix[y][x][2];
                double[] hsv = rgbToHsv(r, g, b);

                if (min) {
                    newMatrix[y][x] = hsvToRgb(hsv[0], hsv[1], minVal);
                } else {
                    newMatrix[y][x] = hsvToRgb(hsv[0], hsv[1], maxVal);
                }

            }
        }

        return newMatrix;
    }

    /**
     * Applies erosion to the image based on the specified structuring element.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param structuralElement The 2D boolean array representing the active cells of the structuring element.
     * @param mode The boundary handling rule for edges.
     * @return A new 3D array representing the modified color image.
     */
    public static int[][][] applyErosion(int[][][] originalMatrix, boolean[][] structuralElement, OptionPanel.BoundaryMode mode) {
        return applyMorphology(originalMatrix, structuralElement, mode, true);
    }

    /**
     * Applies dilation to the image based on the specified structuring element.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param structuralElement The 2D boolean array representing the active cells of the structuring element.
     * @param mode The boundary handling rule for edges.
     * @return A new 3D array representing the modified color image.
     */
    public static int[][][] applyDilation(int[][][] originalMatrix, boolean[][] structuralElement, OptionPanel.BoundaryMode mode) {
        return applyMorphology(originalMatrix, structuralElement, mode, false);
    }


}
