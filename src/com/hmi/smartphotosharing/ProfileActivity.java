package com.hmi.smartphotosharing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ProfileActivity extends NavBarActivity {

    private static final int TAKE_PICTURE = 5;
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.profile);
        super.onCreate(savedInstanceState);
        
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);
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
	
}
