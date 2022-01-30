package com.example.demo.DB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(IncumbentId.class)
public class Incumbent {
	@Id
	private String state;
	@Id
	private Integer district;
	private String incumbent;
	private Integer precinct;
	public Incumbent() {}
	public Incumbent(String state, int district, String incumbent, int precinct) {
		this.state=state;
		this.district=district;
		this.incumbent=incumbent;
		this.precinct=precinct;
	}
	public Integer getDistrict() { return district; }
	public String getIncumbent() { return incumbent; }
	public Integer getPrecinct() { return precinct; }
	public void setState(String s) { state=s; }
}
