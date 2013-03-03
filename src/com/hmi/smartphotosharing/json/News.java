package com.hmi.smartphotosharing.json;


public class News {
		
	public String uid;
	public String uname;
	public String rname;
	public String thumb;	// user's thumbnail url
	
	public long time;		// timestamp
	
	public String photo;	// photo thumbnail url
	public String name;		// name container
	
	public int type;		// Type of message
	public long lid;			// like ID
	public long iid;			// image ID
	public long gid;			// group ID
	public long fid;			// friend user ID
	public long sid;			// checkin ID
	public long cid;			// comment ID
	
	public int getId() {
		return Integer.parseInt(uid);
	}
}
