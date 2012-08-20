package com.hmi.smartphotosharing.json;

public class Comment {

	private String cid;
	private String uid;
	public String comment;
	public String time;
	public String rname;
	public String picture;
	public String thumb;
	
	public long getUid() {
		return Long.parseLong(uid);
	}
	
	public long getId() {
		return Long.parseLong(cid);
	}
}
