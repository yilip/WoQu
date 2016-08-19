package com.lip.woqu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.lip.woqu.utils.MidData;

import java.util.LinkedList;
import java.util.List;

/**
 * 在每一个Activity中的onCreate方法里添加该Activity到MyApplication对象实例容器中 ，
 * 在需要结束所有Activity的时候可以调用exit方法
 * */
public class ApplicationManager extends Application {
	/** 程序中的activity列表 */
	private List<Activity> activityList = new LinkedList<Activity>();
    /** 全局context,获取资源应直接使用该context,不要在bean里声明一个成员变量*/
    public static Context ctx;
    private static ApplicationManager mApplicationManager;
    @Override
	public void onCreate() {
		super.onCreate();
        ctx=getApplicationContext();
        mApplicationManager = this;
        DisplayMetrics dm=getResources().getDisplayMetrics();
        MidData.main_screenWidth=dm.widthPixels;
        MidData.main_screenHeight=dm.heightPixels;
        MidData.system_Verson=android.os.Build.VERSION.SDK_INT;
    }

    public static ApplicationManager getInstance() {
        return mApplicationManager;
    }

	/** 添加Activity到容器中 */
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	/** 移除activity */
	@SuppressLint("NewApi")
	public void removeActivity(Activity activity) {
		int top = activityList.size() - 1;
		if (top >= 0) {
			/** 直接关掉activity则在栈顶 */
			if (activityList.get(top) == activity) {
				activityList.remove(top);
			} else if ((top = top - 1) >= 0) {
				if (activityList.get(top) == activity) {
					activityList.remove(top);
				}
			}
		}
		if (activityList.size() == 0) {
			try {
				exit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}// end if
	}
    /**检查Activity是否在栈顶*/
    public boolean isActivityInTop(Activity activity){
        int top = activityList.size() - 1;
        if (top >= 0) {
            return activityList.get(top) == activity;
        }
        return false;
    }
    /**
     * 退出软件 1、遍历所有Activity并finish 2、清空定义的全局缓存变量
     * */
    public void exit() {
        /** 1、遍历所有Activity并finish */
        int length = activityList.size();
        for (int i = length - 1; i >= 0; i--) {
            activityList.get(i).finish();
        }
        activityList.clear();
    }


}