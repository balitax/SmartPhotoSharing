package com.hmi.smartphotosharing;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringRepsonse;
import com.hmi.smartphotosharing.util.Util;

public class SharePhotoActivity extends Activity implements OnDownloadListener {
	
	private static String TAG = "SHARE";
	
	private Spinner spinner;
	private EditText comment;
	private ImageView imageView;
	//private String imgPath;
	private LocationManager locationManager;
	
	private static final int TAKE_PICTURE = 5;
	private static final int CODE_GROUPS = 2;
	private static final int CODE_UPLOAD = 3;
	private static final int CREATE_GROUP = 4;
	
	private static final int TEN_SECONDS = 10 * 1000;
	private static final float MIN_DISTANCE = 25;
		
	private String newGroupName;

    private LocationManager mLocationManager;
	private Location gpsLocation;
	
	private Uri fileUri;
	
	private ProgressDialog pd;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo); 
				
		// Populate the Spinner
		spinner = (Spinner) findViewById(R.id.groups_spinner);
		comment = (EditText) findViewById(R.id.edit_message);
        imageView = (ImageView) findViewById(R.id.image1);

		// GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        } else {        	
        	setupGps();
        }
        
        if (fileUri == null) {
		    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
		    fileUri = Util.getOutputMediaFileUri(Util.MEDIA_TYPE_IMAGE);
		    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		    startActivityForResult(cameraIntent, TAKE_PICTURE); 
        }
	    
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("uri", fileUri.getPath());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String uri = savedInstanceState.getString("uri");
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
		
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http_locate), hash);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();

    	double lat = 0;
    	double lon = 0;
    	
        if (gpsLocation != null ) {
	        lat = gpsLocation.getLatitude();
	        lon = gpsLocation.getLongitude();
        }
        
        try {
			map.put("sid", new StringBody(hash));
	        map.put("lat", new StringBody(Double.toString(lat)));
	        map.put("lon", new StringBody(Double.toString(lon)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(groupsUrl,map);
		
		new PostRequest(this,CODE_GROUPS).execute(pr);
		
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

    		String group = Long.toString(spinner.getSelectedItemId());
    		
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
    	        map.put("group", new StringBody(group));
    	        map.put("lat", new StringBody(lat));
    	        map.put("lon", new StringBody(lon));
    	        map.put("comment", new StringBody(commentTxt));

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
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == RESULT_OK) {
        	newGroupName = data.getStringExtra("name");
        	Toast.makeText(this, "Group '" + newGroupName + "' Created", Toast.LENGTH_SHORT).show();
        	loadData();
        }
        
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
        
        	if(fileUri != null) {
				
	        	imageView.setImageURI(fileUri);
	        	
	        	loadData();
			}
        	
		        
	    } else if (resultCode == RESULT_CANCELED) {
	        finish();
	    } else {
	        // Image capture failed, advise user 
	    }
	}
    
		

	@Override
	public void parseJson(String json, int code) {

		
		switch(code){
		case(CODE_GROUPS):
			parseGroups(json);
			break;
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

	private void parseGroups(String json) {

		Gson gson = new Gson();
		GroupListResponse gr = gson.fromJson(json, GroupListResponse.class);
		List<Group> list = gr.getObject();
		MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(this,list);
		spinner.setAdapter(spinnerAdapter);

		for(int i = 0; i < spinnerAdapter.getCount(); i++) {
			if (spinnerAdapter.getItem(i).name.equals(newGroupName)) {
				spinner.setSelection(i);
			}
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
        mLocationManager.requestLocationUpdates(networkProvider, TEN_SECONDS, MIN_DISTANCE, listener);
        
        // GPS
        String gpsProvider = LocationManager.GPS_PROVIDER;
        
        if (mLocationManager.isProviderEnabled(gpsProvider)) {
            mLocationManager.requestLocationUpdates(gpsProvider, TEN_SECONDS, MIN_DISTANCE, listener);
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
            	loadData();
        	}

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
    
    // grab the name of the media from the Uri
    protected String getName(Uri uri) {
		String filename = null;

		try {
			String[] projection = { MediaStore.Images.Media.DISPLAY_NAME };
			Cursor cursor = managedQuery(uri, projection, null, null, null);

			if(cursor != null && cursor.moveToFirst()){
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
				filename = cursor.getString(column_index);
			} else {
				filename = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting file name: " + e.getMessage());
		}

		return filename;
	}
}
