package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.util.Util;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * Class that adds a navigation bar at the bottom of the page.
 * In the subclass, you MUST call the setContentView() function first,
 * or else you will get NullPointerExceptions.
 * 
 * The layout that is used must include the @layout/navbar resource.
 * @author Edwin
 *
 */
public class NavBarActivity extends Activity {

	// ImageViews that function as buttons
	private ImageView camera,home,favourite,map,settings;
	 
	// Nav bar listeners
	private OnClickListener cameraListener = new NavBarListener(this, Util.ACTION_CAMERA);
	private OnClickListener homeListener = new NavBarListener(this, Util.ACTION_ARCHIVE);
	private OnClickListener favouriteListener = new NavBarListener(this, Util.ACTION_FAVOURITE);
	private OnClickListener settingsListener = new NavBarListener(this, Util.ACTION_SETTINGS);
	private OnClickListener mapListener = new NavBarListener(this, Util.ACTION_MAP);
   
	@Override
	public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);       
       
       // Set the nav bar listeners
       camera = (ImageView) findViewById(R.id.camera);
       camera.setOnClickListener(cameraListener);
       home = (ImageView) findViewById(R.id.home);
       home.setOnClickListener(homeListener);
       favourite = (ImageView) findViewById(R.id.favourite);
       favourite.setOnClickListener(favouriteListener);
       settings = (ImageView) findViewById(R.id.settings);
       settings.setOnClickListener(settingsListener);
       map = (ImageView) findViewById(R.id.map);
       map.setOnClickListener(mapListener);
	}
}
