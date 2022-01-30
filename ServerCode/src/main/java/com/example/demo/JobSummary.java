package com.example.demo;

public class JobSummary {
	private double minObjScore, avgObjScore, maxObjScore;
	private double minPopScore, avgPopScore, maxPopScore;
	private double minDevAvgScore, avgDevAvgScore, maxDevAvgScore;
	private double minDevEnaScore, avgDevEnaScore, maxDevEnaScore;
	private double minComScore, avgComScore, maxComScore;
	public void setObjScores(double min, double avg, double max) {
		minObjScore=min;
		avgObjScore=avg;
		maxObjScore=max;
	}
	public void setPopScores(double min, double avg, double max) {
		minPopScore=min;
		avgPopScore=avg;
		maxPopScore=max;
	}
	public void setDevAvgScores(double min, double avg, double max) {
		minDevAvgScore=min;
		avgDevAvgScore=avg;
		maxDevAvgScore=max;
	}
	public void setDevEnaScores(double min, double avg, double max) {
		minDevEnaScore=min;
		avgDevEnaScore=avg;
		maxDevEnaScore=max;
	}
	public void setComScores(double min, double avg, double max) {
		minComScore=min;
		avgComScore=avg;
		maxComScore=max;
	}
	public String toString() {
		String res="JobSummary: "+minObjScore+", "+avgObjScore+", "+maxObjScore+" | "+minPopScore+", "+avgPopScore+", "+maxPopScore+" | "+minDevAvgScore+", "+avgDevAvgScore+", "+maxDevAvgScore+" | "
				+minDevEnaScore+", "+avgDevEnaScore+", "+maxDevEnaScore+" | "+minComScore+", "+avgComScore+", "+maxComScore;
		return res;
	}
}
