package com.hmi.json;

import java.util.List;

public class GroupListResponse {

	public int status;
	
	public String msg;
	
	public List<Group> obj;
	
	public List<Group> getObject() {
		return obj;
	}

	public String getMessage() {
		return msg;
	}
}
