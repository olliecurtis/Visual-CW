
/**
 * @author C1227040
 *
 * Difference of Gaussian Class to handle DoG on the image. 
 * 
 * Known Limitations:
 *  - Calculating the DoG orientation is not currently possible so the Hough Space for DoG is not reduced
 * 
 */
public class EdgeDetection{

	private int[][] orientation;
	
	
	// Default constructor
	public EdgeDetection(){}
	
	
	/**
	 * Method to apply DoG operations to an image
	 * 
	 * @param filename - the name of the image to be used
	 * @returns DoG result image
	 */
	public Image DoG(Image edgeImage, boolean isSobel){
		// Create the output image
		Image output = edgeImage;
		
		// Creating a new image for sigma 1 image
		Image im1 = new Image(edgeImage.depth, edgeImage.width, edgeImage.height);
		// Generates 7x7 Kernel with sigma = 1.
		double[] k1 = generateKernel(1);
		// Perform the x and y convolutions on the image with 7x7 kernel
		double[][] im1X = convolveXPixels(output, k1);
		double[][] im1Y = convolveYPixels(output, k1);
		// Creating our first blurred image
		im1 = createImage(edgeImage, im1X, im1Y);
		
		// Creating a new image for sigma 2 image
		Image im2 = new Image(edgeImage.depth, edgeImage.width, edgeImage.height);
		// Generates 13x13 Kernel with sigma = 2.
		double[] k2 = generateKernel(2);
		// Perform the x and y convolutions on the image with 13x13 kernel
		double[][] im2X = convolveXPixels(output, k2);
		double[][] im2Y = convolveYPixels(output, k2);
		// Creating our second blurred image
		im2 = createImage(edgeImage, im2X, im2Y);
		
		// Create a new image to write DoG result to
		Image gaussianImg = new Image(output.depth, output.width, output.height);
		
		// Loop each pixel in image 1 and image 2 
		for (int i = 0; i < output.width; i++) {
            for (int j = 0; j < output.height; j++) {
        		// Subtract the current pixel value in image 2 from image 1 to get DoG pixel value
            	int pixels = (im2.pixels[i][j] - im1.pixels[i][j]);
            	// Truncate negative values to 0
            	if(pixels < 0){
            		pixels = 0;
            	}
            	// Set current DoG pixel to same pixels in output image 
            	gaussianImg.pixels[i][j] = pixels;
            }
        }
		
		// Check if we are using sobel if not create our DoG image
		if(isSobel == false){
			// Write DoG image to file.
	        gaussianImg.WritePGM("DoG.pgm");
		}
        // Return image.
        return gaussianImg;
	}
	
	
	

	private Image createImage(Image edgeImage, double[][] imX, double[][] imY) {
		Image output = new Image(edgeImage.depth, edgeImage.width, edgeImage.height);
		// Looping through and adding convoluted x and y values to make im1 and im2 
		for(int i = 0; i < edgeImage.width; i++){
			for (int j = 0; j < edgeImage.height; j++) {
				output.pixels[i][j] = (int) (imX[i][j] + imY[i][j]);
			}
		}
		return output;
	}


	/**
	 * Performs the convolution on x pixels using 1D Kernel
	 * 
	 * @param input - our image to perform convolution on.
	 * @param kernel - our calculated 1D Kernel
	 * @return image with x pixels convolved.
	 */
	private static double[][] convolveXPixels(Image input, double[] kernel){
		// Determines half kernel width.
		int kHalfWidth = kernel.length / 2;
		// Create our new x convolved image.
		Image convX = new Image(input.depth, input.width, input.height);
		// Initialise our xVals array
		double[][] xVals = new double[input.width][input.height];
		
		for(int x = 0; x < convX.width; x++){
			for(int y = 0; y < convX.height; y++){
				double pixelVal = 0;
				for(int k = - kHalfWidth; k <= kHalfWidth; k++){
					// Ensures we are applying the kernel to the same pixel.
					int xi = x + k;
					int yi = y;
					if(xi < 0){
						xi = -xi;
					}
					else if(xi >= convX.width){
						xi = 2 * convX.width - xi - 1;
					}
					if(yi < 0){
						yi = -yi;
					}
					else if (yi >= convX.height) {
						yi = 2 * convX.height - yi - 1;
					}
					
					// Add to the total, the total value of the kernel being applied to the pixel
					pixelVal += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				xVals[x][y] = (int)pixelVal;
			}
		}
		return xVals;
	}
	
	/**
	 * Performs the convolution on y pixels using 1D Kernel
	 * 
	 * @param input - our image to perform convolution on.
	 * @param kernel - our calculated 1D Kernel
	 * @return image with y pixels convolved.
	 */
	private static double[][] convolveYPixels(Image input, double[] kernel){
		// Determines half kernel width.
		int kHalfWidth = kernel.length / 2;
		// Create our new x convolved image.
		Image convY = new Image(input.depth, input.width, input.height);
		// Initialise our xVals array
		double[][] yVals = new double[input.width][input.height];
		
		for(int x = 0; x < convY.width; x++){
			for(int y = 0; y < convY.height; y++){
				double pixelVal = 0;
				for(int k = - kHalfWidth; k <= kHalfWidth; k++){
					// Ensures we are applying the convolution to the same pixel.
					int xi = x;
					int yi = y + k;
					if(xi < 0){
						xi = -xi;
					}
					else if(xi >= convY.width){
						xi = 2 * convY.width - xi - 1;
					}
					if(yi < 0){
						yi = -yi;
					}
					else if (yi >= convY.height) {
						yi = 2 * convY.height - yi - 1;
					}
					
					// Add to the total, the total value of the kernel being applied to the pixel
					pixelVal += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				yVals[x][y] = (int)pixelVal;
			}
		}
		return yVals;
	}

	/**
	 * Generates a 1D kernel
	 *
	 * @param sigma - the sigma value to generate the kernel
	 *
	 * @return kernel - the DoG kernel.
	*/
	private static double[] generateKernel(int sigma){
		// Kernel size
		int width = (6*sigma+1);
		// Calculate half of kernel.
		int halfwidth = width / 2;
		// Initialise kernel to size 
		double[] kernel = new double[halfwidth*2+1];
		// Total value of kernel
		double total = 0;
		// Loop through -kernel width to +kernel width.
		for(int i = -halfwidth; i <= halfwidth; i++){
			// Computes the Gaussian Value using separable equation
			double comp = computeDOG(i, sigma);
			// Setting kernel value
			kernel[i + halfwidth] = comp;
			// Adding Gaussian value to total
			total += comp;
		}	
		// Dividing kernel values by the total and multiplying by factor to increase the contrast.
		for(int x = 0; x < kernel.length; x++){
			kernel[x] /=  total * 0.49;
		}
		return kernel;
	}

	/**
	 * Computing the Gaussian Value using separable equation.
	 * 
	 * @param x - current kernel value
	 * @param sigma - sigma value
	 * @return Gaussian Value
	 */
	protected static double computeDOG(double x, int sigma){
		return (1.0 / (Math.sqrt(2.0 * Math.PI) * sigma)) *  Math.exp(-x*x / (2.0 * sigma * sigma));
	}
	
	
	/*
	 * SOBEL
	 */
	
	/**
	 * SUMMARY: Generates the Sobel image if input is E
	 * 
	 * @param inputImage - the original image to apply Sobel to.
	 * 
	 * @return edgeImage - the Sobel image.
	 */
	public Image sobelDoG(Image inputImage) {
		
		// Create the Sobel matrices
		int[][] sobelx = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
		int[][] sobely = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		
		// Create our output image
		Image edgeImage = inputImage;
		// Initialise our orientation array
		orientation = new int[inputImage.width][inputImage.height];
		
		// Loop through the input image pixels
		for (int x = 0; x < inputImage.width; x++) {
			for (int y = 0; y < inputImage.height; y++) {
				// Initialise gradient magnitude
				int gradMagnitude = 0;
				// Ensure we are not applying Sobel to the edge of the image
				if ((x > 0) && (x < (inputImage.width - 1)) && (y > 0) && (y < (inputImage.height - 1))) {
					int sumX = 0;
					int sumY = 0;
					// Loop through a 3x3 window
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < 3; j++) {
							// Add the sum of the x pixels convolved with the Sobel X kernel
							sumX += inputImage.pixels[x + i][y + j] * sobelx[2 - i][2 - j];
							// Add the sum of the y pixels convolved with the Sobel Y kernel
							sumY += inputImage.pixels[x + i][y + j] * sobely[2 - i][2 - j];
						}
					}
					// Calculate the gradient magnitude and multiply by a scaling factor to increase contrast
					gradMagnitude = (int)(Math.sqrt((sumX * sumX) + (sumY * sumY)) * 0.3);
					// Normalise into range 0 - 255
					if(gradMagnitude > 255){
						gradMagnitude = 255;
					}
					// Invert the Sobel
					gradMagnitude = (255 - gradMagnitude);
					// Set the edges to display in the output image
					edgeImage.pixels[x][y] = gradMagnitude;
					// Calculate the Sobel orientation
					orientation[x][y] = (int) Math.toDegrees(Math.atan2(sumY, sumX));
				}
			}
		}
		// Sets the orientation
		setOrientation(orientation);
		// Performing the Difference of Gaussian on our Sobel image
		edgeImage = DoG(edgeImage, true);
		
		// Write out the SobelDoG image
		edgeImage.WritePGM("SobelDoG.pgm");
		return edgeImage;
	}
	
	/**
	 * Orientation array getter
	 * 
	 * @return orientation - the orientation array
	 */
	public int[][] getOrientation(){
		return orientation;
	}
	/**
	 * Orientation array setter
	 */
	public void setOrientation(int[][] orientation) {
		this.orientation = orientation;
	}
}
