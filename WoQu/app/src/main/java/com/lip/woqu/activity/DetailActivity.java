package com.lip.woqu.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.bean.ActivityBean;
import com.lip.woqu.bean.PersonalInfoBean;
import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.SynPreferences;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.ToastUtil;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.utils.net.NetParams;
import com.lip.woqu.view.CustomDialogForUserInfo;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.LifeUrlTextView;
import com.lip.woqu.view.base.ETImageView;
import com.lip.woqu.view.utils.ImageUploader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class DetailActivity extends EFragmentActivity implements View.OnClickListener {


    private final int requestCode_selectPic = 0;//选择图片返回
    private final int requestCode_cutPic = 1;//裁剪图片返回
    private CustomDialogForUserInfo cdfi;
    private String temp_nick = "";
    private TextView tv_nickName,tv_type,tv_apply,tv_collect,tv_place,tv_time,tv_gatherTime,tv_gatherPlace,
                         tv_numLimit,tv_otherLimit,tv_limitTime,tv_sign;
    private LifeUrlTextView tv_content;
    private LinearLayout ll_touxiang, ll_nickName,ll_apply,ll_collect;
    private LinearLayout ll_loction;
    private Context ctx;
    private ETNetworkImageView img_touxiang;
    private Button btn_back;
    private String mAvatarUri;
    private String mNickName;
    private PersonalInfoBean mUserInfo;
    private String photoUrl = "";
    private int activityId;
    private int userId;
    private String type;
    private ActivityBean activityBean;
    private int applyNum=0,collectNum=0;
    private final int COLLCET_SUCCESS=1001;
    private final int COLLCET_CANCEL=1000;
    private final int COLLCET_OWN=1002;
    private final int COLLCET_FAIL=1003;

    private final int APPLY_SUCCESS=2001;
    private final int APPLY_CANCEL=2000;
    private final int APPLY_OWN=2002;
    private final int APPLY_FAIL=2003;
    public static final int ACTION_TO_EDIT_NICK = 333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ctx = getApplicationContext();
        activityId=getIntent().getIntExtra("activityId", 1);
        userId=getIntent().getIntExtra("userId",10001);
        init();
        activityBean=new ActivityBean();
        getActivity();
        //initData();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //                       {
//                           "activity": {
//                           "activityId": 1,
//                                   "activityPlace": "奥体",
//                                   "activityTime": 1426485630000,
//                                   "content": "一起跑步吧",
//                                   "gatherPlace": "西园四区",
//                                   "gatherTime": 1426491030000,
//                                   "otherLimit": "IT男|单身",
//                                   "personNum": 5,
//                                   "sign": "都来吧，看谁是跑步健将",
//                                   "typeId": 1,
//                                   "userId": 10001
//                       },
//                           "applyNum": 2,
//                               "collectNum": 2,
//                               "gender": "男",
//                               "nickName": "李小布",
//                               "result": 1,
//                               "type": "运动"
//                       }
                    try {

                        JSONObject jObject = (JSONObject) msg.obj;
                        JSONObject activityObject = jObject.getJSONObject("activity");
                        type=jObject.getString("type");
                        mNickName=jObject.getString("nickName");
                        applyNum=jObject.getInt("applyNum");
                        collectNum=jObject.getInt("collectNum");
                        activityBean.activityId=activityObject.getInt("activityId");
                        activityBean.content=activityObject.getString("content");
                        activityBean.activityPlace=activityObject.getString("activityPlace");
                        activityBean.activityTime=new Date(activityObject.getLong("activityTime"));
                        activityBean.type=activityObject.getInt("typeId");
                        activityBean.userId=activityObject.getInt("userId");
                        if(activityObject.has("gatherTime"))
                          activityBean.gatherTime=new Date(activityObject.getLong("gatherTime"));
                        if(activityObject.has("gatherPlace"))
                          activityBean.gatherPlace=activityObject.getString("gatherPlace");
                        if(activityObject.has("personNum"))
                          activityBean.participateNum=activityObject.getInt("personNum");
                        if(activityObject.has("otherLimit"))
                          activityBean.otherLimit=activityObject.getString("otherLimit");
                        if(activityObject.has("sign"))
                          activityBean.sign=activityObject.getString("sign");

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        initData();
                    }
                    break;
                case APPLY_SUCCESS:
                    ToastUtil.show(ctx,"报名成功");
                    applyNum++;
                    tv_apply.setText("报名:"+applyNum+"/"+activityBean.participateNum);
                    break;
                case APPLY_CANCEL:
                    ToastUtil.show(ctx,"取消报名");
                    applyNum--;
                    tv_apply.setText("报名:"+applyNum+"/"+activityBean.participateNum);
                    break;
                case APPLY_OWN:
                    ToastUtil.show(ctx,"不能报名自己发布的活动");
                    break;
                case APPLY_FAIL:
                    ToastUtil.show(ctx,"报名失败");
                    break;
                case COLLCET_SUCCESS:
                    ToastUtil.show(ctx,"收藏成功");
                    collectNum++;
                    tv_collect.setText("收藏:"+collectNum);
                    break;
                case COLLCET_CANCEL:
                    ToastUtil.show(ctx,"取消收藏");
                    collectNum--;
                    tv_collect.setText("收藏:"+collectNum);
                    break;
                case COLLCET_OWN:
                    ToastUtil.show(ctx,"不能收藏自己发布的活动");
                    break;
                case COLLCET_FAIL:
                    ToastUtil.show(ctx,"收藏失败");
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        tv_nickName = (TextView) findViewById(R.id.tv_nick);
        tv_type=(TextView)findViewById(R.id.tv_type);
        tv_content=(LifeUrlTextView)findViewById(R.id.tv_activity_content);
        tv_apply=(TextView)findViewById(R.id.tv_apply);
        tv_collect=(TextView)findViewById(R.id.tv_collect);
        tv_place=(TextView)findViewById(R.id.tv_addr);
        tv_time=(TextView)findViewById(R.id.tv_time);
        tv_gatherTime=(TextView)findViewById(R.id.tv_gather_time);
        tv_gatherPlace=(TextView)findViewById(R.id.tv_gather_place);
        tv_numLimit=(TextView)findViewById(R.id.tv_people_num);
        tv_otherLimit=(TextView)findViewById(R.id.tv_other);
        tv_sign=(TextView)findViewById(R.id.tv_desc);
        tv_limitTime=(TextView)findViewById(R.id.tv_end_time);
        img_touxiang = (ETNetworkImageView) findViewById(R.id.iv_avatar);
        img_touxiang.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);
//        ll_touxiang = (LinearLayout) findViewById(R.id.ll_touxiang);
//        ll_nickName = (LinearLayout) findViewById(R.id.ll_nickName);
        btn_back = (Button) findViewById(R.id.btn_back);
        ll_loction=(LinearLayout)findViewById(R.id.ll_location);
        ll_apply=(LinearLayout)findViewById(R.id.ll_participate);
        ll_collect=(LinearLayout)findViewById(R.id.ll_collect);
        ll_loction.setOnClickListener(this);
//        ll_touxiang.setOnClickListener(this);
//        ll_nickName.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        ll_apply.setOnClickListener(this);
        ll_collect.setOnClickListener(this);
    }
    private void initData()
    {

        DateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        tv_content.setText(activityBean.content);
        tv_type.setText(type);
        UtilsManager.println("Nick:"+mNickName);
        tv_nickName.setText(mNickName);
        tv_place.setText(activityBean.activityPlace);

        tv_time.setText(format.format(activityBean.activityTime));
        tv_gatherTime.setText(format.format(activityBean.gatherTime));
        tv_gatherPlace.setText(activityBean.gatherPlace);
        tv_numLimit.setText(activityBean.participateNum+"人");
        tv_otherLimit.setText(activityBean.otherLimit);
        tv_sign.setText(activityBean.sign);
        tv_apply.setText("报名:"+applyNum+"/"+activityBean.participateNum);
        tv_collect.setText("收藏:"+collectNum);
        tv_limitTime.setText(format.format(activityBean.activityTime));
    }
   //得到活动的详情
    private void getActivity()
    {
        new Thread()
        {
           public void run()
           {
               try {
                   NetParams params = new NetParams();
                   params.addParam("activityId", activityId + "");
                   String result = NetManager.getInstance().doGetAsString(SysParams.ACTIVITITY_URL, params);
                   Log.d("Result:", result);
                   JSONObject jObject = new JSONObject(result);
                   int stateResult = jObject.getInt("result");
                   if(stateResult>0)
                   {
                      Message msg=mHandler.obtainMessage();
                       msg.what=1;
                       msg.obj=jObject;
                       mHandler.sendMessage(msg);
                   }
               }catch(Exception e)
               {
                  e.printStackTrace();
               }
           }

        }.start();
    }
   //收藏一个活动
    private void collectActivity()
    {
        new Thread()
        {
            public void run()
            {
                try {
//                    NetParams params = new NetParams();
//                    params.addParam("activityId", activityId + "");
//                    params.addParam("userId",userId+"");
                    Hashtable<String,String>params=new Hashtable<String, String>();
                    params.put("activityId", activityId + "");
                    params.put("userId",SysParams.userId+"");
                    String result = NetManager.getInstance().doPostAsString(SysParams.COLLECT_URL,params);
                    Log.d("Result:", result);
                    JSONObject jObject = new JSONObject(result);
                    int stateResult = jObject.getInt("result");
                    if(stateResult>0)
                    {
                        String status=jObject.getString("status");
                        if("success".equals(status))
                           mHandler.sendEmptyMessage(COLLCET_SUCCESS);
                        else
                            mHandler.sendEmptyMessage(COLLCET_CANCEL);
                    }
                    else if(stateResult==0)
                        mHandler.sendEmptyMessage(COLLCET_OWN);
                    else if (stateResult==-1)
                        mHandler.sendEmptyMessage(COLLCET_FAIL);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        }.start();
    }
    //参加一个活动
    private void applyActivity()
    {
        new Thread()
        {
            public void run()
            {
                try {
//                    NetParams params = new NetParams();
//                    params.addParam("activityId", activityId + "");
//                    params.addParam("userId",userId+"");
                    Hashtable<String,String>params=new Hashtable<String, String>();
                    params.put("activityId", activityId + "");
                    params.put("userId",SysParams.userId+"");
                    String result = NetManager.getInstance().doPostAsString(SysParams.APPLY_URL,params);
                    Log.d("Result:", result);
                    JSONObject jObject = new JSONObject(result);
                    int stateResult = jObject.getInt("result");
                    if(stateResult>0)
                    {
                        String status=jObject.getString("status");
                        if("success".equals(status))
                            mHandler.sendEmptyMessage(APPLY_SUCCESS);
                        else
                            mHandler.sendEmptyMessage(APPLY_CANCEL);
                    }
                    else if(stateResult==0)
                        mHandler.sendEmptyMessage(APPLY_OWN);
                    else if (stateResult==-1)
                        mHandler.sendEmptyMessage(APPLY_FAIL);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        }.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.ll_touxiang:
                doCropPhoto();
                break;
            case R.id.ll_nickName:
                temp_nick = tv_nickName.getText().toString();
                showEditDialog(ACTION_TO_EDIT_NICK, temp_nick);
                break;
            case R.id.ll_location:
                Intent details=new Intent(DetailActivity.this,GeocoderActivity.class);
                details.putExtra("addr",tv_place.getText());
                startActivity(details);
                break;
            case R.id.ll_participate:
                if(applyNum>=activityBean.participateNum) {
                    ToastUtil.show(ctx,"报名人数已满");
                    return;
                }
                applyActivity();
                break;
            case R.id.ll_collect:
                collectActivity();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if (requestCode == requestCode_cutPic) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            JSONObject result = new ImageUploader(ctx).doUploadOneImage(photoUrl);
                            if (result.has("status")) {

                                if (result.getString("status") == "1000") {
                                    if (result.has("url")) {
                                        mAvatarUri = result.getString("url");
                                        try {
                                            updatePersonalInfo();
                                        } catch (PackageManager.NameNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                img_touxiang.setImageUrl(photoUrl, -1);
            }
            else if(requestCode == requestCode_selectPic)
            {
                ArrayList<String> stringList = data.getStringArrayListExtra("pictures");
                if (stringList != null && stringList.size() > 0){
                    final String path = stringList.get(0);
                    startPhotoZoom(path);
                }
            }
        }

    }

    private void showEditDialog(final int action, String value) {

        if (cdfi == null) {
            cdfi = new CustomDialogForUserInfo(DetailActivity.this);
        }
        cdfi.switchView(CustomDialogForUserInfo.EDIT_VIEW);
        cdfi.setEditContent(value);
        cdfi.setTitle(getTitle(action));
        cdfi.setOnclick(new CustomDialogForUserInfo.ClickCallback() {
            @Override
            public void ok_click(String value) {
                switch (action) {
                    case ACTION_TO_EDIT_NICK:
                        if (value.length() >= 20) {
                            cdfi.setEditError(getResources().getString(R.string.wrongName));
                            CustomDialogForUserInfo.hasErr = true;
                        } else if (TextUtils.isEmpty(value)) {
                            cdfi.setEditError(getResources().getString(R.string.canNotNull));
                            CustomDialogForUserInfo.hasErr = true;
                        } else {
                            if (!value.equals(temp_nick)) {
                                mNickName = value;
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            updatePersonalInfo();
                                        } catch (PackageManager.NameNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                                tv_nickName.setText(mNickName);
                            } else {
                                tv_nickName.setText(temp_nick);
                            }
                            CustomDialogForUserInfo.hasErr = false;
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void cancel_click() {

            }
        });
        cdfi.show();
    }

    private int getTitle(int action) {
        int title = 0;
        switch (action) {
            case ACTION_TO_EDIT_NICK:
                title = R.string.user_nick;
                break;
            default:
                break;
        }
        return title;
    }

    private void updatePersonalInfo() throws PackageManager.NameNotFoundException {
        MyPreferences pre = MyPreferences.getInstance(ctx.getApplicationContext());
        String devidTemp = pre.getUserImei() + pre.getUserMac() + pre.getUserImsi();
        String devid = UtilsManager.MD5(devidTemp);
        SynPreferences synPreferences = SynPreferences.getInstance(ctx.getApplicationContext());
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = null;
        pi = pm.getPackageInfo(ctx.getPackageName(), 0);


        JSONObject auth_token = new JSONObject();
//        try {
//            auth_token.put("acctk", synPreferences.getAcctk());
//            auth_token.put("up", SysParams.UserPlatform.android);
//            auth_token.put("device", synPreferences.getloginDeviceNumber());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String authToken = Base64.encode(auth_token.toString().getBytes());
//
//        Hashtable<String, String> table = new Hashtable<String, String>();
//        table.put("app_key", SysParams.appkey);
//        table.put("app_ts", System.currentTimeMillis() + "");
//        table.put("devid", devid);
//        table.put("auth_token", authToken);
////        table.put("uid", synPreferences.getUID());//synPreferences.getUID()
////        table.put("uid", Integer.toString(mUserInfo.uId));//synPreferences.getUID()
//        table.put("locale", "zh_CN");
//        table.put("channel", UtilsManager.getChannel(ctx));
//        table.put("local_svc_version", pi.versionCode + "");
//        if (!TextUtils.isEmpty(mAvatarUri)) {
//            table.put("avatar", mAvatarUri);
//        }
//        if (!TextUtils.isEmpty(mNickName)) {
//            table.put("nick_name", mNickName);
//        }
//        table.put("app_sign", UtilsManager.getTheAppSign(table));
//        NetManager netManager = NetManager.getInstance();
//        String result = netManager.doPostAsString(SysParams.UPDATE_PERSONAL_INFO, table);
//        resolveUpdateResult(result);
    }

    private void resolveUpdateResult(String result) {
        Log.i("", "hebin12" + result);
    }

    protected void doCropPhoto() {
        try {
            Intent intent = new Intent(getApplicationContext(), SelectLocalPicturesActivity.class);
            intent.putExtra("only", true);
            intent.putExtra("isFromExistPublish", true);
            startActivityForResult(intent, requestCode_selectPic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPhotoZoom(String imagePath) {
        try{
            photoUrl = MidData.tempDir + System.currentTimeMillis() + ".jpg";
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(Uri.parse("file://" + imagePath), "image/*");
            //发送裁剪信号
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //outputX outputY 是裁剪输出图片宽高
            intent.putExtra("outputX", 400);
            intent.putExtra("outputY", 400);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://"+photoUrl));//直接输出文件
            intent.putExtra("noFaceDetection", true); //关闭人脸检测
            intent.putExtra("return-data", false); //是否返回数据
            startActivityForResult(intent, requestCode_cutPic);
        }catch (Exception e){e.printStackTrace();}
    }

}
