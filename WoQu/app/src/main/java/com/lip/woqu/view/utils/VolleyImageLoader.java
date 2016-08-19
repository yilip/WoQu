package com.lip.woqu.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.UtilsManager;

import java.io.File;



/**
 * 该方法封装了Volley的一些操作，作为ETNetworkImageView的工具类，
 * 加载图片请使用ETNetworkImageView及其中方法
 * */
public class VolleyImageLoader {
	private static final String TAG = "ImageLoader";
	private static VolleyImageLoader myImageLoader2;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

	public static VolleyImageLoader getInstance(Context ctx) {
		if (myImageLoader2 == null) {
			myImageLoader2 = new VolleyImageLoader(ctx);
		}
		return myImageLoader2;
	}

	private VolleyImageLoader(Context context) {
        mQueue = Volley.newRequestQueue(context.getApplicationContext(), MidData.tempDir);
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() );
        int theMaxMemory = maxMemory/6;  // 使用1/6内存
        mImageLoader = new ImageLoader(mQueue,new BitmapLruCache(theMaxMemory));
	}
    public ImageLoader getVolleyImageLoader(){
        return mImageLoader;
    }

    /**判断指定分辨率图片在本地是否存在，存在则返回本地地址，不存在则返回指定分辨率的URL地址*/
    public String getTheTargetScreenImage(String imageUrl,int width){
        try {
            if (TextUtils.isEmpty(imageUrl)){
                return "";
            }
            String newUrl = imageUrl;
            if(imageUrl.startsWith("http")||imageUrl.startsWith("ftp")){
                if (imageUrl.startsWith("http://static.suishenyun.net")) {/* 这个服务器是以.分割缩略图的 */
                    newUrl += ".w" + UtilsManager.getImageWidth(width) + ".jpg";
                } else if (imageUrl.contains(".static.suishenyun.net")) {/* 这个服务器是以!分割缩略图的 */
                    newUrl=imageUrl.replaceAll("!w[0-9]*\\.jpg", "");
                    newUrl += "!w" + UtilsManager.getImageWidth(width) + ".jpg";
                }
                String fileName=UtilsManager.MD5(newUrl);
                File file=new File(MidData.tempDir+fileName);
                if(file.exists()){
                    return file.getAbsolutePath();
                }else{
                    return newUrl;
                }
            }else{
                return newUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    class BitmapLruCache extends android.support.v4.util.LruCache<String, Bitmap> implements ImageLoader.ImageCache {
        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }
        @Override
        protected int sizeOf(String key, Bitmap value) {
            int bytes = (value.getRowBytes() * value.getHeight());
            return bytes;
        }
        @Override
        public Bitmap getBitmap(String key) {
            return this.get(key);
        }

        @Override
        public void putBitmap(String key, Bitmap bitmap) {
            this.put(key, bitmap);
        }
    }



}