package com.lip.woqu.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lip.woqu.R;


/**
 * Created by admin on 2014/8/15.
 */
public class LoadingProgressDialog extends Dialog {
    TextView tipTextView;
    RelativeLayout rl_main;
    public LoadingProgressDialog(Context context) {
        super(context, R.style.loading_dialog);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.simple_progress_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        layout.setBackgroundResource(R.color.trans);
        rl_main= (RelativeLayout) v.findViewById(R.id.rl_main);
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

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        this.setCancelable(false);// 不可以用“返回键”取消
        this.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
    }


    public void setTipText(String msg){
        tipTextView.setText(msg);// 设置加载信息
    }

    public void setTextColor(int color){
        tipTextView.setTextColor(color );
    }
    public void setBackground(int resourceId){
        rl_main.setBackgroundResource(resourceId);
    }

}
