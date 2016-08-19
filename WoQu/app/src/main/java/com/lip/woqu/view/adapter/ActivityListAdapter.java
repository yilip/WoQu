package com.lip.woqu.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.R;
import com.lip.woqu.bean.ActivityBean;
import com.lip.woqu.bean.ActivityListBean;
import com.lip.woqu.view.ETNetworkImageView;
import com.lip.woqu.view.LifeUrlTextView;
import com.lip.woqu.view.base.ETImageView;

/**
 * Created by tiui
 * on 2015/3/23.
 * 附近活动
 */
public class ActivityListAdapter extends BaseAdapter implements View.OnClickListener
{
    private Context context;
    private ActivityListBean activityList;
    private int type;//活动类型
    private Holder holder;
    public ActivityListAdapter(Context context, ActivityListBean activityList,int type) {
        this.context = context;
        this.activityList = activityList;
        this.type=type;
    }

    @Override
    public int getCount() {
        return activityList.getActivityList().size();
    }

    @Override
    public Object getItem(int position) {
        return activityList.getActivityList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView= LayoutInflater.from(context).inflate(R.layout.activity_list_adapter,null);
            holder=new Holder();
            holder.collectLL=(LinearLayout)convertView.findViewById(R.id.ll_collect);
            holder.avatarIV=(ETNetworkImageView)convertView.findViewById(R.id.iv_avatar);
            holder.nickTV=(TextView)convertView.findViewById(R.id.tv_nick);
            holder.timeTV=(TextView)convertView.findViewById(R.id.tv_time);
            holder.contentTV=(LifeUrlTextView)convertView.findViewById(R.id.tv_activity_content);
            holder.applyTV=(TextView)convertView.findViewById(R.id.tv_apply);
            holder.collectTV=(TextView)convertView.findViewById(R.id.tv_collect);
            holder.avatarIV.setDisplayMode(ETImageView.DISPLAYMODE.CIRCLE);//圆角头像
            convertView.setTag(holder);
        }
        else
        {
            holder=(Holder)convertView.getTag();
        }
        holder.avatarIV.setOnClickListener(this);
        holder.collectLL.setOnClickListener(this);
        //设置各个列表项的值
        try{
            ActivityBean ab=activityList.getActivityList().get(position);
            if (ab!=null)
            {
                //暂时没有数据，注释掉
                //holder.avatarIV.setImageUrl(ab.getAvatar(),R.drawable.default_avatar);
                holder.nickTV.setText(ab.getNickName());
                holder.contentTV.setText(ab.getContent());
               // holder.timeTV.setText(ab.getActivityTime().toString());
                holder.applyTV.setText(ab.getParticipateNum()+"/N");
                holder.collectTV.setText(ab.getCollectNum()+"");
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
           switch (v.getId())
           {
               case R.id.iv_avatar:
                   break;
               case R.id.ll_collect:
                   break;
           }
    }
    public void setData(ActivityListBean alb)
    {
        this.activityList=alb;
    }
    public class Holder
    {
        LinearLayout collectLL;
        ETNetworkImageView avatarIV;//头像
        TextView nickTV;//昵称
        TextView timeTV;//时间
        LifeUrlTextView contentTV;//活动内容
        TextView applyTV;//报名人数
        TextView collectTV;//收藏人数
//        TextView typeTV;//活动类型
    }
}
