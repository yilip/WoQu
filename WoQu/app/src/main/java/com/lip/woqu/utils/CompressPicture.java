package com.lip.woqu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

public class CompressPicture {
	/**
	 * bitmap是压缩后的图片文件 savePath是压缩后图片的保存路径
	 */
	private Bitmap bitmap;
	private static final String TAG = "Photo Util";
	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void compressWithBounds(String savePath,String picPath, float width, float heigh) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		bitmap = BitmapFactory.decodeFile(picPath, options); // 此时返回bm为空
		options.inJustDecodeBounds = false;
		int be = Math.round((options.outWidth / width));
		int be2 = Math.round((options.outHeight / heigh));
		if (be < be2)
			be = be2;
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(picPath, options);
		if (bitmap==null) {
			return;
		}
		File file = new File(savePath);
		File parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(CompressFormat.JPEG, 75, out)) {
				out.flush();
				out.close();
			}
			if (!bitmap.isRecycled()) {
				bitmap.recycle();// 记得释放资源，否则会内存溢出
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**存储图片并使其模糊变暗，用于背景图片制作*/
	public boolean compressWithBoundsAndBlur(String savePath,String picPath, float width, float heigh) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高,压缩到需要的大小
            bitmap = BitmapFactory.decodeFile(picPath, options); // 此时返回bm为空
            options.inJustDecodeBounds = false;
            int be = Math.round((options.outWidth / width));
            int be2 = Math.round((options.outHeight / heigh));
            if (be < be2)
                be = be2;
            if (be <= 0)
                be = 1;
            options.inSampleSize = be;
            bitmap = BitmapFactory.decodeFile(picPath, options);
            if (bitmap==null) {
                return false;
            }
//		//模糊化
//       Bitmap bitmapBlur = new JavaBlurProcess().blur(bitmap,bitmap.getWidth()/16);
//		bitmap.recycle();
//		bitmap=bitmapBlur;
//		//变暗
//		Bitmap bitmapDim = dimBitmap(bitmap,0);
//		bitmap.recycle();
//		bitmap=bitmapDim;
            //新模糊方法
//        bitmap=GaussianBlur.BoxBlurFilter(bitmap);
            //存储图片
            File file = new File(savePath);
            File parent = file.getParentFile();
            if (!parent.exists())
                parent.mkdirs();
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (bitmap.compress(CompressFormat.JPEG, 75, out)) {
                    out.flush();
                    out.close();
                }
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();// 记得释放资源，否则会内存溢出
                }
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }catch (Error error) {
            error.printStackTrace();
            return false;
        }
    }

	/**改变图片亮度*/
	public Bitmap dimBitmap(Bitmap srcBitmap,int brightness){
		int imgWidth=srcBitmap.getWidth();
		int imgHeight=srcBitmap.getHeight();
		Bitmap bmp = Bitmap.createBitmap(imgWidth, imgHeight,Config.ARGB_8888);
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[] { 0.7f, 0, 0, 0, brightness, 0, 0.7f,
                0, 0, brightness,// 改变亮度
                0, 0, 0.7f, 0, brightness, 0, 0, 0, 1, 0 });
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return bmp;
	}


	/** 按照宽高读取一个图片，返回为Bitmap
	 * */
	public Bitmap compressWithBoundsPicture(String picPath, float width,float heigh) {
		if (picPath == null || picPath.length() == 0) {
			return null;
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			Bitmap bitmap = BitmapFactory.decodeFile(picPath, options); // 此时返回bm为空
			try {
				options.inJustDecodeBounds = false;
				int be = (int) (options.outWidth / width);
				int be2 = (int) (options.outHeight / heigh);
				if (be < be2){// 使用压缩比例比较小的
					be = be2;
				}
				if (be <= 0)
					be = 1;
				options.inSampleSize =be;
				bitmap = BitmapFactory.decodeFile(picPath, options);
				return bitmap == null ? null : bitmap;
			} catch (OutOfMemoryError e) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/** 按照宽度读取一个图片 */
	public Bitmap compressWithWidth(String picPath,float width) {
		if (picPath == null || picPath.length() == 0) {
			return null;
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			options.inPreferredConfig = Config.RGB_565;
			// 获取这个图片的宽和高
			Bitmap bitmap = BitmapFactory.decodeFile(picPath, options); // 此时返回bm为空
			try {
				options.inJustDecodeBounds = false;
				int be = 1;
				if(width>0){
					be=(int) (options.outWidth / width);
				}
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;
				bitmap = BitmapFactory.decodeFile(picPath, options);
				return bitmap == null ? null : bitmap;
			} catch (OutOfMemoryError e) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

		// int width_new=bitmap.getWidth();
		// int height_new=bitmap.getHeight();
		// float scaleWidth=width/width_new;
		// float scaleHeight=1;
		// if(bitmap.getHeight()<maxHeight){
		// scaleHeight=maxHeight/height_new;
		// }
		// // 创建操作图片用的matrix对象
		// Matrix matrix = new Matrix();
		// // 缩放图片动作
		// matrix.postScale(scaleWidth, scaleHeight);
		// // 创建新的图片
		// bitmap= Bitmap.createBitmap(bitmap, 0, 0,width_new, height_new,
		// matrix, true);
		/** 圆角 */
		// Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
		// (int)maxHeight, Config.ARGB_8888);
		// Canvas canvas = new Canvas(output);
		// int color = 0xff424242;
		// Paint paint = new Paint();
		// Rect rect = new Rect(0, 0, bitmap.getWidth(), (int)maxHeight);
		// RectF rectF = new RectF(rect);
		// final float roundPx = 5;
		//
		// paint.setAntiAlias(true);
		// canvas.drawARGB(0, 0, 0, 0);
		// paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		// canvas.drawBitmap(bitmap, rect, rect, paint);
		// bitmap.recycle();
	}

	/**
	 * 将图片压缩到800*1280左右
	 * @param picPath
	 * @param orientation
	 * @param isShare 是否是分享 若是分享则从新生产图片
	 * @return
	 */
	public String compressImage(String picPath,int orientation,boolean isShare) {
		if (TextUtils.isEmpty(picPath)) {
			return null;
		}

		// 分享的原图都是同一路径，压缩后的图片应该不同
		String shareId = "";
		if (isShare){
			shareId = "_"+System.currentTimeMillis();
		}

		String name;
		try {
			if (picPath.toLowerCase().endsWith(".png")) {
				name = UtilsManager.getMD5(picPath.getBytes())+shareId+".png";
			}else{
				name = UtilsManager.getMD5(picPath.getBytes())+shareId+".jpg";
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			String temp = picPath;
			if (temp.contains("?")) {
				temp = temp.substring(0, temp.indexOf("?"));
			}
			temp = URLDecoder.decode(temp);
			name = temp.substring(temp.lastIndexOf("/") + 1);
		}

		File file = new File(MidData.notebookPicturePath + name);
		File parent = file.getParentFile();
		if (!parent.exists()){
			parent.mkdirs();
		}
		if (file.exists()) {
			if (!isShare && file.length() > 0){
				return MidData.notebookPicturePath + name;
			}
		}else{
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
				if (file.exists()){
					file.delete();
				}
			}
		}

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			options.inPreferredConfig = Config.RGB_565;
			// 获取这个图片的宽和高
			Bitmap bitmap = BitmapFactory.decodeFile(
					picPath, options); // 此时返回bm为空
			try {
				options.inJustDecodeBounds = false;

				int w = options.outWidth;
				int h = options.outHeight;
				float hh = 1280f;
				float ww = 800f;

				//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
				int be = 1;//be=1表示不缩放
				if (h >= w){
					if (h > 1280 && w > 800){
						hh = 1280;
						ww = 800;
					}
				}else{
					if (h > 800 && w > 1280){
						hh = 800;
						ww = 1280;
					}
				}

				if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
					be = (int) (options.outWidth / ww);
				} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
					be = (int) (options.outHeight / hh);
				}
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;//设置缩放比例
				bitmap = BitmapFactory.decodeFile(picPath, options);
				if (orientation != 0){
					Matrix m = new Matrix();
					m.setRotate(orientation);
					Bitmap b2 = Bitmap.createBitmap(
							bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
					if (bitmap != b2) {
						bitmap.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
						bitmap = b2;
	                }
				}

				try {
					FileOutputStream out = new FileOutputStream(file);
					if (picPath.toLowerCase().endsWith(".png")) {
						if (bitmap.compress(CompressFormat.PNG, 90, out)) {
							out.flush();
							out.close();
						}
					} else {
						if (bitmap
								.compress(CompressFormat.JPEG, 90, out)) {
							out.flush();
							out.close();
						}
					}
					bitmap.recycle();
				} catch (FileNotFoundException e) {

					e.printStackTrace();
					return null;
				} catch (IOException e) {

					e.printStackTrace();
					if (file.exists()){
						file.delete();
					}
					return null;
				}
				return MidData.notebookPicturePath + name;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				if (file.exists()){
					file.delete();
				}
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (file.exists()){
				file.delete();
			}
			return null;
		}
	}

	/**
	 * 根据已知图片路径拷贝一份特定宽度的到路径下
	 * @param sourcePath
	 * @param width
	 * @return
	 */
	public String copyImageWithWidth(String sourcePath, String neturl, int width) {
		if (neturl == null || neturl.length() == 0) {
			return null;
		}

		String temp = neturl;
		if (temp.contains("?")) {
			temp = temp.substring(0, temp.indexOf("?"));
		}
		temp = URLDecoder.decode(temp);
		String name = temp.substring(temp.lastIndexOf("/") + 1);

//		if (neturl.toLowerCase().endsWith(".png")) {
//			name = name + ".w" + UtilsManager.getImageWidth(width) + ".png";
//		} else {
			name = name + "!w" + UtilsManager.getImageWidth(width) + ".jpg";
//		}
		File file = new File(MidData.notebookPicturePath + name);
		if (file.exists()) {
			if (file.length() > 0){
				return MidData.notebookPicturePath + name;
			}
		}

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			options.inPreferredConfig = Config.RGB_565;
			// 获取这个图片的宽和高
			Bitmap bitmap = BitmapFactory.decodeFile(
					sourcePath, options); // 此时返回bm为空
			try {
				options.inJustDecodeBounds = false;
				int be = 1;
				if (width > 0) {
					be = (int) (options.outWidth / width);
				}
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;
				bitmap = BitmapFactory.decodeFile(sourcePath, options);

				try {
					FileOutputStream out = new FileOutputStream(file);
					if (sourcePath.toLowerCase().endsWith(".png")) {
						if (bitmap.compress(CompressFormat.PNG, 90, out)) {
							out.flush();
							out.close();
						}
					} else {
						if (bitmap
								.compress(CompressFormat.JPEG, 90, out)) {
							out.flush();
							out.close();
						}
					}
					bitmap.recycle();
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
					if (file.exists()){
						file.delete();
					}
				}
				return MidData.notebookPicturePath + name;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				if (file.exists()){
					file.delete();
				}
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (file.exists()){
				file.delete();
			}
			return null;
		}
	}

	/**
	 * 记事本模块专用
	 * 将图片压缩至万年历目录，并返回图片地址 , isNeedReName 是否需要重命名，重命名则取当前时间戳,选取图片时调用
	 */
	public String compressWithSize(String path,boolean isNeedReName) {
		String name = "";
		if (isNeedReName) {
			String houzhui = path.substring(path.lastIndexOf("."));
			name = new Date().getTime() + houzhui;
		} else {
			name = path.substring(path.lastIndexOf("/") + 1);
		}
		String picPath = MidData.notebookPicturePath + name;
		BitmapFactory.Options options = new BitmapFactory.Options();
		int be = 1;
		File file2 = new File(path);
		if (file2.exists()) {
			long length = file2.length();
			be = (int) (length / 300000) + (length % 300000 > 150000 ? 1 : 0);
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(path, options);
		File file = new File(picPath);
		File parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (path.toLowerCase().endsWith(".png")) {
				if (bitmap.compress(CompressFormat.PNG, 90, out)) {
					out.flush();
					out.close();
				}
			} else {
				if (bitmap.compress(CompressFormat.JPEG, 90, out)) {
					out.flush();
					out.close();
				}
			}
			bitmap.recycle();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return picPath;
	}

	public Bitmap extractThumbNail(final String path, final int height,
			final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0
				&& width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height
					+ ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = "
					+ beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight
					+ ", orig=" + options.outWidth + "x" + options.outHeight
					+ ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
//				Log.e(TAG, "bitmap decode failed");
				return null;
			}
			// Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x"
			// + bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null&&scale!=bm) {
				bm.recycle();
				bm = scale;
			}
			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG,
						"bitmap croped size=" + bm.getWidth() + "x"
								+ bm.getHeight());
			}
			return bm;
		} catch (final OutOfMemoryError e) {
//			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}
		return null;
	}

	public byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		if (bmp==null) {
			return null;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 95, output);
		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/** 按照宽高读取一个图片，返回为Bitmap */
	public static Bitmap getResizedPicture(Context ctx, int resId,
			float s_width, float s_height) {
//		UtilsManager.println(CompressPicture.class, "s_width:"+s_width+"----s_height:"+s_height);
		try {
			// load the origial Bitmap
			Bitmap bitmapOrg = BitmapFactory.decodeResource(ctx
					.getApplicationContext().getResources(), resId);
			int width = bitmapOrg.getWidth();
			int height = bitmapOrg.getHeight();
			// calculate the scale
			float scaleWidth = s_width / (float) width;
			float scaleHeight = s_height / (float) height;
//			UtilsManager.println(CompressPicture.class, "scaleWidth:"+scaleWidth+"----scaleHeight:"+scaleHeight);
			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the Bitmap
			matrix.postScale(scaleWidth, scaleHeight);
			// if you want to rotate the Bitmap
			// matrix.postRotate(45);

			// recreate the new Bitmap
			Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
					height, matrix, true);
//			UtilsManager.println(CompressPicture.class, "w:"+resizedBitmap.getWidth()+"----h:"+resizedBitmap.getHeight());

			bitmapOrg.recycle();
			bitmapOrg=null;
			return resizedBitmap == null ? null : resizedBitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}

	}
}
