package com.hmi.smartphotosharing;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.hmi.smartphotosharing.groups.GroupsActivity;
import com.hmi.smartphotosharing.subscriptions.SubscriptionsActivity;
import com.hmi.smartphotosharing.util.Util;

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
		case Util.ACTION_FAVOURITE:
			action_favourite();
			break;
		case Util.ACTION_CAMERA:
			action_camera();
			break;
		case Util.ACTION_SETTINGS:
			action_settings();
			break;
		case Util.ACTION_MAP:
			action_map();
			break;
		default:
		}			
		
	}

	private void action_favourite() {
        Intent intent = new Intent(c, SubscriptionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
	}

	private void action_settings() {
        Intent intent = new Intent(c, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
		
	}

	private void action_camera() {
		Intent intent = new Intent(c, SharePhotoActivity.class);
		c.startActivity(intent);			
	}

	private void action_archive() {
        Intent intent = new Intent(c, GroupsActivity.class);
        c.startActivity(intent);		
	}

	private void action_map() {
        Intent intent = new Intent(c, MapActivity.class);
        c.startActivity(intent);		
	}
};