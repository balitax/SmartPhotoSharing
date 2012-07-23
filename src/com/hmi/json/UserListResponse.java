package com.hmi.json;

import java.util.List;


public class UserListResponse {

	public int status;
	
	public List<User> msg;
	
	public List<User> getUserList() {
		return msg;
	}
}
