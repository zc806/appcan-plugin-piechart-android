package com.uexPieChart.bean;

public class PieChartBean {
	private String title;
	private String value;
	private String color;
	private String subTitle;
	private String jsonData;
	private int percent;
	private int jiaodu;
	public int getJiaodu() {
		return jiaodu;
	}
	public void setJiaodu(int jiaodu) {
		this.jiaodu = jiaodu;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public String getJsonData() {
		return jsonData;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
}
