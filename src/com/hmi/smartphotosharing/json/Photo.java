package com.hmi.smartphotosharing.json;

import java.util.List;

public class Photo {

	public String name;
	public String location;
	public String uid;
	public String iid;
	public String gid;
	public String longtitude;
	public String latitude;
	
	public String time;
	public String active;

	public String age;
	public String thumb;
	
	public String picname;
	
	public boolean isNew;
	
	// Group attributes
	public String groupname;
	
	// User attributes
	public String uname;
	public String rname;
	public String pword;
	public String email;
	public String fb_id;
	public String fb_oauth;
	public String picture;
		
	public List<Comment> comments;
	
	public String getUrl() {
		return location + picname;
	}
	
	public long getId() {
		return Long.parseLong(iid);		
	}
}
