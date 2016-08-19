package com.lip.woqu.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.view.base.ETImageView;
import com.lip.woqu.view.utils.VolleyImageLoader;

import java.lang.reflect.Field;

/**
 * 该类中大部分代码是从Volley's NetworkImageView中移植而来，并修改扩展以支持下面功能：
 * 1、圆角图片
 * 2、正圆形图片(可设置边框)
 * 3、加载网页、本地图片使用 setImageUrl方法即可
 */
public class ETNetworkImageView extends ETImageView {
    /** The URL of the network image to load */
    private String mUrl;
    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;
    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;
    private VolleyImageLoader volleyImageLoader;
    /** Local copy of the ImageLoader. */
    private ImageLoader mImageLoader;
    /** Current ImageContainer. (either in-flight or finished) */
    private ImageContainer mImageContainer;
    /** 图片载入结果回调 */
    private ETNetImageCallBack mCallBack;
    /**是否仅从缓存(内存缓存和DISK缓存)中加载图片；网络WIFI和非WIFI状态是否已改变*/
    private boolean isJustLoadFromCache=false,isNetStateChanged=false;

    private final static String ERROR1 = "图片地址为空！";
    private final static String ERROR2 = "图片载入失败！";


    public ETNetworkImageView(Context context) {
        super(context);
    }
    public ETNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ETNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * on the view.
     *
     * @param url The URL that should be loaded into this ImageView.
     * @param defaultImage 默认显示的Image,-1则显示系统默认图片(居中显示)
     */
    public void setImageUrl(String url, int defaultImage) {
        setImageUrl(url, defaultImage, null);
    }

    public void setImageUrl(String url, int defaultImage, ETNetImageCallBack callback) {
        this.mCallBack = callback;
        mDefaultImageId = defaultImage;
        mUrl = url==null?"":url;
        if(volleyImageLoader==null){
            volleyImageLoader=VolleyImageLoader.getInstance(getContext());
            mImageLoader= volleyImageLoader.getVolleyImageLoader();
        }
        int width = getViewWidth();
        String newUrl=volleyImageLoader.getTheTargetScreenImage(url,width);
        setTag(newUrl);
        mUrl=newUrl;

        boolean tempIsJustLoadFromCache=false;
        if(tempIsJustLoadFromCache){
            tempIsJustLoadFromCache=!NetManager.getInstance().isWiFiActive(getContext());
        }
        isNetStateChanged=!(isJustLoadFromCache==tempIsJustLoadFromCache);
        isJustLoadFromCache=tempIsJustLoadFromCache;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }
    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }

    @Override
    public void setImageResource(int resId) {
        if (mImageContainer != null) {
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }
        mUrl="";
        mDefaultImageId = resId;
        super.setImageResource(resId);
    }
    /**用于在setImageUrl后在本类内部设置默认图*/
    private void setTheImageResource(int resId) {
        super.setImageResource(resId);
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getViewWidth();
        boolean isFullyWrapContent = getLayoutParams() != null
                && getLayoutParams().height == LayoutParams.WRAP_CONTENT
                && getLayoutParams().width == LayoutParams.WRAP_CONTENT;
        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        if (width == 0 && !isFullyWrapContent) {
            setTheImageResource(mDefaultImageId);
            return;
        }
        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setTheImageResource(mDefaultImageId);
            if (mCallBack != null) {
                mCallBack.error(ETNetworkImageView.this, ERROR1);
            }
            return;
        }
        // enforce a max size to reduce memory usage
        int maxWidth=width;
        if(maxWidth==0){
            DisplayMetrics dm=getResources().getDisplayMetrics();
            maxWidth=dm.widthPixels;
        }
        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)&&!isNetStateChanged) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
                setTheImageResource(mDefaultImageId);
            }
        }
        isNetStateChanged=false;
        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageContainer newContainer = mImageLoader.get(mUrl,
                new ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mErrorImageId != 0) {
                            setTheImageResource(mErrorImageId);
                        }
                    }
                    @Override
                    public void onResponse(final ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    // don't fade in the image since we know it's cached
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }else{
                            handleResponse(response, isImmediate, false);
                        }
                    }
                },maxWidth,isJustLoadFromCache);
        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
     }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(!isInEditMode()){
            loadImageIfNecessary(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mImageContainer != null) {
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
    /**获取图片的实际宽度*/
    public int getViewWidth() {
        int width = getWidth();
        if(width>0){return width;}
        final LayoutParams params = this.getLayoutParams();
        if (params != null && params.width != LayoutParams.WRAP_CONTENT) {
            width = this.getWidth(); // Get actual image width
        }
        if (width <= 0 && params != null) width = params.width; // Get layout width parameter
        if (width <= 0) width = getImageViewFieldValue(this, "mMaxWidth"); // Check maxWidth parameter
        if (width<=0){
            DisplayMetrics dm=getResources().getDisplayMetrics();
            width=dm.widthPixels;
        }
        return width;
    }
    private int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void handleResponse(ImageLoader.ImageContainer response,  boolean isCached,  boolean allowFadeIn) {
        if (response.getBitmap() != null) {
            setImageBitmap(response.getBitmap());
            if (mCallBack != null) {
                mCallBack.success(ETNetworkImageView.this);
            }
            /**动画渐隐出来取消*/
            if (!isCached && allowFadeIn)
                fadeIn();
        } else {
            setTheImageResource(mDefaultImageId);
        }
    }
    private static final int FADE_TRANSITION = 200;
    private void fadeIn() {
        if(android.os.Build.VERSION.SDK_INT>=14){
            ObjectAnimator alpha = ObjectAnimator.ofFloat(this, View.ALPHA, 0.4f, 1.0f);
            alpha.setDuration(FADE_TRANSITION);
            alpha.start();
        }else{
            Animation animation = new AlphaAnimation(0.4f, 1.0f);
            animation.setDuration(FADE_TRANSITION);
            this.startAnimation(animation);
        }
    }

    public interface ETNetImageCallBack{
        void success(ETNetworkImageView v);
        void error(ETNetworkImageView v, String text);
    }
}
