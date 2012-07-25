package com.hmi.json;

import java.util.List;

public class PhotoListResponse extends Response {
	
	private List<Photo> obj;

	@Override
	public List<Photo> getObject() {
		return obj;
	}
	
	
}
