package com.hmi.smartphotosharing.groups;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.util.HelpDialog;
import com.hmi.smartphotosharing.util.Util;

public class SelectLocationActivity extends FragmentActivity implements OnMapLongClickListener, OnMarkerDragListener {

	private static float ZOOM_OUT = -0.5f;
	
    private Location gps1, gps2;
    
    private GoogleMap map;
    private MarkerOptions markerOptions;
    
    private Marker singleMarker, m1, m2, m3, m4;
    private Polygon rect;
    
    private boolean singleLocation;
    
    public static final String SINGLE_LOCATION = "SINGLE_LOCATION";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create_location);
                
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        setUpMapIfNeeded();

        gps1 = new Location(LocationManager.GPS_PROVIDER);
        gps2 = new Location(LocationManager.GPS_PROVIDER);
        
        Intent intent = getIntent();
        
        singleLocation = intent.getBooleanExtra(SINGLE_LOCATION, false);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
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
            	map.setOnMarkerDragListener(this);
            	map.getUiSettings().setTiltGesturesEnabled(false);
            	map.getUiSettings().setRotateGesturesEnabled(false);
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
        
        SharedPreferences settings = getSharedPreferences(HelpDialog.DIALOG_PREFS, MODE_PRIVATE);
        boolean hide = settings.getBoolean(HelpDialog.DIALOG_MAP, false);
        String s = getResources().getString(R.string.dialog_map);
        if (!hide)
        	Util.createSimpleDialog(this,s,HelpDialog.DIALOG_MAP);

        EasyTracker.getInstance().activityStart(this);
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
    	
		if (gps1.getLatitude() == 0 && gps1.getLongitude() == 0 && gps2.getLatitude() == 0 && gps2.getLongitude() == 0) {
			Util.createSimpleDialog(this, getResources().getString(R.string.dialog_location_select));
		} else {
			Intent data = new Intent();
			
	    	data.putExtra("lat1", gps1.getLatitude());
	    	data.putExtra("lon1", gps1.getLongitude());
	
	    	// Send the other coords too if an area is selected
	    	if (!singleLocation) {
	    		data.putExtra("lat2", gps2.getLatitude());
	    		data.putExtra("lon2", gps2.getLongitude());
	    	}
	    	
	    	this.setResult(RESULT_OK, data);
	    	this.finish();
		}
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
	public void onMapLongClick(LatLng point) {		
		
		// If a single location is selected, put a marker
		if (singleLocation) {
			if (singleMarker != null) singleMarker.remove();
			singleMarker = map.addMarker(new MarkerOptions().position(point));
			gps1.setLatitude(point.latitude); gps1.setLongitude(point.longitude);
			
		} 
		
		// If an area is selected, draw the rectangle
		else {
			// Get boundaries of the screen from the projection
			VisibleRegion bounds = map.getProjection().getVisibleRegion();
				
			drawRect(bounds.farLeft, bounds.farRight, bounds.nearRight, bounds.nearLeft);
			map.animateCamera(CameraUpdateFactory.zoomBy(ZOOM_OUT));
		}
		
		// Show a toast
        Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
	}
	
	private void drawRect(LatLng c1, LatLng c2, LatLng c3, LatLng c4) {

		// Remove previously drawn rect
		if (rect != null) rect.remove();
		
		// Add rectangle to the map
		rect = map.addPolygon(new PolygonOptions()
		    .add(c1,c2,c3,c4) // 4 corners, ccw
		    .strokeWidth(5)
		    .strokeColor(Color.GREEN)
		    .fillColor(0x6600ff00));
		
		// Set coordinates that will be passed back to the group creation activity
		gps1.setLatitude(c1.latitude); gps1.setLongitude(c1.longitude);
		gps2.setLatitude(c3.latitude); gps2.setLongitude(c3.longitude);

		// Top left marker
		if (m1 != null) m1.remove();
		m1 = map.addMarker(new MarkerOptions()
			    	.position(c1)
			    	.draggable(true)
			    	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		
		// Top right marker
		if (m2 != null) m2.remove();
		m2 = map.addMarker(new MarkerOptions()
			    	.position(c2)
			    	.draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));		
		
		// Bottom right marker
		if (m3 != null) m3.remove();
		m3 = map.addMarker(new MarkerOptions()
		    	.position(c3)
		    	.draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

		// Bottom right marker
		if (m4 != null) m4.remove();
		m4 = map.addMarker(new MarkerOptions()
		    	.position(c4)
		    	.draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
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

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		LatLng p1 = marker.getPosition();
		LatLng p2 = null;
		if (marker.equals(m1)) {
			p2 = m3.getPosition();
		} else if (marker.equals(m2)) {
			p2 = m4.getPosition();
		} else if (marker.equals(m3)) {
			p2 = m1.getPosition();
		} else if (marker.equals(m4)) {
			p2 = m2.getPosition();
		}
		
		// Make sure the top left corner is the maximum latitude and minimum longitude, etc
		drawRect(new LatLng(Math.max(p1.latitude, p2.latitude), Math.min(p1.longitude, p2.longitude)), 
				new LatLng(Math.max(p1.latitude, p2.latitude), Math.max(p1.longitude, p2.longitude)), 
				new LatLng(Math.min(p1.latitude, p2.latitude), Math.max(p1.longitude, p2.longitude)), 
				new LatLng(Math.min(p1.latitude, p2.latitude), Math.min(p1.longitude, p2.longitude)));
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}
}
