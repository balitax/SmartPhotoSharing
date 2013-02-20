package com.hmi.smartphotosharing.local;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarFragmentActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SinglePhotoDetail;
import com.hmi.smartphotosharing.R.id;
import com.hmi.smartphotosharing.R.layout;
import com.hmi.smartphotosharing.R.string;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MapActivity extends NavBarFragmentActivity implements LocationListener, LocationSource, OnCameraChangeListener, OnDownloadListener {

    private GoogleMap googleMap;
    private Marker lastClicked, lastInfoWindowClicked;
    
    private long lastChange;
    private LatLng lastPos;
    
    private static long MAP_TIME_THRESHOLD = 5000;
    private static int MAP_ZOOM_THRESHOLD = 15;
    private static int MAP_DISTANCE_THRESHOLD = 500;

	private OnLocationChangedListener mListener;
	private LocationManager mLocationManager;

	private ImageLoader imageLoader;
	
	// The first time the camera centers on the user's location it should zoom to street level
	// Afterwards, it should only center on the position without zooming
	private boolean firstZoomCamera;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState,R.layout.local_map);
        

        // Show selection in nav bar
        ImageView settings = (ImageView) findViewById(R.id.local);
        Util.setSelectedBackground(getApplicationContext(), settings);
        
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
            	
            	firstZoomCamera = true;
            	
            	googleMap.setMyLocationEnabled(true);
                // One time fix to set the camera when the map is done loading
                googleMap.setOnCameraChangeListener(this);
                
                // Set the infowindow adapter
                googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter(imageLoader, getLayoutInflater()));
                googleMap.setOnInfoWindowClickListener(new MyInfoWindowClickListener(this));
                googleMap.setOnMarkerClickListener(new MyMarkerClickListener());
                
                // Set the source of location updates to this activity
                googleMap.setLocationSource(this);
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
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if(mLocationManager != null) {
        	boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        	boolean networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        	if(gpsEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
            	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
            } else {
            	Util.createGpsDisabledAlert(this);
            }
        }

        
                
    }
    
    @Override
    public void onPause() {
        if(mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }
    
	@Override
    public void onResume() {
      super.onResume();
      
      setUpMapIfNeeded();
      if(mLocationManager != null) {
          googleMap.setMyLocationEnabled(true);
      }
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
	
	private class MyInfoWindowClickListener implements OnInfoWindowClickListener {

		private Context c;
		
		public MyInfoWindowClickListener(Context c) {
			this.c = c;
		}
		@Override
		public void onInfoWindowClick(Marker marker) {
			// If the user clicks the InfoWindow for the second time, go to the photo detail page
			if (lastInfoWindowClicked != null && lastInfoWindowClicked.equals(marker)) {
				lastInfoWindowClicked = null;

		    	Intent intent = new Intent(c,SinglePhotoDetail.class);
		    	intent.putExtra(SinglePhotoDetail.KEY_ID, Long.parseLong(marker.getSnippet()));
		    	startActivity(intent);
			} 
			
			// First time, so refresh the InfoWindow
			else {
				lastInfoWindowClicked = marker;
				marker.showInfoWindow();
			}
			
		}
	}
	
	private class MyMarkerClickListener implements OnMarkerClickListener {

		@Override
		public boolean onMarkerClick(Marker marker) {
			// Close the InfoWindow if the user clicked it before
			if (lastClicked != null && lastClicked.equals(marker)) {
				lastClicked = null;
				marker.hideInfoWindow();
				return true;
			} else {
				lastClicked = marker;
				return false;
			}
		}
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
	                	.title(p.thumb)
	                	.snippet(p.iid);

	                googleMap.addMarker(markerOptions);
				}
			}
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	// Methods to make this activity the source of location updates for the google map
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;		
	}


	@Override
	public void deactivate() {
		mListener = null;		
	}


	@Override
	public void onLocationChanged(Location location) 
	{
	    if(mListener != null) {
	        mListener.onLocationChanged( location );

	        //Move the camera to the user's location on location update
	        // Only zoom if it is the first update after starting the activity
	        if (firstZoomCamera) {
	        	
	        	firstZoomCamera = false;
	        	googleMap.animateCamera(CameraUpdateFactory
	        		.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_THRESHOLD));
	        } else {
	        	googleMap.animateCamera(CameraUpdateFactory
	        		.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
	        }
	    }
	}


	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	
}
