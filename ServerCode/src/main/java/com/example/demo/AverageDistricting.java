package com.example.demo;
import java.util.Arrays;

public class AverageDistricting {
	private double[] HISP;
	private double[] WHITE;
	private double[] BLACK;
	private double[] AMIN;
	private double[] ASIAN;
	private double[] NHPI;
	private double[] OTHER;
	
	public AverageDistricting(double[] HISP, double[] WHITE, double[] BLACK, double[] AMIN, double[] ASIAN, double[] NHPI, double[] OTHER) {
		this.HISP=HISP;
		this.WHITE=WHITE;
		this.BLACK=BLACK;
		this.AMIN=AMIN;
		this.ASIAN=ASIAN;
		this.NHPI=NHPI;
		this.OTHER=OTHER;
	}
	
	public double[] getMinority(Minority m) {
		switch(m) {
			case HISP: return HISP;
			case WHITE: return WHITE;
			case BLACK: return BLACK;
			case AMIN: return AMIN;
			case ASIAN: return ASIAN;
			case NHPI: return NHPI;
			case OTHER: return OTHER;
			default: return null;
		}
	}
	
	public String toString() {
		return "AverageDistricting: "+Arrays.toString(HISP)+",\n "+Arrays.toString(WHITE)+",\n "+Arrays.toString(BLACK)+",\n "+Arrays.toString(AMIN)+",\n "+Arrays.toString(ASIAN)+",\n "+Arrays.toString(NHPI)+",\n "
				+Arrays.toString(OTHER);
	}
}
