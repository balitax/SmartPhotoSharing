package com.hmi.smartphotosharing.json;


public class Feedback {

	public String fid;	
	public String uid;
	public String date;
	public String file;
	public String message;
	
	public int getId() {
		return Integer.parseInt(fid);
	}
}
