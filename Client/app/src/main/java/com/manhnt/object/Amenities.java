package com.manhnt.object;

import java.io.Serializable;

public class Amenities implements Serializable, Cloneable{

	private String name;
	private boolean selected;
	private String name_json;

	public Amenities(String name, boolean selected, String name_json) {
		this.name = name;
		this.selected = selected;
		this.name_json = name_json;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getName_json() {
		return name_json;
	}
	
}