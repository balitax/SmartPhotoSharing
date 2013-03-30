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
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.camera.CameraActivity;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.groups.SelectGroupActivity;
import com.hmi.smartphotosharing.groups.SelectLocationActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class SharePhotoActivity extends Activity implements OnDownloadListener {
	
	private static String TAG = "SHARE";
	
	private Button btnSelectGroup;
	private EditText comment;
	private ImageView imageView;
	private ImageView locationImg;
	private TextView locationTxt;
	
	//private String imgPath;

	private static final int CODE_SELECT_GROUP = 2;
	private static final int CODE_UPLOAD = 3;
	private static final int CODE_CREATE_GROUP = 4;
	private static final int TAKE_PICTURE = 5;
	private static final int CODE_LOCATION = 6;
	
	private static final int TEN_SECONDS = 10 * 1000;
	private static final float MIN_DISTANCE = 25;
		
	private String newGroupName;

    private LocationManager mLocationManager;
    
    // The location that will be stored with the picture in the db
	private Location gpsLocation;
	
	private ImageLoader imageLoader;
	
	private Uri fileUri;
	private int rotation;
		
	private long gid;
	
	private int screenWidth, margin;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo); 

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = (int)(display.getWidth()*0.95);
        margin = (int)((display.getWidth() - screenWidth)/2);
        
		btnSelectGroup = (Button) findViewById(R.id.groups_spinner);
		comment = (EditText) findViewById(R.id.edit_message);
        imageView = (ImageView) findViewById(R.id.image1);
        
        locationImg = (ImageView) findViewById(R.id.img_location_ok);
        locationTxt = (TextView) findViewById(R.id.txt_location);
        
        // ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
		// GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        }
        
        // Intent from gallery
        Intent intent = getIntent();

        // Figure out what to do based on the intent type
        if (intent.getType() != null && intent.getType().indexOf("image/") != -1) {

        	fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        	
            if (fileUri != null) {
            	        
            	String path = Util.getRealPathFromURI(this,fileUri);
            	fileUri = Uri.parse(path);
            	rotation = Util.getRotationDegrees(fileUri.getPath());
            	
            	imageView.setImageBitmap(Util.decodeSampledBitmapFromFile(path, 200, 200, rotation));
            }
        	
        } else {
        	if (fileUri == null) {
        		Intent cameraIntent = new Intent(this,CameraActivity.class);
        		startActivityForResult(cameraIntent, TAKE_PICTURE);
        	}
	    
        }
        setupGps();
        //loadData();
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
	
	public void onClickSelectGroup(View view) {
		Intent intent = new Intent(this,SelectGroupActivity.class);
		startActivityForResult(intent, CODE_SELECT_GROUP);
	}
	
	public void onClickCreateGroup(View v) {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivityForResult(intent, CODE_CREATE_GROUP);
	}
	
	public void onClickMap(View v) {
		Intent intent = new Intent(this,SelectLocationActivity.class);
		intent.putExtra(SelectLocationActivity.SINGLE_LOCATION, true);
		startActivityForResult(intent, CODE_LOCATION);
	}
	
	public void onClickShare(View v) {
		
		if (fileUri != null) {
		    // Stop listening for GPS updates already
			mLocationManager.removeUpdates(listener);
									
			// Get user session ID
    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);

    		// Group ID
    		String group = Long.toString(gid);
    		
    		// Photo rotation
    		String rotate = Integer.toString(rotation);
    		    		
    		if (gpsLocation != null) {
    			
    			if (gid != 0) {
		    		String lat = Double.toString(gpsLocation.getLatitude());
		    		String lon = Double.toString(gpsLocation.getLongitude());
		    		
		    		String commentTxt = comment.getText().toString();
		    		
		    		String shareUrl = Util.getUrl(this,R.string.url_upload);
		    		
		            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
		            try {
		            	
		    			map.put("sid", new StringBody(hash));
		    			
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
		            new PostRequest(this, CODE_UPLOAD,true).execute(pr);
    			} else {
        			Util.createSimpleDialog(this, getResources().getString(R.string.share_no_group));
    				
    			}
    		} else {
    			Util.createSimpleDialog(this, getResources().getString(R.string.share_no_location));
    		}
            		
		} 
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CODE_CREATE_GROUP && resultCode == RESULT_OK) {
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
        
        else if (requestCode == CODE_LOCATION && resultCode == RESULT_OK) {

        	// Location was set manually, stop listening for GPS updates
    	    mLocationManager.removeUpdates(listener);
    	    
    	    if (gpsLocation == null) gpsLocation = new Location(LocationManager.GPS_PROVIDER);
    	    // Get the selected location 
        	gpsLocation.setLatitude(data.getDoubleExtra("lat1", 0d));
        	gpsLocation.setLongitude(data.getDoubleExtra("lon1", 0d));
        	
        	// Set some information for the user
        	if (gpsLocation.getLatitude() != 0 && gpsLocation.getLongitude() != 0) {
	        	locationTxt.setText(R.string.share_location_manual);
	        	locationImg.setImageDrawable(getResources().getDrawable(R.drawable.button_ok));
        	}
        }
        
        else if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
                
        	fileUri = data.getData();
        	
        	rotation = Util.getRotationDegrees(fileUri.getPath());
        	
        	//imageView.setImageBitmap(Util.decodeSampledBitmapFromFile(fileUri.getPath(), 200, 200, rotation));

    		// Resize the imageview to fit the screen
            LayoutParams params = (LayoutParams) imageView.getLayoutParams();
            params.width = screenWidth;
            params.height = screenWidth;
            params.setMargins(0, margin, 0, margin);
            imageView.setLayoutParams(params);
            
            // Show image with rounded corners
    		DisplayImageOptions roundOptions = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(8)).build();
            imageLoader.loadImage(fileUri.toString(), roundOptions, new SimpleImageLoadingListener() {
        	    @Override
        	    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        	    	imageView.setImageBitmap(Util.decodeSampledBitmapFromFile(loadedImage, rotation));
        	    }
        		
        	});
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
				
		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
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
        		Log.d("GPS","Location: " + location.getLatitude() + "/" + location.getLongitude());
        		gpsLocation = location;
        		locationTxt.setText(R.string.share_location_gps);
        		locationImg.setImageDrawable(getResources().getDrawable(R.drawable.button_ok));
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
