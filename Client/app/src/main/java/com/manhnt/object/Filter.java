package com.manhnt.object;

public class Filter {

	private float rent_min, rent_max, electric_min, electric_max;
	private int water_min, water_max, area_min, area_max, person_min, person_max;
	
	public Filter (float rent_min, float rent_max, float electric_min, float electric_max, int water_min, int water_max,
			int area_min, int area_max, int person_min, int person_max) {
		this.rent_min = rent_min;
		this.rent_max = rent_max;
		this.electric_min = electric_min;
		this.electric_max = electric_max;
		this.water_min = water_min;
		this.water_max = water_max;
		this.area_min = area_min;
		this.area_max = area_max;
		this.person_min = person_min;
		this.person_max = person_max;
	}

	public float getRent_min() {
		return rent_min;
	}

	public float getRent_max() {
		return rent_max;
	}

	public float getElectric_min() {
		return electric_min;
	}

	public float getElectric_max() {
		return electric_max;
	}

	public int getWater_min() {
		return water_min;
	}

	public int getWater_max() {
		return water_max;
	}

	public int getArea_min() {
		return area_min;
	}

	public int getArea_max() {
		return area_max;
	}

	public int getPerson_min() {
		return person_min;
	}

	public int getPerson_max() {
		return person_max;
	}

}
