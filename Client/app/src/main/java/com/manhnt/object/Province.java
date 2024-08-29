package com.manhnt.object;

import java.io.Serializable;

public class Province implements Serializable {
	
	private static final long serialVersionUID = 3880939560795475082L;
	private String province_id, name, type;

	public Province(String province_id){
		this.province_id = province_id;
	}
	
	public Province(String province_id, String name, String type) {
		this.province_id = province_id;
		this.name = name;
		this.type = type;
	}


	public String getProvince_id() {
		return province_id;
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

}
