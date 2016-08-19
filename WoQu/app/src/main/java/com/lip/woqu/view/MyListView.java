package com.lip.woqu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;


/**
 * Created by LJG on 14-3-10. Copy and edit by zbl
 * 1、增加设置ListView是否可以滚动的方法
 */
public class MyListView extends ListView {
    private boolean isCanScroll=true;
    private boolean isDeleteScroll=false;
    private onScrollChangedListener mListener = null;
    public MyListView(Context context) {
        super(context);
        this.setFadingEdgeLength(0);

    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setFadingEdgeLength(0);
    }

    public void setCanScroll(boolean isCanScroll){
        this.isCanScroll=isCanScroll;
    }

    public void setDeleteScroll(boolean isDeleteScroll){
        this.isDeleteScroll = isDeleteScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isCanScroll){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnScrollChangedListener(onScrollChangedListener listener)
    {
        mListener = listener;
    }
    private MainScrollUpDownListener scrollUpDownListener=null;
    public void setMainScrollUpDownListener(MainScrollUpDownListener listener){
        this.scrollUpDownListener=listener;
    }
    float lastPosition=0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isCanScroll){return false;}
        isDeleteScroll = false;
        if(scrollUpDownListener!=null){
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if(lastPosition==0){
                        lastPosition=event.getY();
                    }else{
                        float dis=event.getY()-lastPosition;
                        if(Math.abs(dis)>20){
                            scrollUpDownListener.onScrollUpDown(dis>0?1:0);
                            lastPosition=event.getY();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    lastPosition=0;
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    public boolean getIsCanScroll(){
        return isCanScroll;
    }

    public boolean getIsDeleteScroll(){
        return isDeleteScroll;
    }

    public interface onScrollChangedListener
    {
        public void onScrollValueChanged(int l, int t, int oldl, int oldt);
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mListener != null)
        {
            mListener.onScrollValueChanged(l,t,oldl,oldt);
        }
    }
    public interface MainScrollUpDownListener {
        /**
         * type=0 向上滑动，type=1向下滑动
         */
        public void onScrollUpDown(int type);

    }
}
