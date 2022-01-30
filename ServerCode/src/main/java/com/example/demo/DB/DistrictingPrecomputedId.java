package com.example.demo.DB;

import java.io.Serializable;
import java.util.Objects;

public class DistrictingPrecomputedId implements Serializable{
	private static final long serialVersionUID=1L;
	private String job;
	private Integer districtingIndex;
	public DistrictingPrecomputedId() {}
	public DistrictingPrecomputedId(String job, int districtingIndex) {
		this.job=job;
		this.districtingIndex=districtingIndex;
	}
	public boolean equals(Object o) {
		try {
			DistrictingPrecomputedId other=(DistrictingPrecomputedId) o;
			if(job.equals(other.job) && districtingIndex==other.districtingIndex) return true;
			return false;
		}
		catch(Exception e) { return false; }
	}
	public int hashCode() {
		return Objects.hash(job, districtingIndex);
	}
}
