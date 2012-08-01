package com.hmi.smartphotosharing.json;

import java.util.List;

public class CommentListResponse extends Response{
	private List<Comment> obj;
	
	public List<Comment> getObject() {
		return obj;
	}
}
