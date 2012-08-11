package com.hmi.smartphotosharing;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.camera.CameraActivity;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.groups.SelectGroupActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringRepsonse;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SharePhotoActivity extends Activity implements OnDownloadListener {
	
	private static String TAG = "SHARE";
	
	private Button btnSelectGroup;
	private EditText comment;
	private ImageView imageView;
	//private String imgPath;
	private LocationManager locationManager;

	private static final int CODE_SELECT_GROUP = 2;
	private static final int TAKE_PICTURE = 5;
	private static final int CODE_UPLOAD = 3;
	private static final int CREATE_GROUP = 4;
	
	private static final int TEN_SECONDS = 10 * 1000;
	private static final float MIN_DISTANCE = 25;
		
	private String newGroupName;

    private LocationManager mLocationManager;
	private Location gpsLocation;
	
	private ImageLoader imageLoader;
	
	private Uri fileUri;
	private int rotation;
	
	private ProgressDialog pd;
	
	private long gid;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo); 
				
		// Populate the Spinner
		btnSelectGroup = (Button) findViewById(R.id.groups_spinner);
		comment = (EditText) findViewById(R.id.edit_message);
        imageView = (ImageView) findViewById(R.id.image1);

		// GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        }
        
        if (fileUri == null) {
		    Intent cameraIntent = new Intent(this,CameraActivity.class);
		    startActivityForResult(cameraIntent, TAKE_PICTURE); 
        }
	    
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        setupGps();
        //loadData();
	}
	
	public void onClickSelectGroup(View view) {
		Intent intent = new Intent(this,SelectGroupActivity.class);
		startActivityForResult(intent, CODE_SELECT_GROUP);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("gid", gid);
		if (fileUri != null) 
			savedInstanceState.putString("uri", fileUri.getPath());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String uri = savedInstanceState.getString("uri");
		gid = savedInstanceState.getLong("gid");
		if (uri != null)
			fileUri = Uri.parse(uri);
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
		
	public void onClickCreateGroup(View v) {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivityForResult(intent, CREATE_GROUP);
	}
	
	public void onClickShare(View v) {
		
		if (fileUri != null) {
		    // Stop listening for GPS updates already
			mLocationManager.removeUpdates(listener);
			
			// Find GPS coordinates
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(false);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			
			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			String provider = locationManager.getBestProvider(criteria, true);
			Location location = locationManager.getLastKnownLocation(provider);
			
			// Get user session ID
    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);

    		String group = Long.toString(gid);
    		
    		String rotate = Integer.toString(rotation);
    		
    		String lat, lon;
    		
    		if (location != null) {
	    		lat = Double.toString(location.getLatitude());
	    		lon = Double.toString(location.getLongitude());
    		} else {
    			lat = "0";
    			lon = "0";
    		}
    		String commentTxt = comment.getText().toString();
    		
    		String shareUrl = Util.getUrl(this,R.string.url_upload);
    		/*
			new UploadImage(this).execute(shareUrl,hash,group,lat,lon,commentTxt);
    		*/

            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
            try {
            	
    			map.put("sid", new StringBody(hash));
    			
    			if (gid != 0)
    				map.put("group", new StringBody(group));
    			
    	        map.put("lat", new StringBody(lat));
    	        map.put("lon", new StringBody(lon));
    	        map.put("comment", new StringBody(commentTxt));
    	        map.put("rotation", new StringBody(rotate));
        		File file = new File(fileUri.getPath());
        		ContentBody cbFile = new FileBody(file, "image/jpeg");
        		map.put("photo", cbFile);

    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            PostData pr = new PostData(shareUrl,map);
            new PostRequest(this, CODE_UPLOAD).execute(pr);
            
            pd = new ProgressDialog(SharePhotoActivity.this);
            pd.setMessage("Uploading photo...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();    		
		} 
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == RESULT_OK) {
        	newGroupName = data.getStringExtra("name");
        	Toast.makeText(this, "Group '" + newGroupName + "' Created", Toast.LENGTH_SHORT).show();
        	//loadData();
        }
        
        else if (requestCode == CODE_SELECT_GROUP) {
        	if (resultCode == RESULT_OK) {
	        	gid = data.getLongExtra("group", 0);
	        	String groupName = data.getStringExtra("groupName");
	        	btnSelectGroup.setText(groupName);
	        	btnSelectGroup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
        	} else {
        		// Canceled group selection, reset button and gid
        		gid = 0;
	        	btnSelectGroup.setText(R.string.select_group);
	        	btnSelectGroup.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        	}
        }
        
        else if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
                
        	fileUri = data.getData();
        	
        	rotation = Util.getRotationDegrees(fileUri.getPath());
        	
        	imageView.setImageBitmap(Util.decodeSampledBitmapFromFile(fileUri.getPath(), 200, 200, rotation));
	    } else if (resultCode == RESULT_CANCELED) {
	        finish();
	    }
	}
	

	@Override
	public void parseJson(String json, int code) {

		
		switch(code){
			case(CODE_UPLOAD):
				parseUpload(json);
				break;
		default:
		}
	}

    private void parseUpload(String json) {
		Log.d("Json parse",json);     
		
		if (pd != null) pd.dismiss();
		
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
        	Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
        	
    		setResult(RESULT_OK);
    		finish();
		} else {
			setupGps();
        	Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();	
		}
		
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
        mLocationManager.requestLocationUpdates(networkProvider, TEN_SECONDS, 0, listener);
        
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
            	//loadData();
        	}

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
}
