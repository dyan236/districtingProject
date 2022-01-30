package com.example.demo.DB;

import java.io.Serializable;
import java.util.Objects;

public class IncumbentId implements Serializable{
	private static final long serialVersionUID=1L;
	private String state;
	private Integer district;
	public IncumbentId() {}
	public IncumbentId(String state, int district) {
		this.state=state;
		this.district=district;
	}
	public boolean equals(Object o) {
		try {
			IncumbentId other=(IncumbentId) o;
			if(state.equals(other.state) && district==other.district) return true;
			return false;
		}
		catch(Exception e) { return false; }
	}
	public int hashCode() {
		return Objects.hash(state, district);
	}
}
