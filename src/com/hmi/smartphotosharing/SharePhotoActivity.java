package com.hmi.smartphotosharing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupsResponse;
import com.hmi.json.LoginResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;

public class SharePhotoActivity extends Activity implements OnDownloadListener {
	
	public static final int CREATE_GROUP = 4;
	private Uri imageUri;
	private Spinner spinner;
	private EditText comment;
	private ImageView imageView;
	private String imgPath;
	private LocationManager locationManager;
	
	private static final int CODE_GROUPS = 2;
	private static final int CODE_UPLOAD = 3;
	private static int STATUS_OK = 200;
	private static int STATUS_FAIL = 500;
	
	private boolean isExternal;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo);
				
		// Populate the Spinner
		spinner = (Spinner) findViewById(R.id.groups_spinner);
		comment = (EditText) findViewById(R.id.edit_message);
		isExternal = false;
		
	    loadData();
	}
	
	protected void onStart () {
		imageView = (ImageView) findViewById(R.id.image1);
		 // Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    // Handle the Intent form another app
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	    	
	    	isExternal = true;
	    	
	        if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        } else if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendMultipleImages(intent); // Handle multiple images being sent
	        }
	    } 
	    
	    // Handle the Intent that was sent internally, from another Activity
	    else {
	    	if (type.startsWith("image/")) {
	            handleInternalImage(intent); // Handle single image being sent
	        }
	    }
		super.onStart();
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		String groupsUrl = String.format(getResources().getString(R.string.groups_http), hash);
        new FetchJSON(this, CODE_GROUPS).execute(groupsUrl);
		
	}

	public void onClickCreateGroup(View v) {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivityForResult(intent, CREATE_GROUP);
	}
	
	public void onClickShare(View v) {
		
		if (imageUri != null) {
			
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
    		
    		// TODO fix gps
    		String lat = Double.toString(location.getLatitude());
    		String lon = Double.toString(location.getLongitude());
    		String commentTxt = comment.getText().toString();
    		
    		String shareUrl = getResources().getString(R.string.url_upload);
			new UploadImage().execute(shareUrl,hash,group,lat,lon,commentTxt);
		} else {
			
			setResult(RESULT_CANCELED);
			finish();
		}
	}	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
            	// TODO : refresh group list
            	Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show();
            }
        }
    }	
	
	private void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    if (sharedText != null) {
	        // Update UI to reflect text being shared
	    }
	}

	private void handleSendImage(Intent intent) {
	    imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {

	    	imgPath = getRealPathFromURI(imageUri);
	    	showImageFromPath(imgPath);
	    }
	}

	private void handleInternalImage(Intent intent) {
	    imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {

	    	imgPath = imageUri.getPath();
	    	showImageFromPath(imgPath);
	    }
	}
	
	private void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        // Update UI to reflect multiple images being shared
	    }
	}
	
	private void showImageFromPath(String imgPath) {
    	Display display = getWindowManager().getDefaultDisplay();	    	
    	int targetW = display.getWidth();
		int targetH = imageView.getHeight();
		
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(imgPath,o);
    	
    	int photoW = o.outWidth;
		int photoH = o.outHeight;
		
		// Figure out which way needs to be reduced less
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = photoW/targetW;	
		}
		
		// Set bitmap options to scale the image decode target
		o.inJustDecodeBounds = false;
		o.inSampleSize = scaleFactor;
		o.inPurgeable = true;
		
    	Bitmap myBitmap = BitmapFactory.decodeFile(imgPath,o);
    	imageView.setImageBitmap(myBitmap);
	}	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
		Gson gson = new Gson();
		LoginResponse response = gson.fromJson(json, LoginResponse.class);
		
		if (response.status == STATUS_OK) {
        	Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
        	
        	if (isExternal) {
        		setResult(RESULT_OK);
        		finish();
        	} else {
    			// Send the intent to the class that can handle incoming photos
    			Intent intent = new Intent(this,GroupsActivity.class);    			
    			// Create and start the chooser
    			startActivity(intent);
        	}
		} else if (response.status == STATUS_FAIL) {
        	Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();	
		} else {
        	Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();	
		}
		
	}

	private void parseGroups(String json) {

		Gson gson = new Gson();
		GroupsResponse gr = gson.fromJson(json, GroupsResponse.class);
		List<Group> list = gr. msg;
		spinner.setAdapter(new MySpinnerAdapter(this,list));
		
	}

	private class UploadImage extends AsyncTask<String,Void,String> {
        
    	@Override
    	protected String doInBackground(String... args) {
             
	       // params comes from the execute() call: params[0] is the url.
	           try {
				return sendPost(args[0],args[1],args[2],args[3],args[4],args[5]);
			} catch (ClientProtocolException e) {
				Log.e("Upload image", e.getMessage());
				return null;
			} catch (IOException e) {
				Log.e("Upload image", e.getMessage());
				return null;
			}
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		Log.i("JSON parse", result);
    		parseJson(result, CODE_UPLOAD);
    	}
    	
    	public String sendPost(String url, String sid, String group, String lat, String lon, String comment) throws IOException, ClientProtocolException  {
    		HttpClient httpclient = new DefaultHttpClient();

    		HttpPost httppost = new HttpPost(url);
    		File file = new File(imgPath);

    		MultipartEntity mpEntity = new MultipartEntity();
    		mpEntity.addPart("sid", new StringBody(sid));
    		mpEntity.addPart("group", new StringBody(group));
    		mpEntity.addPart("lat", new StringBody(lat));
    		mpEntity.addPart("lon", new StringBody(lon));
    		mpEntity.addPart("comment", new StringBody(comment));
    		
    		ContentBody cbFile = new FileBody(file, "image/jpeg");
    		mpEntity.addPart("photo", cbFile);
    	
    		httppost.setEntity(mpEntity);
    		Log.d("sendPost","executing request " + httppost.getRequestLine());
    		HttpResponse response = httpclient.execute(httppost);
    		HttpEntity resEntity = response.getEntity();
    		Log.d("sendPost",""+response.getStatusLine());
    		
    		String res = null;
    		if (resEntity != null) {
    			res = EntityUtils.toString(resEntity);
    			Log.d("sendPost",res);
    			resEntity.consumeContent();
    		}
    		httpclient.getConnectionManager().shutdown();
    		return res;
    	}
    }

}
