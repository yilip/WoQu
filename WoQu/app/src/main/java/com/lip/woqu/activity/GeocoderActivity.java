package com.lip.woqu.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.lip.woqu.EFragmentActivity;
import com.lip.woqu.R;
import com.lip.woqu.utils.AMapUtil;
import com.lip.woqu.utils.ToastUtil;
import com.lip.woqu.utils.UtilsManager;

/**
 * @author lip
 * 根据传过来的经纬度，或者地名定位具体位置
 */
public class GeocoderActivity extends EFragmentActivity implements
        GeocodeSearch.OnGeocodeSearchListener {
	private ProgressDialog progDialog = null;
	private GeocodeSearch geocoderSearch;
	private String addressName;
	private AMap aMap;
	private MapView mapView;
	private Marker geoMarker;
	private Marker regeoMarker;
    private Marker locationMarker; // 选择的点
    private LatLonPoint latLonPoint;
    private Button btn_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geocoder_activity);
		mapView = (MapView) findViewById(R.id.map);
        btn_back=(Button)findViewById(R.id.btn_back);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		}
//		Button geoButton = (Button) findViewById(R.id.geoButton);
//		geoButton.setOnClickListener(this);
//		Button regeoButton = (Button) findViewById(R.id.regeoButton);
//		regeoButton.setOnClickListener(this);
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		progDialog = new ProgressDialog(this);
        String addr = getIntent().getStringExtra("addr");
        try {
            double lon = Double.parseDouble(getIntent().getStringExtra("lon"));
            double lan = Double.parseDouble(getIntent().getStringExtra("lan"));
            latLonPoint = new LatLonPoint(lan, lon);
            UtilsManager.println("lip->" + "lon:" + lon + ",lan:" + lan);
            if (0.0==lon||0.0==lan)
            {
                getLatlon(addr);
            }
            else getAddress(latLonPoint);
        }
        catch (Exception e)
        {
            //do nothing

            if (null!=addr&&!addr.equals(""))
            {
                UtilsManager.println("lip->" + addr);
                getLatlon(addr);
            }
        }
        btn_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 显示进度条对话框
	 */
	public void showDialog() {
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("正在获取地址");
		progDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	/**
	 * 响应地理编码
	 */
	public void getLatlon(final String name) {
		showDialog();
		GeocodeQuery query = new GeocodeQuery(name, "010");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		showDialog();
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 0) {
			if (result != null && result.getGeocodeAddressList() != null
					&& result.getGeocodeAddressList().size() > 0) {
				GeocodeAddress address = result.getGeocodeAddressList().get(0);
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
				geoMarker.setPosition(AMapUtil.convertToLatLng(address
                        .getLatLonPoint()));
//				addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
//						+ address.getFormatAddress();
                locationMarker = aMap.addMarker(new MarkerOptions()
                        .anchor(0.5f, 1)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.point))
                        .position(new LatLng(address.getLatLonPoint().getLatitude(), address.getLatLonPoint().getLongitude()))
                        .title(address.getFormatAddress()));
                locationMarker.showInfoWindow();
			} else {
				ToastUtil.show(GeocoderActivity.this, "无结果");
			}

		} else if (rCode == 27) {
			ToastUtil.show(GeocoderActivity.this, "网络出现问题");
		} else if (rCode == 32) {
			ToastUtil.show(GeocoderActivity.this, "error_key");
		} else {
			ToastUtil.show(GeocoderActivity.this,
					"error_other"+ rCode);
		}
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				addressName = result.getRegeocodeAddress().getFormatAddress()
						+ "附近";
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(latLonPoint), 15));
				regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
                locationMarker = aMap.addMarker(new MarkerOptions()
                        .anchor(0.5f, 1)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.point))
                        .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()))
                        .title(addressName));
                locationMarker.showInfoWindow();
				//ToastUtil.show(GeocoderActivity.this, addressName);
			} else {
				ToastUtil.show(GeocoderActivity.this, "无结果");
			}
		} else if (rCode == 27) {
			ToastUtil.show(GeocoderActivity.this, "网络出现问题");
		} else if (rCode == 32) {
			ToastUtil.show(GeocoderActivity.this, "error_key");
		} else {
			ToastUtil.show(GeocoderActivity.this,
					"error_other" + rCode);
		}
	}

    @Override
    public int getAsGestureViewScale() {
        return 10;
    }
    //	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		/**
//		 * 响应地理编码按钮
//		 */
//		case R.id.geoButton:
//			getLatlon("福码大厦");
//			break;
//		/**
//		 * 响应逆地理编码按钮
//		 */
//		case R.id.regeoButton:
//			getAddress(latLonPoint);
//			break;
//		default:
//			break;
//		}
//
//	}
}