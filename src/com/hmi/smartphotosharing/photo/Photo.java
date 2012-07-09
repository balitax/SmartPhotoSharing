package com.hmi.smartphotosharing.photo;

/**
 * This class represents a Group list item, containing an icon and a title.
 * @author Edwin
 *
 */
public class Photo {

	public String src;		// Reference to the Drawable
	public String title;	// Title of the list item
	
	public Photo() {
		super();
	}
	
	public Photo(String src, String title) {
		super();
		this.src = src;
		this.title = title;
	}
	
	
	
}
