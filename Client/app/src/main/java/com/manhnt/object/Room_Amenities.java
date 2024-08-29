package com.manhnt.object;

import java.io.Serializable;
import java.util.ArrayList;

public class Room_Amenities implements Serializable, Cloneable{
	
	private static final long serialVersionUID = -5325815940702049867L;
	private ArrayList<Amenities> list_amenities;
	private String other;
	
	public Room_Amenities (){
		
	}
	
	public Room_Amenities (ArrayList<Amenities> list_amenities, String other){
		this.list_amenities = list_amenities;
		this.other = other;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Room_Amenities cloned = (Room_Amenities) super.clone();
		ArrayList<Amenities> list_amenities_clone = new ArrayList<>();
		for(int i = 0; i < cloned.getList_amenities().size(); i++){
			Amenities amenities = cloned.getList_amenities().get(i);
			list_amenities_clone.add((Amenities) amenities.clone());
		}
		cloned.setList_amenities(list_amenities_clone);
		return cloned;
	}

	public ArrayList<Amenities> getList_amenities() {
		return list_amenities;
	}

	public void setList_amenities(ArrayList<Amenities> list_amenities) {
		this.list_amenities = list_amenities;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

}
