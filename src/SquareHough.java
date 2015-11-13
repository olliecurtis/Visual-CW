import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * @author Ollie
 *
 */
public class SquareHough {

	static double[][] matrixSobelX = new double[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
	static double[][] matrixSobelY = new double[][] { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
	private static int[][] sobelx = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
	private static int[][] sobely = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
	private static boolean sobel;
	

	private static String fileName;
	private static int squareLength;
	private static int theta;
	private static float f1;
	private static float f2;
	private static float f3;
	
	private static int numPoints;
	private static int maxR;
	private static int maxTheta;
	private static double thetaStep;
	private static int doubleHeight;
	private static int[][] accum;
	private static ArrayList<double[]> lineMap;
	
	public static void main(String[] args) throws java.io.IOException {

		fileName = args[0];
		squareLength = Integer.parseInt(args[1]);
		theta = Integer.parseInt(args[2]);
		f1 = Float.parseFloat(args[3]);
		f2 = Float.parseFloat(args[4]);
		f3 = Float.parseFloat(args[5]);

		Image inputImage = new Image();
		inputImage.ReadPGM(fileName);
		
		if (args[6].equals("L")) {
			Image gaussImg = differenceOfGaussian(inputImage);
			//houghTransform(gaussImg);
			houghAccumulator(gaussImg);
			houghLines(accum);
			draw();
		} else if (args[6].equals("E")) {
			sobel = true;
			Image sobel = sobelDoG(inputImage);
		} else {
			System.out.println("Error in selection!");
		}
	}
	
	/*
	 * DIFFERENCE OF GAUSSIAN
	 */
	
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
        	//outputImage.WritePGM("SobelDoG.pgm");
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
	
	/**
	 * SUMMARY: Perform the convolution on each pixel.
	 * 
	 * @param input - current pixel array.
	 * @param x - our current x position
	 * @param y - our current y position
	 * @param k - our 2D kernel
	 * 
	 * @return
	 */
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
	
	/*
	 * SOBEL
	 */
	
	/**
	 * SUMMARY: Generates the Sobel image if input is E
	 * 
	 * @param inputImage - the original image to apply sobel to.
	 * 
	 * @return edgeImage - the sobel image.
	 */
	private static Image sobelDoG(Image inputImage) {
		Image edgeImage = inputImage;
		
		edgeImage = differenceOfGaussian(edgeImage);

		int level = 0;
		for (int x = 0; x < inputImage.width; x++) {
			for (int y = 0; y < inputImage.height; y++) {
				level = 255;
				if ((x > 0) && (x < (inputImage.width - 1)) && (y > 0) && (y < (inputImage.height - 1))) {
					int sumX = 0;
					int sumY = 0;
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < 3; j++) {
							sumX += inputImage.pixels[x + i][y + j] * matrixSobelX[2 - i][2 -j];
							sumY += inputImage.pixels[x + i][y + j] * matrixSobelY[2 - i][2 - j];
						}
					}
					level = (int)Math.sqrt((sumX * sumX) + (sumY * sumY));//Math.abs(sumX) + Math.abs(sumY);
					if (level < 0) {
						level = 0;
					} else if (level > 255) {
						level = 255;
					}
					//level = 255 - level;
					edgeImage.pixels[x][y] = level;
					
				}
				
			}
		}
		edgeImage.WritePGM("SobelDoG.pgm");
		return edgeImage;
	}
	
	/*
	 * HOUGH TRANSFORM
	 */
	
	/**
	 * SUMMARY: Creates the accumulator space and creates the accumulator.pgm
	 * 
	 * @param edgeImg - this is the black and white image that is generated from DoG
	 * 
	 * @return accu - the accumulator of values.
	 */
	private static int[][] houghAccumulator(Image edgeImg){
		int imgW = edgeImg.width;
		int imgH = edgeImg.height;
		
		// Calculating the max height array needs to be
		maxR = (int)(Math.sqrt((imgW * imgW) + (imgH * imgH))) / 2;
		// Max theta value
		maxTheta = 180;
		// Calculate theta step
		thetaStep = Math.PI / maxTheta;
		// Double height so we can cope with negative r values
		doubleHeight = 2 * maxR;
		
		Image houghImg = new Image(edgeImg.depth, doubleHeight, maxTheta);
		accum = new int[doubleHeight][maxTheta];
		
		// Center point
		int cx = imgW / 2;
		int cy = imgH / 2;
		
		// Count number of points
		numPoints = 0;
		
		// Create 
		double[] sinCache = new double[maxTheta];
		double[] cosCache = new double[maxTheta];
		
		for(int i = 0; i < maxTheta; i++){
			double theta = i * thetaStep;
			sinCache[i] = Math.sin(theta);
			cosCache[i] = Math.cos(theta);
		}
		
		// Generate accumulator values 
		for(int x = 0; x < imgW; x++){
			for(int y = 0; y < imgH; y++){
				if(edgeImg.pixels[x][y] != 0){
					for(int j = 0; j < maxTheta; j++){
							int r = (int)(((x - cx) * cosCache[j]) + ((y - cy) * sinCache[j]));
							r += maxR;
							if(r < 0 || r >= doubleHeight) continue;
							accum[r][j]++;
						
					}
					numPoints++;
				}
			}
		}

		int max = getMax(maxTheta, doubleHeight, accum);
		for(int x = 0; x < maxTheta; x++){
			for(int y = 0; y < doubleHeight; y++){
				double value = 255 * ((double) accum[y][x]) / max;
				if(y == doubleHeight / 2){
					value = 0;
				}
				houghImg.pixels[y][x] = (int)value;
			}
		}
		houghImg.WritePGM("accumulator.pgm");
		return accum;
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
	
	private static ArrayList<double[]> houghLines(int[][] accum){
		lineMap = new ArrayList<double[]>();
		int thres = (int) (f1 * getMax(maxTheta, doubleHeight, accum));
		if(thres == 0) return lineMap;
		
		for(int i = 0; i < maxTheta; i++){
			loop:
			for(int j = 19; j < doubleHeight - 19; j++){
				if(accum[j][i] > thres){
					int peak = accum[j][i];
					
					for(int dx = -19; dx <= 19; dx++){
						for(int dy = -19; dy <= 19; dy++){
							int dt = i + dx;
							int dr = j + dy;
							if(dt < 0){
								dt = dt + maxTheta;
							}else if(dt >= maxTheta){
								dt = dt - maxTheta;
							}
							if(accum[dr][dt] > peak){
								continue loop;
							}
						}
					}
					
					double theta = i * thetaStep;
					lineMap.add(new double[]{theta, j});
				}
			}
		}
		return lineMap;
		
	}
	
	private static void draw(){
		Image linesImage = new Image();
		linesImage.ReadPGM(fileName);

		int height = linesImage.height;
		int width = linesImage.width;
		
		ImagePPM outlines = new ImagePPM(linesImage.depth, linesImage.width, linesImage.height);
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++ ){
				for(int z = 0; z < 3; z++){
					outlines.pixels[z][x][y] = linesImage.pixels[x][y];  
				}
			}
		}
		
		int houghHeight = maxR;
		float cx = width / 2;
		float cy = height / 2;
		
		
		
		for(double[] a : lineMap){
			double theta = a[0];
			double r = a[1];
			double tsin = Math.sin(theta);
			double tcos = Math.cos(theta);
			
			if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
	            for (int y = 0; y < height; y++) { 
	                int x = (int) (((( r - houghHeight) - ((y - cy) * tsin)) / tcos) + cx); 
	                if (x < width && x >= 0) {
	                	for(int z = 0; z < 3; z++){
	                		if(z == 1){
	                			outlines.pixels[z][x][y] = 255;
	                		}
		                	else{
		                		outlines.pixels[z][x][y] = 0;
		                	}
	                	}
	                } 
	            } 
	        } else { 
	            for (int x = 0; x < width; x++) { 
	                int y = (int) (((( r - houghHeight) - ((x - cx) * tcos)) / tsin) + cy); 
	                if (y < height && y >= 0) { 
	                	for(int z = 0; z < 3; z++){
	                		if(z == 1){
	                			outlines.pixels[z][x][y] = 255;
	                		}
		                	else{
		                		outlines.pixels[z][x][y] = 0;
		                	}
	                	}
	                } 
	            } 
	        }
		}
		outlines.WritePPM("lines.ppm");
	}
	
	/**
	 * 
	 * SUMMARY: Detects squares in a 19x19x19 window and prints
	 * 
	 * @return
	 */
	private static ArrayList<double[]> squares(){
		
		return null;
	}

}
