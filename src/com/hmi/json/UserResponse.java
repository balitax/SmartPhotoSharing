package com.hmi.json;

public class UserResponse {

	public int status;
	
	public String msg;
	
	public User obj;
	
	public User getObject() {
		return obj;
	}

	public String getMessage() {
		return msg;
	}
}
