package com.hmi.json;

import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;

public class PostData {

	public String url;
	public HashMap<String,ContentBody> map;

	public PostData(String url, HashMap<String,ContentBody> map) {
		this.url = url;
		this.map = map;
	}
	
}
