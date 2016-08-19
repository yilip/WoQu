package com.lip.woqu.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.bean.MapPoiBean;
import com.lip.woqu.bean.MapPoiListBean;
import com.lip.woqu.utils.SearchPoiParser;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.utils.net.LocationUtils;
import com.lip.woqu.utils.net.NetManager;
import com.lip.woqu.utils.net.OtherDataDBManager;

import java.util.Hashtable;



/**
 * Created by lip
 * 根据定位的位置，显示附近位置列表
 */
public class AddressSelectActivity extends EFragmentActivity implements View.OnClickListener {
    /*头部控件*/
    private LinearLayout ll_root;
    private Button btn_back;
    /*我的位置和用户输入的位置*/
    private TextView text_address_user;
    private EditText edit_address;
    private Button button_delete_position;
    private ListView listView_search_adress;
    private LinearLayout ll_my_address;
    /*定位时使用变量*/
//    private LocationClient mLoca1tionClient = null;
    private String lat ="";
    private String lon ="";
    private String cityName = "";
    private String cityCode = "025"; // 默认城市南京
    private String cityKey = "";
    private MapPoiListBean mapPoiListBean = new MapPoiListBean();
    private POIAdapter mAdapter;
    private Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_select);
        ctx = AddressSelectActivity.this;
        init();
        hideSoftInput();
        LocationUtils.getInstance(ctx).startLocation(locationUtilsListener);
    }

    private void init() {
        ll_root = (LinearLayout) findViewById(R.id.ll_root);
        //setTheme(ll_root);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        edit_address = (EditText) findViewById(R.id.edit_address);
        edit_address.addTextChangedListener(mTextWatcher);
        text_address_user = (TextView) findViewById(R.id.text_address_user);
        text_address_user.setOnClickListener(this);
        button_delete_position = (Button) findViewById(R.id.button_delete_position);
        button_delete_position.setOnClickListener(this);
        ll_my_address = (LinearLayout) findViewById(R.id.ll_my_address);
        ll_my_address.setOnClickListener(this);
        listView_search_adress = (ListView) findViewById(R.id.listView_search_adress);
        listView_search_adress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideSoftInput();
                Intent intent = new Intent();
                intent.putExtra("name",mapPoiListBean.list.get(position).PoiName);
                intent.putExtra("address",mapPoiListBean.list.get(position).poiAddress);
                intent.putExtra("cityKey",cityKey);
                intent.putExtra("lat",mapPoiListBean.list.get(position).lat+"");
                intent.putExtra("lon",mapPoiListBean.list.get(position).lon+"");
                intent.putExtra("poi_id",mapPoiListBean.list.get(position).poi_id+"");
                AddressSelectActivity.this.setResult(RESULT_OK,intent);
                AddressSelectActivity.this.finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        hideSoftInput();
        if(v == btn_back){
            this.finish();
        }else if(v == text_address_user){
            if(!TextUtils.isEmpty(text_address_user.getText().toString().trim())){
                Intent intent = new Intent();
                intent.putExtra("name",text_address_user.getText().toString().trim());
                intent.putExtra("address","");
                intent.putExtra("cityKey",cityKey);
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                AddressSelectActivity.this.setResult(RESULT_OK,intent);
                AddressSelectActivity.this.finish();
            }

        }else if(v == button_delete_position){
            edit_address.setText("");
        }else if(v == ll_my_address){
            if(!TextUtils.isEmpty(cityName)){
                Intent intent = new Intent();
                intent.putExtra("name",cityName);
                intent.putExtra("address","");
                intent.putExtra("cityKey",cityKey);
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                AddressSelectActivity.this.setResult(RESULT_OK,intent);
                AddressSelectActivity.this.finish();
            }else{
                LocationUtils.getInstance(ctx).startLocation(locationUtilsListener);
            }

        }
    }
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(edit_address.getText().toString().length()<=0){
                button_delete_position.setVisibility(View.GONE);
                text_address_user.setText(cityName);
            }else {
                button_delete_position.setVisibility(View.VISIBLE);
                text_address_user.setText(edit_address.getText().toString().trim());
            }
            searchAddressByKey(text_address_user.getText().toString().trim(),cityCode,0);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1000:
                    if(mAdapter == null){
                        mAdapter = new POIAdapter();
                        mAdapter.setData(mapPoiListBean);
                        listView_search_adress.setAdapter(mAdapter);
                    }else{
                        mAdapter.setData(mapPoiListBean);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2://定位成功
                    edit_address.setText(cityName);
                    edit_address.setSelection(cityName.length());
                    hideSoftInput();
//                    text_address_user.setText(cityName);
                    break;
                case 3://没有网络定位失败
                    edit_address.setText("");
                    text_address_user.setText("定位失败");
                    break;
            }
        }
    };
    /** 根据用户数据的位置名搜索地址 列表显示 */
    private void searchAddressByKey(final String keyword,final String citycode, final int page) {
        if (!NetManager.isNetworkAvailable(AddressSelectActivity.this)) {
            UtilsManager.Toast(AddressSelectActivity.this, R.string.checknet);
            return;
        }
        new Thread() {
            public void run() {
                mapPoiListBean.list.clear();
                try {
                    Hashtable<String, String> table = new Hashtable<String, String>();
                    table.put("div", "600801v55");
                    table.put("page_num", "20");
                    table.put("page", String.valueOf(page));
                    table.put("keywords", keyword);
                    table.put("citycode", citycode);
                    table.put("t", "search");
                    table.put("_", "134188601896");
                    SearchPoiParser parser = new SearchPoiParser(getApplicationContext());
                    MapPoiListBean tmpListBean = parser.getMsgFromNetwork(null,"http://m2.amap.com/ASS?", table);
                    mapPoiListBean.isHaveNextPage = tmpListBean.isHaveNextPage;
                    mapPoiListBean.list.addAll(tmpListBean.list);
                    if(mapPoiListBean.list.size()>0){
                        mHandler.sendEmptyMessage(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }// end run
        }.start();
    }

    LocationUtils.LocationUtilsListener locationUtilsListener = new LocationUtils.LocationUtilsListener() {
        @Override
        public void onGetLocationSuccess(String city1, String cityKey1, String city2, String cityKey2, String lat, String lon, String address) {
            AddressSelectActivity.this.cityKey = cityKey2;
            AddressSelectActivity.this.cityName = address;
            AddressSelectActivity.this.lat = lat;
            AddressSelectActivity.this.lon = lon;
            String tempCityName = city2;
            if(!TextUtils.isEmpty(tempCityName)){
                tempCityName = tempCityName.replace("市", "");
                OtherDataDBManager db =OtherDataDBManager.getInstance(AddressSelectActivity.this);
                Cursor cur = db.getCitycodeByCityName(tempCityName);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        cityCode = cur.getString(5);
                    }
                    cur.close();
                }
            }
            if (!TextUtils.isEmpty(address)) {
                mHandler.sendEmptyMessage(2);
            }else {
                mHandler.sendEmptyMessage(3);
            }
        }

        @Override
        public void onGetFail() {
            mHandler.sendEmptyMessage(3);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /*搜索出来的地址结果显示*/
    class POIAdapter extends BaseAdapter {
        Holder holder;
        private MapPoiListBean mapPoiListBean;
        public void setData( MapPoiListBean mapPoiListBean){
            this.mapPoiListBean = mapPoiListBean;
        }
        @Override
        public int getCount() {
            return mapPoiListBean.list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = AddressSelectActivity.this.getLayoutInflater();
                convertView = inflater.inflate(R.layout.select_place_activity_item, null);
                holder = new Holder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.textView1);
                holder.tv_address = (TextView) convertView.findViewById(R.id.textView_date);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            MapPoiBean mp = mapPoiListBean.list.get(position);
            holder.tv_title.setText(mp.PoiName);
            if ("".equals(mp.poiAddress)) {
                holder.tv_address.setVisibility(View.GONE);
            } else {
                holder.tv_address.setVisibility(View.VISIBLE);
                holder.tv_address.setText(mp.poiAddress);
            }
            return convertView;
        }
    }

    class Holder {
        TextView tv_title, tv_address;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        if (imm.isActive()) {// 如果开启
            imm.hideSoftInputFromWindow(edit_address.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}