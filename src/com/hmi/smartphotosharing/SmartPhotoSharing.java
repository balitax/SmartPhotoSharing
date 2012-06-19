package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
/**
 * Main Activity class that controls the tabs.
 * @author Edwin
 *
 */
public class SmartPhotoSharing extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Use the layout that is defined in main.xml
	    setContentView(R.layout.main);

	    // Set-up of tabs
	    //----------------------------------------------------------------------
	    
	    Resources res = getResources(); 	// Resource object to get Drawables
	    TabHost tabHost = getTabHost();  	// The activity TabHost
	    TabHost.TabSpec spec;  				// Resusable TabSpec for each tab
	    Intent intent;  					// Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, PopularActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("popular").setIndicator("Popular",
	                      res.getDrawable(R.drawable.ic_tab_popular))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Groups tab
	    intent = new Intent().setClass(this, GroupsActivity.class);
	    spec = tabHost.newTabSpec("groups").setIndicator("Groups",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Camera tab
	    intent = new Intent().setClass(this, CameraActivity.class);
	    spec = tabHost.newTabSpec("camera").setIndicator("Camera",
	                      res.getDrawable(R.drawable.ic_tab_camera))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    // Profile tab
	    intent = new Intent().setClass(this, ProfileActivity.class);
	    spec = tabHost.newTabSpec("profile").setIndicator("Profile",
                res.getDrawable(R.drawable.ic_tab_artists))
            .setContent(intent);
	    tabHost.addTab(spec);
	    
	    // Settings tab
	    intent = new Intent().setClass(this, SettingsActivity.class);
	    spec = tabHost.newTabSpec("settings").setIndicator("Settings",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Set the tab to page 1
	    tabHost.setCurrentTab(1);
	}
	
}