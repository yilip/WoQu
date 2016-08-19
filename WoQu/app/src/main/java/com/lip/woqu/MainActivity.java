package com.lip.woqu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lip.woqu.activity.PublishActivity;
import com.lip.woqu.activity.fragment.HomeActivityFragment;
import com.lip.woqu.activity.fragment.MineActivityFragment;
import com.lip.woqu.activity.fragment.NearyByActivityFragment;
import com.lip.woqu.activity.fragment.OnFragmentInteractionListener;
import com.lip.woqu.activity.fragment.event.PersonalLifeDataEvent;
import com.lip.woqu.activity.fragment.event.ReadFeedBackEvent;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.SynPreferences;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.LocationUtils;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.base.ETImageView;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * 首页
 * header+fragment+bottom
 */

public class MainActivity extends EFragmentActivity implements OnFragmentInteractionListener {

    private Activity mActivity;
    //底部 tab
    private RelativeLayout[] rl_bottom=new RelativeLayout[3];
    private ImageView[] iv_bottom = new ImageView[3];
    private TextView[] tv_bottom=new TextView[3];
    private int[] icons;
    private ImageView btn_publish;
    private  TextView titleTV;

    private int currentTabPosition=-1;
    static final int TAB_POSITION_TAB_0 = 0;
    static final int TAB_POSITION_TAB_1 =1;
    static final int TAB_POSITION_TAB_2 = 2;
    //主页
    private HomeActivityFragment homeActivityFragment;
    private NearyByActivityFragment nearyByActivityFragment;
    private MineActivityFragment mineActivityFragment;

    ETNetworkImageView iv_hasmsg;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity=this;
        Init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }
    public void onEvent(PersonalLifeDataEvent event) {
        if (event != null) {
            Message msg = handler.obtainMessage();
            msg.what = 10;
            handler.sendMessage(msg);

        }
    }

    public void onEvent(ReadFeedBackEvent event) {
        if (event != null) {
            Message msg = handler.obtainMessage();
            msg.what = 11;
            handler.sendMessage(msg);
        }
    }

    private void getPersonalInfo() {
        MyPreferences pre = MyPreferences.getInstance(getApplicationContext());
        String devidTemp = pre.getUserImei() + pre.getUserMac() + pre.getUserImsi();
        String devid = UtilsManager.MD5(devidTemp);
        SynPreferences synPreferences = SynPreferences.getInstance(getApplicationContext());
        PackageManager pm = getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        JSONObject auth_token = new JSONObject();
//        try {
//            auth_token.put("acctk", synPreferences.getAcctk());
//            auth_token.put("up", SysParams.UserPlatform.android);
//            auth_token.put("device", synPreferences.getloginDeviceNumber());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String authToken = Base64.encode(auth_token.toString().getBytes());
//        Hashtable<String, String> table = new Hashtable<String, String>();
//        table.put("app_key", SysParams.appkey);
//        table.put("app_ts", System.currentTimeMillis() + "");
//        table.put("devid", devid);
//        table.put("auth_token", authToken);
//        table.put("uid", synPreferences.getUID());//synPreferences.getUID()
//        table.put("locale", "zh_CN");
//        table.put("channel", UtilsManager.getChannel(this));
//        table.put("local_svc_version", pi.versionCode + "");
//        table.put("app_sign", UtilsManager.getTheAppSign(table));
//        NetManager netManager = NetManager.getInstance();
//        String result = netManager.doGetAsString(SysParams.PERSONAL_INFO, table);
//        rosolveResult(result);

    }

    private boolean rosolveResult(String result) {
        try {
            if (!TextUtils.isEmpty(result)) {

                JSONObject resultObj = new JSONObject(result);
                if (resultObj.has("status") && resultObj.getInt("status") == 1000) {
                    if (resultObj.has("data")) {
                        JSONObject dataObj = resultObj.getJSONObject("data");
                        int messageCount = dataObj.getInt("messageCount");
                        if(messageCount > 0)
                        {
                            Message msg = handler.obtainMessage();
                            msg.what = 10;
                            handler.sendMessage(msg);
                        }
                        else
                        {
                            Message msg = handler.obtainMessage();
                            msg.what = 11;
                            handler.sendMessage(msg);
                        }
                        return true;

                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void Init(){
        titleTV=(TextView)findViewById(R.id.title_tv);
        btn_publish=(ImageView)findViewById(R.id.btn_publish);
        btn_publish.setOnClickListener(onClick);

        iv_hasmsg = (ETNetworkImageView)findViewById(R.id.iv_hasmsg);
        iv_hasmsg.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);

        initBottomArea();
        onClick.onClick(rl_bottom[0]);

        //checkInitAndDoOther();
        LocationUtils.getInstance(this).startLocation(null);
        EventBus.getDefault().register(this);
    }
    /** 底部菜单view初始化 */
    private void initBottomArea() {
        rl_bottom[0]=(RelativeLayout)findViewById(R.id.relativeLayout_bottom_0);
        rl_bottom[1]=(RelativeLayout)findViewById(R.id.relativeLayout_bottom_1);
        rl_bottom[2]=(RelativeLayout)findViewById(R.id.relativeLayout_bottom_2);
        iv_bottom[0] = (ImageView) findViewById(R.id.iv_bottom_0);
        iv_bottom[1] = (ImageView) findViewById(R.id.iv_bottom_1);
        iv_bottom[2] = (ImageView) findViewById(R.id.iv_bottom_2);
        tv_bottom[0]=(TextView)findViewById(R.id.textView_0);
        tv_bottom[1]=(TextView)findViewById(R.id.textView_1);
        tv_bottom[2]=(TextView)findViewById(R.id.textView_2);

        icons = new int[] { R.drawable.main_tab_first, R.drawable.main_tab_first_sel,R.drawable.main_tab_second,R.drawable.main_tab_second_sel
                ,R.drawable.main_tab_third,R.drawable.main_tab_third_sel};
        for (int i = 0; i < rl_bottom.length; i++) {
            rl_bottom[i].setOnClickListener(onClick);
        }// end for
    }
    private View.OnClickListener onClick=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.relativeLayout_bottom_0:
                    if (currentTabPosition == TAB_POSITION_TAB_0) {
                        break;
                    }
                    if (homeActivityFragment== null) {
                        homeActivityFragment = new HomeActivityFragment();
                    }
                    titleTV.setText(getResources().getString(R.string.tab_1));
                    FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content, homeActivityFragment);
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();
                    setCurrentTab(TAB_POSITION_TAB_0);
                    break;
                case R.id.relativeLayout_bottom_1:
                    if (currentTabPosition == TAB_POSITION_TAB_1) {
                        break;
                    }
                    if (nearyByActivityFragment == null) {
                        nearyByActivityFragment = new NearyByActivityFragment();
                    }
                    titleTV.setText(getResources().getString(R.string.tab_2));
                    FragmentTransaction transaction1 = MainActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.content, nearyByActivityFragment);
                    transaction1.addToBackStack(null);
                    transaction1.commitAllowingStateLoss();
                    setCurrentTab(TAB_POSITION_TAB_1);
                    break;
                case R.id.relativeLayout_bottom_2:
                    if (currentTabPosition == TAB_POSITION_TAB_2) {
                        break;
                    }
                    if (mineActivityFragment == null) {
                        mineActivityFragment = new MineActivityFragment();
                    }
                    titleTV.setText(getResources().getString(R.string.tab_3));
                    FragmentTransaction transaction2 = MainActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction2.replace(R.id.content, mineActivityFragment);
                    transaction2.addToBackStack(null);
                    transaction2.commitAllowingStateLoss();
                    setCurrentTab(TAB_POSITION_TAB_2);
                    break;
                case R.id.btn_publish:
                    Intent intent=new Intent(MainActivity.this,PublishActivity.class);
//                    startActivity(intent);
//                    Intent intent = new Intent(MainActivity.this,SelectLocalPicturesActivity.class);
//                    intent.putExtra("only", false);
//                    intent.putExtra("imagesNum",0);
//                    intent.putExtra("isFromTab", true);
//                    intent.putExtra("isNeedShowText",true);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
    /** 设置当前选中tab */
    private void setCurrentTab(int position) {
        int len = iv_bottom.length;
        currentTabPosition = position;
        for (int i = 0; i < len; i++) {
            if (i==position) {
                tv_bottom[i].setTextColor(getResources().getColor(R.color.bottom_green));
                iv_bottom[i].setImageResource(icons[2 * i + 1]);
            }
            else{
                tv_bottom[i].setTextColor(getResources().getColor(R.color.bottom_text));
                iv_bottom[i].setImageResource(icons[2 * i]);
            }
        }
    }
    /** 检查初始化数据和其他操作 */
    private void checkInitAndDoOther() {
        if (!isActivityRun) {
            return;
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 10:
                    iv_hasmsg.setVisibility(View.VISIBLE);
                    break;
                case 11:
                    iv_hasmsg.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        new Thread() {
            @Override
            public void run() {
                getPersonalInfo();
            }
        }.start();
    }

    @Override
    public boolean isUseGestureView() {
        return false;
    }

    private long firstTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (currentTabPosition != 0) {
                onClick.onClick(rl_bottom[0]);
                return true;
            }
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) { //如果两次按键时间间隔大于1秒，则不退出
                UtilsManager.Toast(mActivity, R.string.exit_app);
                firstTime = secondTime;
                return true;
            } else {
                ApplicationManager.getInstance().exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
