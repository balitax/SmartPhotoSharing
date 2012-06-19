package com.hmi.smartphotosharing;

/**
 * This class represents a Group list item, containing an icon and a title.
 * @author Edwin
 *
 */
public class Group {

	public int icon;		// Reference to the Drawable
	public String title;	// Title of the list item
	
	public Group() {
		super();
	}
	
	public Group(int icon, String title) {
		super();
		this.icon = icon;
		this.title = title;
	}
	
	
	
}
