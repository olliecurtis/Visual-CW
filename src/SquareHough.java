import java.util.ArrayList;

/**
 * @author Ollie
 *
 */
public class SquareHough {

	
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
		
		Image edgeImage = inputImage;
			
		EdgeDetection diffGaussian = new EdgeDetection();
		
		if(args[6].equals("L")){
			edgeImage = diffGaussian.DoG(fileName);
		}
		else if (args[6].equals("E")) {
			edgeImage = diffGaussian.sobelDoG(fileName, inputImage);
		}
		
		houghAccumulator(edgeImage);
		houghLines(accum);
		draw();
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
