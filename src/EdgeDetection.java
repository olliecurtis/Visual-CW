
/**
 * @author C1227040
 *
 * Difference of Gaussian Class to handle DoG on the image. 
 */
public class EdgeDetection{

	private static int[][] sobelx = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
	private static int[][] sobely = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
	
	// Default constructor
	public EdgeDetection(){}
	
	
	/**
	 * Method to apply DoG operations to an image
	 * 
	 * @param filename - the name of the image to be used
	 * @returns DoG result image
	 */
	public Image DoG(String filename){

		Image input = new Image();
		input.ReadPGM(filename);
		
		// Generates 7x7 Kernel with sigma = 1.
		double[] k1 = generateKernel(1);
		// Perform the x and y convolutions on the image with 7x7 kernel
		Image im1X = convolvex(input, k1);
		Image im1Y = convolvey(im1X, k1);
		
		// Generates 13x13 Kernel with sigma = 2.
		double[] k2 = generateKernel(2);
		// Perform the x and y convolutions on the image with 13x13 kernel
		Image im2X = convolvex(input, k2);
		Image im2Y = convolvey(im2X, k2);
		
		// Create a new image to write DoG result to
		Image gaussianImg = new Image(input.depth, input.width, input.height);
		
		// Loop each pixel in image 1 and image 2 
		for (int i = 0; i < input.width; i++) {
            for (int j = 0; j < input.height; j++) {

        		// Subtract the current pixel value in image 2 from image 1 to get DoG pixel value
            	int pixels = (im2Y.pixels[i][j] - im1Y.pixels[i][j]);
            	// Truncate negative values to 0
            	if(pixels < 0){
            		pixels = 0;
            	}
            	// Set current DoG pixel to same pixels in output image 
            	gaussianImg.pixels[i][j] = pixels;
            }
        }
		
		// Write DoG image to file.
        gaussianImg.WritePGM("DoG.pgm");
        // Return image.
        return gaussianImg;
	}
	
	
	/**
	 * Performs the convolution on x pixels using 1D Kernel
	 * 
	 * @param input - our image to perform convolution on.
	 * @param kernel - our calculated 1D Kernel
	 * @return image with x pixels convolved.
	 */
	private static Image convolvex(Image input, double[] kernel){
		// Determines half kernel width.
		int kHalfWidth = kernel.length / 2;
		// Create our new x convolved image.
		Image convX = new Image(input.depth, input.width, input.height);
		
		for(int x = 0; x < convX.width; x++){
			for(int y = 0; y < convX.height; y++){
				double pixelVal = 0;
				for(int k = - kHalfWidth; k <= kHalfWidth; k++){
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
					pixelVal += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				convX.pixels[x][y] = (int)pixelVal;
			}
		}
		return convX;
	}
	
	/**
	 * Performs the convolution on y pixels using 1D Kernel
	 * 
	 * @param input - our image to perform convolution on.
	 * @param kernel - our calculated 1D Kernel
	 * @return image with y pixels convolved.
	 */
	private static Image convolvey(Image input, double[] kernel){
		// Determines half kernel width.
		int kHalfWidth = kernel.length / 2;
		// Create our new x convolved image.
		Image convY = new Image(input.depth, input.width, input.height);
		
		for(int x = 0; x < convY.width; x++){
			for(int y = 0; y < convY.height; y++){
				double pixelVal = 0;
				for(int k = - kHalfWidth; k <= kHalfWidth; k++){
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
					pixelVal += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				convY.pixels[x][y] = (int)pixelVal;
			}
		}
		return convY;
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
		// Dividing kernel values by the total and multiplying by factor to rescale.
		for(int x = 0; x < kernel.length; x++){
			kernel[x] /=  total * 0.5;
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
	 * @param inputImage - the original image to apply sobel to.
	 * 
	 * @return edgeImage - the sobel image.
	 */
	public Image sobelDoG(String filename, Image inputImage) {
		
		Image edgeImage = inputImage; //new Image(inputImage.depth, inputImage.width, inputImage.height);
		edgeImage = DoG(filename);

		int level = 0;
		for (int x = 0; x < inputImage.width; x++) {
			for (int y = 0; y < inputImage.height; y++) {
				level = 0;
				if ((x > 0) && (x < (inputImage.width - 1)) && (y > 0) && (y < (inputImage.height - 1))) {
				int sumX = 0;
					int sumY = 0;
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < 3; j++) {
							sumX += inputImage.pixels[x + i][y + j] * sobelx[2 - i][2 - j];
							sumY += inputImage.pixels[x + i][y + j] * sobely[2 - i][2 - j];
						}
					}
					level = (int)Math.sqrt((sumX * sumX) + (sumY * sumY));//Math.abs(sumX) + Math.abs(sumY);

					if (level < 0) {
						level = 0;
					} else if (level > 255) {
						level = 255;
					}
					edgeImage.pixels[x][y] = level;
					
				
				}
			}
		}
		edgeImage.WritePGM("SobelDoG.pgm");
		return edgeImage;
	}
}
