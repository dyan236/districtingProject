package com.example.demo;

import java.util.ArrayList;

public class BoxAndWhiskerData {
	private ArrayList<ArrayList<Double>> data;
	private Double[] enacted;
	private Double[] current;
	public ArrayList<ArrayList<Double>> getData() {
		return data;
	}
	public void setData(ArrayList<ArrayList<Double>> data) {
		this.data = data;
	}
	public Double[] getEnacted() {
		return enacted;
	}
	public void setEnacted(Double[] enacted) {
		this.enacted = enacted;
	}
	public Double[] getCurrent() {
		return current;
	}
	public void setCurrent(Double[] current) {
		this.current = current;
	}
}
	