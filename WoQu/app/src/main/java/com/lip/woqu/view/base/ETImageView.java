package com.lip.woqu.view.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.lip.woqu.R;

/**
 * 扩展ImageView,支持以下功能：
 * 1、正圆形图片(可设置边框及颜色)
 * 2、圆角图片
 */
public class ETImageView extends ImageView {

    /** 图片显示默认模式、正圆形模式、圆角矩形模式*/
    public static enum DISPLAYMODE {NORMAL,
        CIRCLE,
        ROUNDED}
    private DISPLAYMODE mImageMode = DISPLAYMODE.NORMAL;

    private RectF mDrawableRectF = new RectF();
    private RectF mBorderRectF = new RectF();
    private Matrix mShaderMatrix = new Matrix();
    private Paint mBitmapPaint = new Paint();
    private Paint mBorderPaint = new Paint();

    /** 图片源 */
    private Bitmap mBitmap;
    /** 位图渲染器 */
    private BitmapShader mBitmapShader;
    private int mBitmapResId = 0;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private float mDrawableRadius;
    private float mBorderRadius;
    private boolean isHasInitPaint=false;

    /** 圆形图片的边框颜色 默认白色 */
    private int mBorderColor = Color.WHITE;
    /** 圆形图片的边框宽度 默认为0 */
    private int mBorderWidth = 0;
    /** 圆角图片的弧度 */
    private int mRoundedPixel=16;

    public ETImageView(Context context) {
        super(context);
        Init();
    }

    public ETImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public ETImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Init();
    }
    private void Init(){
        int SDK = android.os.Build.VERSION.SDK_INT;
        if(SDK>=11&&SDK<17){//4.2.2以下的系统关闭硬件加速,花屏主要集中在4.0、4.1系统中
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        InitPaint();
    }
    private void InitPaint(){
        isHasInitPaint=true;
        mDrawableRectF = new RectF();
        mBorderRectF = new RectF();
        mShaderMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBorderPaint = new Paint();
    }
    /**设置图片显示模式*/
    public void setDisplayMode(DISPLAYMODE mode){
        mImageMode=mode;
        switch (mode){
            case NORMAL:
                break;
            case CIRCLE:
                this.setScaleType(ScaleType.CENTER_CROP);
                break;
            case ROUNDED:
                this.setScaleType(ScaleType.CENTER_CROP);
                break;
            default:
                break;
        }
    }
    /**正圆图片模式时设置边框颜色及宽度*/
    public void setImageCircleBorderColorAndWidth(int borderColor, int borderWidth){
        mBorderColor = borderColor;
        mBorderWidth = borderWidth;
    }
    /**圆角矩形模式时设置圆角像素值 */
    public void setImageRoundedPixel(int roundedPixel){
        mRoundedPixel = roundedPixel;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setUpForOnDraw();
    }

    @Override
    public void setImageResource(int resId) {
        try {
            if(mBitmapResId==resId){return;}
            mBitmapResId = resId;
            mBitmap=null;
            if(mBitmapResId!=-1){
                super.setImageResource(resId);
                mBitmap = getBitmapFromDrawable(getDrawable());
            }else{
                super.setImageResource(R.drawable.blank);
            }
            setUpForOnDraw();
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error error){
        }
    }
    @Override
    public void setImageDrawable(Drawable drawable) {
        mBitmapResId =0;
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setUpForOnDraw();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mBitmapResId =0;
        mBitmap = bm;
        if (mBitmap==null||mBitmap.isRecycled()) {
            return;
        }
        super.setImageBitmap(bm);
        setUpForOnDraw();
    }

    @Override
    public void setImageURI(Uri uri) {
        mBitmapResId =0;
        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setUpForOnDraw();
    }
    public Bitmap getImageBitmap(){
        return mBitmap;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (mBitmapResId ==0 && mBitmap!=null && mBitmap.isRecycled()){
                return;
            }else if(mBitmapResId ==-1){
                canvas.drawColor(Color.argb(0x22, 0x00, 0x00, 0x00));
            }
            if (mImageMode == DISPLAYMODE.CIRCLE){
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
                if (mBorderWidth != 0) {
                    canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
                }
            } else if (mImageMode == DISPLAYMODE.ROUNDED){
                canvas.drawRoundRect(mDrawableRectF, mRoundedPixel, mRoundedPixel, mBitmapPaint);
            } else{
                super.onDraw(canvas);
            }
//            if (isNeedProgress && mProgress<100){
//                int w = this.getWidth();
//                int h = this.getHeight();
//                int width = Math.min(w,h)/4;
//                if(mCircleBounds== null){
//                    mCircleBounds = new RectF(w/2-width, h/2-width, w/2+width, h/2+width);
//                }
//                canvas.drawArc(mCircleBounds, 0, 360, false, mBackgroundColorPaint);
//                float scale = (float) mProgress / 100 * 360;
//                canvas.drawArc(mCircleBounds, 270, scale, false, mProgressColorPaint);
//            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
    private void setUpForOnDraw() {
        if (mImageMode== DISPLAYMODE.NORMAL){
            return;
        }
        if (!isHasInitPaint||mBitmap == null) {
            return;
        }
        try{
            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();

            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapPaint.setAntiAlias(true);
            mBitmapPaint.setShader(mBitmapShader); // 位图渲染

            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setAntiAlias(true);
            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            mBorderRectF.set(0, 0, getWidth(), getHeight());
            mBorderRadius = Math.min((mBorderRectF.height() - mBorderWidth) / 2, (mBorderRectF.width() - mBorderWidth) / 2);
            mDrawableRectF.set(mBorderWidth, mBorderWidth, mBorderRectF.width() - mBorderWidth, mBorderRectF.height() - mBorderWidth);
            mDrawableRadius = Math.min(mDrawableRectF.height() / 2, mDrawableRectF.width() / 2);

            updateShaderMatrix();
        }catch (Exception e){
            e.printStackTrace();
        }
        invalidate();
    }
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;
        mShaderMatrix.set(null);
        if (mBitmapWidth * mDrawableRectF.height() > mDrawableRectF.width() * mBitmapHeight) {
            scale = mDrawableRectF.height() / (float) mBitmapHeight;
            dx = (mDrawableRectF.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRectF.width() / (float) mBitmapWidth;
            dy = (mDrawableRectF.height() - mBitmapHeight * scale) * 0.5f;
        }
        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
}
