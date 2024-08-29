package com.manhnt.object;

import java.io.Serializable;
import com.manhnt.config.Config;

public class Account implements Serializable, Cloneable{

	private int id, age, account_type;
	private String email, password, api_key, facebook_id, facebook_access_token;
	private String first_name, last_name;
	private String gender, birthday, address, occupation, description, phoneNumber, avatar;

	public Account(int id, String email, String password, String api_key,String facebook_id,
			String facebook_access_token, String first_name, String last_name, String gender, String birthday,
			int age, String address, String occupation, String description, String phoneNumber, String avatar, int account_type){
		this.id = id;
		this.email = email;
		this.password = password;
		this.api_key = api_key;
		this.facebook_id = facebook_id;
		this.facebook_access_token = facebook_access_token;
		this.first_name = first_name;
		this.last_name = last_name;
		this.gender = gender;
		this.birthday = birthday;
		this.age = age;
		this.address = address;
		this.occupation = occupation;
		this.description = description;
		this.phoneNumber = phoneNumber;
		this.avatar = avatar;
		this.account_type = account_type;
	}

	public Account (String email, String password, String first_name, String last_name){
		this.email = email;
		this.password = password;
		this.first_name = first_name;
		this.last_name = last_name;
		this.account_type = Config.ACCOUNT_NORMAL;
	}

	public Account (String email,String password){
		this.email = email;
		this.password = password;
		this.account_type = Config.ACCOUNT_NORMAL;
	}

	public Account (String facebook_id,String facebook_access_token, String email, String first_name, String last_name, String gender, String birthday, String avatar){
		this.facebook_id = facebook_id;
		this.facebook_access_token = facebook_access_token;
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.gender = gender;
		this.birthday = birthday;
		this.avatar = avatar;
		this.account_type = Config.ACCOUNT_FACEBOOK;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFacebook_id() {
		return facebook_id;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAccount_type() {
		return account_type;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApi_key() {
		return api_key;
	}

	public String getFacebook_access_token() {
		return facebook_access_token;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

}
