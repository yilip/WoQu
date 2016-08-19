package com.lip.woqu.view.wheel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollerView extends ScrollView {
	/** 标识wv是否被选中，解决wv在scrollView中无法滚动的问题 */
	public static boolean isWeelViewFocused = false;

	public MyScrollerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyScrollerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollerView(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		boolean tempValue = super.onInterceptTouchEvent(ev);
		if (ev.getAction() == MotionEvent.ACTION_MOVE && isWeelViewFocused) {
			tempValue = false;
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			isWeelViewFocused = false;
		}
		return tempValue;
	}

}
