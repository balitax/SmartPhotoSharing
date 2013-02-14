package com.hmi.smartphotosharing.json;

import com.hmi.smartphotosharing.util.Util;

public class ErrorResponse {
	
	public int status;
	public String msg;
	public Object obj;
	
	public ErrorResponse() {
		status = Util.STATUS_ERROR;
		msg = "Could not connect to the server.";
		obj = null;
	}
}
