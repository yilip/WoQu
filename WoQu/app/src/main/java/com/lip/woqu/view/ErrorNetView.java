package com.lip.woqu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.R;


/**
 * Created ct etouch on 14-5-16.
 */
public class ErrorNetView extends LinearLayout implements View.OnClickListener{

    private View view;
    private Context ctx;
    private Button btn_error_net;
    private TextView tv_message;
    private OnClickListener actionListener;

    public ErrorNetView(Context context) {
        super(context);
        this.ctx = context;

        initView();
    }

    public ErrorNetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;

        initView();
    }

    private void initView(){
        view = inflate(ctx, R.layout.view_error_net,null);
        btn_error_net = (Button) view.findViewById(R.id.btn_error_net);
        tv_message = (TextView) view.findViewById(R.id.tv_message);

        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setActionButton(String btnText, OnClickListener listener){
        this.actionListener = listener;
        btn_error_net.setText(btnText);
        btn_error_net.setVisibility(View.VISIBLE);
        btn_error_net.setOnClickListener(this);
    }

    public void setMessage(String message){
        tv_message.setText(message);
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        if(v == btn_error_net){
            if(actionListener!=null){
                actionListener.onClick(v);
            }
        }
    }
}
