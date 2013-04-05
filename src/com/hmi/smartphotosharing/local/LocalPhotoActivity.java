package com.hmi.smartphotosharing.local;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.NavBarActivity;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class LocalPhotoActivity extends NavBarActivity implements OnDownloadListener {

	private LocationManager mLocationManager;
	private Location gpsLocation;

	private static final int TEN_SECONDS = 10 * 1000;
	private GridView gridView;
	
	// Static distance of about 1 km used for calculating a bounding box around a GPS coordinate
	private static final double DISTANCE_LAT = 5/1E3;
	
	private ImageLoader imageLoader;
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        setContentView(R.layout.local_gridview);   
        super.onCreate(savedInstanceState);
        
        gridView = (GridView) findViewById(R.id.gridview);
        TextView emptyGrid = (TextView) findViewById(R.id.empty_list_view);
        gridView.setEmptyView(emptyGrid);
        
        // ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        // Show selection in nav bar
        ImageView settings = (ImageView) findViewById(R.id.local);
        Util.setSelectedBackground(getApplicationContext(), settings);
        
		// GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        }
        setupGps();
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
	
	@Override
    public void onResume() {
      super.onResume();

      setupGps();
    }

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	public void onClickListMode(View view) {
	}
	
	public void onClickMapMode(View view) {
    	Intent intent = new Intent(this,MapActivity.class);
    	startActivity(intent);
	}

    private class MyOnItemClickListener implements OnItemClickListener {
		private Context c;
		
		public MyOnItemClickListener(Context c) {
			this.c = c;
		}
		// Handle clicks
		@Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent intent = new Intent(c, PhotoDetailActivity.class);
	    	intent.putExtra("id", id);
	    	startActivity(intent);
	    }
    }
    
	private void loadData() {
		
		/*LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
		
		// Find the other two corners
		LatLng nw = new LatLng(bounds.northeast.latitude,bounds.southwest.longitude);
		LatLng se = new LatLng(bounds.southwest.latitude,bounds.northeast.longitude);
		*/
		
		// Manually calculate a bounding box around the current location
		double distLon = distanceLon(gpsLocation.getLatitude());

		double lat1 = gpsLocation.getLatitude() + DISTANCE_LAT;
		double lon1 = gpsLocation.getLongitude() + distLon;

		double lat2 = gpsLocation.getLatitude() - DISTANCE_LAT;
		double lon2 = gpsLocation.getLongitude() - distLon;
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String detailUrl = Util.getUrl(this,R.string.local_http);
		//Log.d("Maps url", detailUrl);
		//new FetchJSON(this).execute(detailUrl);
		Log.i("loadData", lat1 + "," + lon1 + " / " + lat2 + "," + lon2 );
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
        	
			map.put("sid",new StringBody(hash));
			
			map.put("lat1", new StringBody(Double.toString(lat1)));
			map.put("lon1", new StringBody(Double.toString(lon1)));
			
			map.put("lat2", new StringBody(Double.toString(lat2)));
			map.put("lon2", new StringBody(Double.toString(lon2)));
			
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
			
			parsePhoto(json);
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void parsePhoto(String result) {
		Gson gson = new Gson();
		PhotoListResponse list = gson.fromJson(result, PhotoListResponse.class);
		
		List<Photo> photo_list = list.getObject();
		
		// JSON will return null if there are no photos in this group
		if (photo_list == null)
			photo_list = new ArrayList<Photo>();
		
		gridView.setAdapter(
			new MyImageAdapter(
					this, 
					photo_list
		));

        gridView.setOnItemClickListener(new MyOnItemClickListener(this)); 
		
	}
    private void setupGps() { 
        // Request updates from just the fine (gps) provider.
        gpsLocation = requestUpdatesFromProvider();
    	 
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
    private Location requestUpdatesFromProvider() {
        Location location = null;
        
        // Network
        String networkProvider = LocationManager.NETWORK_PROVIDER;

        if (mLocationManager.isProviderEnabled(networkProvider)) {
        	mLocationManager.requestLocationUpdates(networkProvider, TEN_SECONDS, 0, listener);
        }
        
        // GPS
        String gpsProvider = LocationManager.GPS_PROVIDER;
        
        if (mLocationManager.isProviderEnabled(gpsProvider)) {
            mLocationManager.requestLocationUpdates(gpsProvider, TEN_SECONDS, 0, listener);
            location = mLocationManager.getLastKnownLocation(gpsProvider);
        }
        
        return location;
    }

    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        	boolean isBetter = Util.isBetterLocation(location, gpsLocation);
        	
        	// Check if we should update or not
        	if (isBetter) {
        		gpsLocation = location;
            	loadData();
        	}

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
	
    public double distanceLon(double lat) {
    	return DISTANCE_LAT / Math.cos(lat);
    }
}
