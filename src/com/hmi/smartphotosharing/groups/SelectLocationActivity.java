package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.maps.MyItemizedOverlay;
import com.hmi.smartphotosharing.maps.MyMapView;
import com.hmi.smartphotosharing.maps.RectangleOverlay;
import com.hmi.smartphotosharing.util.Util;

public class SelectLocationActivity extends MapActivity {

    private MapController mc;
    private MyMapView mapView;
    
    private LocationManager mLocationManager;
    private Location gpsLocation, gps1, gps2;
    
    private static final int OVERLAY_RECT = 0;
    private static final int OVERLAY_MY_POS = 1;
    
    private CheckBox gpsUpdates;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create_location);
        
        gpsUpdates = (CheckBox) findViewById(R.id.toggle_gps_updates);
        
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        // MapView
        //------------------
        
        mapView = (MyMapView) findViewById(R.id.mapview);

        mapView.getOverlays().add(new RectangleOverlay(new GeoPoint(0,0),new GeoPoint(0,0)));
        mapView.getOverlays().add(new RectangleOverlay(new GeoPoint(0,0),new GeoPoint(0,0)));
        
        mc = mapView.getController();
                        
        gps1 = new Location(LocationManager.GPS_PROVIDER);
        gps2 = new Location(LocationManager.GPS_PROVIDER);

        // Listen for long-press on the map
        
        mapView.setOnLongpressListener(new MyMapView.OnLongpressListener() {
		    public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
			        runOnUiThread(new Runnable() {
				    public void run() {
			            Toast.makeText(getApplicationContext(), "Location updated", Toast.LENGTH_SHORT).show();
				        setRectangle(view);
				        
				    }

					private void setRectangle(MapView view) {
						List<Overlay> overlays = view.getOverlays();
						
				        GeoPoint topLeft = view.getProjection().fromPixels(5, 5);
				        GeoPoint bottomRight = view.getProjection().fromPixels(view.getWidth()-4, view.getHeight()-4);
				        RectangleOverlay rect = new RectangleOverlay(topLeft,bottomRight);
				        overlays.set(OVERLAY_RECT, rect);
						
				        gps1.setLatitude(topLeft.getLatitudeE6()/1E6);
				        gps1.setLongitude(topLeft.getLongitudeE6()/1E6);
				        
				        gps2.setLatitude(bottomRight.getLatitudeE6()/1E6);
				        gps2.setLongitude(bottomRight.getLongitudeE6()/1E6);
				        
				        Log.d("GPS", "New location: (" + gps1.getLatitude() + ", " + gps1.getLongitude() + ") - (" + gps2.getLatitude() + ", " + gps2.getLongitude() + ")");
					}
				});
			    }
		});
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
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        } else {        	
        	setupGps();
        }
        
        String s = getResources().getString(R.string.dialog_map);
        Util.createSimpleDialog(this,s);
        
    }
    
	@Override
    public void onResume() {
      super.onResume();
      
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
	
    private void moveToLocation(int lat, int lon) {
        GeoPoint point = new GeoPoint(lat,lon);
    	
    	List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.pushpin);
        MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable);

        OverlayItem overlayitem = new OverlayItem(point, null, null);
        itemizedoverlay.addOverlay(overlayitem);
        
        mapOverlays.set(OVERLAY_MY_POS,itemizedoverlay);
        
        if (gpsUpdates.isChecked()) 
        	mc.setCenter(point);
        
        //mc.setZoom(ZOOM_LEVEL); 
        mapView.invalidate();
		
	}
    

    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setupGps() { 
    	
        // Request updates from just the fine (gps) provider.
        gpsLocation = requestUpdatesFromProvider();
    	
        if (gpsLocation != null)
        	moveToLocation((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
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
        mLocationManager.requestLocationUpdates(networkProvider, 0, 0, listener);
        
        // GPS
        String gpsProvider = LocationManager.GPS_PROVIDER;
        
        if (mLocationManager.isProviderEnabled(gpsProvider)) {
            mLocationManager.requestLocationUpdates(gpsProvider, 0, 0, listener);
            location = mLocationManager.getLastKnownLocation(gpsProvider);
        }

        return location;
    }

    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        	boolean isBetter = Util.isBetterLocation(location, gpsLocation);
        	
        	Log.d("LocationCheck", "Provider: " + location.getProvider() + " -> better location ? " + Boolean.toString(isBetter));
        	// Check if we should update or not
        	if (isBetter) {
        		gpsLocation = location;
        	}

        	if (gpsLocation != null)            	
        		moveToLocation((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
        }

        @Override
        public void onProviderDisabled(String provider) {
        	//createGpsDisabledAlert();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
	
	
}
