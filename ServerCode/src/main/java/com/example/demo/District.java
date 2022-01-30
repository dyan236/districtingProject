package com.example.demo;

import com.example.demo.DB.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class District {
	private transient HttpSession session;
	private transient FeatureCollection fCollection;
	private transient IncumbentManager incumbentManager;
	private int districtNumber, HVAP, WVAP, BVAP, AMINVAP, ASIANVAP, NHPIVAP;	//Comes with the json
	private int[] precincts;	//Precinct IDs, NOT precinct objects
	private String[] incumbents;	//Stores only conflicting incumbents
	private String[] incumbentsProperties;	//Stores all incumbents, for mapbox display
	private int TOTPOP, HISP, WHITE, BLACK, AMIN, ASIAN, NHPI, OTHER, VAP;
	private Geometry geometry;
	private double area;
	private double compactness;
	private transient boolean init=false;
	
	public void setSession(HttpSession session) {
		this.session=session;
		setFeatureCollection();
		setIncumbentManager();
	}
	private void setFeatureCollection() { fCollection=(FeatureCollection) session.getAttribute("FeatureCollection"); }
	private void setIncumbentManager() { incumbentManager=(IncumbentManager) session.getAttribute("Incumbents"); }
	
	public void init() {
		if(!init) {
			TOTPOP=0;
			area=0;
			ArrayList<String> tmp=new ArrayList<>();
			ArrayList<String> tmp2=new ArrayList<>();
			for(int i:precincts) {
				TOTPOP+=fCollection.getPrecinct(i).getTOTPOP();;
				HISP+=fCollection.getPrecinct(i).getHISP();
				WHITE+=fCollection.getPrecinct(i).getWHITE();
				BLACK+=fCollection.getPrecinct(i).getBLACK();
				AMIN+=fCollection.getPrecinct(i).getAMIN();
				ASIAN+=fCollection.getPrecinct(i).getASIAN();
				NHPI+=fCollection.getPrecinct(i).getNHPI();
				OTHER+=fCollection.getPrecinct(i).getOTHER();
				VAP+=fCollection.getPrecinct(i).getVAP();
				area+=fCollection.getPrecinct(i).getArea();
				Incumbent incumbent=incumbentManager.getIncumbent(i);
				if(incumbent!=null) {
					tmp.add(incumbent.getIncumbent());
					tmp2.add(incumbent.getIncumbent());
				}
			}
			if(tmp.size()>1) {
				incumbents=new String[tmp.size()];
				for(int i=0;i<incumbents.length;i++)
					incumbents[i]=tmp.get(i);
			}
			else 
				incumbents=null;
			if(tmp2.size()>0) {
				incumbentsProperties=new String[tmp2.size()];
				for(int i=0;i<incumbentsProperties.length;i++)
					incumbentsProperties[i]=tmp2.get(i);
			}
			else incumbentsProperties=null;
			//calcCompactness();
			init=true;
		}
	}
	
	public int getMinority(Minority m) {
		switch(m) {
			case HISP: return HISP;
			case WHITE: return WHITE;
			case BLACK: return BLACK;
			case AMIN: return AMIN;
			case ASIAN: return ASIAN;
			case NHPI: return NHPI;
			case OTHER: return OTHER;
			default: return -1;
		}
	}
	
	//See JTS' 'Envelope' object for more info
	public Envelope getEnvelope() {
		Envelope e = fCollection.getPrecinct(precincts[0]).getGeometry().getEnvelopeInternal();
		for(int i=1;i<precincts.length;i++)
			e.expandToInclude(fCollection.getPrecinct(precincts[i]).getGeometry().getEnvelopeInternal());
		return e;
	}
	
	public boolean isMajMinDistrict(double threshold, Minority minority) {
		int minorityPop=0;
		switch(minority) {
			case HISP:
				minorityPop=HISP;
				break;
			case WHITE:
				minorityPop=WHITE;
				break;
			case BLACK:
				minorityPop=BLACK;
				break;
			case AMIN:
				minorityPop=AMIN;
				break;
			case ASIAN:
				minorityPop=ASIAN;
				break;
			case NHPI:
				minorityPop=NHPI;
				break;
			case OTHER:
				minorityPop=OTHER;
				break;
			default:
				return false;
		}
		//System.out.println("DEBUG: "+((double) minorityPop/TOTPOP));
		if(((double) minorityPop/TOTPOP)>=threshold) return true;
		return false;
	}
	
	//Calculates Polsbys Compactness
	private void calcCompactness() {
		Geometry test=getGeometry();
		compactness=4*Math.PI*(area/Math.pow(test.getLength(), 2));
	}
	
	private void calcGeometry() {
		//System.out.println("Working on: "+districtNumber);
		Geometry[] arr=new Geometry[precincts.length];
		for(int i=0;i<arr.length;i++) 
			arr[i]=fCollection.getPrecinct(precincts[i]).getGeometry();
		GeometryFactory gf=new GeometryFactory();
		geometry=gf.createGeometryCollection(arr).union();
		
		//Remove holes from: https://gis.stackexchange.com/questions/364571/remove-fill-holes-in-polygons-from-a-multipolygon-using-jts-topology-suite-progr
		if(geometry.getGeometryType().equalsIgnoreCase("Polygon")) {
			Polygon p = (Polygon) geometry;
			geometry = gf.createPolygon(p.getExteriorRing().getCoordinateSequence());
		}
		else {
			/*MultiPolygon multi = (MultiPolygon) geometry;
			ArrayList<Polygon> list = new ArrayList<>();
			for(int i=0;i<multi.getNumGeometries();i++) {
				Polygon p = gf.createPolygon(((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinateSequence());
				list.add(p);
			}
			geometry = gf.createMultiPolygon(list.toArray(new Polygon[] {})).getBoundary();*/
			double maxArea=-1;
			Polygon p=null;
			MultiPolygon multi = (MultiPolygon) geometry;
			for(int i=0;i<multi.getNumGeometries();i++) {
				if(multi.getGeometryN(i).getArea()>maxArea) {
					maxArea=multi.getGeometryN(i).getArea();
					p=gf.createPolygon(((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinateSequence());
				}
			}
			if(p!=null) {
				geometry=p;
				/*ArrayList<Polygon> list=new ArrayList<>();
				for(Geometry g:arr) {
					try {
						if(!p.intersects(g)) {
							if(g.getGeometryType().equalsIgnoreCase("Polygon"))
								list.add((Polygon) g);
							else {
								MultiPolygon m = (MultiPolygon) g;
								for(int i=0;i<m.getNumGeometries();i++) {
									Polygon poly = (Polygon) multi.getGeometryN(i);
									list.add(poly);
								}
							}
						}
					}
					catch(Exception e) { System.out.println("Geometry skipped. "); }
				}
				list.add(p);
				geometry=gf.createMultiPolygon(list.toArray(new Polygon[] {}));*/
			}
		}
	}
	
	public Geometry getGeometry() {
		if(geometry==null) calcGeometry();
		return geometry;
	}
	
	//Returns the union of all bounding boxes of precincts; Simplifies geometry by a lot
	public Geometry approxGeometry() {
		Geometry[] arr=new Geometry[precincts.length];
		for(int i=0;i<arr.length;i++)
			arr[i]=fCollection.getPrecinct(precincts[i]).getEnvelope();
		GeometryFactory gf=new GeometryFactory();
		return gf.createGeometryCollection(arr).union();
	}
	
	public int getTOTPOP() { return TOTPOP; }
	public double getCompactness() {
		if(compactness==0) calcCompactness();
		return compactness; 
	}
	public String[] getIncumbents() { return incumbents; }
	public String[] getIncumbentsProperties() { return incumbentsProperties; }
	public int getVAP() { return VAP; }
	public int getDistrictNumber() { return districtNumber; }
	public double getArea() { return area; }
	
	public String toString() {
		return "District("+districtNumber+")";
	}
	
	
	
	//TEMP
	/*public double getPolsby() {
		calcGeometry();
		//System.out.println("DEBUG: "+geometry.getArea()+", "+geometry.getLength());
		return 4*Math.PI*(geometry.getArea()/Math.pow(geometry.getLength(), 2));
	}*/
	public double estPolsby() {
		Geometry test=approxGeometry();
		//System.out.println("DEBUG: "+test.getArea()+", "+test.getLength());
		return 4*Math.PI*(geometry.getArea()/Math.pow(test.getLength(), 2));
	}
}
