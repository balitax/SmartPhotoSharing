package com.hmi.smartphotosharing.groups;

/**
 * This class represents a Group list item, containing an icon and a title.
 * @author Edwin
 *
 */
public class Group {

	public String icon;		// Reference to the Drawable
	public String title;	// Title of the list item
	
	public Group() {
		super();
	}
	
	public Group(String icon, String title) {
		super();
		this.icon = icon;
		this.title = title;
	}
	
	
	
}
