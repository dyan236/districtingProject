package com.example.demo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FilteredJob {
	private ArrayList<Districting> plans;
	private String[] protectedIncumbents;
	private AverageDistricting avgDistricting;
	private EnactedDistricting enacted;
	private int majMinDistricts;
	private Minority minority;
	private double MMThresh;
	private double compactness;
	private int popType;
	private double popThresh;
	private double[] popScoreRecord, deviationAvgRecord, deviationEnaAreaRecord, deviationEnaPopRecord, compactnessRecord;
	
	public FilteredJob(String[] incumbents, int majMinDistricts, Minority minority, double MMThresh, double compactness, int popType, double popThresh) {
		plans=new ArrayList<>();
		protectedIncumbents=incumbents;
		this.majMinDistricts=majMinDistricts;
		this.minority=minority;
		this.MMThresh=MMThresh;
		this.compactness=compactness;
		this.popType=popType;
		this.popThresh=popThresh;
	}
	
	//Apply constraints(Assigned by constructor)
	public void apply() {
		for(int i=0;i<plans.size();i++) {
			Districting d=plans.get(i);
			if(containsIncumbents(d))
				plans.remove(i--);
			else if(d.getNumMajMinDistricts(MMThresh, minority)<majMinDistricts) 
				plans.remove(i--);
			else if(d.getCompactness()<compactness) 
				plans.remove(i--);
			else if(d.getPopDiff(popType)>popThresh) 
				plans.remove(i--);
		}
		calcAverageDistricting();
		calcDeviationFromAverageDistricting();
		calcDeviationFromEnactedDistricting();
		popScoreRecord=new double[2];
		deviationAvgRecord=new double[2];
		deviationEnaAreaRecord=new double[2];
		deviationEnaPopRecord=new double[2];
		compactnessRecord=new double[2];
		scale();
	}
	
	//Find min and max of respective scores, then pass those to the plans
	public void scale() {
		double minPopScore=Double.MAX_VALUE, maxPopScore=-1;
		double minDeviationAverage=Double.MAX_VALUE, maxDeviationAverage=-1;
		double minDeviationEnactedArea=Double.MAX_VALUE, maxDeviationEnactedArea=-1;
		double minDeviationEnactedPop=Double.MAX_VALUE, maxDeviationEnactedPop=-1;
		double minCompactness=Double.MAX_VALUE, maxCompactness=-1;
		for(Districting p:plans) {
			if(p.getPopScoreRaw()<minPopScore)
				minPopScore=p.getPopScoreRaw();
			if(p.getPopScoreRaw()>maxPopScore)
				maxPopScore=p.getPopScoreRaw();
			if(p.getDeviationAverage()<minDeviationAverage)
				minDeviationAverage=p.getDeviationAverage();
			if(p.getDeviationAverage()>maxDeviationAverage)
				maxDeviationAverage=p.getDeviationAverage();
			if(p.getDeviationEnactedArea()<minDeviationEnactedArea)
				minDeviationEnactedArea=p.getDeviationEnactedArea();
			if(p.getDeviationEnactedArea()>maxDeviationEnactedArea)
				maxDeviationEnactedArea=p.getDeviationEnactedArea();
			if(p.getDeviationEnactedPop()<minDeviationEnactedPop)
				minDeviationEnactedPop=p.getDeviationEnactedPop();
			if(p.getDeviationEnactedPop()>maxDeviationEnactedPop)
				maxDeviationEnactedPop=p.getDeviationEnactedPop();
			if(p.getCompactness()<minCompactness)
				minCompactness=p.getCompactness();
			if(p.getCompactness()>maxCompactness)
				maxCompactness=p.getCompactness();
		}
		popScoreRecord[0]=minPopScore;
		popScoreRecord[1]=maxPopScore;
		maxPopScore-=minPopScore;
		deviationAvgRecord[0]=minDeviationAverage;
		deviationAvgRecord[1]=maxDeviationAverage;
		maxDeviationAverage-=minDeviationAverage;
		deviationEnaAreaRecord[0]=minDeviationEnactedArea;
		deviationEnaAreaRecord[1]=maxDeviationEnactedArea;
		maxDeviationEnactedArea-=minDeviationEnactedArea;
		deviationEnaPopRecord[0]=minDeviationEnactedPop;
		deviationEnaPopRecord[1]=maxDeviationEnactedPop;
		maxDeviationEnactedPop-=minDeviationEnactedPop;
		compactnessRecord[0]=minCompactness;
		compactnessRecord[1]=maxCompactness;
		maxCompactness-=minCompactness;
		for(Districting d:plans)
			d.scale(minPopScore, maxPopScore, minDeviationAverage, maxDeviationAverage, minDeviationEnactedArea, maxDeviationEnactedArea, minDeviationEnactedPop, maxDeviationEnactedPop, minCompactness, maxCompactness);
	}
	
	public void applyWeights(double[] weights) {
		for(Districting d:plans)
			d.calcObjectiveFunction(weights);
	}
	
	public JobSummary generateSummary() {
		JobSummary res=new JobSummary();
		double minObjScore=Double.MAX_VALUE, maxObjScore=-1, avgObjScore=0;
		double minPopScore=Double.MAX_VALUE, maxPopScore=-1, avgPopScore=0;
		double minDeviationAverage=Double.MAX_VALUE, maxDeviationAverage=-1, avgDeviationAverage=0;
		double minDeviationEnacted=Double.MAX_VALUE, maxDeviationEnacted=-1, avgDeviationEnacted=0;
		double minCompactness=Double.MAX_VALUE, maxCompactness=-1, avgCompactness=0;
		
		for(Districting p:plans) {
			avgPopScore+=p.getPopScore();
			if(p.getPopScore()<minPopScore)
				minPopScore=p.getPopScore();
			if(p.getPopScore()>maxPopScore)
				maxPopScore=p.getPopScore();
			
			avgDeviationAverage+=p.getDeviationAverage();
			if(p.getDeviationAverage()<minDeviationAverage)
				minDeviationAverage=p.getDeviationAverage();
			if(p.getDeviationAverage()>maxDeviationAverage)
				maxDeviationAverage=p.getDeviationAverage();
			
			avgDeviationEnacted+=p.getDeviationEnacted();
			if(p.getDeviationEnacted()<minDeviationEnacted)
				minDeviationEnacted=p.getDeviationEnacted();
			if(p.getDeviationEnacted()>maxDeviationEnacted)
				maxDeviationEnacted=p.getDeviationEnacted();
			
			avgObjScore+=p.getObjScore();
			if(p.getObjScore()<minObjScore)
				minObjScore=p.getObjScore();
			if(p.getObjScore()>maxObjScore)
				maxObjScore=p.getObjScore();
			
			avgCompactness+=p.getCompactnessScore();
			if(p.getCompactnessScore()<minCompactness)
				minCompactness=p.getCompactnessScore();
			if(p.getCompactnessScore()>maxCompactness)
				maxCompactness=p.getCompactnessScore();
		}
		avgObjScore/=plans.size();
		avgPopScore/=plans.size();
		avgDeviationAverage/=plans.size();
		avgDeviationEnacted/=plans.size();
		avgCompactness/=plans.size();
		
		res.setObjScores(minObjScore, avgObjScore, maxObjScore);
		res.setPopScores(minPopScore, avgPopScore, maxPopScore);
		res.setDevAvgScores(minDeviationAverage, avgDeviationAverage, maxDeviationAverage);
		res.setDevEnaScores(minDeviationEnacted, avgDeviationEnacted, maxDeviationEnacted);
		res.setComScores(minCompactness, avgCompactness, maxCompactness);
		return res;
	}
	
	//Check if any protected incumbents appear in a given districting
	private boolean containsIncumbents(Districting d) {
		if(protectedIncumbents.length>1 && d.getConflictingIncumbents()!=null) {
			for(String[] conflicts:d.getConflictingIncumbents()) {
				int occurrences=0;
				for(String incumbent:protectedIncumbents) {
					if(Arrays.asList(conflicts).contains(incumbent))
						occurrences++;
					if(occurrences>1)
						return true;
				}
			}
		}
		return false;
	}
	
	//Helper method for calcAverageDistricting; Adds two arrays according to index
	private double[] addArrays(double[] a, double[] b) {
		double[] sum=new double[a.length];
		for(int i=0;i<a.length;i++) 
			sum[i]=a[i]+b[i];
		return sum;
	}
	
	//Helper method for calcAverageDistricting; Divides all elements by an int
	private double[] divideArray(double[] a, int b) {
		double[] res=new double[a.length];
		for(int i=0;i<a.length;i++)
			res[i]=a[i]/b;
		return res;
	}
	
	private void calcAverageDistricting() {
		if(plans.size()>1) {
			double[] HISP=plans.get(0).rankMinority(Minority.HISP);
			double[] WHITE=plans.get(0).rankMinority(Minority.WHITE);
			double[] BLACK=plans.get(0).rankMinority(Minority.BLACK);
			double[] AMIN=plans.get(0).rankMinority(Minority.AMIN);
			double[] ASIAN=plans.get(0).rankMinority(Minority.ASIAN);
			double[] NHPI=plans.get(0).rankMinority(Minority.NHPI);
			double[] OTHER=plans.get(0).rankMinority(Minority.OTHER);
			for(int i=1;i<plans.size();i++) {
				HISP=addArrays(HISP, plans.get(i).rankMinority(Minority.HISP));
				WHITE=addArrays(WHITE, plans.get(i).rankMinority(Minority.WHITE));
				BLACK=addArrays(BLACK, plans.get(i).rankMinority(Minority.BLACK));
				AMIN=addArrays(AMIN, plans.get(i).rankMinority(Minority.AMIN));
				ASIAN=addArrays(ASIAN, plans.get(i).rankMinority(Minority.ASIAN));
				NHPI=addArrays(NHPI, plans.get(i).rankMinority(Minority.NHPI));
				OTHER=addArrays(OTHER, plans.get(i).rankMinority(Minority.OTHER));
			}
			HISP=divideArray(HISP, plans.size());
			WHITE=divideArray(WHITE, plans.size());
			BLACK=divideArray(BLACK, plans.size());
			AMIN=divideArray(AMIN, plans.size());
			ASIAN=divideArray(ASIAN, plans.size());
			NHPI=divideArray(NHPI, plans.size());
			OTHER=divideArray(OTHER, plans.size());
			avgDistricting=new AverageDistricting(HISP, WHITE, BLACK, AMIN, ASIAN, NHPI, OTHER);
		}
	}
	
	public ArrayList<Districting> getTopTen(){
		ArrayList<Districting> res=new ArrayList<>();
		Collections.sort(plans, new objectiveSort());
		int limit=plans.size();
		if(limit>10) limit=10;
		for(int i=0;i<limit;i++)
			res.add(plans.get(i));
		return res;
	}
	
	public double[] getRecord(int i) {
		switch(i) {
			case 1: return deviationAvgRecord;
			case 2: return deviationEnaAreaRecord;
			case 3: return deviationEnaPopRecord;
			case 4: return compactnessRecord;
			default: return popScoreRecord;
		}
	}
	
	public BoxAndWhiskerData getBoxAndWhiskerData(Minority m) {
		BoxAndWhiskerData res=new BoxAndWhiskerData();
		ArrayList<ArrayList<Double>> data=new ArrayList<>();;
		for(int i=0;i<enacted.getNumDistricts();i++)
			data.add(new ArrayList<Double>());
		for(Districting d:plans)
			d.addBoxAndWhiskerData(m, data);
		res.setData(data);
		res.setEnacted(enacted.getBoxAndWhiskerArray(m));
		return res;
	}
	
	private void calcDeviationFromEnactedDistricting() {
		for(Districting d:plans)
			d.calcDeviationFromEnactedDistricting(enacted);	
	}
	
	private void calcDeviationFromAverageDistricting() {
		for(Districting d:plans) {
			d.calcDeviationFromAverageDistricting(avgDistricting, minority);
		}
	}
	
	public AverageDistricting getAverageDistricting() { return avgDistricting; }
	public EnactedDistricting getEnactedDistricting() { return enacted; }
	public ArrayList<Districting> getPlans() { return plans; }
	public void setPlans(ArrayList<Districting> plans) { this.plans = plans; }
	public void setEnacted(EnactedDistricting enacted) { this.enacted=enacted; }
	public int size() { return plans.size(); }
	public Minority getMinority() { return minority; }
	public double getMMThresh() { return MMThresh; }
	
	public String toString() {
		String res = "FilteredJob: "+plans.size()+", [";
		for(String s:protectedIncumbents)
			res+=s+", ";
		res+="], "+majMinDistricts+", "+minority+", "+MMThresh+", "+compactness+", "+popType+", "+popThresh;
		return res;
	}
	
	public class objectiveSort implements Comparator<Districting>{
		public int compare(Districting a, Districting b) {
			return new Double(b.getObjScore()).compareTo(new Double(a.getObjScore()));
		}
	}
}
