package com.hmi.smartphotosharing.groups;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.util.Util;

public class SelectLocationActivity extends FragmentActivity implements OnMapLongClickListener, OnCameraChangeListener {

    private Location gps1, gps2;
    
    private GoogleMap map;
    private MarkerOptions markerOptions;
    
    private Polygon rect;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create_location);
                
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        setUpMapIfNeeded();

        gps1 = new Location(LocationManager.GPS_PROVIDER);
        gps2 = new Location(LocationManager.GPS_PROVIDER);
        
        map.setOnCameraChangeListener(this);
    }
    
    private void setUpMapIfNeeded() {
        
    	// Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                                .getMap();
            
            // Check if we were successful in obtaining the map.
            if (map != null) {
            	map.setMyLocationEnabled(true);
            	map.setOnMapLongClickListener(this);
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
        
        String s = getResources().getString(R.string.dialog_map);
        Util.createSimpleDialog(this,s);
        
    }
    
	@Override
    public void onResume() {
      super.onResume();
      
      setUpMapIfNeeded();
    }    
    
	@Override
	public void onBackPressed() {
	    this.setResult(RESULT_CANCELED);
	    finish();
	}

    public void onSendClick(View view) {
    	Intent data = new Intent();
    	    	
    	data.putExtra("lat1", gps1.getLatitude());
    	data.putExtra("lon1", gps1.getLongitude());

    	data.putExtra("lat2", gps2.getLatitude());
    	data.putExtra("lon2", gps2.getLongitude());
    	
    	this.setResult(RESULT_OK, data);
    	this.finish();
    }
    
    public void onSearchClick(View view) {

        // Getting reference to EditText to get the user input location
        EditText etLocation = (EditText) findViewById(R.id.et_location);

        // Getting user input location
        String location = etLocation.getText().toString();

        if(location!=null && !location.equals("")) {
            new GeocoderTask().execute(location);
        }
    }

	@Override
	public void onCameraChange(CameraPosition pos) {
		Log.d("SmarthPhotoSharing", "z: " + pos.zoom + " /p: " + pos.target.latitude + "," + pos.target.longitude);
		
	}   
	
	@Override
	public void onMapLongClick(LatLng point) {		
		
		// Get boundaries of the screen from the projection
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		
		// Find the other two corners
		LatLng northWest = new LatLng(bounds.northeast.latitude,bounds.southwest.longitude);
		LatLng southEast = new LatLng(bounds.southwest.latitude,bounds.northeast.longitude);
		
		// Remove previously drawn rect
		if (rect != null) {
			rect.remove();
		}
		
		// Add rectangle to the map
		rect = map.addPolygon(new PolygonOptions()
		    .add(bounds.northeast, northWest, bounds.southwest, southEast) // 4 corners, ccw
		    .strokeWidth(5)
		    .strokeColor(Color.GREEN)
		    .fillColor(0x6600ff00));
		
		// Set coordinates that will be passed back to the group creation activity
		gps1.setLatitude(northWest.latitude); gps1.setLongitude(northWest.longitude);
		gps2.setLatitude(southEast.latitude); gps2.setLongitude(southEast.longitude);
		
		// Show a toast
        Toast.makeText(getApplicationContext(), "Location updated", Toast.LENGTH_SHORT).show();
	}
	
	private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

	        @Override
	        protected List<Address> doInBackground(String... locationName) {
	            // Creating an instance of Geocoder class
	            Geocoder geocoder = new Geocoder(getBaseContext());
	            List<Address> addresses = null;

	            try {
	                // Getting a maximum of 3 Address that matches the input text
	                addresses = geocoder.getFromLocationName(locationName[0], 3);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            return addresses;
	        }

	        @Override
	        protected void onPostExecute(List<Address> addresses) {

	            if(addresses==null || addresses.size()==0){
	                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
	            }

	            // Clears all the existing markers on the map
	            map.clear();

	            // Adding Markers on Google Map for each matching address
	            for(int i=0;i<addresses.size();i++){

	                Address address = (Address) addresses.get(i);

	                // Creating an instance of GeoPoint, to display in Google Map
	                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

	                String addressText = String.format("%s, %s",
	                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                address.getCountryName());

	                markerOptions = new MarkerOptions();
	                markerOptions.position(latLng);
	                markerOptions.title(addressText);

	                map.addMarker(markerOptions);

	                // Locate the first location
	                if(i==0)
	                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
	            }
	        }
	    }
}
