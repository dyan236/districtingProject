package com.example.demo;

import java.util.ArrayList;

public class CalculationDetails {
	private ArrayList<String> rows;
	private ArrayList<Double> data;
	public ArrayList<String> getRows() {
		return rows;
	}
	public void setRows(ArrayList<String> rows) {
		this.rows = rows;
	}
	public ArrayList<Double> getData() {
		return data;
	}
	public void setData(ArrayList<Double> data) {
		this.data = data;
	}
	public void addMinMax(Double min, Double max) {
		rows.add("Min(raw)");
		data.add(min);
		rows.add("Max(raw)");
		data.add(max);
	}
	public void addMinMaxArea(Double min, Double max) {
		rows.add("Min Area(raw)");
		data.add(min);
		rows.add("Max Area(raw)");
		data.add(max);
	}
	public void addMinMaxPop(Double min, Double max) {
		rows.add("Min Pop(raw)");
		data.add(min);
		rows.add("Max Pop(raw)");
		data.add(max);
	}
	public void addFinal(Double score) {
		rows.add("Adjusted(Final)");
		data.add(score);
	}
	public void addFinalArea(Double score) {
		rows.add("Adjusted(Area Score)");
		data.add(score);
	}
	public void addFinalPop(Double score) {
		rows.add("Adjusted(Pop Score)");
		data.add(score);
	}
}
