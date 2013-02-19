package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.groups.SelectFriendsActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.maps.MyInfoWindowAdapter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MapActivity extends NavBarFragmentActivity implements OnCameraChangeListener, OnDownloadListener {

    private GoogleMap googleMap;
    private long lastChange;
    private LatLng lastPos;
    
    private static long MAP_TIME_THRESHOLD = 5000;
    private static int MAP_ZOOM_THRESHOLD = 15;
    private static int MAP_DISTANCE_THRESHOLD = 500;

	private ImageLoader imageLoader;
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState,R.layout.map);
        

        // ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        setUpMapIfNeeded();
    }
    

    private void setUpMapIfNeeded() {
        
    	// Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googlemap))
                                .getMap();
            
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
            	googleMap.setMyLocationEnabled(true);
                // One time fix to set the camera when the map is done loading
                googleMap.setOnCameraChangeListener(this);
                
                // Set the infowindow adapter
                googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter(imageLoader, getLayoutInflater()));
                
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the GPS setting is currently enabled on the device.
        // This verification should be done during onStart() because the system calls this method
        // when the user returns to the activity, which ensures the desired location provider is
        // enabled each time the activity resumes from the stopped state.
        LocationManager mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        }
                
    }
    
	@Override
    public void onResume() {
      super.onResume();
      
      setUpMapIfNeeded();
    }


	@Override
	public void onCameraChange(CameraPosition pos) {

        long now = System.currentTimeMillis(); 
        if (lastChange == 0 || lastPos == null) {
        	lastChange = now;
        	lastPos = pos.target;
        } else {
        	
        	// If it is at least X milliseconds since the last change and
        	// the zoom level is greater than Y, request new photo locations
        	
        	if (now - lastChange >= MAP_TIME_THRESHOLD && pos.zoom >= MAP_ZOOM_THRESHOLD){
        		
        		// Check if the difference in distance from last camera pos is greater than X meters
        		lastChange = now;
        		lastPos = pos.target;
        		Log.d("SmarthPhotoSharing", "z: " + pos.zoom + " / d: " );
        		
        		loadData();
        	}
        }		
	}
	public void onClickListMode(View view) {
    	Intent intent = new Intent(this,LocalPhotoActivity.class);
    	startActivity(intent);
	}
	
	public void onClickMapMode(View view) {
		// do nothing
	}	
	
	private void loadData() {
		
		LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
		
		// Find the other two corners
		LatLng nw = new LatLng(bounds.northeast.latitude,bounds.southwest.longitude);
		LatLng se = new LatLng(bounds.southwest.latitude,bounds.northeast.longitude);
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String detailUrl = Util.getUrl(this,R.string.map_http);
		//Log.d("Maps url", detailUrl);
		//new FetchJSON(this).execute(detailUrl);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
        	
			map.put("sid",new StringBody(hash));
			
			map.put("lat1", new StringBody(Double.toString(nw.latitude)));
			map.put("lon1", new StringBody(Double.toString(nw.longitude)));
			
			map.put("lat2", new StringBody(Double.toString(se.latitude)));
			map.put("lon2", new StringBody(Double.toString(se.longitude)));
			
		} catch (UnsupportedEncodingException e) {
			Log.e("Map activity", e.getMessage());
		}
        
        PostData pr = new PostData(detailUrl,map);
        new PostRequest(this).execute(pr);
		        
	}
	
	@Override
	public void parseJson(String json, int code) {

		Gson gson = new Gson();
		PhotoListResponse response = gson.fromJson(json, PhotoListResponse.class);
		Log.d("JSON parse", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
			// Put markers on the map
			List<Photo> list = response.getObject();
			
			if (list != null && list.size() > 0) {
				
				for(Photo p : list) {
					Double lat = Double.parseDouble(p.latitude);
					Double lon = Double.parseDouble(p.longitude);
					
					MarkerOptions markerOptions = new MarkerOptions()
	                	.position(new LatLng(lat,lon))
	                	.title(p.thumb);

	                googleMap.addMarker(markerOptions);
				}
			}
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

 
	
}
