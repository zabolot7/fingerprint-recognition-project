package core;

public class FingerprintProcessor {
    private static final boolean[] KMM_DELETION_TABLE = createKMMDeletionTable();

    /**
     * Calculates ROI (Region of Interest) coordinates based on projections while ignoring the background noise.
     *
     * @param projections The vertical and horizontal projections.
     * @param noiseThreshold The minimum number of pixels to count as a part of the interest area.
     * @return Array of minimum and maximum X and Y coordinates of the ROI frame.
     */
    public static int[] calculateROIBounds(int[][] projections, int noiseThreshold) {
        if (projections == null || projections.length != 2) return null;

        int[] verticalProj = projections[0];
        int[] horizontalProj = projections[1];

        int minX = 0;
        int maxX = verticalProj.length - 1;
        int minY = 0;
        int maxY = horizontalProj.length - 1;

        for (int x = 0; x < verticalProj.length; x++) {
            if (verticalProj[x] > noiseThreshold) {
                minX = x;
                break;
            }
        }

        for (int x = verticalProj.length - 1; x >= 0; x--) {
            if (verticalProj[x] > noiseThreshold) {
                maxX = x;
                break;
            }
        }

        for (int y = 0; y < horizontalProj.length; y++) {
            if (horizontalProj[y] > noiseThreshold) {
                minY = y;
                break;
            }
        }

        for (int y = horizontalProj.length - 1; y >= 0; y--) {
            if (horizontalProj[y] > noiseThreshold) {
                maxY = y;
                break;
            }
        }

        if (minX > maxX) { minX = 0; maxX = verticalProj.length - 1; }
        if (minY > maxY) { minY = 0; maxY = horizontalProj.length - 1; }

        return new int[]{minX, minY, maxX, maxY};
    }

    /**
     * Crops the image matrix to selected boundaries.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param bounds ROI boundaries [minX, minY, maxX, maxY].
     * @return A new 3D array representing the cropped image.
     */
    public static int[][][] cropToROI(int[][][] originalMatrix, int[] bounds) {
        if (originalMatrix == null || bounds == null || bounds.length != 4) return null;

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int newWidth = maxX - minX + 1;
        int newHeight = maxY - minY + 1;

        int[][][] croppedMatrix = new int[newHeight][newWidth][3];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                croppedMatrix[y][x][0] = originalMatrix[y + minY][x + minX][0];
                croppedMatrix[y][x][1] = originalMatrix[y + minY][x + minX][1];
                croppedMatrix[y][x][2] = originalMatrix[y + minY][x + minX][2];
            }
        }

        return croppedMatrix;
    }

    /**
     * Draws a red box on the image according to given boundaries.
     *
     * @param originalMatrix The 3D array representing the image.
     * @param bounds Selected boundaries [minX, minY, maxX, maxY].
     * @return A new 3D array representing the image with a red boundary.
     */
    public static int[][][] drawRedBox(int[][][] originalMatrix, int[] bounds) {
        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] matrixWithBox = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrixWithBox[y][x][0] = originalMatrix[y][x][0];
                matrixWithBox[y][x][1] = originalMatrix[y][x][1];
                matrixWithBox[y][x][2] = originalMatrix[y][x][2];
            }
        }

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        for (int x = minX; x <= maxX; x++) {
            if (minY >= 0 && minY < height) {
                matrixWithBox[minY][x][0] = 255; // R
                matrixWithBox[minY][x][1] = 0;   // G
                matrixWithBox[minY][x][2] = 0;   // B
            }
            if (maxY >= 0 && maxY < height) {
                matrixWithBox[maxY][x][0] = 255;
                matrixWithBox[maxY][x][1] = 0;
                matrixWithBox[maxY][x][2] = 0;
            }
        }

        for (int y = minY; y <= maxY; y++) {
            if (minX >= 0 && minX < width) {
                matrixWithBox[y][minX][0] = 255;
                matrixWithBox[y][minX][1] = 0;
                matrixWithBox[y][minX][2] = 0;
            }
            if (maxX >= 0 && maxX < width) {
                matrixWithBox[y][maxX][0] = 255;
                matrixWithBox[y][maxX][1] = 0;
                matrixWithBox[y][maxX][2] = 0;
            }
        }

        return matrixWithBox;
    }

    /**
     * A helper function checking if the target pixel is surrounded by a single, contiguous group of 2, 3, or 4 active neighbors.
     *
     * @param bitMatrix The image matrix.
     * @param x The X coordinate of the pixel to evaluate.
     * @param y The Y coordinate of the pixel to evaluate.
     * @return {@code true} if the neighborhood criteria are met, {@code false} otherwise.
     */
    private static boolean checkNeighbours(int[][] bitMatrix, int x, int y) {
        int[] n = new int[8];
        n[0] = bitMatrix[y-1][x] > 0 ? 1 : 0;
        n[1] = bitMatrix[y-1][x+1] > 0 ? 1 : 0;
        n[2] = bitMatrix[y][x+1] > 0 ? 1 : 0;
        n[3] = bitMatrix[y+1][x+1] > 0 ? 1 : 0;
        n[4] = bitMatrix[y+1][x] > 0 ? 1 : 0;
        n[5] = bitMatrix[y+1][x-1] > 0 ? 1 : 0;
        n[6] = bitMatrix[y][x-1] > 0 ? 1 : 0;
        n[7] = bitMatrix[y-1][x-1] > 0 ? 1 : 0;

        int totalNeighbors = n[0] + n[1] + n[2] + n[3] + n[4] + n[5] + n[6] + n[7];

        if (totalNeighbors < 2 || totalNeighbors > 4) {
            return false;
        }

        int transitions = 0;
        for (int i = 0; i < 8; i++) {
            int current = n[i];
            int next = n[(i + 1) % 8];

            if (current == 0 && next == 1) {
                transitions++;
            }
        }

        return transitions == 1;
    }

    /**
     * A helper function to calculate the pixel's weight.
     *
     * @param bitMatrix The image matrix.
     * @param x The X coordinate of the pixel to evaluate.
     * @param y The Y coordinate of the pixel to evaluate.
     * @return The weight of the pixel.
     */
    private static int calculateWeight(int[][] bitMatrix, int x, int y) {
        int weight = 0;
        if (bitMatrix[y-1][x] > 0)   weight += 1;
        if (bitMatrix[y-1][x+1] > 0) weight += 2;
        if (bitMatrix[y][x+1] > 0)   weight += 4;
        if (bitMatrix[y+1][x+1] > 0) weight += 8;
        if (bitMatrix[y+1][x] > 0)   weight += 16;
        if (bitMatrix[y+1][x-1] > 0) weight += 32;
        if (bitMatrix[y][x-1] > 0)   weight += 64;
        if (bitMatrix[y-1][x-1] > 0) weight += 128;

        return weight;
    }

    /**
     * A helper function to create the Deletion Table needed in the KMM algorithm.
     *
     * @return The Deletion Table.
     */
    private static boolean[] createKMMDeletionTable() {
        boolean[] table = new boolean[256];

        int[] weights = {
                3, 5, 7, 12, 13, 14, 15, 20,
                21, 22, 23, 28, 29, 30, 31, 48,
                52, 53, 54, 55, 56, 60, 61, 62,
                63, 65, 67, 69, 71, 77, 79, 80,
                81, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 93, 94, 95, 97, 99, 101,
                103, 109, 111, 112, 113, 115, 116, 117,
                118, 119, 120, 121, 123, 124, 125, 126,
                127, 131, 133, 135, 141, 143, 149, 151,
                157, 159, 181, 183, 189, 191, 192, 193,
                195, 197, 199, 205, 207, 208, 209, 211,
                212, 213, 214, 215, 216, 217, 219, 220,
                221, 222, 223, 224, 225, 227, 229, 231,
                237, 239, 240, 241, 243, 244, 245, 246,
                247, 248, 249, 251, 252, 253, 254, 255
        };

        for (int w : weights) {
            table[w] = true;
        }
        return table;
    }

    /**
     * A helper function checking if the selected weight is in the Deletion Table.
     *
     * @param weight Pixel's weight.
     * @return {@code true} if the weight is within the Deletion Table, {@code false} otherwise.
     */
    private static boolean isWithinDeletionArray(int weight) {
        if (weight < 0 || weight > 255) return false;
        return KMM_DELETION_TABLE[weight];
    }

    /**
     * Applies the KMM algorithm to the matrix.
     *
     * @param originalMatrix The 3D array representing the image.
     * @return A new 3D array representing the image after KMM algorithm.
     */
    public static int[][][] applyKMM(int[][][] originalMatrix) {
        int height = originalMatrix.length;
        int width = originalMatrix[0].length;
        int[][][] newMatrix = new int[height][width][3];

        int[][] bitMatrix = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (originalMatrix[y][x][0] == 0) {
                    bitMatrix[y][x] = 1; // marking all black pixels as ones
                }
            }
        }

        boolean hasChanged = true;

        while (hasChanged) {
            hasChanged = false;

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (bitMatrix[y][x] == 1) {
                        boolean edgeContact = (bitMatrix[y-1][x] == 0 || bitMatrix[y+1][x] == 0 ||
                                bitMatrix[y][x-1] == 0 || bitMatrix[y][x+1] == 0);
                        if (edgeContact) {
                            bitMatrix[y][x] = 2;
                        } else {
                            boolean cornerContact = (bitMatrix[y-1][x-1] == 0 || bitMatrix[y+1][x+1] == 0 ||
                                    bitMatrix[y+1][x-1] == 0 || bitMatrix[y-1][x+1] == 0);
                            if (cornerContact) {
                                bitMatrix[y][x] = 3;
                            }
                        }
                    }
                }
            }

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (bitMatrix[y][x] == 2 || bitMatrix[y][x] == 3) {
                        if (checkNeighbours(bitMatrix, x, y)) {
                            bitMatrix[y][x] = 4;
                        }
                    }
                }
            }

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (bitMatrix[y][x] == 4) {
                        bitMatrix[y][x] = 0;
                        hasChanged = true;
                    }
                }
            }

            for (int N = 2; N <= 3; N++) {
                for (int y = 1; y < height - 1; y++) {
                    for (int x = 1; x < width - 1; x++) {
                        if (bitMatrix[y][x] == N) {
                            int weight = calculateWeight(bitMatrix, x, y);

                            if (isWithinDeletionArray(weight)) {
                                bitMatrix[y][x] = 0;
                                hasChanged = true;
                            } else {
                                bitMatrix[y][x] = 1;
                            }
                        }
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = (bitMatrix[y][x] == 1) ? 0 : 255;
                newMatrix[y][x][0] = color;
                newMatrix[y][x][1] = color;
                newMatrix[y][x][2] = color;
            }
        }

        return newMatrix;
    }

}
