package com.lip.woqu.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.lip.woqu.R;
import com.lip.woqu.utils.UtilsManager;

/** 用于实现Activit滑动关闭的view */
public class MyGestureView extends RelativeLayout {

	private Context ctx;
	float down_x = 0, down_y=0,up_x = 0;
	private Scroller mScroller;
	private MyGestureViewChanged myGestureViewChanged = null;
	private MyGestureViewScrollStateChanged myGestureViewScrollStateChanged = null;

	private int screenWidth = 0, asGestureViewShowWidth = 0;
	private boolean isTouch = false;
	private boolean mScrolling = false;

	/** 可以认为是滚动的最小距离 */
	private int mTouchSlop;
	/** 最后点击的点 */
	private float mLastMotionX,mLastMotionY;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;

	/** 手势View是否有效 */
	private boolean isGertureViewEnable = true;

	/** 阴影view */
	private ImageView imageViewShadow;
    public ImageView imageViewBg=null;/**主题背景view*/
	private int shadowImageWidth = 0;

	public MyGestureView(Context context) {
		super(context);
		this.ctx = context;
		Init();
	}
	public MyGestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
		Init();
	}
	private void Init() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		final ViewConfiguration configuration = ViewConfiguration.get(ctx);
		// 获得可以认为是滚动的距离
		mTouchSlop = 2*configuration.getScaledTouchSlop();
		mScroller = new Scroller(getContext());
        /**添加背景imageView*/
//        imageViewBg=new ImageView(ctx);
//        imageViewBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageViewBg.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
//        this.addView(imageViewBg);
        /** 添加边缘阴影imageView */
        imageViewShadow = new ImageView(ctx);
        shadowImageWidth = UtilsManager.dip2px(ctx, 10);
        imageViewShadow.setLayoutParams(new RelativeLayout.LayoutParams(shadowImageWidth, LayoutParams.MATCH_PARENT));
        imageViewShadow.getMeasuredWidth();
        imageViewShadow.setBackgroundResource(R.drawable.slidebar_shadow);
        this.addView(imageViewShadow);
        scrollTo(shadowImageWidth, 0);
	}
	/** 设置监听事件 */
	public void setMyGestureViewChanged(MyGestureViewChanged changed) {
		this.myGestureViewChanged = changed;
	}
	/** 设置监听事件 */
	public void setMyGestureViewScrollStateChanged(MyGestureViewScrollStateChanged changed) {
		this.myGestureViewScrollStateChanged = changed;
	}

	/**
	 * 设置可以认为是展开侧滑栏的屏幕宽度比例 即用户在屏幕左侧screenwith/scale 区域向右滑动时认为是展开侧滑
	 */
	public void setAsGestureViewScale(int scale) {
		asGestureViewShowWidth = screenWidth / scale;
	}

	/** 设置手势View是否可用，用于在activity中在某些特殊时候禁用手势View */
	public void setGestureViewEnable(boolean isEnable) {
		this.isGertureViewEnable = isEnable;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int paddingTop=0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode= MeasureSpec.getMode(heightMeasureSpec);
        int myheight= MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - paddingTop,
                heightMode);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                if(child==imageViewShadow){
                    child.measure(MeasureSpec.makeMeasureSpec(shadowImageWidth, widthMode),myheight);
                }else if (child == imageViewBg) {
                    child.measure(widthMeasureSpec, heightMeasureSpec);
                }else {
                    child.measure(widthMeasureSpec, myheight);
                }
            }
		}
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
        int paddingTop=0;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
                if(child==imageViewShadow){
                    child.layout(0, 0, child.getMeasuredWidth(),child.getMeasuredHeight());
                }else if(child==imageViewBg){
                    child.layout(shadowImageWidth, 0, shadowImageWidth + child.getMeasuredWidth(),
                            child.getMeasuredHeight());
                }else {
                    child.layout(shadowImageWidth, paddingTop, shadowImageWidth + child.getMeasuredWidth(),
                            child.getMeasuredHeight()+paddingTop);
                }
			}
		}
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!isGertureViewEnable) {
			return false;
		}
		int action = ev.getAction();
		float x = ev.getX();
		float y = ev. getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY=y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:// 向右滑动切横向距离大于纵向滑动距离
			int xDiff = (int) (x - mLastMotionX);
			int yDiff=(int) Math.abs(y - mLastMotionY);
			boolean xMoved = (mLastMotionX < asGestureViewShowWidth)&& (xDiff > mTouchSlop)&&(yDiff<mTouchSlop);
			// 判断是否是移动
			if (xMoved) {
				mTouchState = TOUCH_STATE_SCROLLING;
				down_x = x;
				isTouch = true;
				setScrollingCacheEnabled(true);
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		boolean result = mTouchState != TOUCH_STATE_REST;
		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isGertureViewEnable) {
			return false;
		}
		if (mScrolling) {
			return false;
		}
		boolean result = true;
		int action = event.getAction();
		float now_x = event.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			down_x = now_x;
			down_y=event.getY();
			setScrollingCacheEnabled(true);
			break;
		case MotionEvent.ACTION_MOVE:
			if(!isTouch){
				int xDiff = (int) (now_x -down_x);
				int yDiff=(int) Math.abs(event.getY() - down_y);
				boolean xMoved = (down_x < asGestureViewShowWidth)&& (xDiff > mTouchSlop)&&(yDiff<mTouchSlop);
				if(xMoved){
					isTouch=true;
					down_x=now_x;
				}
			}
			if (isTouch) {
				int tox = -(int) (now_x - down_x + 0);
				if (tox > shadowImageWidth) {
					tox = shadowImageWidth;
				}
				// 回调开始滑动
				if (myGestureViewScrollStateChanged!=null) {
					myGestureViewScrollStateChanged.onScrollStateChanged(true);
				}
				scrollTo(tox, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isTouch) {
				isTouch = false;
				if (now_x -down_x>screenWidth/4) {
					smoothScrollTo(-screenWidth, 0);
				} else {
					smoothScrollTo(shadowImageWidth, 0);
				}
			}
			// 回调结束滑动
			if (myGestureViewScrollStateChanged!=null) {
				myGestureViewScrollStateChanged.onScrollStateChanged(false);
			}
			break;
		}
		return result;
	}
	/** 关闭当前view */
	public void close() {
		smoothScrollTo(-screenWidth, 0);
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}
	
	// ////////////////////////////////////
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();
				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}
				if(x>-screenWidth&&x<shadowImageWidth){//未滑出视野
					// Keep on drawing until the animation has finished.
					invalidate();
					return;
				}
				//view已经滑出视野，则终止
			}
		}
		// Done with scroll, clean up state.
		completeScroll();
	}
	private void setScrollingCacheEnabled(boolean enabled) {
		final int size = getChildCount();
		for (int i = 0; i < size; ++i) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				child.setDrawingCacheEnabled(enabled);
			}
		}
	}

	/** 滑动结束 */
	private void completeScroll() {
		boolean needPopulate = mScrolling;
		if (needPopulate) {
			// Done with scroll, no longer want to cache view drawing.
			setScrollingCacheEnabled(false);
			mScroller.abortAnimation();
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			if (oldX != x || oldY != y) {
				scrollTo(x, y);
			}
			if (mScroller.getCurrX() < -screenWidth + 10) {// View隐藏完成
				Message msg = new Message();
				msg.what = 100;
				msg.arg1 = 1;
				handler.sendMessage(msg);
			}
		}
		mScrolling = false;
	}

	private void smoothScrollTo(int x, int y) {
		if (getChildCount() == 0) {
			// Nothing to do.
			setScrollingCacheEnabled(false);
			return;
		}
		int sx = getScrollX();
		int sy = getScrollY();
		int dx = x - sx;
		int dy = y - sy;
		if (dx == 0 && dy == 0) {
			completeScroll();
			if (mScroller.getCurrX() < -screenWidth + 10) {// View隐藏完成
				Message msg = new Message();
				msg.what = 100;
				msg.arg1 = 1;
				handler.sendMessage(msg);
			}
			return;
		}
		setScrollingCacheEnabled(true);
		mScrolling = true;
		int duration = Math.max(Math.abs(dx), 600);
		mScroller.startScroll(sx, sy, dx, dy, duration);
		invalidate();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// 刷新显示图片
				break;
			case 100:// 回调动作
				switch (msg.arg1) {
				case 1:// view隐藏完毕
					if (myGestureViewChanged != null) {
						myGestureViewChanged.onClosed();
					}
					break;
				}
				break;
			}// end switch
		}
	};


	public interface MyGestureViewChanged {
		/** View隐藏完成 */
		public void onClosed();
	}
	public interface MyGestureViewScrollStateChanged {
		public void onScrollStateChanged(boolean isScrolling);
	}
}
