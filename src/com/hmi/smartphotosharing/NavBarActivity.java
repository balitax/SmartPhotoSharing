package com.hmi.smartphotosharing;

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
	private ImageView camera,archive,settings;
	 
	// Nav bar listeners
	private OnClickListener cameraListener = new NavBarListener(this, Util.ACTION_CAMERA);
	private OnClickListener archiveListener = new NavBarListener(this, Util.ACTION_ARCHIVE);
	private OnClickListener settingsListener = new NavBarListener(this, Util.ACTION_SETTINGS);
   
	@Override
	public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);       
       
       // Set the nav bar listeners
       camera = (ImageView) findViewById(R.id.camera);
       camera.setOnClickListener(cameraListener);
       archive = (ImageView) findViewById(R.id.archive);
       archive.setOnClickListener(archiveListener);
       settings = (ImageView) findViewById(R.id.settings);
       settings.setOnClickListener(settingsListener);
              
	}
}
