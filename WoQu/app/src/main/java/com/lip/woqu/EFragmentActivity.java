package com.lip.woqu;

import android.os.Bundle;
import android.support.v4.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.view.MyGestureView;

/**
 * 继承此类的子类无需再写requestWindowFeature()方法
 *  实现了很多基础的方法
 */
public abstract class EFragmentActivity extends FragmentActivity {

	private MyGestureView myGestureView;
	protected ApplicationManager myApplicationManager=null;
	protected boolean isActivityRun=true;
	private GetstureViewScrollStateChanged onScrollStateChanged;
    protected MyPreferences myPreferences=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isActivityRun=true;
        myPreferences=MyPreferences.getInstance(this.getApplicationContext());
		myApplicationManager=(ApplicationManager) this.getApplication();
		myApplicationManager.addActivity(this);
	}
	@Override
	public void setContentView(int layoutResID) {
        if(isUseGestureView()){
            myGestureView=new MyGestureView(this);
            setContentView(myGestureView);
            myGestureView.setMyGestureViewChanged(new MyGestureView.MyGestureViewChanged() {
                @Override
                public void onClosed() {//View关闭完成则结束Activity
                    EFragmentActivity.this.close();
                }
            });
            myGestureView.setMyGestureViewScrollStateChanged(new MyGestureView.MyGestureViewScrollStateChanged() {
                @Override
                public void onScrollStateChanged(boolean isScrolling) {
                    if (onScrollStateChanged != null) {
                        onScrollStateChanged.onScrollStateChanged(isScrolling);
                    }
                }
            });
            myGestureView.setAsGestureViewScale(getAsGestureViewScale());
            LayoutInflater.from(EFragmentActivity.this).inflate(layoutResID,myGestureView,true);
        }else{
            super.setContentView(layoutResID);
        }
	}
    private void setTranslucentStatusAndNavigation() {
        /**透明导航栏*/
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    private View.OnClickListener click=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onResume() {
        super.onResume();
	}
    @Override
    protected void onDestroy() {
        isActivityRun=false;
        myApplicationManager.removeActivity(this);
        super.onDestroy();
    }
	/**准备关闭当前activity，在调用finish()方法之前调用，用于某些类检测是或否需要setResult(RESULT_OK)等操作*/
	protected void prepareDestroy(){
		
	}
	/**关闭当前activity,子类关闭自身时调用该类*/
	public void close(){
		EFragmentActivity.this.prepareDestroy();
		EFragmentActivity.this.finish();
	}

	/**如果设置了软件加密，该activity是否需要用户输入密码(能看到用户数据的页面需要，看不到用户数据、添加数据页面不需要)*/
	public boolean isNeedUserInputPsw(){
		return false;
	}
	
	/**★★★★注意★★★★★ 只有在特殊时候使用，如果确定了Activity不需要使用手势滚动，必须直接复写isUseGestureView()return false
	 * 而不是调用该方法
	 * 设置手势View是否可用，用于在activity中在某些特殊时候禁用手势View
	 * */
	public void setIsGestureViewEnable(boolean isEnable){
		if(myGestureView!=null){
			myGestureView.setGestureViewEnable(isEnable);
		}
	}
	/**是否使用手势View,如果子类不使用则必须重写该方法并返回false*/
	public boolean isUseGestureView(){
		return true;
	}
	/**在屏幕左侧1/result宽度向右滑动时触发手势View
	 * 如果子类中有向右滑动事件则必须重写该方法，否则事件会被覆盖
	 * */
	public int getAsGestureViewScale(){
		return 1;
	}

	public void setOnScrollStateChanged(GetstureViewScrollStateChanged onScrollStateChanged) {
		this.onScrollStateChanged = onScrollStateChanged;
	}
	public interface GetstureViewScrollStateChanged{
		public void onScrollStateChanged(boolean isScrolling);
	}

}
