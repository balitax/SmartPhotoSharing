package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.util.HelpDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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

	public void onClickReset(View view) {         

		SharedPreferences settings = getSharedPreferences(HelpDialog.DIALOG_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
        Toast.makeText(this, "All help messages will be shown again.", Toast.LENGTH_SHORT).show();
	}
	
	public void onClickListPics(View view) {
		Intent intent = new Intent(this, MyPicturesActivity.class);
		startActivity(intent);
	}
	
	public void onClickLogout(View view) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		settings.edit().remove(Login.SESSION_HASH).commit();
		
		Intent intent = new Intent(this, Login.class);
		intent.putExtra("finish", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
	                    Intent.FLAG_ACTIVITY_NEW_TASK); 
		
		startActivity(intent);
		finish();	
	
	}
	
}
