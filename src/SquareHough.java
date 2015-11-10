
public class SquareHough {

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
			Image houghImg = houghTransform(gaussImg, theta);
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
	
	private static Image houghTransform(Image gaussImg, int theta){
		Image houghImg = gaussImg;
		double cx = gaussImg.width/2;
		double cy = gaussImg.height/2;
		
		double[][] accumulator = new double[gaussImg.height][gaussImg.width];
		for(int a=0; a<gaussImg.height; a++ ){
			for(int b=0; b<gaussImg.width; b++){
				accumulator[a][b] = 0;
			}
		}
		
		
		int rmax = (int) Math.sqrt((cx * cx) + (cy * cy));
		
		houghImg.WritePGM("houghAcc.pgm");
		return houghImg;
	}
	
	/*private static Image houghTransform(Image gaussImg){
		Image houghImg = gaussImg;
		
		int imgW = gaussImg.width;
		int imgH = gaussImg.height;
		double h1 = 0;
		if(gaussImg.height > gaussImg.width){
			h1 = gaussImg.height;
		}else{
			h1 = gaussImg.width;
		}
		double h2 = ( (Math.sqrt(2.0) * h1 ) / 2.0);
		double h3 = 180.0;
		double[][] accu = new double[imgW][imgH];
		
		
		for(int y = 0; y < imgH; y++){
			for(int x = 0; x < imgW; x++){
				//System.out.println(houghImg.pixels[x][y]);
				if(gaussImg.pixels[y][x] > 0){
					for(int t = 0; t < 180; t++){
						double r = ( ((double)x - cx) * Math.cos((double)t)) + (((double)y - cy) * Math.sin((double)t)); 
						//System.out.println(r);
						accu[y][x] = (r + h2) / h3;
					}
				}
				houghImg.pixels[x][y] = (int)accu[x][y];
				//System.out.println(houghImg.pixels[x][y]);
			}
		}
		for(int x = 1; x < gaussImg.width; x++){
			for(int y = 1; y < gaussImg.height; y++){
				if(gaussImg.pixels[x][y] == 0){
					for(int m = -45; m < 45; m++){
						int b = (int) (y - Math.tan( ((m*Math.PI) / 180) * x));
						if(b < gaussImg.width && b > 0){
							//System.out.println(b);
							//System.out.println(m + ": " + acc1[b][m+45+1]);
							acc1[b][m+45+1] = acc1[b][m+45+1]+1;
						}
					}
					for(int m = 45; m <= 135; m++){
						int b = (int) ( (x - y) / Math.tan( ((m*Math.PI) / 180)));
						if(b <= gaussImg.width && b > 0){
							acc2[b][m-45+1] = acc1[b][m-45+1] + 1;
						}
					}
				}
			}
		}
		houghImg.WritePGM("houghAcc.pgm");
		return houghImg;
	}*/
	private static void printgrid(double[][] grid) {
		for(int r=0; r<grid.length; r++) {
		       for(int c=0; c<grid[r].length; c++){
		           System.out.print(grid[r][c] + " ");
		       
		       }
		     System.out.println();
		    }
		
	}
}
