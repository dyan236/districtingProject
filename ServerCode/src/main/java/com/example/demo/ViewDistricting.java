package com.example.demo;

public class ViewDistricting {
	private String type="FeatureCollection";
	private Feature[] features;
	public void setFeatures(Feature[] f) {
		this.features=f;
	}
	public String getType() { return type; }
	public Feature[] getFeatures() { return features; }
}
