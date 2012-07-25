package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.StringRepsonse;
import com.hmi.json.OnDownloadListener;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MapsListener;
import com.hmi.smartphotosharing.MyItemizedOverlay;
import com.hmi.smartphotosharing.MyMapView;
import com.hmi.smartphotosharing.R;

public class GroupCreateActivity extends MapActivity implements MapsListener, OnDownloadListener {

    private MapController mc;
    private MapView mapView;
    
    private LocationManager mLocationManager;
    private Location gpsLocation;
    
    private static final int TEN_SECONDS = 10000;
    private static final int TEN_METERS = 10;
    private static final int ZOOM_LEVEL = 16;
	private static int STATUS_OK = 200;
	private static int STATUS_FAIL = 500;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create);
		
        // MapView
        //------------------
        
        mapView = (MapView) findViewById(R.id.mapview);

        mc = mapView.getController();
                
        // Location Manager 
        //------------------
                  
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
		
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the GPS setting is currently enabled on the device.
        // This verification should be done during onStart() because the system calls this method
        // when the user returns to the activity, which ensures the desired location provider is
        // enabled each time the activity resumes from the stopped state.
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	createGpsDisabledAlert();
        } else {        	
        	setupGps();
        }
    }
    
    @Override
    public void onResume() {
      super.onResume();
      
      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                  TEN_SECONDS, TEN_METERS,
                                  listener);
    }    
    
    @Override
    public void onPause() {
      super.onPause();
      
      mLocationManager.removeUpdates(listener);
    }
    
	@Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(listener);
    }  
	
    private void addMyLocationToMap(int lat, int lon) {
    	List<Overlay> mapOverlays = mapView.getOverlays();
    	mapOverlays.clear();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable,this);

        GeoPoint point = new GeoPoint(lat,lon);
        OverlayItem overlayitem = new OverlayItem(point, null, null);
        itemizedoverlay.addOverlay(overlayitem);
        
        mapOverlays.add(itemizedoverlay);
        
        mc.setCenter(point);
        mc.setZoom(ZOOM_LEVEL); 
        mapView.invalidate();
		
	}
    
	public void onCreateClick(View v) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		Log.d("Session ID", hash);
		EditText nameView = (EditText) findViewById(R.id.group_create_name);
		String name = nameView.getText().toString();

		EditText descView = (EditText) findViewById(R.id.group_create_desc);
		String desc = descView.getText().toString();
		
		double lat = gpsLocation.getLatitude();
		double lon = gpsLocation.getLongitude();
		
    	// Get group info
		String createUrl = String.format(getResources().getString(R.string.groups_http_create),hash,name,desc,lat,lon,0,0);
		Log.d("SmarthPhotoSharing", "Create url: " + createUrl);
		new FetchJSON(this).execute(createUrl);
		
	}

    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setupGps() { 
    	
        // Request updates from just the fine (gps) provider.
        gpsLocation = requestUpdatesFromProvider(
                LocationManager.GPS_PROVIDER, R.string.not_support_gps);
    	
        if (gpsLocation != null)            	
    		addMyLocationToMap((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
    }
    
    /**
     * Method to register location updates with a desired location provider.  If the requested
     * provider is not available on the device, the app displays a Toast with a message referenced
     * by a resource id.
     *
     * @param provider Name of the requested provider.
     * @param errorResId Resource id for the string message to be displayed if the provider does
     *                   not exist on the device.
     * @return A previously returned {@link android.location.Location} from the requested provider,
     *         if exists.
     */
    private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
        Location location = null;
        if (mLocationManager.isProviderEnabled(provider)) {
            mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
            location = mLocationManager.getLastKnownLocation(provider);
        } else {
            Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
        }
        return location;
    }

    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            gpsLocation = location;

        	if (gpsLocation != null)            	
        		addMyLocationToMap((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
	
	private void createGpsDisabledAlert(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS is disabled! Would you like to enable it?")
		     .setCancelable(false)
		     .setPositiveButton("Enable GPS",
		          new DialogInterface.OnClickListener(){
		          public void onClick(DialogInterface dialog, int id){
		               showGpsOptions();
		          }
		     });
		     builder.setNegativeButton("Do nothing",
		          new DialogInterface.OnClickListener(){
		          public void onClick(DialogInterface dialog, int id){
		               dialog.cancel();
		          }
		     });
		AlertDialog alert = builder.create();
		alert.show();
	}  
	
	private void showGpsOptions(){
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}

	@Override
	public void updateGPS(double lat, double lon) {
		gpsLocation.setLatitude(lat);
		gpsLocation.setLongitude(lon);	
		Log.d(this.getClass().getSimpleName(), "Updated location: (" + lat + "; " + lon + ")");
	}

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		Log.i("Json parse", json);
		
		if (response.getStatus() == STATUS_OK) {
        	Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show();
    		setResult(RESULT_OK);
    		finish();
		} else if (response.getStatus() == STATUS_FAIL) {
        	Toast.makeText(this, "Group creation failed", Toast.LENGTH_SHORT).show();	
		} else {
        	Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();	
		}
		
	}

}
