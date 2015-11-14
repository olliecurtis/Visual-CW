
public class DifferenceOfGaussian{

	private static String fileName;

	public Image DifferenceOfGaussian(String filename){

		fileName = filename;

		Image input = new Image();
		input.ReadPGM(fileName);

		//Image gaussianImg = diffOfGaussian(input);
		double[] k1 = generateKernel(1);
		Image im1X = convolvex(input, k1);
		Image im1Y = convolvey(im1X, k1);
		im1Y.WritePGM("im1.pgm");

		double[] k2 = generateKernel(2);
		Image im2X = convolvex(input, k2);
		Image im2Y = convolvey(im2X, k2);
		im2Y.WritePGM("im2.pgm");

		Image gaussianImg = new Image(input.depth, input.width, input.height);
		for (int i = 0; i < input.width; i++) {
            for (int j = 0; j < input.height; j++) {
            	int pixels = (im2Y.pixels[i][j] - im1Y.pixels[i][j]);
            	if(pixels < 0){
            		pixels = 0;
            	}
            	gaussianImg.pixels[i][j] = pixels;
            }
        }

        gaussianImg.WritePGM("DoG.pgm");
        return gaussianImg;
	}

	/*
	 * Performs the Difference of Gaussian on an Image
	 *
	 * @param input - the image to perform DoG on
	 *
	 * @return gaussianImg - the image with DoG performed on.
	*/
	public static Image diffOfGaussian(Image input){
		Image gaussianImg = input;

		double[][] pixels = new double[input.width][input.height];
		for (int i = 0; i < input.width; i++) {
			for (int j = 0; j < input.height; j++) {
				pixels[i][j] = input.pixels[i][j];
			}
		}
		return gaussianImg;
	}


	private static Image convolvex(Image input, double[] kernel){
		int kHalfWidth = kernel.length / 2;
		Image convX = new Image(input.depth, input.width, input.height);
		for(int x = 0; x < convX.width; x++){
			for(int y = 0; y < convX.height; y++){
				double d = 0;
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
					d += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				convX.pixels[x][y] = (int)d;
			}
		}
		return convX;
	}

	private static Image convolvey(Image input, double[] kernel){
		int kHalfWidth = kernel.length / 2;
		Image convY = new Image(input.depth, input.width, input.height);
		for(int x = 0; x < convY.width; x++){
			for(int y = 0; y < convY.height; y++){
				double d = 0;
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
					d += input.pixels[xi][yi] * kernel[k + kHalfWidth];
				}
				convY.pixels[x][y] = (int)d;
			}
		}
		return convY;
	}

	/*
	 * Generates a 1D kernel
	 *
	 * @param sigma - the sigma value to generate the kernel
	 *
	 * @return kernel - the DoG kernel.
	*/
	private static double[] generateKernel(int sigma){
		int width = (6*sigma+1);
		int halfwidth = width / 2;

		double[] kernel = new double[halfwidth*2+1];
		double total = 0;
		
		for(int i = -halfwidth; i <= halfwidth; i++){
			double comp = computeDOG(i, sigma);
			kernel[i + halfwidth] = comp;
			total += comp;
		}
		for(int x = 0; x < kernel.length; x++){
			kernel[x] /=  total * 0.5;
		}
		return kernel;
	}

	protected static double computeDOG(double x, int sigma){
		return (1.0 / (Math.sqrt(2.0 * Math.PI) * sigma)) *  Math.exp(-x*x / (2.0 * sigma * sigma));
	}


	private static void printgrid(double[] grid) {
		for(int r=0; r<grid.length; r++) {
		           System.out.print(grid[r] + " ");

		     }
				 System.out.println();

	}

}
