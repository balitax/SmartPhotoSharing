package com.hmi.smartphotosharing.json;

import java.util.List;

public class User {
	
	public String uid;
	public String active;
	public String uname;
	public String rname;
	public String fname;
	public String pword;
	public String email;
	public String fb_id;
	public String fb_oauth;
	public String picture;
	public String thumb;
	
	public int groups;
	public int photos;
	public int following;
	public int followers;
	
	public boolean isprivate;
	public boolean isfriend;
	
	public List<Photo> newest_photos;
	
	public long getId() {
		return Long.parseLong(uid);
	}
	
	public String getName() {
		return rname;
	}
	
	public boolean isPrivate() {
		return isprivate;
	}
	
	public boolean isFriend() {
		return isfriend;
	}
}
