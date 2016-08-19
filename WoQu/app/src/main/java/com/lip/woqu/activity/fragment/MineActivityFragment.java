package com.lip.woqu.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.R;
import com.lip.woqu.activity.ApplyActivity;
import com.lip.woqu.activity.CollectActivity;
import com.lip.woqu.activity.MyPublishActivity;
import com.lip.woqu.activity.UserFeedbackActivity;
import com.lip.woqu.activity.UserInfoActivity;
import com.lip.woqu.activity.fragment.event.PersonalLifeDataEvent;
import com.lip.woqu.utils.DBManagerHistoryCache;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.SynPreferences;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.base.ETImageView;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MineActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MineActivityFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private String mParam2;
    private boolean isNeedReadFromNet = true;    //是否从本地读数据
    private OnFragmentInteractionListener mListener;
    private Context ctx;
    private View root;
    private TextView  tv_publish;
    private TextView tv_nick, tv_collect, tv_feed_back,tv_apply;
    private LinearLayout ll_userinfo;
    private ETNetworkImageView personal_ico;

//    /*我的消息 add xujun*/
//    private RelativeLayout rl_mine_msg;
//    private ETNetworkImageView iv_msgbg;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MineActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MineActivityFragment newInstance(String param1, String param2) {
        MineActivityFragment fragment = new MineActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MineActivityFragment() {
        // Required empty public constructor
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity().getApplicationContext();
        root = getActivity().getLayoutInflater().inflate(R.layout.fragment_mine_activity, null);
        init();
        //initData();
    }

    private void init() {
        EventBus.getDefault().register(this);
        tv_publish = (TextView) root.findViewById(R.id.tv_publish);
        tv_collect = (TextView) root.findViewById(R.id.tv_collect);
        tv_feed_back = (TextView) root.findViewById(R.id.tv_feed_back);
        tv_apply=(TextView)root.findViewById(R.id.tv_apply);
        tv_nick=(TextView)root.findViewById(R.id.tv_nick);

        ll_userinfo = (LinearLayout) root.findViewById(R.id.ll_userinfo);
        personal_ico = (ETNetworkImageView) root.findViewById(R.id.personal_ico);
        personal_ico.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);
        tv_nick = (TextView) root.findViewById(R.id.tv_nick);
//        iv_msgbg.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);

        tv_publish.setOnClickListener(this);
        ll_userinfo.setOnClickListener(this);
        tv_collect.setOnClickListener(this);
        tv_feed_back.setOnClickListener(this);
        tv_apply.setOnClickListener(this);
//        rl_mine_msg.setOnClickListener(this);


    }
    //从本地取出用户数据
    private void initData() {
        DBManagerHistoryCache db = DBManagerHistoryCache.open(ctx);
        Cursor cur = db.getCache("personalInfo");
        String result;
        if (cur != null) {
            if (cur.moveToFirst()) {
                result = cur.getString(2);
                if (!TextUtils.isEmpty(result)) {
                    if (System.currentTimeMillis() - cur.getLong(3) < 600000) {
                        isNeedReadFromNet = false;
                    }
                    rosolveResult(result);
                }
            }
            cur.close();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        ViewGroup group = (ViewGroup) root.getParent();
        if (group != null) {
            group.removeView(root);
        }
        return root;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        new Thread() {
            @Override
            public void run() {
                getPersonalInfo();
            }
        }.start();

    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.tv_apply:
                Intent intent = new Intent(getActivity(), ApplyActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.tv_publish:
                Intent pubIntent = new Intent(getActivity(), MyPublishActivity.class);
                getActivity().startActivity(pubIntent);
                break;
            case R.id.ll_userinfo:
                Intent infoIntent = new Intent(getActivity(), UserInfoActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putSerializable("userinfo", mUserInfo);
                infoIntent.putExtras(bundle);
                getActivity().startActivity(infoIntent);
                break;
            case R.id.tv_collect:
                Intent collectIntent = new Intent(getActivity(), CollectActivity.class);
                getActivity().startActivity(collectIntent);
                break;
            case R.id.tv_feed_back:
                Intent feedbackIntent = new Intent(getActivity(), UserFeedbackActivity.class);
                getActivity().startActivity(feedbackIntent);
                break;
            default:
                break;
        }
    }


    private void getPersonalInfo() {
        MyPreferences pre = MyPreferences.getInstance(ctx.getApplicationContext());
        String devidTemp = pre.getUserImei() + pre.getUserMac() + pre.getUserImsi();
        String devid = UtilsManager.MD5(devidTemp);
        SynPreferences synPreferences = SynPreferences.getInstance(ctx.getApplicationContext());
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(ctx.getPackageName(), 0);
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
//        table.put("channel", UtilsManager.getChannel(ctx));
//        table.put("local_svc_version", pi.versionCode + "");
//        table.put("app_sign", UtilsManager.getTheAppSign(table));
//        NetManager netManager = NetManager.getInstance();
//        String result = netManager.doGetAsString(SysParams.PERSONAL_INFO, table);
//        if (rosolveResult(result)) {
//            DBManagerHistoryCache db = DBManagerHistoryCache.open(ctx);
//            db.insertToCache("personalInfo", result, System.currentTimeMillis());
//        }

    }

    public void onEvent(PersonalLifeDataEvent event) {
        if (event != null) {
            new Thread() {
                @Override
                public void run() {
                    getPersonalInfo();
                }
            }.start();
        }
    }


    private boolean rosolveResult(String result) {
        try {
            if (!TextUtils.isEmpty(result)) {

                JSONObject resultObj = new JSONObject(result);
                if (resultObj.has("status") && resultObj.getInt("status") == 1000) {
                    if (resultObj.has("data")) {
//                        mUserInfo = new PersonalInfoBean();
//                        JSONObject dataObj = resultObj.getJSONObject("data");
//
//                        String nickName = dataObj.getString("nick_name");
//                        mUserInfo.nick_Name = nickName;
//
//                        int uId = dataObj.getInt("uid");
//                        mUserInfo.uId = uId;
////                        SynPreferences synPreferences = SynPreferences.getInstance(ctx.getApplicationContext());
////                        synPreferences.setUID(Integer.toString(uId));
//
//                        int collectionCount = dataObj.getInt("collectionCount");
//                        mUserInfo.collectionCount = collectionCount;
//
//                        int postCount = dataObj.getInt("postCount");
//                        mUserInfo.postCount = postCount;
//
//                        int messageCount = dataObj.getInt("messageCount");
//                        mUserInfo.messageCount = messageCount;
//
//                        int commentCount = dataObj.getInt("commentCount");
//                        mUserInfo.commentCount = commentCount;
//
//                        String avatar = dataObj.getString("avatar");
//                        mUserInfo.avatar = avatar;
//
//
//                        Message msg = mHandler.obtainMessage();
//                        msg.what = 10;
//                        msg.obj = mUserInfo;
//                        mHandler.sendMessage(msg);
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
}
