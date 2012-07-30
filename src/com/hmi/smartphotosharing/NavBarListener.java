package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.camera.CameraActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class NavBarListener implements OnClickListener {
   
	private Context c;
	private int code;
	
	public NavBarListener(Context c, int code) {
		this.c = c;
		this.code = code;
	}
	
	@Override
	public void onClick(View v) {
		switch (code) {
		case Util.ACTION_ARCHIVE:
			action_archive();
			break;
		case Util.ACTION_CAMERA:
			action_camera();
			break;
		case Util.ACTION_SETTINGS:
			action_settings();
			break;
		default:
		}			
		
	}

	private void action_settings() {
        Intent intent = new Intent(c, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
		
	}

	private void action_camera() {
		Intent intent = new Intent(c, CameraActivity.class);
		c.startActivity(intent);			
	}

	private void action_archive() {
        Intent intent = new Intent(c, GroupsActivity.class);
        c.startActivity(intent);		
	}
	
};