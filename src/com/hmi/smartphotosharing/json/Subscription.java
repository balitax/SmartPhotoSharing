package com.hmi.smartphotosharing.json;

import java.util.List;
/**
 * This class represents a Subscription list item, containing an icon and a title.
 * @author Edwin
 *
 */
public class Subscription {
	
	public String ssid;
	public String name;
	public String uid;
	public String person;
	public String lon1;
	public String lat1;
	public String lon2;
	public String lat2;
	public String time;
	
	public String icon;
	public int totalnew;
	
	public List<Photo> photos;
	
	public long getId() {
		return Long.parseLong(ssid);
	}	
}
