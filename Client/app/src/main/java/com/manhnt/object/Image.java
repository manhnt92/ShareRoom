package com.manhnt.object;

import java.io.Serializable;

public class Image implements Serializable, Cloneable{

	private int id;
	private String path;
	private String note;
	private boolean isUrl;
	private boolean isCloseButton;

	public Image(String path, String note, boolean isUrl, boolean isCloseButton){
		this(0, path, note, isUrl, isCloseButton);
	}
	
	public Image(int id, String path, String note, boolean isUrl, boolean isCloseButton){
		this.id = id;
		this.path = path;
		this.note = note;
		this.isUrl = isUrl;
		this.isCloseButton = isCloseButton;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public boolean isUrl() {
		return isUrl;
	}

	public void setUrl(boolean isUrl) {
		this.isUrl = isUrl;
	}

	public boolean isCloseButton() {
		return isCloseButton;
	}

}
