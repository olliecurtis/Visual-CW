
public class SquareBackup {

	static double[][] matrixSobelX = new double[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
	static double[][] matrixSobelY = new double[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
	private static int[][] sobelx = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
	private static int[][] sobely = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
	private static boolean sobel;

	public static void main(String[] args) throws java.io.IOException {

		String fileNameIn = args[0];
		int squareLength = Integer.parseInt(args[1]);
		int theata = Integer.parseInt(args[2]);
		float f1 = Float.parseFloat(args[3]);
		float f2 = Float.parseFloat(args[4]);
		float f3 = Float.parseFloat(args[5]);

		Image inputImage = new Image();
		inputImage.ReadPGM(fileNameIn);

		if (args[6].equals("L")) {
			differenceOfGaussian(inputImage);
		} else if (args[6].equals("E")) {
			sobel = true;
			sobelDoG(inputImage);
		} else {
			System.out.println("Error in selection!");
		}
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
							sumX += inputImage.pixels[x + i][y + j] * sobelx[i + 1][j + 1];
							sumY += inputImage.pixels[x + i][y + j] * sobely[i + 1][j + 1];
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
		//edgeImage.WritePGM("SobelDoG.pgm");
		return edgeImage;
	}

	public static Image differenceOfGaussian(Image inputImage) {
		Image outputImage = inputImage;
		//Image ref1 = inputImage;
		//Image ref2 = inputImage;
		double[][] input = new double[inputImage.width][inputImage.height];
		
		for(int i = 0; i < inputImage.width; i++){
			for(int j = 0; j < inputImage.height; j++){
				input[i][j] = inputImage.pixels[i][j];
			}
		}
		double[][] k1 = getGaussianKernel2D(1);
		double[][] k2 = getGaussianKernel2D(2);
		
		double large1[][] = new double[input.length][input[0].length];
		double large2[][] = new double[input.length][input[0].length];
		
        for (int i = 0; i < input.length; ++i) {
            for (int j = 0; j < input[i].length; ++j) {
                large1[i][j] = (int) singlePixelConvolution(input, i - k1.length / 2, j - k1.length / 2, k1);
                large2[i][j] = (int) singlePixelConvolution(input, i - k2.length / 2, j - k2.length / 2, k2);
                double pixels = large2[i][j] - large1[i][j];
                if(pixels > 255){
                	pixels = 255;
                }
                if(pixels < 0){
                	pixels = 0;
                }
                outputImage.pixels[i][j] = (int)pixels;
                //ref1.pixels[i][j] = (int)large1[i][j];
            }
        }

		/*double large2[][] = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; ++i) {
            for (int j = 0; j < input[i].length; ++j) {
                large2[i][j] = (int) singlePixelConvolution(input, i - k2.length / 2, j - k2.length / 2, k2);
                ref2.pixels[i][j] = (int)large2[i][j];
            }
        }*/
        
        /*for (int i = 0; i < input.length; ++i) {
            for (int j = 0; j < input[i].length; ++j) {
                double pixels = large2[i][j] - large1[i][j];
                if(pixels > 255){
                	pixels = 255;
                }
                if(pixels < 0){
                	pixels = 0;
                }
                outputImage.pixels[i][j] = (int)pixels;
            }
        }*/
        //ref1.WritePGM("ref1.pgm");
        //ref2.WritePGM("ref2.pgm");
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
			res[x] /= total;
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

	public static double[][] convolute(double[][] input, double[][] kernel) {
		double large[][] = new double[input.length][input[0].length];
		for (int i = 0; i < input.length; ++i) {
			for (int j = 0; j < input[i].length; ++j) {
				large[i][j] = singlePixelConvolution(input, i - kernel.length / 2, j - kernel.length / 2, kernel);
				double pixels = large[i][j];
                if(pixels > 255){
                	pixels = 255;
                }
                if(pixels < 0){
                	pixels = 0;
                }
                large[i][j] = pixels;
			}
		}
		return large;
	}
}
