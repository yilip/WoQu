package com.lip.woqu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.lip.woqu.ApplicationManager;
import com.lip.woqu.R;
import com.lip.woqu.view.LoadingProgressDialog;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class UtilsManager {
	// ------------------------打包前设置-----------------------------------
	 /**初始化so包，发布时type务必修改为★非-44★的数字*/
	public final static int CHECK_ISPIRATE_VALUE =-44;
	/** 发布软件时该模式需要修改为false */
	public final static boolean DEVELOP_MODE = true;
    private static final String TAG = "UtilsManager";
    // -----------------------------------------------------------
	private static Toast toast = null;
    /**记事图片所有宽度的集合*/
    public static int[] width_collection={110,160,210,240,320,480,640,720,1200};


	/**
	 * Toast消息
	 */
	public static void Toast(Context ctx, int stringId) {
		if (toast != null) {
			toast.setText(ctx.getResources().getString(stringId));
			toast.setDuration(Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(ctx, ctx.getResources().getString(stringId),
                    Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	public static void Toast(Context ctx, String string) {
		if (toast != null) {
			toast.setText(string);
			toast.setDuration(Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(ctx, string, Toast.LENGTH_SHORT);
		}
		toast.show();
	}
	/** 打印开启 */
	public static void println(String str) {
		if (!DEVELOP_MODE) {
			return;
		}
		System.out.println(str == null ? "" : str);
	}

    /**
     * 打印日志 flag 取值e、i、d
     */
    public static void printlog(String flag, String tag, String log) {
        if (!DEVELOP_MODE) {
            return;
        }
        if (flag.equals("e")) {
            Log.e(tag, log);
        } else if (flag.equals("i")) {
            Log.i(tag, log);
        } else if (flag.equals("d")) {
            Log.d(tag, log);
        }
    }

    /**
     * 打印日志 定制4ace 信鸽通知
     */
    public static void printlog4AceXGPush(String log) {
        if (!DEVELOP_MODE) {
            return;
        }
        printlog("e", "aceXGPush",  log);
    }

    /**
     * 打印日志 定制4ace 米推送
     */
    public static void printlog4AceMiPush(String log) {
        if (!DEVELOP_MODE) {
            return;
        }
        printlog("e", "aceMiPush",  log);
    }

	/** 将整数转化为两位的string */
	public static String intTo2String(int num) {
		String result = "";
		if (num < 10) {
			result = "0" + num;
		} else {
			result = num + "";
		}
		return result;
	}


	private static float scale=-1;
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		if (scale==-1) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		return (int) (dpValue * scale + 0.5f);
	}


	/**
	 * 宽度：110,160,210,240,320,480,640,720,1200;
	 *
	 * @param width
	 * @return 返回width和上述给定的数值相近的
	 */
	public static int getImageWidth(int width) {
		int result = width;
		if (width <= 135)// 110+(160-110)/2
		{
			result = width_collection[0];
		} else if (width <= 185)// 160+(210-160)/2
		{
			result = width_collection[1];
		} else if (width <= 225)// 210+(240-210)/2
		{
			result = width_collection[2];
		} else if (width <= 280)// 240+(320-240)/2
		{
			result = width_collection[3];
		} else if (width <= 400)// 320+(480-320)/2
		{
			result = width_collection[4];
		} else if (width <= 560)// 480+(640-480)/2
		{
			result = width_collection[5];
		} else if (width <= 680)// 640+(720-640)/2
		{
			result = width_collection[6];
		} else if (width <= 960)// 720+(1200-720)/2
		{
			result = width_collection[7];
		} else {
			result = width_collection[8];
		}
		return result;
	}
    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context ctx) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = ctx.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
	/**
	 * Converts a byte array into a String hexidecimal characters
	 *
	 * null returns null
	 */
	private static String bytesToHexString(byte[] bytes) {
		if (bytes == null)
			return null;
		String table = "0123456789abcdef";
		StringBuilder ret = new StringBuilder(2 * bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int b;
			b = 0x0f & (bytes[i] >> 4);
			ret.append(table.charAt(b));
			b = 0x0f & bytes[i];
			ret.append(table.charAt(b));
		}
		return ret.toString();
	}
    /**获取md5字符串*/
	public static String MD5(String string) {
        byte[] bytes=string.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(bytes);
            return bytesToHexString(md.digest());
        } else {
            return string;
        }
	}

    /**将HashTable中的参数生成签名（参数排序后连接起来md5）*/
    public static String getTheAppSign(Hashtable<String,String> table){
        TreeMap<String,String> tree=new TreeMap<String, String>();
        if (table != null) {
            Enumeration<String> enu = table.keys();
            String key = "";
            while (enu.hasMoreElements()) {
                key = enu.nextElement();
                String val = table.get(key);
                tree.put(key,val);
            }
        }
        StringBuffer sb=new StringBuffer();
        Set<String> keys=tree.keySet();
        for (String key:keys){
            sb.append(key).append(tree.get(key));
        }
        //sb.append(SysParams.appSecret);
        return MD5(sb.toString());
    }

    /** 显示软键盘*/
    public static void showKeyBord(final EditText edt) {
        edt.requestFocus();
        Timer timer = new Timer(); // 设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // 弹出软键盘的代码
                if (edt.getParent() != null) {
                    InputMethodManager imm = (InputMethodManager) ApplicationManager.ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edt, InputMethodManager.RESULT_SHOWN);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        }, 200);
    }
    /**隐藏软键盘*/
    public static void hideKeyBord(EditText edt) {
        InputMethodManager imm = (InputMethodManager) ApplicationManager.ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    /**传一个长整型时间，将时间格式化输出
     * 刚刚，1分钟前，2分钟前，。。。59分钟前，1小时前，。。。23小时前，format格式
     * */
    public static String formatTime(long time,SimpleDateFormat format){
        long jiange=(System.currentTimeMillis()-time)/1000;
        if(jiange<60){
            if(jiange<-10){//未来的时间则显示format格式
                if(format==null){
                    format=new SimpleDateFormat("MM-dd HH:mm");
                }
                return format.format(new Date(time));
            }
            return ApplicationManager.ctx.getString(R.string.time_now_ago);
        }else if(jiange<3600){
            return jiange/60+ApplicationManager.ctx.getString(R.string.time_min_ago);
        }else if(jiange<86400){
            return jiange/3600+ApplicationManager.ctx.getString(R.string.time_hour_ago);
        }else{
            if(format==null){
                format=new SimpleDateFormat("MM-dd HH:mm");
            }
            return format.format(new Date(time));
        }
    }
    /**传一个int型时间，如果大于10000则显示1.x万
     * */
    public static String formatLifeNum(int num){
        if(num>9999){
            int qian=num%10000/1000;
            if(qian==0){
                return num/10000+"万";
            }else {
                return num/10000+"."+qian+"万";
            }
        }else{
            return num+"";
        }
    }

    /**WebView中如果是历知社区则跳转到帖子详情页面 start*/
    public static String WEB_LIZHI_SHARE="http://lz.rili.cn/t/";
    public static boolean checkAndJumpToThreadDetails(Activity activity,String url){
        /** http://lz.rili.cn/t/123 */
        String tempUrl=url.toLowerCase();
        int index = url.indexOf("?");
        if (index != -1) {
            tempUrl = url.substring(0, index);
        }
        index=tempUrl.lastIndexOf("/");
        String threadid=tempUrl.substring(index+1);
        if(!TextUtils.isEmpty(threadid)){
//            Intent intent=new Intent(activity, LifeDetailsActivity.class);
//            intent.putExtra("tid",threadid);
//            activity.startActivity(intent);
            return true;
        }
        return false;
    }
    /**WebView中如果是历知社区则跳转到帖子详情页面 end*/

    /**获取md5字符串*/
    public static String getMD5(byte[] input) {
        return bytesToHexString(MD5(input));
    }

    /** 计算给定 byte [] 串的 MD5 */
    private static byte[] MD5(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(input);
            return md.digest();
        } else
            return null;
    }
    /** 判断是否有可用网络 */
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivity = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /***
     * @param b
     *            x坐标
     * @param c
     *            y坐标
     * @param f
     *            取值20
     * @return [0] 维度，[1]经度
     */
    public static double[] JieMiLocation(double b, double c, double f) {
        double[] result = new double[2];
        double a = Math.PI;
        double e = 12756274 * a;
        f = e / (256 * (1 << (int) f));
        b = b * f - e / 2;
        c = a / 2 - 2 * Math.atan(Math.exp(-(e / 2 - c * f) / 6378137));
        c = c * (180 / a);
        e = b / 6378137 * (180 / a);
        result[0] = c;
        result[1] = e;
        return result;
    }

    /**
     * 得到自定义的progressdialog
     * @param context
     * @param msg
     * @return
     */
    public static LoadingProgressDialog createLoadingDialog(Context context, String msg,boolean canBeCancel) {

        LoadingProgressDialog loadingProgressDialog=new LoadingProgressDialog(context);
        loadingProgressDialog.setTipText(msg);
        loadingProgressDialog.setCancelable(false);
        return loadingProgressDialog;
    }

    /** 0:正常渠道发布,1：google渠道发布,2:cpa包 */
    public static int getAppsPublishChannelByPkgName(Context ctx){
        String name =ctx.getPackageName();
//        if ("cn.etouch.ecalendar".equals(name)){
//            return 0;
//        }else if("cn.etouch.ecalendar2".equals(name)){
//            return 1;
//        }else if("cn.etouch.ecalendar.cpa".equals(name)){
//            return 2;
//        }
        return 0;
    }

    public static String getChannel(Context ctx){
        ApplicationInfo ai = null;
        String client_channel = "";
        try {
            ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA);
            client_channel= String.valueOf(ai.metaData.get("UMENG_CHANNEL"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return client_channel;
    }
}