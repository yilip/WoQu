package com.lip.woqu.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lip.woqu.R;
import com.lip.woqu.activity.DetailActivity;
import com.lip.woqu.bean.ActivityBean;
import com.lip.woqu.bean.ActivityListBean;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.view.ErrorNetView;
import com.lip.woqu.view.MyListView;
import com.lip.woqu.view.adapter.ActivityListAdapter;
import com.lip.woqu.view.refresh.PullToRefreshBase;
import com.lip.woqu.view.refresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link NearyByActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearyByActivityFragment extends Fragment implements View.OnClickListener{


    private LinearLayout ll_progress;
    private ActivityListAdapter myAdapter = null;
    private View footView,headerView;
    private LinearLayout ll_header;
    private ImageView iv_anim_loading_new;
    private AnimationDrawable loadingAnim;
    private LinearLayout ll_nodata;
    private int lastViewRom = 0;
    private boolean isLoadingFootView = false;
    private boolean isLoadingData = false;
    private ArrayList<ActivityBean> activityList = new ArrayList<ActivityBean>();
    private ErrorNetView errNetView;
    private boolean isRefresh = false;
    private MyListView mListview;
    private PullToRefreshListView pullToRefreshListView;
    private Activity mActivity;
    private boolean isHasNext = false;
    private int minHeight = 0;
    private int listHeight = Integer.MAX_VALUE;
    //0默认热门 1对应targetuid的topic
    private int data_type = 0;
    View rootView;
    private MyListView.MainScrollUpDownListener scrollUpDownListener;

    public static NearyByActivityFragment newInstance(String param1, String param2) {
        NearyByActivityFragment fragment = new NearyByActivityFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NearyByActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mActivity = getActivity();
       // initView();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_neary_by_activity,null);
        initView();
        return rootView;
    }

    private MyListView.MainScrollUpDownListener scrolListener = new MyListView.MainScrollUpDownListener() {
        @Override
        public void onScrollUpDown(int type) {
            if (scrollUpDownListener != null && listHeight >= minHeight) {
                UtilsManager.println("lip--->listHeight:"+listHeight);
                UtilsManager.println("lip--->minHeight:"+minHeight);
                scrollUpDownListener.onScrollUpDown(type);
            }
        }
    };

    private void initView() {
       // rootView=LayoutInflater.from(mActivity).inflate(R.layout.fragment_neary_by_activity,null);

        data_type = mActivity.getIntent().getIntExtra("data_type",0);
        ll_nodata = (LinearLayout) rootView.findViewById(R.id.ll_nodata);
        pullToRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.listView1);
        pullToRefreshListView.setDisableScrollingWhileRefreshing(false);
        mListview = pullToRefreshListView.getRefreshableView();
        mListview.setMainScrollUpDownListener(scrolListener);
        ll_progress = (LinearLayout) rootView.findViewById(R.id.ll_progress);
        ll_progress.setVisibility(View.VISIBLE);
        footView = LayoutInflater.from(getActivity()).inflate(R.layout.more_activity_footview, null);
        iv_anim_loading_new = (ImageView) footView.findViewById(R.id.iv_anim_loading_new);
        loadingAnim = (AnimationDrawable) iv_anim_loading_new.getDrawable();
        loadingAnim.start();

        errNetView = (ErrorNetView) rootView.findViewById(R.id.error_net_view);
        errNetView.hide();
        errNetView.setMessage(getResources().getString(R.string.error_net_msg));
        errNetView.setActionButton(getResources().getString(R.string.error_net_action), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryAction();
            }
        });

        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefreshByPullUp() {

            }

            @Override
            public void onRefreshByPullDown() {
                if (!isLoadingData){
                    isRefresh = true;
                    getActivities(true,true);
                    if (scrollUpDownListener != null) {
                        scrollUpDownListener.onScrollUpDown(1);
                    }
                }else {
                    if (pullToRefreshListView != null){
                        pullToRefreshListView.onRefreshComplete();
                    }
                }
            }
        });
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int headerCount = mListview.getHeaderViewsCount();
                if (arg2 < headerCount) {
                    return;
                }
                ActivityBean ab=activityList.get(arg2-headerCount);
                myAdapter.notifyDataSetChanged();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                UtilsManager.println("activityId:"+ab.getActivityId());
                UtilsManager.println("userId:"+ab.getUserId());
                intent.putExtra("activityId",ab.getActivityId());
                intent.putExtra("userId",ab.getUserId());
                startActivity(intent);
            }
        });
        int mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        minHeight = mScreenHeight - UtilsManager.dip2px(getActivity(), 104) - UtilsManager.getStatusBarHeight(getActivity());
        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isHasNext && lastViewRom >= activityList.size()) {
                    //getHotTags(true,true,lastTime);
                    getActivities(true,true);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastViewRom = firstVisibleItem + visibleItemCount;
                if (absListView.getCount() > 0) {
                    View height = absListView.getChildAt(absListView.getLastVisiblePosition());
                    if (height != null){
                        listHeight = height.getTop() + height.getHeight();
                    }
                }
            }
        });
        isRefresh = true;
        //getHotTags(false, false,"");
        getActivities(true,true);

    }

    @Override
    public void onClick(View view) {

    }

    private void retryAction() {
        errNetView.setVisibility(View.GONE);
        isRefresh = true;
        getActivities(true ,true);
        ll_progress.setVisibility(View.VISIBLE);
    }
     //得到所有活动信息
    private void getActivities(final boolean isOnlyFromNet, final boolean isPullUpRefresh) {
        if (isLoadingData) {
            return;
        }
        isLoadingData = true;
        new Thread() {
            public void run() {
                if (!isPullUpRefresh) {
                    handler.sendEmptyMessage(3);
                }
                try {
                    activityList.clear();
//                    TagListBean listBeanFromNet = tagParser.getTopicSquareTags(map, TextUtils.isEmpty(lastTimestamp));
                    String result= NetManager.getInstance().doGetAsString(SysParams.ACTIVITIES_URL);
                    Log.d("Result:",result);
                    JSONObject jObject=new JSONObject(result);
                    int stateResult=jObject.getInt("result");
                    if(stateResult>0)//有返回结果
                    {
                        JSONArray jsonArray=jObject.getJSONArray("activities");
                        for(int i=0;i<jsonArray.length();i++)
                        {
//                            {
//                                "activity": {
//                                "activityId": 1,
//                                        "activityPlace": "奥体",
//                                        "activityTime": 1426485630000,
//                                        "content": "一起跑步吧",
//                                        "gatherPlace": "西园四区",
//                                        "gatherTime": 1426491030000,
//                                        "sign": "都来吧，看谁是跑步健将",
//                                        "typeId": 1,
//                                        "userId": 10001
//                            },
//                                "applyNum": 3,
//                                    "collectNum": 3,
//                                    "nickName": "李小布"
//                            },

                            JSONObject object=(JSONObject)jsonArray.get(i);
                            JSONObject activityObject=object.getJSONObject("activity");
                            Log.d("Activity:",activityObject.toString());
                            int applyNum=object.getInt("applyNum");
                            int collectNum=object.getInt("collectNum");
                            String nickName=object.getString("nickName");
                            String acitvityTime=activityObject.getString("activityTime");
                            String content=activityObject.getString("content");
                            int userId=activityObject.getInt("userId");
                            ActivityBean ab=new ActivityBean();
                            ab.setUserId(userId);
                            ab.setParticipateNum(applyNum);
                            ab.setCollectNum(collectNum);
                            ab.setNickName(nickName);
                            ab.setContent(content);
                            activityList.add(ab);
                        }
                    }
                        if (activityList.size()> 10){
                            isHasNext = true;
                        }else {
                            isHasNext = false;
                        }
                    if(activityList.size()>0)
                    {
                        Message msg2 = handler.obtainMessage();
                        msg2.what = 1;
                       // msg2.obj = activityList;
                        handler.sendMessage(msg2);
                    }else {
                        handler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                } finally {
                    if (pullToRefreshListView != null){
                        pullToRefreshListView.onRefreshComplete();
                    }
                }
            }

        }.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity() == null || getActivity().isFinishing()){
                return;
            }
            switch (msg.what) {
                case 0:
                    errNetView.hide();
                    pullToRefreshListView.setVisibility(View.VISIBLE);
                    ll_progress.setVisibility(View.GONE);
                    ArrayList<ActivityBean> tempList = (ArrayList<ActivityBean>) msg.obj;
                    if (tempList == null) {
                        return;
                    }
                    activityList.clear();
                    activityList.addAll(tempList);
                    if (activityList.size() == 0) {
                        ll_nodata.setVisibility(View.VISIBLE);
                    } else {
                        ll_nodata.setVisibility(View.GONE);
                    }
                    if (isHasNext && mListview.getFooterViewsCount() < 1
                            && !isLoadingFootView) {
                        mListview.addFooterView(footView);
                        isLoadingFootView = true;
                    } else if (!isHasNext && mListview.getFooterViewsCount() > 0
                            && isLoadingFootView) {
                        isLoadingFootView = false;
                        mListview.removeFooterView(footView);
                    }
                    if (myAdapter == null) {
                        myAdapter = new ActivityListAdapter(getActivity(),new ActivityListBean(0,0,activityList),0);
                        mListview.setAdapter(myAdapter);
                    } else {
                        if(mListview.getAdapter()!=myAdapter)
                            mListview.setAdapter(myAdapter);
                        myAdapter.setData(new ActivityListBean(0,0,activityList));
                        myAdapter.notifyDataSetChanged();
                    }
                    break;
                case 1:
                    isLoadingData = false;
                    errNetView.hide();
                    ll_progress.setVisibility(View.GONE);
                    pullToRefreshListView.setVisibility(View.VISIBLE);
//                    tempList = (ArrayList<ActivityBean>) msg.obj;
//                    if (tempList == null) {
//                        return;
//                    }
                    if (isRefresh){
                        isRefresh = false;
                       // activityList.clear();
                    }
                   // activityList.addAll(tempList);
                    if (isHasNext){
                        footView.setVisibility(View.VISIBLE);
                    }
                    if (isHasNext && mListview.getFooterViewsCount() < 1
                            && !isLoadingFootView) {
                        mListview.addFooterView(footView);
                        isLoadingFootView = true;
                    } else if (!isHasNext && mListview.getFooterViewsCount() > 0
                            && isLoadingFootView) {
                        isLoadingFootView = false;
                        mListview.removeFooterView(footView);
                    }
                    if (activityList.size() == 0) {
                        ll_nodata.setVisibility(View.VISIBLE);
                    } else {
                        ll_nodata.setVisibility(View.GONE);
                    }
                    if (myAdapter == null) {
                        myAdapter = new ActivityListAdapter(getActivity(),new ActivityListBean(0,0,activityList),0);
                        mListview.setAdapter(myAdapter);
                    } else {
                        if(mListview.getAdapter()!=myAdapter)
                            mListview.setAdapter(myAdapter);
                        myAdapter.setData(new ActivityListBean(0,0,activityList));
                        myAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    isLoadingData = false;
                    ll_progress.setVisibility(View.GONE);
                    ll_nodata.setVisibility(View.GONE);
                    if (activityList != null && activityList.size() != 0) {
                        UtilsManager.Toast(getActivity(), getString(R.string.net_error));
                        return;
                    }
                    errNetView.show();
                    pullToRefreshListView.setVisibility(View.GONE);
                    break;
                case 3:
                    errNetView.hide();
                    ll_progress.setVisibility(View.VISIBLE);
                    ll_nodata.setVisibility(View.GONE);
                    pullToRefreshListView.setVisibility(View.GONE);
                    break;
            }
        }
    };

}
