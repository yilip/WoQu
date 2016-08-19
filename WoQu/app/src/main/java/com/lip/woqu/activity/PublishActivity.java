package com.lip.woqu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.bean.ActivityBean;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.ToastUtil;
import com.lip.woqu.utils.net.LocationUtils;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.utils.net.NetParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class PublishActivity extends EFragmentActivity implements View.OnClickListener{
    private int current_step=0;//当前发布的页面
    private Button btn_back;
//    private View v_seperator;
    private TextView tv_title;
    private Button tv_nextStep;//下一步
    private LinearLayout ll_publish1,ll_publish2;
    //第一页
    private TextView tv_type;
    private LinearLayout ll_type;
    private TextView tv_sport,tv_relaxtion,tv_enterttaiment,tv_study,tv_other;
    private EditText ed_content;
    private CheckBox cb_publishDirect;
    private TextView tv_contentImg;
    private TextView tv_place,tv_time;

    //第二页
    private TextView tv_gatherPlace,tv_gatherTime,tv_numPerson,tv_otherLimit,tv_endTime;
    private EditText ed_sign;
    private Button btn_back_step,btn_publish;
    //地址信息
    private String address;
    private String lat;
    private String lon;
    private int userId=10001;

    private ActivityBean activityBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulish1);
        initView();
        initEvent();
    }
    private void initView()
    {
        current_step=1;
        btn_back=(Button)findViewById(R.id.btn_back);
        //v_seperator=findViewById(R.id.v_seperator);
        tv_title=(TextView)findViewById(R.id.tv_title);
        tv_nextStep=(Button)findViewById(R.id.btn_next);
        ll_publish1=(LinearLayout)findViewById(R.id.ll_publish1);
        ll_publish2=(LinearLayout)findViewById(R.id.ll_publish2);
        tv_type=(TextView)findViewById(R.id.activity_type);
        ll_type=(LinearLayout)findViewById(R.id.ll_activity_type);
        tv_sport=(TextView)findViewById(R.id.activity_sport);
        tv_relaxtion=(TextView)findViewById(R.id.activity_relaxtion);
        tv_enterttaiment=(TextView)findViewById(R.id.activity_entertainment);
        tv_study=(TextView)findViewById(R.id.activity_study);
        tv_other=(TextView)findViewById(R.id.activity_other);
        ed_content=(EditText)findViewById(R.id.ed_activity_content);
        cb_publishDirect=(CheckBox)findViewById(R.id.cbox_publish_direct);
        tv_contentImg=(TextView)findViewById(R.id.tv_picture);
        tv_place=(TextView)findViewById(R.id.tv_addr);
        tv_time=(TextView)findViewById(R.id.tv_time);
        tv_gatherPlace=(TextView)findViewById(R.id.tv_gather_addr);
        tv_gatherTime=(TextView)findViewById(R.id.tv_gather_time);
        tv_numPerson=(TextView)findViewById(R.id.tv_people_num);
        tv_otherLimit=(TextView)findViewById(R.id.tv_other);
        tv_endTime=(TextView)findViewById(R.id.tv_end_time);
        ed_sign=(EditText)findViewById(R.id.tv_activity_sign);
        btn_back_step=(Button)findViewById(R.id.btn_back_step);
        btn_publish=(Button)findViewById(R.id.btn_publish);
    }
    private void initEvent()
    {
        btn_back.setOnClickListener(this);
        ll_type.setVisibility(View.GONE);
        ll_publish1.setVisibility(View.VISIBLE);
        ll_publish2.setVisibility(View.GONE);
        tv_nextStep.setOnClickListener(this);
        tv_type.setOnClickListener(this);
        tv_sport.setOnClickListener(this);
        tv_relaxtion.setOnClickListener(this);
        tv_enterttaiment.setOnClickListener(this);
        tv_study.setOnClickListener(this);
        tv_other.setOnClickListener(this);
        cb_publishDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(isChecked)
                 {
                     current_step=2;
                     tv_nextStep.setText("发布");
                 }else{
                     current_step=1;
                     tv_nextStep.setText(getString(R.string.next_step));
                 }
            }
        });

        cb_publishDirect.setOnClickListener(this);
        tv_place.setOnClickListener(this);
        tv_time.setOnClickListener(this);
        tv_gatherPlace.setOnClickListener(this);
        tv_gatherTime.setOnClickListener(this);
        tv_numPerson.setOnClickListener(this);
        tv_otherLimit.setOnClickListener(this);
        tv_endTime.setOnClickListener(this);
        btn_back_step.setOnClickListener(this);
        btn_publish.setOnClickListener(this);
        activityBean=new ActivityBean();
        LocationUtils.getInstance(getApplicationContext()).startLocation(locationUtilsListener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
//            if(requestCode == RESULT_PICTURE){
//                ArrayList<String> pic = data.getStringArrayListExtra("pictures");
//                ArrayList<Integer> org = data.getIntegerArrayListExtra("orientation");
//                dealImage(pic, org);
//            }else if(requestCode == RESULT_PICTUREVIER){
//                ArrayList<Integer> delPosition = data.getIntegerArrayListExtra("DelPositionArray");
//                if(delPosition != null && delPosition.size()>0){
//                    int tempLength = delPosition.size();
//                    for(int i=0;i<tempLength;i++){
//                        picList.remove(delPosition.get(i).intValue());
//                    }
//                    if (publicImageView != null){
//                        publicImageView.resetView();
//                        for (int i =0;i<picList.size();i++){
//                            publicImageView.addPath(picList.get(i));
//                        }
//                        publicImageView.addPath("");
//                    }
//                }
//            }else
         if (requestCode == 1000){
                String name = data.getStringExtra("name");
                address = data.getStringExtra("address");
                //cityKey = data.getStringExtra("cityKey");
                lat = data.getStringExtra("lat");
                lon = data.getStringExtra("lon");
                if (!TextUtils.isEmpty(address)){
                    tv_place.setText(address);
                }
               // poi_id = data.getStringExtra("poi_id");
            }
        }
    }
    @Override
    public void onClick(View v)
    {
      if(tv_nextStep==v)//下一步，显示第二页
      {
          Log.d("step:",current_step+"");
          if(current_step==1) {
              current_step = 2;
              activityBean.type = getType(tv_type.getText() + "");
              activityBean.content = ed_content.getText() + "";
              activityBean.activityPlace = tv_place.getText() + "";
              activityBean.activityTime = new Date(tv_time.getText() + "");
//              v_seperator.setVisibility(View.GONE);
//              tv_nextStep.setVisibility(View.GONE);
              ll_publish1.setVisibility(View.GONE);
              ll_publish2.setVisibility(View.VISIBLE);
              tv_title.setText("告诉Ta");
          }
          else
          {
              //直接发布
              if(ed_content.getText().length()<10) {
                  ToastUtil.show(this,"请输入活动内容");
                  return;
              }
              publishActivity();;
          }
      }else if(tv_type==v)
      {
          if(ll_type.getVisibility()==View.GONE)
              ll_type.setVisibility(View.VISIBLE);
          else
              ll_type.setVisibility(View.GONE);
      }else if(btn_back_step==v)
      {
          current_step=1;
          ll_publish2.setVisibility(View.GONE);
          ll_publish1.setVisibility(View.VISIBLE);
      }else if(btn_back==v)
      {
          Log.d("step:",current_step+"");
          if(current_step==1)
              this.finish();
          else
          {
              current_step=1;
//              v_seperator.setVisibility(View.VISIBLE);
//              tv_nextStep.setVisibility(View.VISIBLE);
              tv_title.setText("发布活动");
              ll_publish2.setVisibility(View.GONE);
              ll_publish1.setVisibility(View.VISIBLE);
          }
      }else if(tv_sport==v||tv_enterttaiment==v||tv_relaxtion==v||tv_study==v||tv_other==v)
      {
          tv_type.setText(((TextView)v).getText());
      }else if(v==tv_place)
      {
//          Intent intent=new Intent(PublishActivity.this, AddressSelectActivity.class);
//          startActivity(intent);
          startActivityForResult(new Intent(PublishActivity.this, AddressSelectActivity.class),1000);
      }else if(btn_publish==v)
      {
          //直接发布
          if(ed_content.getText().length()<10) {
              ToastUtil.show(this,"请输入活动内容");
              return;
          }
          publishActivity();
      }
    }
    private int getType(String type)
    {
        if(type.equals("运动"))
            return 1;
        else if(type.equals("娱乐"))
            return  2;
        else if(type.equals("休闲"))
            return 3;
        else if(type.equals("学习"))
            return 4;
        else
            return 0;
    }
    private void publishActivity()
    {
       new Thread()
       {
           public void run()
           {
               try
               {
//                   int userId=Integer.parseInt(request.getParameter("userId"));
//                   int typeId=Integer.parseInt(request.getParameter("typeId"));
//                   String gatherPlace=request.getParameter("gatherPlace");
//                   Date gatherTime=new Date(request.getParameter("gatherTime"));
//                   Date activityTime=new Date(request.getParameter("activityTime"));
//                   String activityPlace=request.getParameter("activityPlace");
//                   String content=request.getParameter("content");
//                   String sign=request.getParameter("sign");
//                   int personNum=Integer.parseInt(request.getParameter("personNum"));
//                   String otherLimit=request.getParameter("otherLimit");
                   Hashtable<String,String>params=new Hashtable<String, String>();
                   params.put("userId",userId+"");
                   params.put("typeId",getType(tv_type.getText()+"")+"");
                   params.put("activityPlace",tv_place.getText()+"");
                   params.put("activityTime",tv_time.getText()+"");
                   params.put("content",ed_content.getText()+"");
                   params.put("gatherPlace",tv_gatherPlace.getText()+"");
                   params.put("gatherTime",tv_gatherTime.getText()+"");
                   params.put("personNum",tv_numPerson.getText()+"");
                   params.put("otherLimit",tv_otherLimit.getText()+"");
                   params.put("sign",ed_sign.getText()+"");
                   params.put("endTime",tv_time.getText()+"");
                   String result = NetManager.getInstance().doPostAsString(SysParams.PUBLISH_URL,params);
                   JSONObject jObject = new JSONObject(result);
                   int stateResult = jObject.getInt("result");
                   if(stateResult>0)
                   {
//                       Message msg=mHandler.obtainMessage();
//                       msg.what=1;
//                       msg.obj=jObject;
//                       mHandler.sendMessage(msg);
                       mHandler.sendEmptyMessage(1);
                   }
                   else
                       mHandler.sendEmptyMessage(0);
               }catch (Exception e)
               {
                   e.printStackTrace();
               }
           }
       }.start();
    }
    LocationUtils.LocationUtilsListener locationUtilsListener = new LocationUtils.LocationUtilsListener() {
        @Override
        public void onGetLocationSuccess(String city1, String cityKey1, String city2, String cityKey2, String lat, String lon, String address) {
//            LifePublishActivity.this.cityKey = cityKey2;
//            LifePublishActivity.this.address = address;
//            LifePublishActivity.this.lat = lat;
//            LifePublishActivity.this.lon = lon;
            Log.d("address:",address);
            if (!TextUtils.isEmpty(address)){
                tv_place.setText(address);
                tv_gatherPlace.setText(address);
            }
        }

        @Override
        public void onGetFail() {
        }
    };
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ToastUtil.show(getApplicationContext(),"发布失败");
                case 1:
                    ToastUtil.show(getApplicationContext(),"发布成功");
                    PublishActivity.this.finish();
                    break;
            }
        }
    };
}
