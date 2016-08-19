package com.lip.woqu.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.view.ETWebView;


/**
 * Created by etouch on 15/2/28.
 */
public class WebViewActivity extends EFragmentActivity {

    private ImageView btn_back;
    private TextView tv_title;
    private ETWebView mWebView;
    private ProgressBar mProgressBar;
    private String webUrl="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webUrl=getIntent().getStringExtra("webUrl");
        if(webUrl==null){
            webUrl="";
        }
        Init();
    }
    private void Init(){
        btn_back=(ImageView)findViewById(R.id.imageview_back);
        btn_back.setOnClickListener(onClick);
        tv_title=(TextView)findViewById(R.id.textview1);
        mWebView=(ETWebView)findViewById(R.id.webView1);
        mProgressBar=(ProgressBar)findViewById(R.id.progressBar1);
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                loadUrl(mWebView, url);
                return true;
            }// 重写点击动作,用webview载入
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
                if (progress == 100) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(progress);
                }
                super.onProgressChanged(view, progress);
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                tv_title.setText(title);
            }
        });
        loadUrl(mWebView,webUrl);
    }
    private View.OnClickListener onClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v==btn_back){
                finish();
            }
        }
    };
    private String tempUrl = "";
    public void loadUrl(WebView wv, String url) {
        int index = url.indexOf("?");
        if (index != -1) {
            tempUrl = url.substring(0, index);
        } else {
            tempUrl = url;
        }
        wv.loadUrl(url);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                WebViewActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
