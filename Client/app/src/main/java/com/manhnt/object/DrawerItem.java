package com.manhnt.object;

public class DrawerItem {

	private String name;
	private int resId;
	private String title;
	private String url;

	public DrawerItem(String name, int resId) {
		this.name = name;
		this.resId = resId;
	}

	public DrawerItem(String title) {
		this(null, 0);
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
