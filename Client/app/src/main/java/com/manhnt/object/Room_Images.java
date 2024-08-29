package com.manhnt.object;

import java.io.Serializable;
import java.util.ArrayList;

public class Room_Images implements Serializable {
	
	private static final long serialVersionUID = -7965586928737158514L;
	private ArrayList<Image> list_images;
	
	public Room_Images () {
		
	}
	
	public Room_Images(ArrayList<Image> list_images){
		this.list_images = list_images;
	}

	public ArrayList<Image> getList_images() {
		return list_images;
	}

	public void setList_images(ArrayList<Image> list_images) {
		this.list_images = list_images;
	}

}
