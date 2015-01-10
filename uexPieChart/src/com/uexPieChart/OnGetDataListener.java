package com.uexPieChart;

public interface OnGetDataListener {
	void onPieChartMove(String opID,int type,String jsonData);
	void onPieChartStop(String ipID,int type,String jsonData);
}
