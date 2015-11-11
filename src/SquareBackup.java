
/**
 * @author Ollie
 *
 */
public class SquareBackup {

	static double[][] matrixSobelX = new double[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
	static double[][] matrixSobelY = new double[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
	private static int[][] sobelx = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
	private static int[][] sobely = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
	private static boolean sobel;

	public static void main(String[] args) throws java.io.IOException {

		String fileNameIn = args[0];
		int squareLength = Integer.parseInt(args[1]);
		int theta = Integer.parseInt(args[2]);
		float f1 = Float.parseFloat(args[3]);
		float f2 = Float.parseFloat(args[4]);
		float f3 = Float.parseFloat(args[5]);

		Image inputImage = new Image();
		inputImage.ReadPGM(fileNameIn);
		if (args[6].equals("L")) {
			Image gaussImg = differenceOfGaussian(inputImage);
			houghTransform(gaussImg);
		} else if (args[6].equals("E")) {
			sobel = true;
			sobelDoG(inputImage);
		} else {
			System.out.println("Error in selection!");
		}
	}

	public static Image differenceOfGaussian(Image inputImage) {
		Image outputImage = inputImage;
		
		double[][] pixelArray = new double[inputImage.width][inputImage.height];
		
		for(int i = 0; i < inputImage.width; i++){
			for(int j = 0; j < inputImage.height; j++){
				pixelArray[i][j] = inputImage.pixels[i][j];
			}
		}
		double[][] k1 = getGaussianKernel2D(1);
		double[][] k2 = getGaussianKernel2D(2);
		
		double[][] im1 = new double[pixelArray.length][pixelArray[0].length];
		double[][] im2 = new double[pixelArray.length][pixelArray[0].length];
        
        for (int i = 0; i < inputImage.width; i++) {
            for (int j = 0; j < inputImage.height; j++) {
                im1[i][j] = (int) singlePixelConvolution(pixelArray, i - k1.length / 2, j - k1.length / 2, k1);
                im2[i][j] = (int) singlePixelConvolution(pixelArray, i - k2.length / 2, j - k2.length / 2, k2);
            	double pixels = im2[i][j] - im1[i][j];
                /*if(pixels > 255){
                	pixels = 255;
                }*/
                if(pixels < 0){
                	pixels = 0;
                }
                outputImage.pixels[i][j] = (int)pixels;
            }
        }

        if(sobel == true){
        	outputImage.WritePGM("SobelDoG.pgm");
        }else{
        	outputImage.WritePGM("DoG.pgm");
        }		
		return outputImage;
	}

	public static double[] diffGaussianKernal(int sigma) {
		int kernelSize = (sigma * 6) + 1;
		int kernelWidth = kernelSize / 2;
		double[] res = new double[kernelWidth * 2 + 1];
		double norm = 1.0 / (Math.sqrt(2 * Math.PI) * sigma);
		double coeff = 2 * sigma * sigma;
		double total = 0;
		for (int x = -kernelWidth; x <= kernelWidth; x++) {
			double g = norm * Math.exp(-x * x / coeff);
			res[x + kernelWidth] = g;
			total += g;
		}
		for (int x = 0; x < res.length; x++) {
			res[x] /= (total * 0.5);
		}
		return res;
	}

	public static double[][] getGaussianKernel2D(int sigma) {
		double[] gaus = diffGaussianKernal(sigma);
		double[][] res = new double[gaus.length][gaus.length];
		for (int x = 0; x < gaus.length; x++) {
			for (int y = 0; y < gaus.length; y++) {
				res[x][y] = gaus[x] * gaus[y];
			}
		}
		return res;
	}

	public static double singlePixelConvolution(double[][] input, int x, int y, double[][] k) {
		int kernelWidth = k.length;
		int kernelHeight = k[0].length;
		double output = 0;
		for (int i = 0; i < kernelWidth; ++i) {
			for (int j = 0; j < kernelHeight; ++j) {
				if (x + i >= 0 && y + j >= 0 && x + i < input.length && y + j < input[0].length)
					output += (input[x + i][y + j] * k[i][j]);
			}
		}
		return output;
	}
	
	private static Image sobelDoG(Image inputImage) {
		Image edgeImage = inputImage;

		int level = 0;
		for (int x = 0; x < inputImage.width; x++) {
			for (int y = 0; y < inputImage.height; y++) {
				level = 255;
				if ((x > 0) && (x < (inputImage.width - 1)) && (y > 0) && (y < (inputImage.height - 1))) {
					int sumX = 0;
					int sumY = 0;
					for (int i = -1; i < 2; i++) {
						for (int j = -1; j < 2; j++) {
							sumX += inputImage.pixels[x + i][y + j] * matrixSobelX[i + 1][j + 1];
							sumY += inputImage.pixels[x + i][y + j] * matrixSobelY[i + 1][j + 1];
						}
					}
					level = Math.abs(sumX) + Math.abs(sumY);
					if (level < 0) {
						level = 0;
					} else if (level > 255) {
						level = 255;
					}
					level = 255 - level;
				}
				edgeImage.pixels[x][y] = level;
			}
		}
		differenceOfGaussian(edgeImage);
		return edgeImage;
	}
	
	/**
	 * Method to generate the Hough Transform and create the accumulator space
	 * 
	 * @param edgeImg - the black and white edge image.
	 * 
	 * @return - the accumulator image.
	 */
	private static void houghTransform(Image edgeImg){
		
		int[][] accum = houghAccumulator(edgeImg);
		
	}
	
	private static int[][] houghAccumulator(Image edgeImg){
		int imgW = edgeImg.width;
		int imgH = edgeImg.height;
		
		// Calculating the max height array needs to be
		int maxR = (int)(Math.sqrt((imgW * imgW) + (imgH * imgH))) / 2;
		// Max theta value
		int maxTheta = 180;
		// Calculate theta step
		double thetaStep = Math.PI / maxTheta;
		// Double height so we can cope with negative r values
		int doubleHeight = 2 * maxR;
		
		Image houghImg = new Image(edgeImg.depth, doubleHeight, maxTheta);
		int[][] accu = new int[doubleHeight][maxTheta];
		
		// Center point
		int cx = imgW / 2;
		int cy = imgH / 2;
		
		// Count number of points
		int numPoints = 0;
		
		// Create 
		double[] sinCache = new double[maxTheta];
		double[] cosCache = new double[maxTheta];
		
		for(int i = 0; i < maxTheta; i++){
			double theta = i * thetaStep;
			sinCache[i] = Math.sin(theta);
			cosCache[i] = Math.cos(theta);
		}
		
		// Accu
		for(int x = 0; x < imgW; x++){
			for(int y = 0; y < imgH; y++){
				if(edgeImg.pixels[x][y] != 0){
					for(int j = 0; j < maxTheta; j++){
						int r = (int)(((x - cx) * cosCache[j]) + ((y - cy) * sinCache[j]));
						r += maxR;
						if(r < 0 || r >= doubleHeight) continue;
						accu[r][j]++;
					}
					numPoints++;
				}
			}
		}

		int max = getMax(maxTheta, doubleHeight, accu);
		for(int x = 0; x < maxTheta; x++){
			for(int r = 0; r < doubleHeight; r++){
				double value = 255 * ((double) accu[r][x]) / max;
				if(r == doubleHeight / 2){
					value = 0;
				}
				houghImg.pixels[r][x] = (int)value;
			}
		}
		
		houghImg.WritePGM("accumulator.pgm");
		return accu;
	}
	
	/**
	 * SUMMARY: Gets the maximum value in the Hough space
	 * 
	 * @param maxTheta 
	 * @param doubleHeight
	 * @param accumulator
	 * 
	 * @return max - maximum Hough space value.
	 */
	private static int getMax(int maxTheta, int doubleHeight, int[][] accu){
		int max = 0;
		for(int k = 0; k < maxTheta; k++){
			for(int l = 0; l < doubleHeight; l++){
				if(accu[l][k] > max){
					max = accu[l][k];
				}
			}
		}
		return max;
	}

}
