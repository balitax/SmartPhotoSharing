package com.hmi.smartphotosharing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.hmi.smartphotosharing.groups.GroupCreateActivity;

public class SharePhotoActivity extends Activity  {
	
	public static final int CREATE_GROUP = 4;
	private Uri imageUri;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo);
		
		// Populate the Spinner
		Spinner spinner = (Spinner) findViewById(R.id.groups_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.demo_groups, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
	    // Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    // Handle the Intent form another app
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
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
	            handleSendImage(intent); // Handle single image being sent
	        }
	    }
	}
	
	public void onClickCreateGroup(View v) {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivityForResult(intent, CREATE_GROUP);
	}
	
	public void onClickShare(View v) {
		
		if (imageUri != null) {
			new UploadImage().execute(getResources().getString(R.string.url_upload),Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/rozen.jpg");
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
	        ImageView view = (ImageView) findViewById(R.id.image1);
	        view.setImageURI(imageUri);
	    }
	}

	private void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        // Update UI to reflect multiple images being shared
	    }
	}
	

    private class UploadImage extends AsyncTask<String,Void,String> {
        
    	@Override
    	protected String doInBackground(String... urls) {
             
	       // params comes from the execute() call: params[0] is the url.
	           try {
				return sendPost(urls[0],urls[1]);
			} catch (ClientProtocolException e) {
				Log.e("Upload image", e.getMessage());
				return null;
			} catch (IOException e) {
				Log.e("Upload image", e.getMessage());
				return null;
			}
    	}
    	
    	public String sendPost(String url, String imagePath) throws IOException, ClientProtocolException  {
    		HttpClient httpclient = new DefaultHttpClient();
    		//httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

    		HttpPost httppost = new HttpPost(url);
    		File file = new File(imagePath);

    		MultipartEntity mpEntity = new MultipartEntity();
    		ContentBody cbFile = new FileBody(file, "image/jpeg");
    		mpEntity.addPart("userfile", cbFile);
    	
    		httppost.setEntity(mpEntity);
    		Log.d("sendPost","executing request " + httppost.getRequestLine());
    		HttpResponse response = httpclient.execute(httppost);
    		HttpEntity resEntity = response.getEntity();
    		Log.d("sendPost",""+response.getStatusLine());
    		if (resEntity != null) {
    			Log.d("sendPost",EntityUtils.toString(resEntity));
    			resEntity.consumeContent();
    		}
    		httpclient.getConnectionManager().shutdown();
    		return "ok";
    	}
    }
}
