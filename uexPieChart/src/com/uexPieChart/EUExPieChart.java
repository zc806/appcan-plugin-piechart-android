package com.uexPieChart;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import com.uexPieChart.bean.PieChartBean;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

public class EUExPieChart extends EUExBase implements OnGetDataListener {
	public EUExPieChart(Context context, EBrowserView arg1) {
		super(context, arg1);
		this.mainActivity = (Activity) context;
	}

	static String opID = "0";
	static final String functionName = "uexPieChart.loadData";
	static final String callBackName = "uexPieChart.callBackData";
	static final String stopName = "uexPieChart.pieChartStop";
	static final String cbOpenFunName = "uexPieChart.cbOpen";
	static final String onDataFunName = "uexPieChart.onData";
	static final String onTouchUpFunName = "uexPieChart.onTouchUp";
	private Activity mainActivity;
	private PieChartActivity pieContext;
	public static final String TAG = "uexPieChart";

	private int startX = 0;
	private int startY = 0;
	public static int screenWidth = 0;
	public static int screenHeight = 0;

	@Override
	protected boolean clean() {
		close(null);
		return false;
	}

	public void open(String[] params) {
		if(pieContext!=null){
			return;
		}
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		opID = params[0];
		if (params[1].length() != 0) {
			startX = Integer.parseInt(params[1]);
		}
		if (params[2].length() != 0) {
			startY = Integer.parseInt(params[2]);
		}
		if (params[3].length() != 0) {
			screenWidth = Integer.parseInt(params[3]);
		}
		if (params[4].length() != 0) {
			screenHeight = Integer.parseInt(params[4]);
		}
		mainActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				LocalActivityManager mgr = ((ActivityGroup) mContext)
						.getLocalActivityManager();
				Intent intent = new Intent(mContext, PieChartActivity.class);
				Window window = mgr.startActivity(TAG, intent);
				pieContext = (PieChartActivity) window.getContext();
				PieChartActivity.setOpid(opID);
				if (0 == screenWidth || 0 == screenHeight) {
					Display display = pieContext.getWindowManager()
							.getDefaultDisplay();
					screenWidth = display.getWidth();
					screenHeight = display.getHeight();
				}
				View pieDecorView = window.getDecorView();
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						screenWidth, screenHeight);
				lp.leftMargin = startX;
				lp.topMargin = startY;
				addViewToCurrentWindow(pieDecorView, lp);
				pieContext.setGetDataListener(EUExPieChart.this);
			}
		});

		loadData(opID);
	}

	public void loadData(String opID) {
		jsCallback(functionName, Integer.parseInt(opID), 0, 0);
		jsCallback(cbOpenFunName, Integer.parseInt(opID), 0, 0);
	}

	public void close(String[] params) {
		if (null != pieContext) {
			LocalActivityManager mgr = ((ActivityGroup) mContext)
					.getLocalActivityManager();
			destroy(((ActivityGroup) mContext), "3");
			View mPieView = pieContext.getWindow().getDecorView();
			removeViewFromCurrentWindow(mPieView);
			pieContext = null;
		}
	}

	public void setJsonData(String[] params) {
		try {
			JSONObject json = new JSONObject(params[0]);
			String jsonResult = json.getString("data");
			final List<PieChartBean> pieList = PieChartUtility
					.parseData(jsonResult);
			mainActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					pieContext.setData(pieList, screenWidth, screenHeight);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static boolean destroy(ActivityGroup activityGroup, String id) {
		final LocalActivityManager activityManager = activityGroup
				.getLocalActivityManager();
		if (activityManager != null) {
			activityManager.destroyActivity(id, false);
			try {
				final Field mActivitiesField = LocalActivityManager.class
						.getDeclaredField("mActivities");
				if (mActivitiesField != null) {
					mActivitiesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					final Map<String, Object> mActivities = (Map<String, Object>) mActivitiesField
							.get(activityManager);
					if (mActivities != null) {
						mActivities.remove(id);
					}
					final Field mActivityArrayField = LocalActivityManager.class
							.getDeclaredField("mActivityArray");
					if (mActivityArrayField != null) {
						mActivityArrayField.setAccessible(true);
						@SuppressWarnings("unchecked")
						final ArrayList<Object> mActivityArray = (ArrayList<Object>) mActivityArrayField
								.get(activityManager);
						if (mActivityArray != null) {
							for (Object record : mActivityArray) {
								final Field idField = record.getClass()
										.getDeclaredField("id");
								if (idField != null) {
									idField.setAccessible(true);
									final String _id = (String) idField
											.get(record);
									if (id.equals(_id)) {
										mActivityArray.remove(record);
										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onPieChartMove(String opID, int type, String jsonData) {
		jsCallback(callBackName,Integer.parseInt(opID), 0, jsonData);
		jsCallback(onDataFunName,Integer.parseInt(opID), 0, jsonData);

	}

	@Override
	public void onPieChartStop(String ipID, int type, String jsonData) {
		jsCallback(stopName,Integer.parseInt(opID), 0, jsonData);
		jsCallback(onTouchUpFunName,Integer.parseInt(opID), 0, jsonData);
	}
}
