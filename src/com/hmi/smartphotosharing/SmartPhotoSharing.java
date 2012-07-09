package com.hmi.smartphotosharing;


import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hmi.smartphotosharing.camera.CameraFragment;
import com.hmi.smartphotosharing.groups.GroupsFragment;
/**
 * Main Activity class that controls the tabs.
 * @author Edwin
 *
 */
public class SmartPhotoSharing extends SherlockFragmentActivity implements OnLoadDataListener {

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
	public static final int HONEYCOMB = 11;
	
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;
    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();
    // The user's current network preference setting.
    public static String sPref = null;
    public DrawableManager dm;
    
	public void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT < HONEYCOMB){
	        this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		enableHttpResponseCache();
		
		// Setup action bar for tabs
		ActionBar actionBar = getSupportActionBar();
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

		// Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
	}
		
	@Override 
    public void onDestroy() {
        super.onDestroy();
        // Unregisters BroadcastReceiver when app is destroyed.
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }
    
    // Refreshes the display if the network connection and the
    // pref settings allow it.
    
    @Override
    public void onStart () {
        super.onStart();  
        
        dm = new DrawableManager(this);
        
        updatePrefs();

        updateConnectedFlags(); 
    }
    
    // Retrieves a string value for the preferences. The second parameter
    // is the default value to use if a preference value is not found.
	public void updatePrefs() {
        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sPref = sharedPrefs.getString("listPref", "Wi-Fi");
		
	}
	
    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly. 
    public void updateConnectedFlags() {
        ConnectivityManager connMgr = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }  
    }
          
	/**
	 * This method sets the items of the menu form an inflated xml file.
	 * @param menu The menu that was created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
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
        /*
        	case android.R.id.home:
	            // App icon in action bar clicked; go home
	            Intent intent = new Intent(this, SmartPhotoSharing.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;*/
	        case R.id.camera:
	        	Util.replaceTab(this,CameraFragment.class);
		        return true;
	        case R.id.settings:
	            Intent i = new Intent(this, SettingsActivity.class);
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(i);
		        return true;
		    case R.id.help:
		    	Util.replaceTab(this,HelpFragment.class);
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }
	
	private void enableHttpResponseCache() {
		try {
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			File httpCacheDir = new File(getCacheDir(), "http");
			Class.forName("android.net.http.HttpResponseCache")
				.getMethod("install", File.class, long.class)
				.invoke(null, httpCacheDir, httpCacheSize);
			} catch (Exception httpResponseCacheNotAvailable) {
				Log.d(this.getClass().getName(), "HTTP response cache is unavailable.");
			}
	}
	
	public class NetworkReceiver extends BroadcastReceiver {   
	    
		@Override
		public void onReceive(Context context, Intent intent) {
		    ConnectivityManager conn =  (ConnectivityManager)
		        context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
		       
		    // Checks the user prefs and the network connection. Based on the result, decides whether
		    // to refresh the display or keep the current display.
		    // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
		    if (WIFI.equals(sPref) && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
		        // If device has its Wi-Fi connection, sets refreshDisplay
		        // to true. This causes the display to be refreshed when the user
		        // returns to the app.
		        refreshDisplay = true;
		        Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();

		    // If the setting is ANY network and there is a network connection
		    // (which by process of elimination would be mobile), sets refreshDisplay to true.
		    } else if (ANY.equals(sPref) && networkInfo != null) {
		        refreshDisplay = true;
		                 
		    // Otherwise, the app can't download content--either because there is no network
		    // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there 
		    // is no Wi-Fi connection.
		    // Sets refreshDisplay to false.
		    } else {
		        refreshDisplay = false;
		        Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
		    }
		}
	}

	@Override
	public void onLoadData() {

        updatePrefs();
        updateConnectedFlags(); 
		
	}
	
	@Override
    public boolean canLoad() {
    	return (sPref.equals(ANY) && (wifiConnected || mobileConnected))
        			|| (sPref.equals(WIFI) && wifiConnected);
    }

	@Override
	public DrawableManager getDrawableManager() {
		return dm;
	}

}