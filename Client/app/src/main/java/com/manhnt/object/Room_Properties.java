package com.manhnt.object;

import java.io.Serializable;

public class Room_Properties implements Serializable, Cloneable{

	private static final long serialVersionUID = -2386239991199509610L;
	private float rent_per_month, electric; 
	private int water, area, number_per_room, min_stay;
	private String available_from, room_type, room_state;

	public Room_Properties(){
		
	}

	public Room_Properties(float rent_per_month, float electric, int water, int area, int number_per_room, int min_stay,
			String available_from, String room_type, String room_state){
		this.rent_per_month = rent_per_month;
		this.electric = electric;
		this.water = water;
		this.area = area;
		this.number_per_room = number_per_room;
		this.min_stay = min_stay;
		this.available_from = available_from;
		this.room_type = room_type;
		this.room_state = room_state;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public float getRent_per_month() {
		return rent_per_month;
	}

	public void setRent_per_month(float rent_per_month) {
		this.rent_per_month = rent_per_month;
	}

	public float getElectric() {
		return electric;
	}

	public void setElectric(float electric) {
		this.electric = electric;
	}

	public int getWater() {
		return water;
	}

	public void setWater(int water) {
		this.water = water;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getNumber_per_room() {
		return number_per_room;
	}

	public void setNumber_per_room(int number_per_room) {
		this.number_per_room = number_per_room;
	}

	public int getMin_stay() {
		return min_stay;
	}

	public void setMin_stay(int min_stay) {
		this.min_stay = min_stay;
	}

	public String getAvailable_from() {
		return available_from;
	}

	public void setAvailable_from(String available_from) {
		this.available_from = available_from;
	}

	public String getRoom_type() {
		return room_type;
	}

	public void setRoom_type(String room_type) {
		this.room_type = room_type;
	}

	public String getRoom_state() {
		return room_state;
	}

	public void setRoom_state(String room_state) {
		this.room_state = room_state;
	}

}
