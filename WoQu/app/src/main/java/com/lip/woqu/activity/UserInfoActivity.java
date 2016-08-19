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
import com.lip.woqu.bean.PersonalInfoBean;
import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.SynPreferences;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.utils.net.NetParams;
import com.lip.woqu.view.CustomDialogForUserInfo;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.base.ETImageView;
import com.lip.woqu.view.utils.ImageUploader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class UserInfoActivity extends EFragmentActivity implements View.OnClickListener {


    private final int requestCode_selectPic = 0;//选择图片返回
    private final int requestCode_cutPic = 1;//裁剪图片返回
    private CustomDialogForUserInfo cdfi;
    private String temp_nick = "";
    private TextView nickNameTV,genderTV,phoneTV;
    private LinearLayout touxiangLL, nickNameLL,genderLL,phoneLL;
    private Context ctx;
    private ETNetworkImageView img_touxiang;
    private Button btn_back;
    private String mAvatarUri;
    private String mNickName;
    private PersonalInfoBean mUserInfo;
    private String photoUrl = "";
    private boolean isLoadingData=false;
    private String userId="10001";


    public static final int ACTION_TO_EDIT_NICK = 333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ctx = getApplicationContext();
        init();
        initData();
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mUserInfo=(PersonalInfoBean)msg.obj;
                    nickNameTV.setText(mUserInfo.nickName);
                    phoneTV.setText(mUserInfo.phone);
                    genderTV.setText(mUserInfo.gender);
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        nickNameTV = (TextView) findViewById(R.id.tv_nickName);
        genderTV=(TextView)findViewById(R.id.tv_gender);
        phoneTV=(TextView)findViewById(R.id.tv_phone);

        img_touxiang = (ETNetworkImageView) findViewById(R.id.img_touxiang);
        img_touxiang.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);
        touxiangLL = (LinearLayout) findViewById(R.id.ll_touxiang);
        nickNameLL = (LinearLayout) findViewById(R.id.ll_nickName);
        genderLL=(LinearLayout)findViewById(R.id.ll_gender);
        phoneLL=(LinearLayout)findViewById(R.id.ll_phone);
        btn_back = (Button) findViewById(R.id.btn_back);

        touxiangLL.setOnClickListener(this);
        nickNameLL.setOnClickListener(this);
        genderLL.setOnClickListener(this);
        phoneLL.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    private void initData() {
        getUseInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if(v==btn_back)
        {
            UserInfoActivity.this.finish();
        }
        else if(v==touxiangLL)
        {
            doCropPhoto();
        }
        else if(v==nickNameLL)
        {
            temp_nick = nickNameTV.getText().toString();
            showEditDialog(ACTION_TO_EDIT_NICK, temp_nick);
        }
        else if(v==genderLL)
        {

        }
        else if(v==phoneLL)
        {

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
            cdfi = new CustomDialogForUserInfo(UserInfoActivity.this);
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
                                nickNameTV.setText(mNickName);
                            } else {
                                nickNameTV.setText(temp_nick);
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
    /**********************Lip*****************************/
    //得到用户的资料
    private void getUseInfo()
    {
        if (isLoadingData) {
            return;
        }
        isLoadingData = true;
        new Thread() {
            public void run() {
                try {
                    NetParams params = new NetParams();
                    params.addParam("userId",userId);
                    String result= NetManager.getInstance().doGetAsString(SysParams.MYPROFILE_URL,params);
                    Log.d("Result:",result);
                    JSONObject jObject=new JSONObject(result);
                    int stateResult=jObject.getInt("result");
                    JSONObject userObject=jObject.getJSONObject("user");
                    PersonalInfoBean personalInfoBean=new PersonalInfoBean();
                    personalInfoBean.gender=(userObject.getInt("gender")==0)?"男":"女";
                    personalInfoBean.nickName=userObject.getString("nickName");
                    personalInfoBean.phone=userObject.getString("phone");
                    if(stateResult>0) {
                        if (personalInfoBean != null) {
                            Message msg2 = handler.obtainMessage();
                            msg2.what = 1;
                            msg2.obj = personalInfoBean;
                            handler.sendMessage(msg2);
                        } else {
                            handler.sendEmptyMessage(2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }

        }.start();
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
