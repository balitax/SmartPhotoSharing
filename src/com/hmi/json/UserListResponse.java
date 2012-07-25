package com.hmi.json;

import java.util.List;


public class UserListResponse {

	public int status;
	
	public String msg;
	
	public List<User> obj;
	
	public List<User> getUserList() {
		return obj;
	}
}
