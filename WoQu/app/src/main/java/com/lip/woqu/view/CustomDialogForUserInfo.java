package com.lip.woqu.view;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lip.woqu.R;
import com.lip.woqu.utils.UtilsManager;
import com.lip.woqu.view.wheel.ArrayWheelAdapter;
import com.lip.woqu.view.wheel.NumericWheelAdapter;
import com.lip.woqu.view.wheel.OnWheelChangedListener;
import com.lip.woqu.view.wheel.WheelView;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.etouch.ecalendar.nongliManager.CnNongLiManager;


public class CustomDialogForUserInfo extends Dialog implements View.OnClickListener{
	private Context ctx;
	private LinearLayout  rootView, ll_fragment_datepick;
	private TextView tv_title;
	/** 点击对话框外任意区域是否关闭 */
	private boolean isDismissDialogWhenTouch = true;
	private ClickCallback callBack;
	private Button btn_ok, btn_cancel;
	private EditText et_content;
	private WheelView wv_year, wv_month, wv_day;
	public static final int EDIT_VIEW = 0;// 用于区别显示view，0：edittext，1：wheelView
	public static final int WHEEL_VIEW = 1;// 用于区别显示view，0：edittext，1：wheelView
	public static final int WHEEL_VIEW_TIME = 2;// 时分wheelView
	private int y = 0, M = 0, d = 0, h = 0, m = 0;
	private int which = -1;
	public static boolean hasErr = false,is_normal = true;//true的时候代表公历
	private CheckBox ckb_isnongli;

	public CustomDialogForUserInfo(Context context) {
		super(context, R.style.no_background_dialog);
		ctx = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		rootView = (LinearLayout) inflater.inflate(R.layout.customdialog_for_userinfo, null);
		tv_title = (TextView) rootView.findViewById(R.id.textView1);
		btn_ok = (Button) rootView.findViewById(R.id.button1);
		btn_cancel = (Button) rootView.findViewById(R.id.button2);
		wv_year = (WheelView) rootView.findViewById(R.id.wv_fragmeng_year);
		wv_month = (WheelView) rootView.findViewById(R.id.wv_fragmeng_month);
		wv_day = (WheelView) rootView.findViewById(R.id.wv_fragmeng_day);
		ll_fragment_datepick = (LinearLayout) rootView.findViewById(R.id.ll_fragment_datepick);
		et_content = (EditText) rootView.findViewById(R.id.et_content1);
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		ckb_isnongli = (CheckBox) rootView.findViewById(R.id.ckb_isnongli);
		if(is_normal){
			ckb_isnongli.setChecked(false);
		}else{
			ckb_isnongli.setChecked(true);
		}
		ckb_isnongli.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (0 != y) {
					if (isChecked) {
						is_normal = false;//农历
						setDate(y, M, d,true);
					} else {
						is_normal = true;//公历
						setDate(y, M, d,true);
					}

				}
			}
		});
		this.setContentView(rootView);
	}

	/**
	 * 选择需要显示的UI，edittext还是日期选择
	 * 
	 * @param which
	 */
	public void switchView(int which) {
		this.which = which;
		if (EDIT_VIEW == which) {
			if (View.GONE == et_content.getVisibility()) {
				et_content.setVisibility(View.VISIBLE);
			}
			if (View.VISIBLE == ll_fragment_datepick.getVisibility()) {
				ll_fragment_datepick.setVisibility(View.GONE);
			}
		} else if (WHEEL_VIEW == which || WHEEL_VIEW_TIME == which) {
			if (View.VISIBLE == et_content.getVisibility()) {
				et_content.setVisibility(View.GONE);
			}
			if (View.GONE == ll_fragment_datepick.getVisibility()) {
				ll_fragment_datepick.setVisibility(View.VISIBLE);
			}
		}
	}

    public void setEditViewTypePwd(){
        if (et_content!=null){
            et_content.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            et_content.postInvalidate();
        }

    }

	public void setEditContent(String content) {
		if (!TextUtils.isEmpty(content)) {
			et_content.setText(content);
			int len = content.length();
			if (len > 0) {
				et_content.setSelection(len);
			}
		} else {
			et_content.setText("");
		}
	}

	public void setEditError(String error) {
		et_content.setError(Html.fromHtml("<font color=\"#000000\">" + error + "</font>"));
		et_content.requestFocus();
	}

    /**弹出软键盘*/
    private void showKeyBord(final View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        Timer timer = new Timer(); // 设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // 弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, 0);
            }
        }, 300);
    }

	/**
	 * 日期选择器UI
	 * 
	 * @param year
	 * @param month
	 * @param date
	 */
	public void setDate(int year, int month, int date,boolean isClicked) {
		this.y = year;
		this.M = month;
		this.d = date;
		ckb_isnongli.setVisibility(View.VISIBLE);
		CnNongLiManager cm = new CnNongLiManager();
		if (!is_normal) {//农历
			String[] nongli_months, nongli_days, nongli_days_shao = { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三",
					"十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九" };
			nongli_months = cm.lunarMonth;
			nongli_days = cm.lunarDate;
			if(isClicked){
			long[] t = cm.calGongliToNongli(y, M, d);
			y = (int) t[0];
			M = (int) t[1];
			d = (int) t[2];
			}
			wv_month.setAdapter(new ArrayWheelAdapter<String>(nongli_months, 4));
  			wv_month.setLabel("");
			int monthDay = cm.monthDays(y, M);
			wv_day.setAdapter(new ArrayWheelAdapter<String>(monthDay == 30 ? nongli_days : nongli_days_shao, 4));
			wv_day.setLabel("");
		} else {//公历
			if(isClicked){
				long[] t = cm.nongliToGongli(y, M, d,false);
				y = (int) t[0];
				M = (int) t[1];
				d = (int) t[2];
			}
			wv_month.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
			int monthDay = getOneMonthDays(y, M);
			wv_day.setAdapter(new NumericWheelAdapter(1, monthDay, "%02d"));
			wv_day.setLabel("日");
			wv_month.setLabel("月");
		}
		wv_year.setAdapter(new NumericWheelAdapter(1901, 2070, "%02d"));
		wv_year.setCyclic(true);
		wv_year.setCurrentItem(y - 1901);
		wv_year.setVisibleItems(3);
		wv_year.setLabel("年");
		wv_month.setCyclic(true);
		wv_month.setCurrentItem(M - 1);
		wv_month.setVisibleItems(3);
		wv_day.setCyclic(true);
		wv_day.setCurrentItem(d - 1);
		wv_day.setVisibleItems(3);
		wv_year.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				y = newValue + 1901;
			}
		});
		wv_month.addChangingListener(new OnWheelChangedListener() {

			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				M = newValue + 1;

			}
		});
		wv_day.addChangingListener(new OnWheelChangedListener() {

			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				d = newValue + 1;
			}
		});

	}

	/** 调用者是定时分享页面，设置弹框时间 */
	public void setTime(int hour, int min) {
		this.h = hour;
		this.m = min;
		ckb_isnongli.setVisibility(View.GONE);
		wv_day.setVisibility(View.GONE);
		wv_year.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));// 这里wv_year为hour，wv_month为minute
		wv_year.setCyclic(true);
		wv_year.setVisibleItems(3);
		wv_year.setLabel(ctx.getString(R.string.shijian_shi));
		wv_year.setCurrentItem(hour);
		wv_month.setCyclic(true);
		wv_month.setVisibleItems(3);
		wv_month.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_month.setLabel(ctx.getString(R.string.shijian_fen));
		wv_month.setCurrentItem(m);
		wv_year.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				h = newValue;
			}
		});
		wv_month.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				m = newValue;
			}
		});
	}

	/**
	 * 获取UI中的文字或者日期
	 * 
	 * @param which
	 * @return
	 */
	public String getResult(int which) {
		if (EDIT_VIEW == which) {
			return et_content.getText().toString().trim();
		} else if (WHEEL_VIEW == which) {
			String result = y + "-" + UtilsManager.intTo2String(M) + "-" + UtilsManager.intTo2String(d);
//			if(is_normal){//公历
				return result;
//			}else{
//				return UtilsManager.convertToNongli(result);
//			}
		} else if (WHEEL_VIEW_TIME == which) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int year = cal.get(Calendar.YEAR);
			String month = UtilsManager.intTo2String(cal.get(Calendar.MONTH) + 1);
			String date = UtilsManager.intTo2String(cal.get(Calendar.DATE));
			return year + month + date + UtilsManager.intTo2String(h) + UtilsManager.intTo2String(m);

		}
		return null;
	}

	/** 返回y年m月的天数 */
	public int getOneMonthDays(int y, int m) {

		if (m == 1 || m == 3 || m == 5 || m == 7 || m == 8 || m == 10 || m == 12) {
			return 31;
		} else if (m == 4 || m == 6 || m == 9 || m == 11) {
			return 30;
		} else {
			if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) {
				return 29;
			} else {
				return 28;
			}
		}
	}

    @Override
    public void onClick(View v) {
        
        if (v == btn_ok) {
            if (callBack != null) {
                callBack.ok_click(getResult(which));// 确定按钮回调返回字符串
                if (!hasErr) {
                    dismiss();
                }
            }
        } else if (v == btn_cancel) {
            if (callBack != null) {
                callBack.cancel_click();
                et_content.setError(null);
                hasErr = false;
                dismiss();
            }
        }
    }

	public void setTitle(String title) {
		if (title != null) {
			tv_title.setText(title);
		}
	}

	public void setTitle(int titleid) {
		tv_title.setText(ctx.getResources().getString(titleid));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 关闭显示日期对话框
		if (isDismissDialogWhenTouch) {
			hasErr = false;
			et_content.setError(null);
			dismiss();
		}
		return super.onTouchEvent(event);
	}

	public void setOnclick(ClickCallback c) {
		this.callBack = c;
	}

	public interface ClickCallback {
		void ok_click(String value);

		void cancel_click();
	}

    @Override
    public void show() {
        if (which==EDIT_VIEW){
            showKeyBord(et_content);
        }
        super.show();
    }
}
