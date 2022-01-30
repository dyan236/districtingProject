package com.example.demo;

import java.util.ArrayList;

public class DeviationEnactedDetails {
	private ArrayList<String> headers;
	private ArrayList<ArrayList<Double>> data;
	private CalculationDetails details;
	public ArrayList<String> getHeaders() {
		return headers;
	}
	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}
	public ArrayList<ArrayList<Double>> getData() {
		return data;
	}
	public void setData(ArrayList<ArrayList<Double>> data) {
		this.data = data;
	}
	public CalculationDetails getDetails() {
		return details;
	}
	public void setDetails(CalculationDetails details) {
		this.details = details;
	}
}
