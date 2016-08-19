package com.lip.woqu.utils;

import android.os.Environment;

public class MidData {
	 /**
     * app中所有action的抬头必须加入这个字符串
     */
    public final static String ActionFirsTittle = "com.lip.woqu";
	/** 软件数据保存目录 */
	public final static String appDir = Environment.getExternalStorageDirectory().getPath() + "/woqu/";
	/** 缓存目录 */
	public final static String tempDir = appDir + ".temp/";
    public final static String exitPageDir = appDir + "exitpage/";
    public final static String notebookPicturePath = appDir + ".icon/";

    /**start万年历中使用的一些全局变量===================*/
    public static int main_screenWidth;
    public static int main_screenHeight;
    public static int system_Verson=0;//系统版本号int
    /**end万年历中使用的一些全局变量=====================*/
    /**Web页面显示的广告 */
    public final  static  String  ShareTrakID="share";
    /** 腾讯一键下载连接*/
    public static final String one_key_download = "http://126.am/OudVr0";
}

