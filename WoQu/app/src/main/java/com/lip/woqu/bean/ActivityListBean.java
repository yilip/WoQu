package com.lip.woqu.bean;

import java.util.ArrayList;

/**
 * Created by lip
 * on 2015/3/24.
 */
public class ActivityListBean
{
    private int type;//活动类型
    private int sort;//活动排序，目前：0 表示按附件排序
    private ArrayList<ActivityBean>activityList;

    public ActivityListBean(int type, int sort, ArrayList<ActivityBean> activityList) {
        this.type = type;
        this.sort = sort;
        this.activityList = activityList;
    }
    public ArrayList<ActivityBean> getActivityList() {
        return activityList;
    }

    public int getType() {
        return type;
    }

    public int getSort() {
        return sort;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setActivityList(ArrayList<ActivityBean> activityList) {
        this.activityList = activityList;
    }
}
