package com.lip.woqu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lip.woqu.R;

/** Loading View  */
public class LoadingView extends LinearLayout {

	private Context ctx;
    TextView tipTextView;
    RelativeLayout dialog_circle;

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Init(context,attrs);
	}
    /**用于设置背景*/
    public void setBackground(int resourceId){
        dialog_circle.setBackgroundResource(resourceId);
    }
    public void setText(String str){
        tipTextView.setText(str);
    }
    public void setTextColor(int color){
        tipTextView.setTextColor(color );
    }
	private void Init(Context context,AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_view, null);// 得到加载view
        dialog_circle=(RelativeLayout) v.findViewById(R.id.rl_main);
        // main.xml中的ImageView
        ImageView iv_min = (ImageView) v.findViewById(R.id.iv_min);
        ImageView iv_hour = (ImageView) v.findViewById(R.id.iv_hour);
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation min_anim = AnimationUtils.loadAnimation(context, R.anim.loading_clock_min);
        Animation hour_anim = AnimationUtils.loadAnimation(context, R.anim.loading_clock_hour);
        // 使用ImageView显示动画
        iv_min.startAnimation(min_anim);
        iv_hour.startAnimation(hour_anim);

        this.addView(v,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}


}
