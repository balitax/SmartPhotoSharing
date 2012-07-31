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
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.Util;
import com.hmi.smartphotosharing.maps.MyItemizedOverlay;
import com.hmi.smartphotosharing.maps.RectangleOverlay;

public class ShowLocationActivity extends MapActivity{

    private MapView mapView;
    
    private LocationManager mLocationManager;
    private Location gpsLocation;
    
    private static final int OVERLAY_RECT = 0;
    private static final int OVERLAY_MY_POS = 1;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.show_location);
                
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        // MapView
        //------------------
        
        mapView = (MapView) findViewById(R.id.mapview);

        // Two dummy overlays
        mapView.getOverlays().add(new RectangleOverlay(new GeoPoint(0,0),new GeoPoint(0,0)));
        mapView.getOverlays().add(new RectangleOverlay(new GeoPoint(0,0),new GeoPoint(0,0)));
                    
        // Get the coordinates from the intent
        Intent intent = getIntent();
        Double lat1 = Double.parseDouble(intent.getStringExtra("lat1"));
        Double lat2 = Double.parseDouble(intent.getStringExtra("lat2"));
        Double lon1 = Double.parseDouble(intent.getStringExtra("lon1"));
        Double lon2 = Double.parseDouble(intent.getStringExtra("lon2"));
        
        // Add the rectangle showing the location
        List<Overlay> overlays = mapView.getOverlays();
        GeoPoint topLeft = new GeoPoint((int)(lat1*1E6),(int)(lon1*1E6));
        GeoPoint bottomRight = new GeoPoint((int)(lat2*1E6),(int)(lon2*1E6));
        RectangleOverlay rect = new RectangleOverlay(topLeft,bottomRight);
        overlays.set(OVERLAY_RECT, rect);
        mapView.invalidate();
        
        // Move to the center of the Group location
        int centerLat = (int) ((topLeft.getLatitudeE6() + bottomRight.getLatitudeE6())/2);
        int centerLong = (int) ((topLeft.getLongitudeE6() + bottomRight.getLongitudeE6())/2);
        
        MapController mapController = mapView.getController();
        mapController.zoomToSpan(
                (topLeft.getLatitudeE6() - bottomRight.getLatitudeE6()),
                (bottomRight.getLongitudeE6() - topLeft.getLongitudeE6()));

        mapController.setCenter(new GeoPoint(centerLat,centerLong));
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
	  

    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setupGps() { 
    	
        // Request updates from just the fine (gps) provider.
        gpsLocation = requestUpdatesFromProvider();

        if (gpsLocation != null)
        	showMyLocation((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
    }
    
    private void showMyLocation(int lat, int lon) {
        GeoPoint point = new GeoPoint(lat,lon);
    	
    	List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.pushpin);
        MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable);

        OverlayItem overlayitem = new OverlayItem(point, null, null);
        itemizedoverlay.addOverlay(overlayitem);
        
        mapOverlays.set(OVERLAY_MY_POS,itemizedoverlay);
                
        //mc.setZoom(ZOOM_LEVEL); 
        mapView.invalidate();
		
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
            	showMyLocation((int)(gpsLocation.getLatitude()*1E6),(int)(gpsLocation.getLongitude()*1E6));   
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
