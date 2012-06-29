package com.hmi.smartphotosharing;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hmi.smartphotosharing.camera.CameraFragment;
import com.hmi.smartphotosharing.groups.GroupsFragment;
/**
 * Main Activity class that controls the tabs.
 * @author Edwin
 *
 */
public class SmartPhotoSharing extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		// Popular tab
		Tab tab = actionBar
		.newTab()
		.setText("Profile")
		.setTabListener(new MyTabListener<ProfileFragment>(this, "profile",
				ProfileFragment.class));
		actionBar.addTab(tab);	

		// Profile tab
		tab = actionBar
		.newTab()
		.setText("Popular")
		.setTabListener(new MyTabListener<PopularFragment>(this, "popular",
				PopularFragment.class));
		actionBar.addTab(tab);	
		
		// Groups tab
		tab = actionBar
				.newTab()
				.setText("Groups")
				.setTabListener(new MyTabListener<GroupsFragment>(this, "groups",
						GroupsFragment.class));
		actionBar.addTab(tab);

	}
	
	/**
	 * This method sets the items of the menu form an inflated xml file.
	 * @param menu The menu that was created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	/**
	 * Method that handles the interaction with the menu buttons and the app icon.
	 * @param item The menu item that was interacted with.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home:
	            // App icon in action bar clicked; go home
	            Intent intent = new Intent(this, SmartPhotoSharing.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.camera:
	        	Util.replaceTab(this,CameraFragment.class);
		        return true;
	        case R.id.settings:
	        	Util.replaceTab(this,SettingsFragment.class);
		        return true;
		    case R.id.help:
		    	Util.replaceTab(this,HelpFragment.class);
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }
}