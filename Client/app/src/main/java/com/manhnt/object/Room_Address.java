package com.manhnt.object;

import java.io.Serializable;

public class Room_Address implements Serializable, Cloneable{
	
	private static final long serialVersionUID = -4397285669569547077L;
	private Province province;
	private District district;
	private Ward ward;
	private double latitude, longitude;
	private String address;
	
	public Room_Address (){
		
	}
	
	public Room_Address (Province province, District district, Ward ward, String address, double latitude, double longitude){
		this.province = province;
		this.district = district;
		this.ward = ward;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public Ward getWard() {
		return ward;
	}

	public void setWard(Ward ward) {
		this.ward = ward;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
