package com.hmi.json;

public class PhotoResponse {
	public int status;
	
	public String msg;
	
	public Photo obj;

	public Photo getObject() {
		return obj;
	}

	public String getMessage() {
		return msg;
	}
}
