import java.awt.Point;
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
	
	private static int maxRadius;
	private static int[][] accum;
	private static double[] sinLookupTable;
	private static double[] cosLookupTable;
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
		houghSquares();
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
	private static int[][] houghAccumulator(Image edgeImg, int[] orientation){
		
		// Calculating the max r value
		maxRadius = (int)Math.sqrt(2) * ((edgeImg.width + edgeImg.height) / 2);
		// Create a new Accumulator Image of height twice the width of maxRho and height of our max theta 
		Image houghImg = new Image(edgeImg.depth, 2 * maxRadius, 180);
		// Initialise accumulator 
		accum = new int[180][2 * maxRadius];
		
		// Generate accumulator values 
		for(int x = 0; x < edgeImg.width; x++){
			for (int y = 0; y < edgeImg.height; y++) {
				if(edgeImg.pixels[x][y] > 0){
					for(int theta = 0; theta < 180; theta++){
						int rho = (int)((x - (edgeImg.width / 2)) * Math.cos(Math.toRadians(theta))  + (y - (edgeImg.height / 2)) * Math.sin(Math.toRadians(theta)));
						rho += maxRadius; 
						accum[theta][rho]++;
					}
				}
			}
		}

		int max = 0;
		// Plot the accumulator values
		for(int x = 0; x < 180; x++){
			for(int y = 0; y < 2 * maxRadius; y++){
				// Finding the max accumulator value
				if(accum[x][y] > max){
					max = accum[x][y];
				}
				// Calculating the pixel value for our lines image
				double value = 255 * ((double) accum[x][y]) / max;
				// If pixel center point set it to black
				if(y == 2 * maxRadius / 2){
					value = 0;
				}
				houghImg.pixels[y][x] = (int)value;
			}
		}
		
		houghImg.WritePGM("accumulator.pgm");
		return accum;
	}
	
	/**
	 * Finds the lines within the Hough Space
	 * @param accum - the accumulator
	 * @return lineMap - an ArrayList of lines found.
	 */
	private static ArrayList<double[]> houghLines(int[][] accum){
		// Create new lineMap
		lineMap = new ArrayList<double[]>();
		
		int max = 0;
		for(int k = 0; k < 180; k++){
			for(int l = 0; l < 2 * maxRadius; l++){
				if(accum[k][l] > max){
					max = accum[k][l];
				}
			}
		}
		
		// The threshold value
		int thres = (int) (f1 * max);
		// If the threshold is zero return
		if(thres == 0) return lineMap;
		
		// Loop through range [0, 180] and search for the lines
		for(int i = 0; i < 180; i++){
			Start:
			// Loop 
			for(int j = 19; j < 2 * maxRadius - 19; j++){
				// If accumulator value is above threshold then we found a line
				if(accum[i][j] > thres){
					// Set value of accumulator
					int peak = accum[i][j];
					// Loop through a 19 x 19 window to check for local maximum
					for(int x = -19; x <= 19; x++){
						for(int y = -19; y <= 19; y++){
							int a = i + x;
							int b = j + y;
							// If a less than zero bring it into our range.
							if(a < 0){
								a += 180;
							}
							// If a greater than 180 bring it back into range.
							else if(a >= 180){
								a -= 180;
							}
							// If there is a better peak restart loop @Start
							if(accum[a][b] > peak){
								continue Start;
							}
						}
					}
					// Find our theta value
					double theta = i * (Math.PI / 180);
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
					int x = (int) (((( r - maxRadius) - ((y - (linesImage.height / 2)) * Math.sin(theta))) / Math.cos(theta)) + (linesImage.width / 2)); 
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
	                int y = (int) (((( r - maxRadius) - ((x - (linesImage.width / 2)) * Math.cos(theta))) / Math.sin(theta)) + (linesImage.height / 2)); 
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
		for(double[] a: lineMap){
			double theta = a[0];
			double rho = a[1];
			
			 
			
		}
		return null;
	}

}


// TODO: Sort this out
/* orientation = new int[edgeImg.width][edgeImg.height];
		
		// Generate accumulator values 
		for(int x = 1; x < edgeImg.width - 1; x++){
			for(int y = 1; y < edgeImg.height - 1; y++){
				for (int i = -7; i < 7; i++) {
					for (int j = -7; j < 7; j++) {
						int theta = (int)Math.atan(y / x);
						orientation[x][y] = (int)theta;

						System.out.println(orientation[x][y]);
					}
				}
				// Check to make sure we are dealing with an edge.
				if(edgeImg.pixels[x][y] != 0){
					for(int i = 0; i < 180; i++){
						if(orientation[x][y] > -90 && orientation[x][y] < 90){
							// Calculating r value - width / 2 and - height / 2 is so we can handle values in rotated orientation 
							int r = (int)(((x - (edgeImg.width / 2)) * Math.cos(orientation[x][y])) + ((y - (edgeImg.height / 2)) * Math.sin(orientation[x][y])));
							//System.out.println(r);
							r += maxR;
							if(r < 0 && r >= (2 * maxR)){
								continue;
							}
							accum[r + maxR][orientation[x][y]]++;
							/*if(r >= -maxR && r <= maxR){
								accum[r + maxR][t]++;
							}
						}
					}
							
				}
						
				}
			}*/