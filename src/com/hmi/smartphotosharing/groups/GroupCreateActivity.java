package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hmi.smartphotosharing.MyItemizedOverlay;
import com.hmi.smartphotosharing.R;

public class GroupCreateActivity extends MapActivity {

    private MapController mc;
    private MapView mapView;
    
    private LocationManager mLocationManager;
    private Location gpsLocation;
    
    private static final int TEN_SECONDS = 10000;
    private static final int TEN_METERS = 10;
    private static final int ZOOM_LEVEL = 16;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create);
        
		Button createGroupBtn = (Button) findViewById(R.id.button1);	
		createGroupBtn.setOnClickListener(mCreateGroupOnClickListener);
		
        // MapView
        //------------------
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

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
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            // call enableLocationSettings()
            new EnableGpsDialogFragment().show(getFragmentManager(), "enableGpsDialog");
        } else {
        	
        	setup();
        	
        	// TODO check if getLastKnownLocation works
            if (gpsLocation == null) {
            	gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            	
            addMyLocationToMap((int)(gpsLocation.getLongitude()*1E6),(int)(gpsLocation.getLatitude()*1E6));
            
        }
    }
    
    private void addMyLocationToMap(int lng, int lat) {
    	List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable);

        GeoPoint point = new GeoPoint(lng,lat);
        OverlayItem overlayitem = new OverlayItem(point, null, null);
        itemizedoverlay.addOverlay(overlayitem);
        
        mapOverlays.add(itemizedoverlay);
        
        mc.setCenter(point);
        mc.setZoom(ZOOM_LEVEL); 
        mapView.invalidate();
		
	}

	@Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(listener);
    }
    
	Button.OnClickListener mCreateGroupOnClickListener = 
		new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK); // TODO : cancel button
				finish();
			}
	};	
	
    // Method to launch Settings
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }    
    
    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setup() { 
        gpsLocation = null;
        mLocationManager.removeUpdates(listener);
        
        // Request updates from just the fine (gps) provider.
        gpsLocation = requestUpdatesFromProvider(
                LocationManager.GPS_PROVIDER, R.string.not_support_gps);
        
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
            // A new location update is received.  Do something useful with it.  Update the UI with
            // the location update.
            gpsLocation = location;
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

    /**
     * Dialog to prompt users to enable GPS on the device.
     */
    public class EnableGpsDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.enable_gps)
                    .setMessage(R.string.enable_gps_dialog)
                    .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enableLocationSettings();
                        }
                    })
                    .create();
        }
    }
}
