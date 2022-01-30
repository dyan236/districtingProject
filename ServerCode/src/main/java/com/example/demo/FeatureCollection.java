package com.example.demo;
//Class for gson to use during json conversion
public class FeatureCollection {
	private String type;
	private Crs crs;
	private Precinct[] features;
	public void init() {
		for(Precinct p:features)
			p.init();
	}
	public Precinct getPrecinct(int i) {
		return features[i];
	}
	public String toString() {
		//return type+" "+features.length+"\n(0)"+features[0].toString()+"\n(17)"+features[17].toString();
		return type+" "+features.length;
	}
	
	
	
	//TEST
	public Precinct[] getFeatures() { return features; }
	
	
	
	//Java class for holding "crs" properties
	public class Crs{
		String name;
		CrsProperties properties;
		public class CrsProperties{
			String name;
		}
	}
}
