package com.hmi.json;

import java.util.List;

public class GroupListResponse {

	public int status;
	
	public List<Group> msg;
	
	public List<Group> getGroupsList() {
		return msg;
	}
}
