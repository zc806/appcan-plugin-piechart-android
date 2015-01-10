package com.uexPieChart;

import java.util.List;


import com.uexPieChart.bean.PieChartBean;


import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class PieChartActivity extends Activity{
	PieChartView mChartView;
	
	public static  OnGetDataListener getDataListener;
	private static String opid;

	public static String getOpid() {
		return opid;
	}

	public static void setOpid(String opid) {
		PieChartActivity.opid = opid;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	public void setGetDataListener(OnGetDataListener os) {
		getDataListener = os;
	}
	
	public void setData(List<PieChartBean> pieList,int screenWidth, int screenHeight){
		RelativeLayout relativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int size = pieList.size();
		float percentSum = 0;
		for(int i=0;i<size;i++){
			PieChartBean pieBean = pieList.get(i);
			percentSum += Float.parseFloat(pieBean.getValue());
		}
		int jiaoduSum = 0;
		int pSum = 0;
		for(int i=0;i<size-1;i++){
			PieChartBean pieBean = pieList.get(i);
			pieBean.setPercent((int)(Float.parseFloat(pieBean.getValue())/percentSum*100));
			pieBean.setJiaodu((int)(Float.parseFloat(pieBean.getValue())/percentSum*360));
			jiaoduSum += pieBean.getJiaodu();
			pSum += pieBean.getPercent();
			pieList.set(i, pieBean);
		}
		PieChartBean lastBean = pieList.get(size-1);
		lastBean.setPercent(100-pSum);
		lastBean.setJiaodu(360-jiaoduSum);
		pieList.set(size-1, lastBean);
		View view = new PieChartView(this,pieList,screenWidth,screenHeight);
		
		relativeLayout.addView(view,layoutParams);
		setContentView(relativeLayout);
	}
}
