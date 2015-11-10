
public class SquareHough_working {
	
	static double[][] matrixSobelX = new double[][]{
        {1,     0,  -1},
        {2,     0,  -2},
        {1,     0,  -1}
	};
	static double[][] matrixSobelY = new double[][]{
        {-1,    -2,     -1},
        {0,     0,      0},
        {1,     2,      1}
	};
	private static int[][] sobelx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
	private static int[][] sobely = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
	public static void main(String[] args) throws java.io.IOException {
		
		String fileNameIn = args[0];
		int squareLength = Integer.parseInt(args[1]);
		int theata = Integer.parseInt(args[2]);
		float f1 = Float.parseFloat(args[3]);
		float f2 = Float.parseFloat(args[4]);
		float f3 = Float.parseFloat(args[5]);
		
		Image inputImage = new Image();
		inputImage.ReadPGM(fileNameIn);
		
		if(args[6].equals("L")){
			differenceOfGaussian(inputImage);
		}
		else if(args[6].equals("E")){
			sobelDoG(inputImage);
		}
		else{
			System.out.println("Error in selection!");
		}
	}

	private static Image sobelDoG(Image inputImage) {
		Image edgeImage = inputImage;
		
		int level = 0;
		for(int x = 0; x < inputImage.width; x++){
			for(int y = 0; y < inputImage.height; y++){
				level = 255;
				if((x > 0) && (x < (inputImage.width - 1)) && (y > 0) && (y < (inputImage.height - 1))){
					int sumX = 0;
					int sumY = 0;
					for(int i = -1; i < 2; i++){
						for(int j = -1; j < 2; j++){
							sumX += inputImage.pixels[x+i][y+j]* sobelx[i+1][j+1];
							sumY += inputImage.pixels[x+i][y+j]* sobely[i+1][j+1];
						}
					}
					level = Math.abs(sumX) + Math.abs(sumY);
					if(level < 0){
						level = 0;
					}else if(level > 255){
						level = 255;
					}
					level = 255 - level;
				}
				edgeImage.pixels[x][y] = level;
			}
		}
    
		edgeImage.WritePGM("SobelDoG.pgm");
		return edgeImage;
	}
	
	
	public static Image differenceOfGaussian(Image inputImage){
		Image outputImage = inputImage;
		 
		double[][] kernal1 = diffGaussianKernal(1);
		double[][] kernal2 = diffGaussianKernal(2);
		
		Image i1 = diffGaussian(inputImage, kernal1);
		Image i2 = diffGaussian(inputImage, kernal2);
		
		
		for(int x = 0; x < inputImage.width; x++){
			for(int y = 0; y < inputImage.height; y++){
				int pixels = i2.pixels[x][y] - i1.pixels[x][y];
				if(pixels > 255){
					pixels = 255;
				}
				if(pixels < 0){
					pixels = 0;
				}
				outputImage.pixels[x][y] = pixels;
			}
		}
		
		outputImage.WritePGM("DoG.pgm");
		return outputImage;
	} 
	
	public static double[][] diffGaussianKernal(int sigma){
		
		int kernelSize = (sigma * 6) + 1;
		int kernelWidth = kernelSize / 2;
		double[][] kernel = new double[kernelSize][kernelSize];
		//System.out.println(kernelWidth);
		for(int x = - kernelWidth; x <= kernelWidth; x++){
			for(int y = -kernelWidth; y <= kernelWidth; y++){
				kernel[x + kernelWidth][y + kernelWidth] = gaussianEquation(x, y, sigma);
				System.out.println("X: " + x + " " + "Y: " + y);
			}
		}
		//printgrid(kernel);
		return kernel;
	}
	private static void printgrid(double[][] grid) {
		for(int r=0; r<grid.length; r++) {
		       for(int c=0; c<grid[r].length; c++){
		           System.out.print(grid[r][c] + " ");
		       
		       }
		     System.out.println();
		    }
		
	}

	public static double gaussianEquation(int x, int y, int sigma){
		//System.out.println("Kernel Val: " + 1 /  (Math.sqrt(2 * Math.PI) * sigma) * Math.exp( -((x*x) + (y*y) / (2 * (sigma * sigma))) ));
		return 1 /  (Math.sqrt(2 * Math.PI) * sigma) * Math.exp( -((x*x) + (y*y) / (2 * (sigma * sigma))) );
	}
	public static Image diffGaussian(Image image, double[][] kernal){
		
		Image out = new Image(image.depth, image.width, image.height);
		int kernelWidth = kernal[0].length;
		int center = kernelWidth / 2;
		
		for(int i = 0; i < image.width; i++){
			for(int j = 0; j < image.height; j++){
				for(int x = 0; x < kernelWidth; x++){
					for(int y = 0; y < kernelWidth; y++){
						int x2 = i - (x - center);
						int y2 = j - (y - center);
						if(x2 >= 0 && x2 < image.width && y2 >= 0 && y2 < image.height){
							out.pixels[i][j] += image.pixels[x2][y2] * kernal[x][y];
						}
					}
				}
				if(out.pixels[i][j] > 255){
					out.pixels[i][j] = 255;
				}
				if(out.pixels[i][j] < 0){
					out.pixels[i][j] = 0;
				}
			}
		}
		return out;
	}

}
