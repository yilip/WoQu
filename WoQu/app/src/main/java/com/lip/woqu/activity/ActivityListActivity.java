package com.lip.woqu.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.bean.ActivityBean;
import com.lip.woqu.bean.ActivityListBean;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.utils.net.NetParams;
import com.lip.woqu.view.ErrorNetView;
import com.lip.woqu.view.MyListView;
import com.lip.woqu.view.adapter.ActivityListAdapter;
import com.lip.woqu.view.refresh.PullToRefreshBase;
import com.lip.woqu.view.refresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityListActivity extends EFragmentActivity implements View.OnClickListener {

    private LinearLayout ll_progress;
    private ActivityListAdapter myAdapter = null;
    private View footView,headerView;
    private LinearLayout ll_header;
    private ImageView iv_anim_loading_new;
    private AnimationDrawable loadingAnim;
    private TextView titleTV;
    private LinearLayout ll_nodata;
    private int lastViewRom = 0;
    private boolean isLoadingFootView = false;
    private boolean isLoadingData = false;
    private ArrayList<ActivityBean> activityList = new ArrayList<ActivityBean>();
    private ErrorNetView errNetView;
    private boolean isRefresh = false;
    private MyListView mListview;
    private PullToRefreshListView pullToRefreshListView;
    private boolean isHasNext = false;
    private int minHeight = 0;
    private int listHeight = Integer.MAX_VALUE;
    private int type = -1;//活动类型,默认全部
    private MyListView.MainScrollUpDownListener scrollUpDownListener;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initView();
    }

    private void init(){

    }

    private MyListView.MainScrollUpDownListener scrolListener = new MyListView.MainScrollUpDownListener() {
        @Override
        public void onScrollUpDown(int type) {
            if (scrollUpDownListener != null && listHeight >= minHeight) {
                UtilsManager.println("lip--->listHeight:" + listHeight);
                UtilsManager.println("lip--->minHeight:"+minHeight);
                scrollUpDownListener.onScrollUpDown(type);
            }
        }
    };

    private void initView() {
        type = getIntent().getIntExtra("type",0);
        ll_nodata = (LinearLayout) findViewById(R.id.ll_nodata);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.listView1);
        pullToRefreshListView.setDisableScrollingWhileRefreshing(false);
        mListview = pullToRefreshListView.getRefreshableView();
        mListview.setMainScrollUpDownListener(scrolListener);
        ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
        ll_progress.setVisibility(View.VISIBLE);
        footView = LayoutInflater.from(this).inflate(R.layout.more_activity_footview, null);
        iv_anim_loading_new = (ImageView) footView.findViewById(R.id.iv_anim_loading_new);
        loadingAnim = (AnimationDrawable) iv_anim_loading_new.getDrawable();
        loadingAnim.start();
        //标题活动类型
        titleTV=(TextView)findViewById(R.id.tv_title);
        switch (type)
        {
            case -1:
                titleTV.setText(getResources().getString(R.string.activity_all));break;
            case 0:
                titleTV.setText(getResources().getString(R.string.activity_other));break;
            case 1:
                titleTV.setText(getResources().getString(R.string.activity_sport));break;
            case 2:
                titleTV.setText(getResources().getString(R.string.activity_entertainment));break;
            case 3:
                titleTV.setText(getResources().getString(R.string.activity_relaxtion));break;
            case 4:
                titleTV.setText(getResources().getString(R.string.activity_study));break;
            default:
                titleTV.setText(getResources().getString(R.string.activity_other));break;
        }
        errNetView = (ErrorNetView)findViewById(R.id.error_net_view);
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
                Intent intent = new Intent(ActivityListActivity.this, DetailActivity.class);
                UtilsManager.println("activityId:"+ab.getActivityId());
                UtilsManager.println("userId:"+ab.getUserId());
                intent.putExtra("activityId",ab.getActivityId());
                intent.putExtra("userId",ab.getUserId());
                startActivity(intent);
            }
        });
        int mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        minHeight = mScreenHeight - UtilsManager.dip2px(this, 104) - UtilsManager.getStatusBarHeight(this);
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
        getActivities(true ,true);
//        if (myAdapter == null) {
//            myAdapter = new ActivityListAdapter(this,new ActivityListBean(0,0,activityList),0);
//            mListview.setAdapter(myAdapter);
//        } else {
//            if(mListview.getAdapter()!=myAdapter)
//                mListview.setAdapter(myAdapter);
//            myAdapter.setData(new ActivityListBean(0,0,activityList));
//            myAdapter.notifyDataSetChanged();
//        }
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
    //按活动类型得到所有活动信息
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
                    NetParams params = new NetParams();
                    params.addParam("typeId",type+"");
                    String result= NetManager.getInstance().doGetAsString(SysParams.ACTIVITIES_URL,params);
                    Log.d("Result:", result);
                    JSONObject jObject=new JSONObject(result);
                    int stateResult=jObject.getInt("result");
                    if(stateResult>0)//有返回结果
                    {
                        JSONArray jsonArray=jObject.getJSONArray("activities");
                        for(int i=0;i<jsonArray.length();i++)
                        {
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
                            ab.activityId=activityObject.getInt("activityId");
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
            switch (msg.what) {
                case 0:
                    errNetView.hide();
                    pullToRefreshListView.setVisibility(View.VISIBLE);
                    ll_progress.setVisibility(View.GONE);
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
                        myAdapter = new ActivityListAdapter(ActivityListActivity.this,new ActivityListBean(0,0,activityList),0);
                        mListview.setAdapter(myAdapter);
                    } else {
                        myAdapter.setData(new ActivityListBean(0,0,activityList));
                        myAdapter.notifyDataSetChanged();
                    }
                    break;
                case 1:
                    isLoadingData = false;
                    errNetView.hide();
                    ll_progress.setVisibility(View.GONE);
                    pullToRefreshListView.setVisibility(View.VISIBLE);
                    if (isRefresh){
                        isRefresh = false;
                        //activityList.clear();
                    }
                    //activityList.addAll(tempList);
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
                        myAdapter = new ActivityListAdapter(ActivityListActivity.this,new ActivityListBean(0,0,activityList),0);
                        mListview.setAdapter(myAdapter);
                    } else {
                        myAdapter.setData(new ActivityListBean(0,0,activityList));
                        myAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    isLoadingData = false;
                    ll_progress.setVisibility(View.GONE);
                    ll_nodata.setVisibility(View.GONE);
                    if (activityList != null && activityList.size() != 0) {
                        UtilsManager.Toast(ActivityListActivity.this, getString(R.string.net_error));
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
