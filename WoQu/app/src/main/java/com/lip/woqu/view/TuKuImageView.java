package com.lip.woqu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lip.woqu.R;

import java.lang.reflect.Field;



/**
 * Created by etouch on 15/1/29.
 * 本地图库选择图片ImageView
 */
public class TuKuImageView extends ImageView {

    private int width=0;
    private Context mContext;
    private int mBitmapResId = 0;

    public TuKuImageView(Context ctx){
        this(ctx, null);
    }

    public TuKuImageView(Context ctx, AttributeSet attrs){
        super(ctx, attrs);
        this.mContext =ctx;
        Init();
    }
    private void Init(){
        int SDK = android.os.Build.VERSION.SDK_INT;
        if(SDK>=11&&SDK<17){//4.2.2以下的系统关闭硬件加速,花屏主要集中在4.0、4.1系统中
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    public void setImageResource(int resId) {
        mBitmapResId = resId;
        super.setImageResource(resId);
    }
    /**设置ImageView的计算宽度，用于显示图片时计算（如果没有为ImageView设置固定尺寸则该方法必须在代码中显式调用）*/
    public void setCustomBitmapWidth(int width){
        this.width=width;
    }
    /**获取图片的实际宽度*/
    public int getCustomWidth() {
        if(width>0){return width;}
        final ViewGroup.LayoutParams params = this.getLayoutParams();
        if (params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = this.getWidth(); // Get actual image width
        }
        if (width <= 0 && params != null) width = params.width; // Get layout width parameter
        if (width <= 0) width = getImageViewFieldValue(this, "mMaxWidth"); // Check maxWidth parameter
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
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if(mBitmapResId == R.drawable.blank){
                canvas.drawColor(0x22000000);
            }
            super.onDraw(canvas);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
