package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class JobDescription {
	@Id
	private String id;
	private String state;
	private int districtings;
	private String params;
	public JobDescription() {}
	public JobDescription(String id, String state, int districtings, String params) {
		this.id=id;
		this.state=state;
		this.districtings=districtings;
		this.params=params;
	}
	public String getId() { return id; }
	public String getState() { return state; }
	public int getNumDistrictings() { return districtings; }
	public String getParams() { return params; }
}
