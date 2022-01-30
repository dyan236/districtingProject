package com.example.demo;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Precinct {
	private String type;
	private PrecinctProperties properties;
	private JsonObject geometry;
	private PrecinctGeometry geometry2;
	private Geometry jtsGeo;
	private Geometry jtsEnvelope;
	private double area;
	
	public void init() {
		convertJson();
		getGeometry();
	}
	
	//Converts from json to the corresponding precinct object, needed to do this way because coordinate structure varies
	public void convertJson() {
		if(geometry!=null) {
			Gson g=new Gson();
			if((geometry.get("type").getAsString()).equals("Polygon"))
				geometry2=g.fromJson(geometry, PrecinctPoly.class);
			else
				geometry2=g.fromJson(geometry, PrecinctMulti.class);
			geometry=null;	//Free up memory afterward, should not use geometry JsonObject itself
		}
	}
	
	public Geometry getGeometry() {
		//convertJson();
		if(jtsGeo==null) {
			if(geometry2.type.equals("Polygon")) {
				double[][] coordinates=((PrecinctPoly) geometry2).coordinates[0];
				Coordinate[] shell=new Coordinate[coordinates.length];
				for(int i=0;i<shell.length;i++)
					shell[i]=new Coordinate(coordinates[i][0], coordinates[i][1]);
				GeometryFactory gf=new GeometryFactory();
				jtsGeo = gf.createPolygon(shell);
			}
			else {
				double[][][] coordinates=((PrecinctMulti) geometry2).coordinates[0];
				Polygon[] polygons=new Polygon[coordinates.length];
				GeometryFactory gf=new GeometryFactory();
				for(int i=0;i<polygons.length;i++) {
					Coordinate[] shell=new Coordinate[coordinates[i].length];
					for(int j=0;j<shell.length;j++)
						shell[j]=new Coordinate(coordinates[i][j][0], coordinates[i][j][1]);
					polygons[i]=gf.createPolygon(shell);
				}
				jtsGeo = gf.createMultiPolygon(polygons);
			}
			area=jtsGeo.getArea();
			jtsEnvelope=jtsGeo.getEnvelope();
			return jtsGeo;
		}
		else return jtsGeo;
	}
	
	//See JTS' Envelope object for more info
	public Geometry getEnvelope() {
		//if(jtsEnvelope==null) getGeometry();
		return jtsEnvelope;
	}
	public double getArea() { return area; }
	public int getTOTPOP() { return properties.TOTPOP; }
	public int getHISP() { return properties.HISP; }
	public int getWHITE() { return properties.WHITE; }
	public int getBLACK() { return properties.BLACK; }
	public int getAMIN() { return properties.AMIN; }
	public int getASIAN() { return properties.ASIAN; }
	public int getNHPI() { return properties.NHPI; }
	public int getOTHER() { return properties.OTHER; }
	public int getVAP() { return properties.VAP; }
	
	public String toString() {
		//convertJson();
		System.out.println(geometry2.toString());
		return type+" properties: "+properties.toString();
	}
	
	
	
	//Java class for holding precinct properties
	public class PrecinctProperties{
		private int ID, CD, TOTPOP, HISP, WHITE, BLACK, AMIN, ASIAN, NHPI, OTHER, VAP, HVAP, WVAP, BVAP, AMINVAP, ASIANVAP, NHPIVAP, OTHERVAP;
		private String NEIGHBORS;
		public String toString() {
			return ID+" "+TOTPOP+" "+HISP+" "+WHITE+" "+BLACK+" "+AMIN+" "+ASIAN+" "+NEIGHBORS;
		}
	}
	//Interface for compatibility
	public abstract class PrecinctGeometry{
		private String type;
	}
	//Geometry class for "Polygon"
	public class PrecinctPoly extends PrecinctGeometry{
		//private String type;
		private double[][][] coordinates;
		public String toString() {
			return super.type+", coordinates: "+coordinates.length;
		}
	}
	//Geometry class for "MultiPolygon"
	public class PrecinctMulti extends PrecinctGeometry{
		//private String type;
		private double[][][][] coordinates;
		public String toString() {
			return super.type+", coordinates: "+coordinates.length;
		}
	}
}