package com.example.demo;
import java.util.ArrayList;
import java.util.Arrays;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Feature {
	private String type;
	private FeatureProperties properties;
	private JsonObject geometry;
	private transient PrecinctGeometry geometry2;
	private transient Geometry jtsGeo;
	
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
			//geometry=null;	//Free up memory afterward, should not use geometry JsonObject itself
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
			properties.area=jtsGeo.getArea();
			//jtsEnvelope=jtsGeo.getEnvelope();
			return jtsGeo;
		}
		else return jtsGeo;
	}
	
	public FeatureProperties getProperties() {
		return properties;
	}
	
	public void setProperties(String[] incumbents, int TOTPOP, int HISP, int WHITE, int BLACK, int AMIN, int ASIAN, int NHPI, int OTHER, int VAP, int districtNumber) {
		properties=new FeatureProperties(incumbents, TOTPOP, HISP, WHITE, BLACK, AMIN, ASIAN, NHPI, OTHER, VAP, districtNumber);
	}
	
	public int getMinority(Minority m) {
		switch(m) {
			case HISP: return properties.HISP;
			case BLACK: return properties.BLACK;
			case AMIN: return properties.AMIN;
			case ASIAN: return properties.ASIAN;
			case NHPI: return properties.NHPI;
			case OTHER: return properties.OTHER;
			default: return properties.WHITE;
		}
	}
	
	public double getArea() { return properties.area; }
	public int getTOTPOP() { return properties.TOTPOP; }
	public void setType(String s) { type=s; }
	public void setGeometry(JsonObject j) { geometry=j; }
	
	public String toString() {
		return "Feature: "+properties.toString();
	}
	
	
	
	public class FeatureProperties{
		private String Code;
		private String District;
		private String color="Green";
		private int[] precincts;
		private transient ArrayList<Integer> pBuffer;
		private String[] incumbents;
		private transient ArrayList<String> sBuffer;
		private int TOTPOP=0, HISP=0, WHITE=0, BLACK=0, AMIN=0, ASIAN=0, NHPI=0, OTHER=0, VAP=0;
		private double area;
		private double compactness;
		private int districtNumber;
		public FeatureProperties() {}
		public FeatureProperties(String[] incumbents, int TOTPOP, int HISP, int WHITE, int BLACK, int AMIN, int ASIAN, int NHPI, int OTHER, int VAP, int districtNumber) {
			this.incumbents=incumbents;
			this.TOTPOP=TOTPOP;
			this.HISP=HISP;
			this.WHITE=WHITE;
			this.BLACK=BLACK;
			this.AMIN=AMIN;
			this.ASIAN=ASIAN;
			this.NHPI=NHPI;
			this.OTHER=OTHER;
			this.VAP=VAP;
			this.districtNumber=districtNumber;
		}
		public void init() {
			pBuffer=new ArrayList<>();
			sBuffer=new ArrayList<>();
		}
		public void flush() {
			precincts=new int[pBuffer.size()];
			for(int i=0;i<precincts.length;i++)
				precincts[i]=pBuffer.get(i);
			incumbents=new String[sBuffer.size()];
			for(int i=0;i<incumbents.length;i++)
				incumbents[i]=sBuffer.get(i);
		}
		public void setArea(double d) { area=d; }
		public void setCompactness(double d) { compactness=d; }
		public String toString() {
			return Code+", "+", "+Arrays.asList(incumbents)+", "+TOTPOP+", "+HISP+", "+WHITE+", "+BLACK+", "+AMIN+", "+ASIAN+", "+NHPI+", "+OTHER+", "+VAP+", "+area;
		}
	}
	//Interface for compatibility
	public abstract class PrecinctGeometry{
		private String type;
	}
	//Geometry class for "Polygon"
	public class PrecinctPoly extends PrecinctGeometry{
		private double[][][] coordinates;
		public String toString() {
			return super.type+", coordinates: "+coordinates.length;
		}
	}
	//Geometry class for "MultiPolygon"
	public class PrecinctMulti extends PrecinctGeometry{
		private double[][][][] coordinates;
		public String toString() {
			return super.type+", coordinates: "+coordinates.length;
		}
	}
}
