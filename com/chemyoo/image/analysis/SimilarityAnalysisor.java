package com.chemyoo.image.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * 图片相似度分析
 * @author Administrator
 */
public class SimilarityAnalysisor {
	
	private static final String IMGAGE_EXT = Arrays.toString(ImageIO.getReaderFormatNames());
	
	private static final int W = 32;
	private static final int H = 32;
	
	public static double getSimilarity(File imgFile1, File imgFile2){
		if(isImageFile(imgFile1) && isImageFile(imgFile2)){
			Image picImage1 = file2Image(imgFile1);
			Image picImage2 = file2Image(imgFile2);
			// 获取两个图的汉明距离
		    int hammingDistance = getHammingDistance(
		    		getPixelsWithHanming(picImage1), 
		    		getPixelsWithHanming(picImage2));
		    
		    // 清空图片占用的内存
		    picImage1.flush();
		    picImage2.flush();
		    
		    // 通过汉明距离计算相似度，取值范围 [0.0, 1.0]
		    return calSimilarity(hammingDistance);
		}
		return 0D;
	}
	
	/**
	 * 文件对象转Image对象
	 * @param image
	 * @return
	 */
	private static Image file2Image(File image){
		try (FileInputStream fis = new FileInputStream(image);){
			return ImageIO.read(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static int[] getPixelsWithHanming(Image image){
		// 转换至灰度
		image = toGrayscale(image);
	    // 缩小成32x32的缩略图
	    image = scale(image);
	    // 获取灰度像素数组
	    int[] pixels = getPixels(image);
	    //释放图片对象
	    image.flush();
	    // 获取平均灰度颜色
	    int averageColor = getAverageOfPixelArray(pixels);
	    // 获取灰度像素的比较数组（即图像指纹序列）
	    return getPixelDeviateWeightsArray(pixels, averageColor);
	}
	
	/**(
	 * 判断是否是图片文件
	 * @param f
	 * @return
	 */
	private static boolean isImageFile(File f){
		return f.isFile() && IMGAGE_EXT.contains(getFileExt(f));
	}
	
	private static String getFileExt(File f){
		if(f != null){
			String fileName = f.getName();
			int index = fileName.lastIndexOf('.') + 1;
			if(index > 0){
				return fileName.substring(index);
			}
		}
		return null;
	}
	
	private SimilarityAnalysisor() {
	}

	// 将任意Image类型图像转换为BufferedImage类型，方便后续操作
	private static BufferedImage convertToBufferedFrom(Image srcImage) {
		BufferedImage bufferedImage = new BufferedImage(
				srcImage.getWidth(null), srcImage.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(srcImage, null, null);
		g.dispose();
		return bufferedImage;
	}

	// 转换至灰度图
	private static BufferedImage toGrayscale(Image image) {
		BufferedImage sourceBuffered = convertToBufferedFrom(image);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		return op.filter(sourceBuffered, null);
	}

	// 缩放至32x32像素缩略图
	private static Image scale(Image image) {
		image = image.getScaledInstance(W, H, Image.SCALE_SMOOTH);
		return image;
	}

	// 获取像素数组
	private static int[] getPixels(Image image) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		return convertToBufferedFrom(image).getRGB(0, 0, width, height,
				null, 0, width);
	}

	// 获取灰度图的平均像素颜色值
	private static int getAverageOfPixelArray(int[] pixels) {
		Color color;
		long sumRed = 0;
		for (int i = 0; i < pixels.length; i++) {
			color = new Color(pixels[i], true);
			sumRed += color.getRed();
		}
		return (int) (sumRed / pixels.length);
	}

	// 获取灰度图的像素比较数组（平均值的离差）
	private static int[] getPixelDeviateWeightsArray(int[] pixels, int averageColor) {
		Color color;
		int[] dest = new int[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			color = new Color(pixels[i], true);
			dest[i] = color.getRed() - averageColor > 0 ? 1 : 0;
		}
		return dest;
	}

	// 获取两个缩略图的平均像素比较数组的汉明距离（距离越大差异越大）
	private static int getHammingDistance(int[] a, int[] b) {
		int sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i] == b[i] ? 0 : 1;
		}
		return sum;
	}

	// 通过汉明距离计算相似度
	private static double calSimilarity(int hammingDistance) {
		int length = W * H;
		double similarity = (length - hammingDistance) / (double) length;

		// 使用指数曲线调整相似度结果
		similarity = Math.pow(similarity, 2);
		return similarity;
	}
	
	public static void main(String[] args) {
		File f1 = new File("F:/picture/images/2345.jpg");
		File f2 = new File("F:/picture/images/1529244174585.jpg");
		System.err.println(SimilarityAnalysisor.getSimilarity(f1, f2));
	}
}
