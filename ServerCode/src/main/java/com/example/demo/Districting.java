package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpSession;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Districting {
	private transient HttpSession session;
	private int districtingIndex;
	private District[] districts;
	private int[] gill;
	private int minPop, maxPop, TOTPOP;
	private int minVAP, maxVAP, VAP;
	private int numMajMinDistricts;
	private String[][] conflictingIncumbents;
	private double compactness, compactnessScore, popScore, popScoreRaw, deviationAvg, deviationEnacted, deviationEnactedArea, deviationEnactedPop, objFunctionScore;
	private transient boolean init=false;
	
	public void setSession(HttpSession session) {
		this.session=session;
		for(District d:districts)
			d.setSession(session);
	}
	
	public void init() {
		if(!init) {
			for(District d:districts)
				d.init();
			calcPops();
			calcConflictingIncumbents();
			//calcCompactness();
			calcPopulationScore();
			init=true;
		}
	}
	
	public int getNumMajMinDistricts(double threshold, Minority minority) {
		int res=0;
		for(District d:districts) {
			if(d.isMajMinDistrict(threshold, minority))
				res++;
		}
		return res;
	}
	
	public double getPopDiff(int popType) {
		switch(popType) {
			case 1:
				return (double) (maxVAP-minVAP)/minVAP;
			default:
				return (double) (maxPop-minPop)/minPop;
		}
	}
	
	//Returns array of the specified minority count in ascending order
	public double[] rankMinority(Minority m) {
		double[] res=new double[districts.length];
		for(int i=0;i<districts.length;i++)
			res[i]=districts[i].getMinority(m);
		Arrays.sort(res);
		return res;
	}
	
	private void calcPops() {
		minPop=-1;
		minVAP=-1;
		maxPop=-1;
		maxVAP=-1;
		TOTPOP=0;
		VAP=0;
		for(District d:districts) {
			int pop=d.getTOTPOP();
			TOTPOP+=pop;
			if(pop>maxPop)
				maxPop=pop;
			if(pop<minPop || minPop==-1)
				minPop=pop;
			pop=d.getVAP();
			VAP+=pop;
			if(pop>maxVAP)
				maxVAP=pop;
			if(pop<minVAP || minVAP==-1)
				minVAP=pop;
		}
	}
	
	//If two(or more) incumbents are in the same district, they are considered conflicting
	private void calcConflictingIncumbents() {
		ArrayList<String[]> tmp=new ArrayList<>();
		for(District d:districts) {
			String[] incumbents=d.getIncumbents();
			if(incumbents!=null)
				tmp.add(incumbents);
		}
		if(tmp.size()>1) {
			//printAList(tmp);
			conflictingIncumbents = new String[tmp.size()][];
			for(int i=0;i<conflictingIncumbents.length;i++)
				conflictingIncumbents[i]=tmp.get(i);
		}
		else conflictingIncumbents=null;
	}
	
	private void calcPopulationScore() {
		double sum=0;
		double popIdeal=(double) TOTPOP/districts.length;
		for(District d:districts) {
			sum+=Math.pow((((double) d.getTOTPOP()/popIdeal)-1), 2);
		}
		popScore=Math.sqrt(sum);
		popScoreRaw=popScore;
	}
	
	public CalculationDetails showPopScore() {
		ArrayList<String> rows=new ArrayList<>();
		ArrayList<Double> data=new ArrayList<>();
		double sum=0;
		double popIdeal=(double) TOTPOP/districts.length;
		for(District d:districts) {
			sum+=Math.pow((((double) d.getTOTPOP()/popIdeal)-1), 2);
		}
		rows.add("Ideal Population");
		data.add(popIdeal);
		for(District d:districts) {
			rows.add("Population (D"+d.getDistrictNumber()+")");
			data.add((double) d.getTOTPOP());
			rows.add("Deviation from ideal (Abs.)");
			data.add(Math.abs((double) d.getTOTPOP()-popIdeal));
			rows.add("Deviation from ideal (%)");
			data.add((((double) d.getTOTPOP()-popIdeal)/popIdeal)*100);
		}
		rows.add("Population Score (raw)");
		data.add(Math.sqrt(sum));
		CalculationDetails calcs=new CalculationDetails();
		calcs.setRows(rows);
		calcs.setData(data);
		return calcs;
	}
	
	public void calcDeviationFromAverageDistricting(AverageDistricting avg, Minority minority) {
		double sumOfSquares=0;
		double[] arr1=rankMinority(minority);
		double[] arr2=avg.getMinority(minority);
		//System.out.println("DEBUG: "+Arrays.toString(arr1)+"\n "+Arrays.toString(arr2));
		for(int i=0;i<arr1.length;i++) {
			sumOfSquares+=Math.pow((arr1[i]-arr2[i]), 2);
		}
		deviationAvg=sumOfSquares;
	}
	
	public CalculationDetails showDevAvgScore(AverageDistricting avg, Minority minority) {
		ArrayList<String> rows=new ArrayList<>();
		ArrayList<Double> data=new ArrayList<>();
		double sumOfSquares=0;
		double[] arr1=rankMinority(minority);
		double[] arr2=avg.getMinority(minority);
		for(int i=0;i<arr1.length;i++) {
			rows.add("Minority Count [D"+(i+1)+"]");
			data.add(arr1[i]);
			rows.add("Minority Count (Average) [D"+(i+1)+"]");
			data.add(arr2[i]);
			sumOfSquares+=Math.pow((arr1[i]-arr2[i]), 2);
		}
		rows.add("Sum of Squares");
		data.add(sumOfSquares);
		CalculationDetails calcs=new CalculationDetails();
		calcs.setRows(rows);
		calcs.setData(data);
		return calcs;
	}
	
	public void calcDeviationFromEnactedDistricting(EnactedDistricting enacted) {
		//gillConstruct(enacted);
		double sumOfSquaresArea=0;
		double sumOfSquaresPop=0;
		Feature[] enactedDistricts=enacted.getFeatures();
		for(int i=0;i<enactedDistricts.length;i++) {
			sumOfSquaresArea+=Math.pow(districts[gill[i]-1].getArea()-enactedDistricts[i].getArea(), 2);
			sumOfSquaresPop+=Math.pow(districts[gill[i]-1].getTOTPOP()-enactedDistricts[i].getTOTPOP(), 2);
		}
		deviationEnactedArea=sumOfSquaresArea;
		deviationEnactedPop=sumOfSquaresPop;
	}
	
	public DeviationEnactedDetails showDevEnaScore(EnactedDistricting enacted) {
		ArrayList<String> headers=new ArrayList<>();
		headers.add("District Number");
		headers.add("Area Difference");
		headers.add("Area Difference (%)");
		headers.add("Population Difference");
		headers.add("Population Difference (%)");
		ArrayList<ArrayList<Double>> data=new ArrayList<>();
		ArrayList<String> rows = new ArrayList<>();
		ArrayList<Double> data2 = new ArrayList<>();
		double sumOfSquaresArea=0;
		double sumOfSquaresPop=0;
		Feature[] enactedDistricts=enacted.getFeatures();
		for(int i=0;i<enactedDistricts.length;i++) {
			/*rows.add("Area[D"+(i+1)+"]");
			data.add(districts[gill[i]-1].getArea());
			rows.add("Area(Enacted)[D"+(i+1)+"]");
			data.add(enactedDistricts[i].getArea());
			sumOfSquaresArea+=Math.pow(districts[gill[i]-1].getArea()-enactedDistricts[i].getArea(), 2);
			rows.add("Pop[D"+(i+1)+"]");
			data.add((double) districts[gill[i]-1].getTOTPOP());
			rows.add("Pop(Enacted)[D"+(i+1)+"]");
			data.add((double) enactedDistricts[i].getTOTPOP());
			sumOfSquaresPop+=Math.pow(districts[gill[i]-1].getTOTPOP()-enactedDistricts[i].getTOTPOP(), 2);*/
			ArrayList<Double> tmp=new ArrayList<>();
			tmp.add((double) (i+1));
			double areaDiff=districts[gill[i]-1].getArea()-enactedDistricts[i].getArea();
			tmp.add(areaDiff);
			areaDiff/=enactedDistricts[i].getArea();
			areaDiff*=100;
			tmp.add(areaDiff);
			double popDiff=(double) districts[gill[i]-1].getTOTPOP()-enactedDistricts[i].getTOTPOP();
			tmp.add(popDiff);
			popDiff/=enactedDistricts[i].getTOTPOP();
			popDiff*=100;
			tmp.add(popDiff);
			sumOfSquaresArea+=Math.pow(districts[gill[i]-1].getArea()-enactedDistricts[i].getArea(), 2);
			sumOfSquaresPop+=Math.pow(districts[gill[i]-1].getTOTPOP()-enactedDistricts[i].getTOTPOP(), 2);
			data.add(tmp);
		}
		rows.add("Sum of Squares (Area)");
		data2.add(sumOfSquaresArea);
		rows.add("Sum of Squares (Pop)");
		data2.add(sumOfSquaresPop);
		CalculationDetails calcs=new CalculationDetails();
		calcs.setRows(rows);
		calcs.setData(data2);
		DeviationEnactedDetails res=new DeviationEnactedDetails();
		res.setHeaders(headers);
		res.setData(data);
		res.setDetails(calcs);
		return res;
	}
	
	//After receiving min and max of respective categories, scale scores so they fall within [0,1]
	public void scale(double minPopScore, double maxPopScore, double minDeviationAverage, double maxDeviationAverage, double minDeviationEnactedArea, double maxDeviationEnactedArea, double minDeviationEnactedPop, 
			double maxDeviationEnactedPop, double minCompactness, double maxCompactness) {
		popScore=popScoreRaw;
		popScore-=minPopScore;
		popScore/=maxPopScore;
		if(maxDeviationAverage!=0) {
			deviationAvg-=minDeviationAverage;
			deviationAvg/=maxDeviationAverage;
			deviationAvg=(1-deviationAvg);
		}
		else { deviationAvg=1; }
		deviationEnactedArea-=minDeviationEnactedArea;
		deviationEnactedArea/=maxDeviationEnactedArea;
		deviationEnactedPop-=minDeviationEnactedPop;
		deviationEnactedPop/=maxDeviationEnactedPop;
		compactnessScore=compactness;
		compactnessScore-=minCompactness;
		compactnessScore/=maxCompactness;
		deviationEnacted=(deviationEnactedArea+deviationEnactedPop)/2;
	}
	
	public void calcObjectiveFunction(double[] weights) {
		objFunctionScore=(weights[0]*popScore)+(weights[2]*deviationAvg)+(weights[3]*deviationEnacted)+(weights[4]*compactnessScore);
	}
	
	//Renumber districts according to overlapping area
	public int[] gillConstruct(EnactedDistricting enacted) {
		District[] temp=new District[districts.length];
		ArrayList<District> pool=new ArrayList<>();
		for(District d:districts) pool.add(d);
		Feature[] enactedDistricts = enacted.getFeatures();
		for(int i=0;i<enactedDistricts.length;i++) {
			int bestOverlappingIndex=-1;
			double bestOverlappingArea=-1;
			for(int j=0;j<pool.size();j++) {
				District d=pool.get(j);
				double overlap = enactedDistricts[i].getGeometry().intersection(d.approxGeometry()).getArea();
				if(overlap>bestOverlappingArea) {
					bestOverlappingIndex=j;
					bestOverlappingArea=overlap;
				}
			}
			temp[i]=pool.remove(bestOverlappingIndex);
		}
		//districts=temp;
		int[] res=new int[temp.length];
		for(int i=0;i<res.length;i++)
			res[i]=temp[i].getDistrictNumber();
		return res;
	}
	
	public int getMinorityCount(Minority m) {
		int res=0;
		for(District d:districts)
			res+=d.getMinority(m);
		return res;
	}
	
	//Averages compactness of districts within
	private void calcCompactness() {
		compactness=0;
		for(District d:districts)
			compactness+=d.getCompactness();
		compactness/=districts.length;
	}
	
	public CalculationDetails showCompactness() {
		ArrayList<String> rows=new ArrayList<>();
		ArrayList<Double> data=new ArrayList<>();
		for(District d:districts) {
			rows.add("Area [D"+d.getDistrictNumber()+"]");
			data.add(d.getArea());
			rows.add("Perimeter [D"+d.getDistrictNumber()+"]");
			data.add(d.approxGeometry().getLength());
			rows.add("Compactness [D"+d.getDistrictNumber()+"]");
			data.add(d.estPolsby());
		}
		rows.add("Districting Average Compactness");
		data.add(compactness);
		CalculationDetails calcs=new CalculationDetails();
		calcs.setRows(rows);
		calcs.setData(data);
		return calcs;
	}
	
	public void addBoxAndWhiskerData(Minority m, ArrayList<ArrayList<Double>> data) {
		Double[] tmp=getBoxAndWhiskerArray(m);
		for(int i=0;i<tmp.length;i++) {
			ArrayList<Double> list=data.get(i);
			list.add(tmp[i]);
		}
	}
	
	public Double[] getBoxAndWhiskerArray(Minority m) {
		ArrayList<Double> tmp=new ArrayList<>();
		for(District d:districts)
			tmp.add((double) d.getMinority(m)/d.getTOTPOP());
		Double[] res=new Double[tmp.size()];
		for(int i=0;i<res.length;i++)
			res[i]=tmp.get(i);
		Arrays.sort(res);
		return res;
	}
	
	public DeviationEnactedDetails getMinorityDetail(Minority m) {
		ArrayList<String> headers=new ArrayList<>();
		headers.add("District Number");
		headers.add("Total Population");
		headers.add("Minority Population");
		headers.add("Minority Percentage (%)");
		ArrayList<ArrayList<Double>> data=new ArrayList<>();
		for(District d:districts) {
			ArrayList<Double> tmp=new ArrayList<>();
			tmp.add((double) d.getDistrictNumber());
			tmp.add((double) d.getTOTPOP());
			tmp.add((double) d.getMinority(m));
			tmp.add((double) d.getMinority(m)/d.getTOTPOP()*100);
			data.add(tmp);
		}
		//TODO: Sort
		Collections.sort(data, new MinorityComparator());
		DeviationEnactedDetails res=new DeviationEnactedDetails();
		res.setHeaders(headers);
		res.setData(data);
		return res;
	}
	
	public double getPopScore() { return popScore; }
	public double getPopScoreRaw() { return popScoreRaw; }
	public int getNumDistricts() { return districts.length; }
	public void setCompactness(double c) { compactness=c; }
	public void setGill(int[] i) { gill=i; }
	public double getCompactness() { return compactness; }
	public int getTOTPOP() { return TOTPOP; }
	public double getDeviationAverage() { return deviationAvg; }
	public double getDeviationEnacted() { return deviationEnacted; }
	public double getDeviationEnactedArea() { return deviationEnactedArea; }
	public double getDeviationEnactedPop() { return deviationEnactedPop; }
	public String[][] getConflictingIncumbents(){ return conflictingIncumbents; }
	public double getCompactnessScore() { return compactnessScore; }
	public double getObjScore() { return objFunctionScore; }
	public void setIndex(int i) { districtingIndex=i; }
	public int getIndex() { return districtingIndex; }
	public int getVAP() { return VAP; }
	public void setMajMinDistricts(int i) { numMajMinDistricts=i; }
	public int getMajMinDistricts() { return numMajMinDistricts; }
	@JsonIgnore
	public District[] getDistricts() { return districts; }
	
	public String toString() {
		return "Districting: "+compactness+", "+popScore+", "+deviationAvg+", "+deviationEnacted+", obj: "+objFunctionScore;
	}
	
	public class MinorityComparator implements Comparator<ArrayList<Double>>{
		public int compare(ArrayList<Double> a, ArrayList<Double> b) {
			return (int) Math.round((double) b.get(3)-(double) a.get(3));
		}
	}
	
	
	
	//TEMP
	/*
	public double getPolsby() {
		/*Geometry[] arr=new Geometry[districts.length];
		for(int i=0;i<arr.length;i++) {
			districts[i].calcGeometry();
			arr[i]=districts[i].getGeometry();
		}
		GeometryFactory gf=new GeometryFactory();
		Geometry test=gf.createGeometryCollection(arr);
		test=test.union();
		double perimeter = test.getLength();
		double area = test.getArea();
		System.out.println("DEBUG: "+area+", "+perimeter);
		return 4*Math.PI*(area/Math.pow(perimeter, 2));
		double res=0;
		for(District d:districts) 
			res+=d.getPolsby();
		return res/districts.length;
	}
	public double estPolsby() {
		/*Geometry[] arr=new Geometry[districts.length];
		for(int i=0;i<arr.length;i++)
			arr[i]=districts[i].approxGeometry();
		GeometryFactory gf=new GeometryFactory();
		Geometry test=gf.createGeometryCollection(arr);
		test=test.union();
		/*Geometry test=districts[0].approxGeometry();
		for(int i=0;i<districts.length;i++)
			test=test.union(districts[i].approxGeometry());
		double perimeter = test.getLength();
		double area = test.getArea();
		System.out.println("DEBUG: "+area+", "+perimeter);
		return 4*Math.PI*(area/Math.pow(perimeter, 2));
		double res=0;
		for(District d:districts) 
			res+=d.estPolsby();
		return res/districts.length;
	}
	private void printAList(ArrayList<String[]> list) {
		for(String[] arr:list) {
			System.out.print("[");
			for(String s:arr)
				System.out.print(s+", ");
			System.out.print("], ");
		}
		System.out.println();
	}*/
}
