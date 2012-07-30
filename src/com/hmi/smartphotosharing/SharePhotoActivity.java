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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupListResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.StringRepsonse;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;

public class SharePhotoActivity extends Activity implements OnDownloadListener {
	
	public static final int CREATE_GROUP = 4;
	private Uri imageUri;
	private Spinner spinner;
	private EditText comment;
	private ImageView imageView;
	//private String imgPath;
	private LocationManager locationManager;
	
	private static final int CODE_GROUPS = 2;
	private static final int CODE_UPLOAD = 3;
	private static int STATUS_OK = 200;
	
	private boolean isExternal;
	private ProgressDialog pd;
	
	private String newGroupName;
	
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
	            handleImage(intent); // Handle single image being sent
	        }
	    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendMultipleImages(intent); // Handle multiple images being sent
	        }
	    } 
	    
	    // Handle the Intent that was sent internally, from another Activity
	    else {
	    	if (type.startsWith("image/")) {
	            handleImage(intent); // Handle single image being sent
	        }
	    }
		super.onStart();
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http), hash);
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
    		
    		pd = new ProgressDialog(SharePhotoActivity.this);
    		pd.setMessage("Uploading photo...");
    		pd.setCancelable(false);
    		pd.setIndeterminate(true);
    		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		pd.show();
    		
			new UploadImage(this).execute(shareUrl,hash,group,lat,lon,commentTxt);
		} else {
			
			setResult(RESULT_CANCELED);
			finish();
		}
	}	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == RESULT_OK) {
        	newGroupName = data.getStringExtra("name");
        	Toast.makeText(this, "Group '" + newGroupName + "' Created", Toast.LENGTH_SHORT).show();
        	loadData();
        }
    }	
	
	private void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    if (sharedText != null) {
	        // Update UI to reflect text being shared
	    }
	}

	private void handleImage(Intent intent) {
	    imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    
	    showImageFromPath(imageUri);
	    
	    /*
	    if (imageUri != null) {

	    	imgPath = getRealPathFromURI(imageUri);
	    	showImageFromPath(imgPath);
	    }*/
	}

	/*
	private void handleInternalImage(Intent intent) {
	    imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {

	    	imgPath = imageUri.getPath();
	    	showImageFromPath(imgPath);
	    }
	}*/
	
	private void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        // Update UI to reflect multiple images being shared
	    }
	}
	
	private void showImageFromPath(Uri uri) {
		// Image can come from different sources, so check the scheme
		String imgPath = "";

		imgPath = getImgPath(imageUri);
		
    	int targetW = imageView.getWidth();
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
		
		imageView.setMaxHeight(photoH*scaleFactor);
		
		// Set bitmap options to scale the image decode target
		o.inJustDecodeBounds = false;
		o.inSampleSize = scaleFactor;
		o.inPurgeable = true;
		
    	Bitmap myBitmap = BitmapFactory.decodeFile(imgPath,o);
    	
    	// Check rotation
    	
    	Matrix matrix = new Matrix();
    	float rotation = rotationForImage(this, imageUri);
    	if (rotation != 0f) {
    	     matrix.preRotate(rotation);
    	}

    	myBitmap = Bitmap.createBitmap(
    	     myBitmap, 0, 0, photoW, photoH, matrix, true);
    	
    	imageView.setImageBitmap(myBitmap);
	}	
	
	private String getImgPath(Uri uri) {
		if (isExternal)
			return getRealPathFromURI(uri);
		else
			return uri.getPath();
	}

	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	public float rotationForImage(Context context, Uri uri) {
	    if (isExternal) {
            String[] projection = { Images.ImageColumns.ORIENTATION };
            Cursor c = context.getContentResolver().query(
                    uri, projection, null, null, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } else {
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                int rotation = (int)exifOrientationToDegrees(
                        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL));
                return rotation;
            } catch (IOException e) {
                Log.e("SharePhoto", "Error checking exif", e);
            }
        }
	    return 0f;
	}

	private static float exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

	@Override
	public void parseJson(String json, int code) {

		Log.d("Json parse",json);
		
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
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response.getStatus() == STATUS_OK) {
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
		} else {
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

	private class UploadImage extends AsyncTask<String,Integer,String> {
        
		private Context context;
		
		public UploadImage(Context c) {
			this.context = c;
			
		}
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
        protected void onProgressUpdate(Integer... progress) {     
            pd.setProgress(progress[0]);
        }
    	
    	@Override
    	protected void onPostExecute(String result) {
    		Log.i("JSON parse", result);
    		
    		try {
    			parseJson(result, CODE_UPLOAD);
    		} catch (JsonSyntaxException e) {
    			Toast.makeText(context, "Something went wrong with the server, please try again later", Toast.LENGTH_SHORT).show();
    			Log.e("JSON", "Json syntax exception: " + e.getMessage());
    		}
    		pd.dismiss();
    	}
    	
    	public String sendPost(String url, String sid, String group, String lat, String lon, String comment) throws IOException, ClientProtocolException  {
    		HttpClient httpclient = new DefaultHttpClient();

    		HttpPost httppost = new HttpPost(url);
    		File file = new File(getImgPath(imageUri));
    		    		
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
