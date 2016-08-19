package com.lip.woqu.bean;

import java.security.Timestamp;
import java.util.Date;

/**
 * Created by tiui on 2015/3/24.
 */
public class ActivityBean
{
    public int activityId;//活动Id
    public int userId;//用户Id
    public String nickName;//用户昵称
    public String avatar;//用户头像地址
    public int type;//活动类型
    public String gatherPlace;//距离地
    public String activityPlace;//活动地
    public Date gatherTime;//聚集时间
    public Date activityTime;//活动时间
    public String content;//活动内容
    public String sign;//活动说明
    public int collectNum;//收藏数
    public int participateNum;//参与数
    public String otherLimit;//其他限制
    public String cover;//背景图片
    public ActivityBean()
    {
        //.....
    }
    public ActivityBean(int activityId, int userId, String nickName, String avatar,
                        int type, String content, int collectNum, int participateNum,
                        String cover) {
        this.activityId = activityId;
        this.userId = userId;
        this.nickName = nickName;
        this.avatar = avatar;
        this.type = type;
        this.content = content;
        this.collectNum = collectNum;
        this.participateNum = participateNum;
        this.cover = cover;
    }

    public ActivityBean(int activityId, int userId, String nickName, String avatar,
                        int type, String gatherPlace, String activityPlace,
                        Date gatherTime, Date activityTime, String content,
                        String sign, int collectNum, int participateNum, String cover)
    {
        this.activityId = activityId;
        this.userId = userId;
        this.nickName = nickName;
        this.avatar = avatar;
        this.type = type;
        this.gatherPlace = gatherPlace;
        this.activityPlace = activityPlace;
        this.gatherTime = gatherTime;
        this.activityTime = activityTime;
        this.content = content;
        this.sign = sign;
        this.collectNum = collectNum;
        this.participateNum = participateNum;
        this.cover = cover;
    }
    public String toString()
    {
        return "";
    }
    public int getActivityId() {
        return activityId;
    }

    public int getUserId() {
        return userId;
    }

    public int getType() {
        return type;
    }

    public String getGatherPlace() {
        return gatherPlace;
    }

    public String getActivityPlace() {
        return activityPlace;
    }

    public Date getGatherTime() {
        return gatherTime;
    }

    public Date getActivityTime() {
        return activityTime;
    }

    public String getContent() {
        return content;
    }

    public String getSign() {
        return sign;
    }

    public int getCollectNum() {
        return collectNum;
    }

    public int getParticipateNum() {
        return participateNum;
    }

    public String getNickName() {
        return nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCover() {
        return cover;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setGatherPlace(String gatherPlace) {
        this.gatherPlace = gatherPlace;
    }

    public void setActivityPlace(String activityPlace) {
        this.activityPlace = activityPlace;
    }

    public void setGatherTime(Date gatherTime) {
        this.gatherTime = gatherTime;
    }

    public void setActivityTime(Date activityTime) {
        this.activityTime = activityTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setCollectNum(int collectNum) {
        this.collectNum = collectNum;
    }

    public void setParticipateNum(int participateNum) {
        this.participateNum = participateNum;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
