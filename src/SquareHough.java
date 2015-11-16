import java.util.ArrayList;

/**
 * @author Ollie
 *
 */
public class SquareHough {

	private static String fileName;
	private static int squareLength;
	private static int changeInTheta;
	private static float f1;
	private static float f2;
	private static float f3;
	private static boolean sobel;
	
	private static int maxR;
	private static double thetaStep;
	private static int doubleHeight;
	private static int[][] accum;
	private static double[] sinCache;
	private static double[] cosCache;
	private static ArrayList<double[]> lineMap;
	
	/**
	 * @param args
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws java.io.IOException {

		fileName = args[0];
		squareLength = Integer.parseInt(args[1]);
		changeInTheta = Integer.parseInt(args[2]);
		f1 = Float.parseFloat(args[3]);
		f2 = Float.parseFloat(args[4]);
		f3 = Float.parseFloat(args[5]);

		Image inputImage = new Image();
		inputImage.ReadPGM(fileName);
		
		Image edgeImage = inputImage;
			
		EdgeDetection diffGaussian = new EdgeDetection();
		boolean sobel = false;
		if(args[6].equals("L")){
			edgeImage = diffGaussian.DoG(inputImage, sobel);
		}
		else if (args[6].equals("E")) {
			edgeImage = diffGaussian.sobelDoG(inputImage);
		}
		houghAccumulator(edgeImage, diffGaussian.getOrientation());
		houghLines(accum);
		drawLines();
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
	private static int[][] houghAccumulator(Image edgeImg, int[][] orientation){
		int imgW = edgeImg.width;
		int imgH = edgeImg.height;
		
		// Calculating the max radius array needs to be
		maxR = (int)(Math.sqrt((imgW * imgW) + (imgH * imgH))) / 2;
		// Calculate theta step
		thetaStep = Math.PI / 180;
		// Double height so we can cope with negative r values
		doubleHeight = 2 * maxR;
		
		Image houghImg = new Image(edgeImg.depth, doubleHeight, 180);
		accum = new int[doubleHeight][180];
		
		genAngleCache();
		
		// Generate accumulator values 
		for(int x = 0; x < imgW; x++){
			for(int y = 0; y < imgH; y++){
				// Check to make sure we are dealing with an edge.
				if(edgeImg.pixels[x][y] != 0){
					for(int j = 0; j < 180; j++){
						
						// Calculating 
						int r = (int)(((x - (imgW/2)) * cosCache[j]) + ((y - (imgH / 2)) * sinCache[j]));
						r += maxR;
						if(r < 0 || r >= doubleHeight) continue;
						accum[r][j]++;
								
					}
				}
			}
		}

		int max = getMax(doubleHeight, accum);
		for(int x = 0; x < 180; x++){
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
	 * Method to setup a cache for the sin and cos values.
	 * This is to save on processing power speeding up the transform.
	 */
	private static void genAngleCache() {
		// Create cache of sin and cos for faster access.
		sinCache = new double[180];
		cosCache = new double[180];
		for(int i = 0; i < 180; i++){
			double theta = i * thetaStep;
			sinCache[i] = Math.sin(theta);
			cosCache[i] = Math.cos(theta);
		}
		
	}

	/**
	 * SUMMARY: Gets the maximum value in the Hough space
	 * 
	 * @param 180 
	 * @param doubleHeight
	 * @param accumulator
	 * 
	 * @return max - maximum Hough space value.
	 */
	private static int getMax(int doubleHeight, int[][] accu){
		int max = 0;
		for(int k = 0; k < 180; k++){
			for(int l = 0; l < doubleHeight; l++){
				if(accu[l][k] > max){
					max = accu[l][k];
				}
			}
		}
		return max;
	}
	
	/**
	 * Finds the lines within the Hough Space
	 * @param accum - the accumulator
	 * @return lineMap - an ArrayList of lines found.
	 */
	private static ArrayList<double[]> houghLines(int[][] accum){
		// Create new lineMap
		lineMap = new ArrayList<double[]>();
		// The threshold value
		int thres = (int) (f1 * getMax(doubleHeight, accum));
		// If the threshold is zero return
		if(thres == 0) return lineMap;
		
		// Loop through range [0, 180] and search for the lines
		for(int i = 0; i < 180; i++){
			loop:
			// Loop 
			for(int j = 19; j < doubleHeight - 19; j++){
				// If accumulator value is above threshold then we found a line
				if(accum[j][i] > thres){
					// Set value of accumulator
					int peak = accum[j][i];
					// Loop through a 19 x 19 window 
					for(int dx = -19; dx <= 19; dx++){
						for(int dy = -19; dy <= 19; dy++){
							int dt = i + dx;
							int dr = j + dy;
							if(dt < 0){
								dt = dt + 180;
							}else if(dt >= 180){
								dt = dt - 180;
							}
							if(accum[dr][dt] > peak){
								continue loop;
							}
						}
					}
					// Find our theta value
					double theta = i * thetaStep;
					// Add our line to the linemap
					lineMap.add(new double[]{theta, j});
				}
			}
		}
		return lineMap;
		
	}
	
	/**
	 * Draws the lines onto the image
	 */
	private static void drawLines(){
		// Obtain the image we are using
		Image linesImage = new Image();
		linesImage.ReadPGM(fileName);
		
		// Create our output PPM image
		ImagePPM outlines = new ImagePPM(linesImage.depth, linesImage.width, linesImage.height);
		outlines = setUpPPM(outlines, linesImage);
		
		// Looping through our Line Map for each (r, theta) values
		for(double[] a : lineMap){
			double theta = a[0];
			double r = a[1];
			
			// For plotting the vertical lines. Assume vertical lines are between 45 and 135 degrees
			if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
	           // Looping through y values to set the line colour to Green
				for (int y = 0; y < linesImage.height; y++) { 
	                //
					int x = (int) (((( r - maxR) - ((y - (linesImage.height / 2)) * Math.sin(theta))) / Math.cos(theta)) + (linesImage.width / 2)); 
	                //
					if (x < linesImage.width && x >= 0) {
	                	// Loop through RGB values
						for(int z = 0; z < 3; z++){
	                		// Setting the Green value 
							if(z == 1){
	                			outlines.pixels[z][x][y] = 255;
	                		}
							// Setting Red and Blue to zero as not required.
		                	else{
		                		outlines.pixels[z][x][y] = 0;
		                	}
	                	}
	                } 
	            } 
	        }else { 
	        	// Looping through x values to set line colour to Green
	            for (int x = 0; x < linesImage.width; x++) { 
	            	//
	                int y = (int) (((( r - maxR) - ((x - (linesImage.width / 2)) * Math.cos(theta))) / Math.sin(theta)) + (linesImage.height / 2)); 
	                //
	                if (y < linesImage.height && y >= 0) { 
	                	// Loop through RGB values
						for(int z = 0; z < 3; z++){
	                		// Setting the Green value 
							if(z == 1){
	                			outlines.pixels[z][x][y] = 255;
	                		}
							// Setting Red and Blue to zero as not required.
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
	 * Method to setup our PPM image
	 * 
	 * @param outlines - our line output image
	 * @param linesImage - our lines image
	 * 
	 * @return outlines - the PPM setup.
	 */
	private static ImagePPM setUpPPM(ImagePPM outlines, Image linesImage) {
		// Loop through PGM image and set it to our output PPM image.
		for(int x = 0; x < linesImage.width; x++){
			for(int y = 0; y < linesImage.height; y++ ){
				for(int z = 0; z < 3; z++){
					outlines.pixels[z][x][y] = linesImage.pixels[x][y];  
				}
			}
		}
		return outlines;
	}

	/**
	 * 
	 * SUMMARY: Detects squares in a 19x19x19 window and prints
	 * 
	 * @return
	 */
	private static double[][][] houghSquares(){
		return null;
	}

}