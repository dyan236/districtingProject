package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class EnactedDistricting{
	private transient FeatureCollection fc;
	private String type="FeatureCollection";
	private Feature[] features;
	
	public void init() {
		//for(Feature f:features) f.properties.init();
		for(Feature f:features) f.init();
	}
	
	public Feature[] getFeatures() { return features; }
	
	public Double[] getBoxAndWhiskerArray(Minority m) {
		ArrayList<Double> tmp=new ArrayList<>();
		for(Feature f:features)
			tmp.add((double) f.getMinority(m)/f.getTOTPOP());
		Double[] res=new Double[tmp.size()];
		for(int i=0;i<res.length;i++)
			res[i]=tmp.get(i);
		Arrays.sort(res);
		return res;
	}
	
	public int getNumDistricts() {
		return features.length;
	}
	
	public String toString() {
		String res = "EnactedDistricting: "+features.length+"\n";
		for(Feature f:features) res+=f.toString()+"\n";
		return res;
	}
	
	
	//TEMP
	/*public void calc() {
	for(int i=0;i<fc.getFeatures().length;i++) {
		//System.out.println("DEBUG: Working("+i+")");
		Precinct p=fc.getFeatures()[i];
		for(Feature f:features) {
			double area=f.jtsGeo.getArea();
			Geometry[] geometries= {f.jtsGeo, p.getGeometry()};
			GeometryFactory gf=new GeometryFactory();
			Geometry combined=gf.createGeometryCollection(geometries).union();
			if(area==combined.getArea()) {
				f.properties.pBuffer.add(i);
				if(incumbents.getIncumbent(i)!=null) f.properties.sBuffer.add(incumbents.getIncumbent(i));
				f.properties.TOTPOP+=p.getTOTPOP();
				f.properties.HISP+=p.getHISP();
				f.properties.WHITE+=p.getWHITE();
				f.properties.BLACK+=p.getBLACK();
				f.properties.AMIN+=p.getAMIN();
				f.properties.ASIAN+=p.getASIAN();
				f.properties.NHPI+=p.getNHPI();
				f.properties.OTHER+=p.getOTHER();
				f.properties.VAP+=p.getVAP();
				break;
			}
		}
	}
	//Forcefully add anomaly
	Precinct p=fc.getFeatures()[454];
	features[1].properties.pBuffer.add(454);
	if(incumbents.getIncumbent(454)!=null) features[1].properties.sBuffer.add(incumbents.getIncumbent(454));
	features[1].properties.TOTPOP+=p.getTOTPOP();
	features[1].properties.HISP+=p.getHISP();
	features[1].properties.WHITE+=p.getWHITE();
	features[1].properties.BLACK+=p.getBLACK();
	features[1].properties.AMIN+=p.getAMIN();
	features[1].properties.ASIAN+=p.getASIAN();
	features[1].properties.NHPI+=p.getNHPI();
	features[1].properties.OTHER+=p.getOTHER();
	features[1].properties.VAP+=p.getVAP();
	for(Feature f:features) f.properties.flush();
	}*/
}
