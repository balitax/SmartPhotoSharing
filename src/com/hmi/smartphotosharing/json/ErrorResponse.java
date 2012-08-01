package com.hmi.smartphotosharing.json;

import com.hmi.smartphotosharing.util.Util;

public class ErrorResponse {
	
	public int status;
	public String msg;
	public Object obj;
	
	public ErrorResponse() {
		status = Util.STATUS_ERROR;
		msg = "Something went wrong with the server. Please try again later.";
		obj = null;
	}
}
