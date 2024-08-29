package com.manhnt.object;

import java.io.Serializable;
import java.util.ArrayList;

public class MyRooms implements Serializable{

	private static final long serialVersionUID = 7188286968413175675L;
	private ArrayList<Room> list_room;
	private int position;
	
	public MyRooms (ArrayList<Room> list_room){
		this.list_room = list_room;
	}
	
	public MyRooms(ArrayList<Room> list_room, int position){
		this.list_room = list_room;
		this.position = position;
	}
	
	public ArrayList<Room> getList_room() {
		return list_room;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
