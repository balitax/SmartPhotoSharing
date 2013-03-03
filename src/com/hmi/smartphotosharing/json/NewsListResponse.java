package com.hmi.smartphotosharing.json;

import java.util.List;

public class NewsListResponse extends Response {
	
	private List<News> obj;

	@Override
	public List<News> getObject() {
		return obj;
	}
	
	
}
