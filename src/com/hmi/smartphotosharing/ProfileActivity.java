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

public class ProfileActivity extends NavBarActivity implements OnDownloadListener {

    private static final int CODE_PROFILE = 1;
    private static final int TAKE_PICTURE = 5;
	
    private ImageLoader imageLoader;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.profile);
        super.onCreate(savedInstanceState);
        
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        loadData();
    }
	
    @Override
    public void onResume() {
      super.onResume();
      
      loadData();
    }  
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
			loadData();
	    } 
	}
	
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String profileUrl = String.format(Util.getUrl(this,R.string.profile_http),hash);		
        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);
		
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

	@Override
	public void parseJson(String json, int code) {

		switch (code) {
			case CODE_PROFILE:
				parseProfile(json);
				break;
				
			default:
		}
		
	}

	private void parseProfile(String result) {
		Gson gson = new Gson();
		UserResponse response = gson.fromJson(result, UserResponse.class);
		User user = response.getObject();
		if (user != null) {
			// Set the user name
			TextView name = (TextView) findViewById(R.id.groups_name);
			name.setText(user.getName());
			
			// Set the user icon
			ImageView pic = (ImageView) findViewById(R.id.groups_icon);
			//dm.fetchDrawableOnThread(userPic, pic);
			imageLoader.displayImage(user.thumb, pic);
		}
	}


}
