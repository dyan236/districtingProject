package com.example.demo;

import com.example.demo.DB.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class Job {
	private transient HttpSession session;
	private transient DistrictingPrecomputedRepo precomputeRepo;
	private transient FeatureCollection fCollection;
	private transient EnactedDistricting enacted;
	private Districting[] plans;
	
	public void setSession(HttpSession session) {
		this.session = session;
		for(Districting d:plans)
			d.setSession(session);
		setFeatureCollection();
		setEnacted();
		loadPrecompute();
		index();
	}
	private void setFeatureCollection() {
		fCollection=(FeatureCollection) session.getAttribute("FeatureCollection");
	}
	private void setEnacted() {
		enacted=(EnactedDistricting) session.getAttribute("enacted");
	}
	private void loadPrecompute() {
		precomputeRepo=(DistrictingPrecomputedRepo) session.getAttribute("precomputeRepo");
		List<DistrictingPrecomputed> data=precomputeRepo.retrieveForJob((String) session.getAttribute("JobID"));
		for(int i=0;i<data.size();i++) {
			DistrictingPrecomputed entry=data.get(i);
			plans[entry.getIndex()].setCompactness(entry.getCompactness());
			plans[entry.getIndex()].setGill(entry.getGill());
		}
	}
	private void index() {
		for(int i=0;i<plans.length;i++)
			plans[i].setIndex(i);
	}
	
	public void init() {
		fCollection.init();
		//int count=0;
		for(Districting d:plans) {
			d.init();
			//System.out.println("DEBUG: Working("+count+++") popScore="+d.getPopScore());
		}
	}
	
	public FilteredJob applyConstraints(String[] incumbents, int majMinDistricts, Minority minority, double MMThresh, double compactness, int popType, double popThresh) {
		FilteredJob fj=new FilteredJob(incumbents, majMinDistricts, minority, MMThresh, compactness, popType, popThresh);
		for(Districting d:plans)
			fj.getPlans().add(d);
		fj.setEnacted(enacted);
		fj.apply();
		return fj;
	}
	
	//TODO
	public ViewDistricting constructGeometry(int districtingIndex) {
		ViewDistricting res=new ViewDistricting();
		Districting toConvert=plans[districtingIndex];
		ArrayList<Feature> tmp=new ArrayList<>();
		
		for(District d:toConvert.getDistricts()) {
			Feature f=new Feature();
			f.setType("Feature");
			f.setProperties(d.getIncumbentsProperties(), d.getTOTPOP(), d.getMinority(Minority.HISP), d.getMinority(Minority.WHITE), d.getMinority(Minority.BLACK), d.getMinority(Minority.AMIN), d.getMinority(Minority.ASIAN), 
					d.getMinority(Minority.NHPI), d.getMinority(Minority.OTHER), d.getVAP(), d.getDistrictNumber());
			f.getProperties().setArea(d.getArea());
			f.getProperties().setCompactness(d.getCompactness());
			Geometry jtsGeometry=d.getGeometry();
			Coordinate[] jtsCoordinates=jtsGeometry.getCoordinates();
			ArrayList<double[]> doubleCoordinates=new ArrayList<>();
			for(Coordinate c:jtsCoordinates) {
				double[] arr=new double[2];
				arr[0]=c.getX();
				arr[1]=c.getY();
				doubleCoordinates.add(arr);
			}
			double[][][] translatedCoordinates=new double[1][][];
			translatedCoordinates[0]=new double[doubleCoordinates.size()][];
			for(int i=0;i<doubleCoordinates.size();i++)
				translatedCoordinates[0][i]=doubleCoordinates.get(i);
			JSONPolygon poly=new JSONPolygon();
			poly.setType("Polygon");
			poly.setCoordinates(translatedCoordinates);
			Gson g=new Gson();
			f.setGeometry(g.fromJson((g.toJson(poly, JSONPolygon.class)), JsonObject.class));
			tmp.add(f);
		}
		Feature[] arr=new Feature[tmp.size()];
		for(int i=0;i<arr.length;i++)
			arr[i]=tmp.get(i);
		res.setFeatures(arr);
		return res;
	}
	
	public Districting getDistricting(int index) { return plans[index]; }
	
	public int size() {
		return plans.length;
	}
	
	public String toString() {
		return "Georgia-5000 Job: "+plans.length+" districtings";
	}
	
	public class JSONPolygon{
		private String type;
		private double[][][] coordinates;
		public String getType() { return type; }
		public void setType(String s) { type=s; }
		public double[][][] getCoordinates() { return coordinates; }
		public void setCoordinates(double[][][] coordinates) { this.coordinates=coordinates; }
	}
	
	
	
	//TEMP
	/*public void testCompactness(int i) {
		System.out.println("Polsby(actual): ");
		for(int j=0;j<i;j++)
			System.out.println(plans[j].getPolsby());
		System.out.println("Polsby(est): ");
		for(int j=0;j<i;j++)
			System.out.println(plans[j].estPolsby());
	}
	public void precompute() {
		fCollection.init();
		int count=0;
		try {
			File f = new File("output.json");
			if(f.createNewFile()) System.out.println("DEBUG: file created.");
			else System.out.println("DEBUG: file already exists.");
			FileWriter fw=new FileWriter("output.json", true);
			fw.write("[\n");
			for(Districting d:plans) {
				d.init();
				fw.write("{\"ID\":"+count+",\"compactness\":"+d.getCompactness()+"},\n");
				fw.flush();
				System.out.println("DEBUG: Working("+count+++") compactness="+d.getCompactness());
			}
			fw.write("]");
			fw.close();
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	public void precompute2() {
		int count=0;
		try {
			FileWriter fw=new FileWriter("Georgia-5000-Gill.json", true);
			fw.write("[\n");
			for(Districting d:plans) {
				fw.write("{\"ID\":"+count+",\"gill\":"+Arrays.toString(d.gillConstruct(enacted))+"},\n");
				fw.flush();
				System.out.println("DEBUG: Working("+count+++")"); //gill="+Arrays.toString(d.gillConstruct(enacted)));
			}
			fw.write("]");
			fw.close();
		}
		catch(Exception e) { e.printStackTrace(); }
	}*/
}
