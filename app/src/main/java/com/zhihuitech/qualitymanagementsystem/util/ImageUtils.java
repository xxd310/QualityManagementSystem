package com.zhihuitech.qualitymanagementsystem.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

	public static String encode(String filePath) {
//		Bitmap bitmap = BitmapFactory.decodeFile(filePath);
		Bitmap bitmap = compressBySize(filePath, 1080, 1920);
		//convert to byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			int options = 100;
			System.out.println("baos.toByteArray().length=" + baos.toByteArray().length);
			while(baos.toByteArray().length / 1024 > 100) {	//循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();//重置baos即清空baos
				options -= 10;//每次都减少10
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] bytes = baos.toByteArray();
		//base64 encode
		byte[] encode = Base64.encode(bytes, Base64.DEFAULT);
		String encodeString = new String(encode);
		return encodeString;
	}

	public static Bitmap compressBySize(String path, int targetWidth, int targetHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 不去真的解析图片，只是获取图片的头部信息，包含宽高等；
		Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
		// 得到图片的宽度、高度；
		float imgWidth = opts.outWidth;
		float imgHeight = opts.outHeight;
		// 分别计算图片宽度、高度与目标宽度、高度的比例；取大于等于该比例的最小整数；
		int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
		int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
		opts.inSampleSize = 1;
		if (widthRatio > 1 || widthRatio > 1) {
			if (widthRatio > heightRatio) {
				opts.inSampleSize = widthRatio;
			} else {
				opts.inSampleSize = heightRatio;
			}
		}
		System.out.println("inSampleSize=" + opts.inSampleSize);
		//设置好缩放比例后，加载图片进内容；
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}

}
