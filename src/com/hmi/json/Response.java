package com.hmi.json;

public abstract class Response {
	private int status;
	
	private String msg;
	
	public int getStatus() {
		return status;
	}
	
	public String getMessage() {
		return msg;
	}

	public abstract Object getObject();
}
