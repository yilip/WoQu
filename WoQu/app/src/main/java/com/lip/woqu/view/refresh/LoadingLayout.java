package com.lip.woqu.view.refresh;


import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lip.woqu.R;

import java.util.Random;


public class LoadingLayout extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

//	private final ProgressBar headerProgress;
	private final TextView headerText,subHeaderText;
    private ImageView iv_anim_loading_new;

	private String pullLabel;
	private String refreshingLabel;
	private String releaseLabel;
    private AnimationDrawable loadingAnim;
    private boolean motionless = false; //true:只显示，headText无变化
    private String[] tips;
    private Random r;

//	private final Animation rotateAnimation, resetRotateAnimation;

	public LoadingLayout(Context context, final int mode, String releaseLabel, String pullLabel, String refreshingLabel) {
		super(context);
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
		headerText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
        headerText.getPaint().setFakeBoldText(true);
		subHeaderText= (TextView) header.findViewById(R.id.pull_to_refresh_text_sub);
//        headerProgress = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress);

        iv_anim_loading_new = (ImageView) findViewById(R.id.iv_anim_loading_new);
        loadingAnim = (AnimationDrawable) iv_anim_loading_new.getDrawable();

//		final Interpolator interpolator = new LinearInterpolator();
//		rotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//		        0.5f);
//		rotateAnimation.setInterpolator(interpolator);
//		rotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
//		rotateAnimation.setFillAfter(true);
//
//		resetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
//		        Animation.RELATIVE_TO_SELF, 0.5f);
//		resetRotateAnimation.setInterpolator(interpolator);
//		resetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
//		resetRotateAnimation.setFillAfter(true);

		this.releaseLabel = releaseLabel;
		this.pullLabel = pullLabel;
		this.refreshingLabel = refreshingLabel;

		switch (mode) {
			case PullToRefreshBase.MODE_PULL_UP_TO_REFRESH:
				headerText.setText(context.getString(R.string.pull_to_refresh_pull_label_up));
				break;
			case PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH:
			default:
				headerText.setText(context.getString(R.string.pull_to_refresh_pull_label_down));
				break;
		}
	}

	public void reset() {
        if (!motionless) {
            headerText.setText(pullLabel);
        }else{
            setRandomTips();
        }
    }

    public void releaseToRefresh() {
        if(!motionless){
            headerText.setText(releaseLabel);
        }
        loadingAnim.start();
    }

    /**
     * 设置上拉后显示tips提示
     * @param motionless true:只显示,text在pull过程中无变化,默认false
     * */
    protected void setDisplayMode(boolean motionless,String[] tips){
        this.motionless = motionless;
        this.tips = tips;
        headerText.getPaint().setFakeBoldText(false);
        setRandomTips();
    }

    public void stopAnim(){
        if(loadingAnim.isRunning()){
            loadingAnim.stop();
        }
    }

	public void setPullLabel(String pullLabel) {
		this.pullLabel = pullLabel;
	}

	public void refreshing() {
		headerText.setText(refreshingLabel);
		//headerProgress.setVisibility(View.VISIBLE);
    }

	public void setRefreshingLabel(String refreshingLabel) {
		this.refreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		this.releaseLabel = releaseLabel;
	}

	public void pullToRefresh() {
        if (!motionless) {
            headerText.setText(pullLabel);
        }else{
            setRandomTips();
        }
	}

    private void setRandomTips(){
        if (null != tips) {
            if (null == r) {
                r = new Random();
            }
            pullLabel = tips[r.nextInt(tips.length)];
            headerText.setText(pullLabel);
        }
    }

	public void setTextColor(int color) {
		headerText.setTextColor(color);
	}

}