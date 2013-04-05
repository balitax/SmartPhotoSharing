package com.hmi.smartphotosharing.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SharePhotoActivity;
import com.hmi.smartphotosharing.util.Util;

/**
 * This class handles the Camera page.
 * It creates an intent to let a different app handle the photo capturing.
 * The resulting picture can be stored in the gallery, shared with friends or deleted.
 * @author Edwin
 *
 */
public class CameraActivity extends Activity {

	private Uri fileUri;
	private static final int TAKE_PICTURE = 5;
	private static final String ACTION_TAKE_PICTURE = "com.hmi.smartphotosharing.TAKE_PICTURE";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
	    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
	    fileUri = Util.getOutputMediaFileUri(Util.MEDIA_TYPE_IMAGE);
	    
	    if (fileUri == null) {
	    	Toast.makeText(this, "Could not find a location to store photos. Try inserting an SD-card.", Toast.LENGTH_LONG).show();
	    	setResult(RESULT_CANCELED);
	    	finish();
	    } else {
		    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		    startActivityForResult(cameraIntent, TAKE_PICTURE); 
	    }
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (fileUri != null) 
			savedInstanceState.putString("uri", fileUri.getPath());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String uri = savedInstanceState.getString("uri");
		if (uri != null)
			fileUri = Uri.parse(uri);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
        	
        	if(fileUri != null) {
        		Intent res = new Intent(ACTION_TAKE_PICTURE,fileUri);

        		setResult(Activity.RESULT_OK, res);
        		finish();
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
        	
	    } else if (resultCode == RESULT_CANCELED) {
			setResult(Activity.RESULT_CANCELED);
	        finish();
	    } else {
	        // Image capture failed, advise user 
	    }
	}

}