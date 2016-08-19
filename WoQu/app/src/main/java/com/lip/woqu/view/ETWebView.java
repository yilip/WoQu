package com.lip.woqu.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

/**
 * Created by etouch on 14/12/3.
 * 封装了WebView的常用属性，外部直接引用即可。
 * 1、解决了跳转地址回退时循环跳转无法退出的问题(判断
 *    页面停留时间<600ms认为是跳转地址，回退时跳过该地址)
 */
public class ETWebView extends WebView {

    private Context ctx;
    protected ETWebViewHelper mETWebViewHelper;
    /**用户设置的WebViewClient*/
    private WebViewClient theUserWebViewClient=null;
    private final int The_Default_time=600;
    private HashMap<String,Long> urlTables=new HashMap<String, Long>();
    private long lastTime=0;
    private String lastUrl="";

    public ETWebView(Context context) {
        super(context);
        Init(context);
    }

    public ETWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public ETWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Init(context);
    }

    private void Init(Context ctx){
        this.ctx=ctx;
        mETWebViewHelper=new ETWebViewHelper(ctx);
        this.getSettings().setJavaScriptEnabled(true);
        this.addJavascriptInterface(mETWebViewHelper, "etouch_client");
        super.setWebViewClient(myWebViewClient);
        WebSettings settings = this.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        int SDK = android.os.Build.VERSION.SDK_INT;
        try {
            settings.setAllowFileAccess(true);
            if (SDK >= 5) {
                settings.setDatabaseEnabled(true);
            }
            if (SDK >= 7) {
                settings.setAppCacheEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setLoadWithOverviewMode(true);//webView全屏
            }
            if (SDK >= 8) {
                settings.setPluginState(WebSettings.PluginState.ON);
            }
            /**将大网页默认缩小到屏幕尺寸*/
            settings.setUseWideViewPort(true);
            /**支持缩放网页*/
            settings.setBuiltInZoomControls(true); //显示放大缩小controler
            settings.setSupportZoom(true);//可以缩放
        } catch (Exception e) {}
        this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
    }
    /**设置网页是否支持缩放，默认支持*/
    public void setIsSupportZoom(boolean canZoom){
        WebSettings settings = this.getSettings();
        settings.setBuiltInZoomControls(canZoom); //显示放大缩小controler
        settings.setSupportZoom(canZoom);//可以缩放
    }
    @Override
    public void loadUrl(String url) {
        String tempUrl=url.toLowerCase();
        int index = url.indexOf("?");
        if (index != -1) {
            tempUrl = url.substring(0, index);
        }
        if(tempUrl.endsWith(".apk")){
//            String fileName = tempUrl.substring(tempUrl.lastIndexOf("/") + 1);
//            DownloadMarketService.DownloadTheApk(ctx, 0, "", fileName, url);
        }else {
            if(tempUrl.startsWith("http")||tempUrl.startsWith("ftp")||tempUrl.startsWith("javascript")){
                super.loadUrl(url);
            }else{
                try{
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                }catch (Exception e){}
            }//end else
        }
    }
    /**如果是www.zhwnl.cn/share的网址，则从网页Description中取出分享标题*/
    public String getTheDescriptionContent(){
        if(mETWebViewHelper!=null){
            return mETWebViewHelper.shareContent;
        }else{
            return "";
        }
    }
    /**用于处理返回时由于重定向而产生的死循环start============*/
    private WebViewClient myWebViewClient=new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(theUserWebViewClient!=null){
                return theUserWebViewClient.shouldOverrideUrlLoading(view,url);
            }else{
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if(lastTime==0){lastTime= System.currentTimeMillis();}
            if(!TextUtils.isEmpty(lastUrl)){
                urlTables.put(lastUrl, System.currentTimeMillis()-lastTime);
            }
            lastTime= System.currentTimeMillis();
            lastUrl=url;
            if(theUserWebViewClient!=null){
                theUserWebViewClient.onPageStarted(view,url,favicon);
            }else{
                super.onPageStarted(view, url, favicon);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            if(theUserWebViewClient!=null){
                theUserWebViewClient.onPageFinished(view, url);
            }else{
                super.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if(theUserWebViewClient!=null){
                theUserWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }else{
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
    };
    @Override
    public void setWebViewClient(WebViewClient client) {
        this.theUserWebViewClient=client;
    }

    @Override
    public boolean canGoBack() {
        boolean result=super.canGoBack();
        if(result){/**如果可以返回则判断url栈中是否是跳转地址，停留时间小于500认为是跳转地址*/
            WebBackForwardList list = this.copyBackForwardList();
            int nowIndex=list.getCurrentIndex();
            int position;
            for(position=nowIndex-1;position>=0;position--){
                String url = list.getItemAtIndex(position).getUrl();
                long time=urlTables.containsKey(url)?urlTables.get(url):0;
                if(time>The_Default_time){
                    break;
                }
            }
            result=position>=0;
        }
        return result;
    }
    @Override
    public void goBack() {
        WebBackForwardList list = this.copyBackForwardList();
        int nowIndex=list.getCurrentIndex();
        int position;
        for(position=nowIndex-1;position>=0;position--){
            String url = list.getItemAtIndex(position).getUrl();
            long time=urlTables.containsKey(url)?urlTables.get(url):0;
            if(time>The_Default_time){
                break;
            }
        }
        if(position>=0){
            goBackOrForward(position-nowIndex);
        }
    }
    /**用于处理返回时由于重定向而产生的死循环start============*/

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try{
            this.destroy();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
