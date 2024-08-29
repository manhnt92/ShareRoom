package com.manhnt.object;

import java.io.Serializable;

public class District implements Serializable {

	private String district_id, name, type, location;
	String province_id;

	public District(String district_id){
		this.district_id = district_id;
	}

	public District(String district_id, String name, String type, String location, String province_id) {
		this.district_id = district_id;
		this.name = name;
		this.type = type;
		this.location = location;
		this.province_id = province_id;
	}

	public String getDistrict_id() {
		return district_id;
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
