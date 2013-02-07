package com.hmi.smartphotosharing.groups;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.util.Util;

public class ShowLocationActivity extends FragmentActivity {

    private GoogleMap map;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.show_location);
                
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        setUpMapIfNeeded();
    }
    

    private void setUpMapIfNeeded() {
        
    	// Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                                .getMap();
            
            // Check if we were successful in obtaining the map.
            if (map != null) {
            	map.setMyLocationEnabled(true);
            	
                Intent intent = getIntent();
                Double lat1 = Double.parseDouble(intent.getStringExtra("lat1"));
                Double lat2 = Double.parseDouble(intent.getStringExtra("lat2"));
                Double lon1 = Double.parseDouble(intent.getStringExtra("lon1"));
                Double lon2 = Double.parseDouble(intent.getStringExtra("lon2"));
                
                LatLng ne = new LatLng(lat1,lon2);
                LatLng nw = new LatLng(lat1,lon1);
                LatLng sw = new LatLng(lat2,lon1);
                LatLng se = new LatLng(lat2,lon2);
                
                final LatLngBounds bounds = new LatLngBounds(sw, ne);
                
                map.addPolygon(new PolygonOptions()
	    		    .add(ne, nw, sw, se) // 4 corners, ccw
	    		    .strokeWidth(5)
	    		    .strokeColor(Color.GREEN)
	    		    .fillColor(0x6600ff00));
                
                // One time fix to set the camera when the map is done loading
                map.setOnCameraChangeListener(new OnCameraChangeListener() {

                    @Override
                    public void onCameraChange(CameraPosition arg0) {
                        // Move camera.
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                        // Remove listener to prevent position reset on camera move.
                        map.setOnCameraChangeListener(null);
                    }
                });
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

 
	
}
