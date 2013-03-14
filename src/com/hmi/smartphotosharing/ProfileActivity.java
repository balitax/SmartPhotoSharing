package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ProfileActivity extends NavBarActivity {

    private static final int TAKE_PICTURE = 5;
	
    private ImageLoader imageLoader;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.profile);
        super.onCreate(savedInstanceState);
        
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
                
    }
	 
	public void onClickPicture(View view) {
		Intent intent = new Intent(this, ChangePictureActivity.class);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	public void onClickPrefs(View view) {         
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void onClickListPics(View view) {
		Intent intent = new Intent(this, MyPicturesActivity.class);
		startActivity(intent);
	}
	
	public void onClickLogout(View view) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		settings.edit().remove(Login.SESSION_HASH).commit();
		
		Intent intent = new Intent(this, Login.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
