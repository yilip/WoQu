package com.lip.woqu.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.lip.woqu.activity.WebViewActivity;

/**
 * Created by etouch on 15/1/4.
 * 社区模块中可以点击的Span,点击后调跳转到网页
 */
public class LifeUrlSpan extends ClickableSpan implements View.OnClickListener{

    private Context ctx;
    private String url;

    private boolean isPressed=false;

    public LifeUrlSpan(Context ctx, String url) {
        super();
        this.ctx=ctx;
        this.url=url;
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent(ctx, WebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("webUrl",url);
        ctx.startActivity(intent);
    }
    public void setPressed(boolean isSelected) {
        this.isPressed = isSelected;
    }

    @Override
     public void updateDrawState(TextPaint ds) {
        if(isPressed){
            ds.bgColor= Color.LTGRAY;
            ds.setColor(Color.rgb(0x00, 0xa1, 0xf2));
        }else{
            ds.bgColor= Color.TRANSPARENT;
            ds.setColor(Color.rgb(0x00, 0xa1, 0xf2));
        }
        ds.setUnderlineText(false); //去掉下划线</span>

    }


}
