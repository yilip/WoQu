package com.lip.woqu.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.webkit.JavascriptInterface;

/**
 * Created by etouch on 14/12/3.
 * ETWebView辅助类，用于在webView中支持js调用本地方法
 */
public class ETWebViewHelper {

    private Context ctx;

    public ETWebViewHelper(Context context){
        this.ctx=context;
    }

    /**截取描述字符串，用于万年历中webView分享*/
    public String shareContent = "";
    @JavascriptInterface
    public void dealString(String html) {
        shareContent = html;
        if (shareContent.length() >= 110) {
            shareContent = shareContent.substring(0, 100);
            shareContent += "...";
        }
    }
    /**
     * 检测本地应用安装的情况
     * @param pkg 包名
     * @return 本地已安装的应用的vercode， 如果未安装，则返回-1
     */
    @JavascriptInterface
    public int zhwnlCheckApp(String pkg){
        try {
            PackageManager packageManager = ctx.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            if (intent == null){
                return -1;
            }
            return packageManager.getPackageInfo(pkg, 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * 根据包名启动本地应用
     * @param pkg 包名
     */
    @JavascriptInterface
    public void zhwnlStartApp(String pkg){
        try {
            Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
            if(intent != null){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
