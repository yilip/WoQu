package com.lip.woqu.view;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by etouch on 15/1/5.
 */
public class LifeUrlTextView extends TextView {

    private LifeUrlSpan mPressedSpan;
    private boolean theResult=false;

    public LifeUrlTextView(Context context) {
        super(context);
    }

    public LifeUrlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LifeUrlTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setText(ArrayList<CharSequence> list){
        this.setText("");
        int size=list!=null?list.size():0;
        for(int i=0;i<size;i++){
            this.append(list.get(i));
        }//end for
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CharSequence text=this.getText();
        Spannable spannable= Spannable.Factory.getInstance().newSpannable(text);
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mPressedSpan = getPressedSpan(this, spannable, event);
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                    theResult=true;
                }else{
                    theResult=false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                LifeUrlSpan touchedSpan = getPressedSpan(this, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    if(action== MotionEvent.ACTION_UP){
                        mPressedSpan.onClick(this);
                    }
                }
                mPressedSpan = null;
                Selection.removeSelection(spannable);
                break;
        }
        return theResult;
    }

    private LifeUrlSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();
        x += textView.getScrollX();
        y += textView.getScrollY();
        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        LifeUrlSpan[] link = spannable.getSpans(off, off, LifeUrlSpan.class);
        LifeUrlSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }


}
