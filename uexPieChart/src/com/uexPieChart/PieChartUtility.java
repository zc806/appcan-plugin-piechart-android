package com.uexPieChart;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uexPieChart.bean.PieChartBean;

import android.graphics.Color;

public class PieChartUtility {
	public static int parseColor(String colorStr) {
		colorStr.trim();
		if ("rgb".equals(colorStr.toLowerCase().substring(0, 3))) {
			if ("rgba".equals(colorStr.toLowerCase().subSequence(0, 4))) {
				String colorTemp = colorStr.substring(5, colorStr.length() - 1);
				String colorArray[] = colorTemp.split(",");
				return Color.argb(Integer.parseInt(colorArray[3]),
						Integer.parseInt(colorArray[0]),
						Integer.parseInt(colorArray[1]),
						Integer.parseInt(colorArray[2]));
			} else {
				String colorTemp = colorStr.substring(4, colorStr.length() - 1);
				String colorArray[] = colorTemp.split(",");
				return Color.rgb(Integer.parseInt(colorArray[0]),
						Integer.parseInt(colorArray[1]),
						Integer.parseInt(colorArray[2]));
			}
		} else if (colorStr.charAt(0) == '#' && colorStr.length() == 4) {
			String colorTemp = colorStr.substring(1);
			StringBuffer colorSb = new StringBuffer();
			colorSb.append("#");
			colorSb.append(colorTemp.charAt(0));
			colorSb.append(colorTemp.charAt(0));
			colorSb.append(colorTemp.charAt(1));
			colorSb.append(colorTemp.charAt(1));
			colorSb.append(colorTemp.charAt(2));
			colorSb.append(colorTemp.charAt(2));
			return Color.parseColor(colorSb.toString());
		} else if (colorStr.charAt(0) == '#' && colorStr.length() == 9) {
			return Color.argb(Integer.parseInt(colorStr.substring(1, 3), 16),
					Integer.parseInt(colorStr.substring(3, 5), 16),
					Integer.parseInt(colorStr.substring(5, 7), 16),
					Integer.parseInt(colorStr.substring(7, 9), 16));
		} else {
			return Color.parseColor(colorStr);
		}
	}

	public static List<PieChartBean> parseData(String jsonData) {
		if (null == jsonData || jsonData.length() == 0) {
			return null;
		}
		List<PieChartBean> list = new ArrayList<PieChartBean>();
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				PieChartBean pieBean = new PieChartBean();
				pieBean.setTitle(jsonItem.optString("title"));
				pieBean.setValue(jsonItem.optString("value"));
				pieBean.setColor(jsonItem.optString("color"));
				pieBean.setSubTitle(jsonItem.optString("subTitle"));
				pieBean.setJsonData(jsonArray.getString(i));
				list.add(pieBean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

}
