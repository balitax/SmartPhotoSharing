package com.hmi.smartphotosharing;

import android.content.Context;

public class Util {

	// JSON response statuses
	public static final int STATUS_OK = 200;
	public static final int STATUS_LOGIN = 304;
	public static final int STATUS_DENIED = 403;
	public static final int STATUS_404 = 404;
	public static final int STATUS_ERROR = 500;
	
	// Nav bar actions
    public static final int ACTION_ARCHIVE = 1;
    public static final int ACTION_CAMERA = 2;
    public static final int ACTION_SETTINGS = 3;

    public static final String API = "http://sps.juursema.com/api.php?";
    public static final String USER_DB = "http://sps.juursema.com/profilepicdb/";
    public static final String GROUP_DB = "http://sps.juursema.com/logodb/";
    
    public static String getUrl(Context c, int resource) {
    	return API + c.getResources().getString(resource);
    }
}
