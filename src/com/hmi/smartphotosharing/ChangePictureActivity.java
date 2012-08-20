package com.hmi.smartphotosharing;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.camera.CameraActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;

public class ChangePictureActivity extends Activity implements OnDownloadListener {

	private static final int CODE_UPLOAD = 3;
	private static final int TAKE_PICTURE = 5;
	
	private Uri fileUri;
	private int rotation;

	private ProgressDialog pd;
	
	private boolean started;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_picture); 
		
        if (!started) {
        	started = true;
		    Intent cameraIntent = new Intent(this,CameraActivity.class);
		    startActivityForResult(cameraIntent, TAKE_PICTURE); 
        }
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("started", started);
		if (fileUri != null) 
			savedInstanceState.putString("uri", fileUri.getPath());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		started = savedInstanceState.getBoolean("started");
		String uri = savedInstanceState.getString("uri");
		if (uri != null)
			fileUri = Uri.parse(uri);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
            started = true;    
        	fileUri = data.getData();
        	
        	rotation = Util.getRotationDegrees(fileUri.getPath());
        	
        	ImageView imageView = (ImageView) findViewById(R.id.image1);
        	imageView.setImageBitmap(Util.decodeSampledBitmapFromFile(fileUri.getPath(), 200, 200, rotation));
	    } else if (resultCode == RESULT_CANCELED) {
	        finish();
	    }
	}

	public void onClickSend(View v) {
		
		if (fileUri != null) {
		    			
			// Get user session ID
    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
    		String rotate = Integer.toString(rotation);
    		    		
    		String pictureUrl = Util.getUrl(this,R.string.profile_http_picture);
    		
            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
            try {
    			map.put("sid", new StringBody(hash));
    	        map.put("rotation", new StringBody(rotate));
        		File file = new File(fileUri.getPath());
        		ContentBody cbFile = new FileBody(file, "image/jpeg");
        		map.put("photo", cbFile);

    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            PostData pr = new PostData(pictureUrl,map);
            new PostRequest(this, CODE_UPLOAD).execute(pr);
            
            pd = new ProgressDialog(this);
            pd.setMessage("Uploading photo...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();    		
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
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
        	Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
        	
    		finish();
		} else {
        	Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();	
		}
		
	}
}
