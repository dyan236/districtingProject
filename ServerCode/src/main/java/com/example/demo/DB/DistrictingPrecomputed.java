package com.example.demo.DB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(DistrictingPrecomputedId.class)
public class DistrictingPrecomputed{
	@Id
	private String job;
	@Id
	private Integer districtingIndex;
	private Double compactness;
	private int[] gill;
	public DistrictingPrecomputed() {}
	public DistrictingPrecomputed(String job, int districtingIndex, double compactness, int[] gill) {
		this.job=job;
		this.districtingIndex=districtingIndex;
		this.compactness=compactness;
		this.gill=gill;
	}
	public int getIndex() { return districtingIndex; }
	public Double getCompactness() { return compactness; }
	public int[] getGill() { return gill; }
}
