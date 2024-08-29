package com.manhnt.object;

import java.io.Serializable;

public class Ward implements Serializable{

	private String ward_id, name, type, location;
	String district_id;

	public Ward(String ward_id){
		this.ward_id = ward_id;
	}

	public Ward(String ward_id, String name, String type, String location, String district_id) {
		this.ward_id = ward_id;
		this.name = name;
		this.type = type;
		this.location = location;
		this.district_id = district_id;
	}

	public String getWard_id() {
		return ward_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}
	
}
