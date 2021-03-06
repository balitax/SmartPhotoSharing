package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.feedback.FeedbackActivity;
import com.hmi.smartphotosharing.friends.FriendsActivity;
import com.hmi.smartphotosharing.util.Util;

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
	private ImageView camera,home,favourite,friends, map;
	 
	// Nav bar listeners
	private OnClickListener cameraListener = new NavBarListener(this, Util.ACTION_CAMERA);
	private OnClickListener homeListener = new NavBarListener(this, Util.ACTION_ARCHIVE);
	private OnClickListener favouriteListener = new NavBarListener(this, Util.ACTION_FAVOURITE);
	private OnClickListener friendsListener = new NavBarListener(this, Util.ACTION_FRIENDS);
	private OnClickListener mapListener = new NavBarListener(this, Util.ACTION_MAP);
   
	@Override
	public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);       
       
       TextView title = (TextView) findViewById(R.id.header_title);
       
       if (title != null) 
    	   title.setText(getTitle());
       
       // Set the nav bar listeners
       camera = (ImageView) findViewById(R.id.camera);
       camera.setOnClickListener(cameraListener);
       home = (ImageView) findViewById(R.id.home);
       home.setOnClickListener(homeListener);
       favourite = (ImageView) findViewById(R.id.favourite);
       favourite.setOnClickListener(favouriteListener);
       friends = (ImageView) findViewById(R.id.friends);
       friends.setOnClickListener(friendsListener);

       map = (ImageView) findViewById(R.id.local);
       map.setOnClickListener(mapListener);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		return handleOptionItemSelected(item,this)|super.onOptionsItemSelected(item);
	}

	public static boolean handleOptionItemSelected(MenuItem item, Activity a) {
		Intent intent;
	    switch (item.getItemId()) {
	        case R.id.settings:
	            intent = new Intent(a, ProfileActivity.class);
	            a.startActivity(intent);
	            return true;
	        case R.id.help:
	    		Uri uri = Uri.parse(Util.HELP_URL);
	            intent = new Intent(Intent.ACTION_VIEW, uri);
	            a.startActivity(intent);
	        case R.id.feedback:
	            intent = new Intent(a, FeedbackActivity.class);
	            a.startActivity(intent);
	    		return true;
	        default:
	            return false;
	    }
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	public void onClickMenu (View view) {
		openOptionsMenu();	
	}
	
	public void onClickBack (View view) {
		finish();
	}
}
