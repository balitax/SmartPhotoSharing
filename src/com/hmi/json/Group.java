package com.hmi.json;

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
	public String timelink;
	public String starttime;
	public String endtime;
	public String longstart;
	public String longend;
	public String latstart;
	public String latend;
	
	@SerializedName("private")
	public String private_group;
	public String logo;
	public String description;
	public String owner;
	
	public List<String> photos;
	
	public long getId() {
		return Long.parseLong(gid);
	}	
			
	
}
