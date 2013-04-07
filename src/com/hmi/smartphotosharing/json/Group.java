package com.hmi.smartphotosharing.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;
/**
 * This class represents a Group list item, containing an icon and a title.
 * @author Edwin
 *
 */
public class Group {
	
	public String gid;
	public String name;
	public String locationlink;
	public String longstart;
	public String longend;
	public String latstart;
	public String latend;
		
	// is the user a member?
	public boolean member;
	public int totalnew;
	
	@SerializedName("private")
	private String private_group;
	public String logo;
	public String description;
	public String owner;

	public List<Photo> photos;
	public List<User> users;
		
	public boolean isLocationLocked() {
		return locationlink.equals("1");
	}
	
	
	public boolean isPrivate() {
		return private_group.equals("1");
	}
	
	public long getId() {
		return Long.parseLong(gid);
	}	
}
