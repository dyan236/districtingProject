package com.example.demo;

import java.util.List;

import com.example.demo.DB.Incumbent;

public class IncumbentManager {
	List<Incumbent> list;
	public void setIncumbents(List<Incumbent> l) {
		list=l;
	}
	public Incumbent getIncumbent(int precinct) {
		for(Incumbent i:list)
			if(i.getPrecinct()==precinct)
				return i;
		return null;
	}
}
