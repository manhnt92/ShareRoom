package com.manhnt.object;

import java.io.Serializable;

public class Room implements Serializable {

	private int id;
	private Room_Address room_address;
	private Room_Properties room_properties;
	private Room_Images room_images;
	private Room_Amenities room_amenities;
	
	public Room (){
		
	}
	
	public Room(int id, Room_Address room_address, Room_Properties room_properties, Room_Images room_images, Room_Amenities room_amenities){
		this.id = id;
		this.room_address = room_address;
		this.room_properties = room_properties;
		this.room_images = room_images;
		this.room_amenities = room_amenities;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Room_Address getRoom_address() {
		return room_address;
	}

	public void setRoom_address(Room_Address room_address) {
		this.room_address = room_address;
	}
	
	public Room_Properties getRoom_properties() {
		return room_properties;
	}
	
	public void setRoom_properties(Room_Properties room_properties) {
		this.room_properties = room_properties;
	}
	
	public Room_Images getRoom_images() {
		return room_images;
	}
	
	public void setRoom_images(Room_Images room_images) {
		this.room_images = room_images;
	}

	public Room_Amenities getRoom_amenities() {
		return room_amenities;
	}

	public void setRoom_amenities(Room_Amenities room_amenities) {
		this.room_amenities = room_amenities;
	}
	
}
