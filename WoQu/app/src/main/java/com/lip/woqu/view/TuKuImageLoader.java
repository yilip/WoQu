package com.lip.woqu.view;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.lip.woqu.utils.CompressPicture;
import com.lip.woqu.utils.MemoryCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by etouch on 15/1/29.
 * 从本地图区加载Image工具类,只用于加载本地图库图片使用
 */
public class TuKuImageLoader {


    public static TuKuImageLoader myImageLoader2;
    private MemoryCache memoryCache = new MemoryCache();
    private Map<Integer, String> imageViews;
    private Map<ImageView, Future> futureMap;
    private Map<String, ReentrantLock> uriLocks;
    // 线程池-专用于加载本地图片
    private ExecutorService executorService4LocalPic;
    private CompressPicture compressPicture;
    private Context ctx;
    private ContentResolver contentResolver;
    /** 加载缩略图精度 */
    int thumbnailsType = MediaStore.Video.Thumbnails.MICRO_KIND;

    public static TuKuImageLoader getInstance(Context ctx) {
        if (myImageLoader2 == null) {
            myImageLoader2 = new TuKuImageLoader(ctx, true);
        }
        return myImageLoader2;
    }

    private TuKuImageLoader(Context context, boolean needThreadPool) {
        if (needThreadPool) {
            // 固定工作线程数量的线程池
            executorService4LocalPic = Executors.newFixedThreadPool(5);
        }
        ctx = context;
        contentResolver = ctx.getContentResolver();
        memoryCache = new MemoryCache();
        imageViews = Collections.synchronizedMap(new HashMap<Integer, String>());
        futureMap = Collections.synchronizedMap(new HashMap<ImageView, Future>());
        uriLocks = new WeakHashMap<String, ReentrantLock>();
        DisplayMetrics metric = ctx.getResources().getDisplayMetrics();
        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        if (width > 480 && height > 800) {
            thumbnailsType = MediaStore.Video.Thumbnails.MINI_KIND;
        } else {
            thumbnailsType = MediaStore.Video.Thumbnails.MICRO_KIND;
        }
    }

    /**
     * @param imageView
     *            要显示图片的ImageView
     * @param url
     *            图片地址
     * @param defaultResourceId
     *            ImageView需要显示的默片认图片本地ResourceId,如果为-1则不加载默认图
     * @param id
     *            imageView中要显示的图片id(用于如加载本地图库时记录图片id),默认-1
     * @param isLoadOnlyFromCache
     *            true：滑动中不加载图片，false：直接加载图片
     */
    public void displayImage(final TuKuImageView imageView, final String url, final int defaultResourceId,
                             final long id, final boolean isLoadOnlyFromCache) {
        if (TextUtils.isEmpty(url)) {
            if (defaultResourceId != -1) {
                imageView.setImageResource(defaultResourceId);
            }
            return;
        }
        imageViews.put(imageView.hashCode(), url);
        Bitmap bitmap = memoryCache.get(url);// 先从内存缓存中查找
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (defaultResourceId != -1) {
                imageView.setImageResource(defaultResourceId);
            }
            if (!isLoadOnlyFromCache) {
                queuePhoto(url,imageView, id);
            }
        }
    }

    private void queuePhoto(String url, TuKuImageView imageView, long id) {
        ImageInfo imageInfo = new ImageInfo(url, imageView, getLockForUri(url), id);
        Future oldFuture = futureMap.get(imageView);
        if (oldFuture != null && !oldFuture.isCancelled() && !oldFuture.isDone()) {
            oldFuture.cancel(true);
        }
        if (!executorService4LocalPic.isShutdown()) {
            Future future = executorService4LocalPic.submit(new PhotosLoader(imageInfo));
            futureMap.put(imageView, future);
        }
    }

    private class ImageInfo {
        public String url;
        public TuKuImageView imageView;
        public ReentrantLock loadFromUriLock;
        /**图库图片id*/
        public long id=-1;

        public ImageInfo(String u, TuKuImageView i, ReentrantLock r, long id) {
            url = u;
            imageView = i;
            loadFromUriLock = r;
            this.id = id;
        }
    }
    // Task for the queue
    class PhotosLoader implements Runnable {
        ImageInfo imageInfo;

        PhotosLoader(ImageInfo imageInfo) {
            this.imageInfo = imageInfo;
        }
        @Override
        public void run() {
            ReentrantLock loadFromUriLock = imageInfo.loadFromUriLock;
            loadFromUriLock.lock();
            Bitmap bmp = null;
            int imageViewWidth = imageInfo.imageView.getCustomWidth();
            try {
                if (imageViewReused(imageInfo)) {
                    imageViews.remove(imageInfo.imageView.hashCode());
                    return;
                }
                if (imageInfo.id != -1) {// 选择图片界面加载图片方式(图库)
                    bmp = MediaStore.Images.Thumbnails
                            .getThumbnail(contentResolver, imageInfo.id, thumbnailsType, null);
                    if (bmp == null) {
                        if (compressPicture == null) {
                            compressPicture = new CompressPicture();
                        }
                        bmp = compressPicture.compressWithWidth(imageInfo.url, 100);
                    }
                    if (bmp != null) {
                        int bmp_w = bmp.getWidth();
                        int view_w = imageViewWidth * 3 / 4;
                        if (bmp_w > view_w) {
                            Bitmap scaleBmp = Bitmap.createScaledBitmap(bmp, view_w, view_w * bmp.getHeight() / bmp_w, false);
                            bmp.recycle();
                            bmp = scaleBmp;
                        }
                    }
                } // end 其他页面图片加载
                if (bmp != null) {
                    memoryCache.put(imageInfo.url, bmp);
                }
            } catch(Exception e){

            } finally {
                loadFromUriLock.unlock();
            }
            if (imageViewReused(imageInfo)) {
                imageViews.remove(imageInfo.imageView.hashCode());
                return;
            }
            BitmapDisplayer bd = new BitmapDisplayer(bmp, imageInfo);
            handler.post(bd);
        }
    }
    Handler handler = new Handler();
    /** 停止指定的imageView的图片加载线程 */
    public void stopLoad(ImageView imageView) {
        Future oldFuture = futureMap.get(imageView);
        if (oldFuture != null && !oldFuture.isCancelled() && !oldFuture.isDone()) {
            oldFuture.cancel(true);
        }
    }

    /**
     * 防止图片错位
     */
    boolean imageViewReused(ImageInfo imageInfo) {
        String tag = imageViews.get(imageInfo.imageView.hashCode());
        if (tag == null || !tag.equals(imageInfo.url))
            return true;
        return false;
    }

    private ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = uriLocks.get(uri);
        if (lock == null) {
            lock = new ReentrantLock();
            uriLocks.put(uri, lock);
        }
        return lock;
    }

    // 用于在UI线程中更新界面
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ImageInfo imageInfo;
        public BitmapDisplayer(Bitmap b, ImageInfo p) {
            bitmap = b;
            imageInfo = p;
        }

        public void run() {
            if (imageViewReused(imageInfo)) {
                return;
            }
            if (bitmap != null) {
                imageInfo.imageView.setImageBitmap(bitmap);
            }
        }
    }

    public Bitmap getBitmapFromCache(String url) {
        if (url != null) {
            return memoryCache.get(url);
        } else {
            return null;
        }
    }

    public MemoryCache getMemoryCache() {
        return this.memoryCache;
    }

    /** 清图片缓存 */
    public void clearCache() {
        memoryCache.clearAll();
    }

    /** 因为本类改为单例，所以此方法只需在退出App的时候调用即可，其他时候不用调! */
    public void shutDown() {
        shutdownAndAwaitTermination(executorService4LocalPic);
        myImageLoader2 = null;
    }

    // shuts down an ExecutorService in two phases,
    // first by calling shutdown to reject incoming tasks,
    // and then calling shutdownNow, if necessary,
    // to cancel any lingering tasks
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        pool.shutdownNow(); // Cancel currently executing tasks
    }


}
