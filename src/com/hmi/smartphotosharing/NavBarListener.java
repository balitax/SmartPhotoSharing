package com.hmi.smartphotosharing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.hmi.smartphotosharing.friends.FriendsActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;
import com.hmi.smartphotosharing.local.MapActivity;
import com.hmi.smartphotosharing.news.NewsActivity;
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
		case Util.ACTION_FRIENDS:
			action_friends();
			break;
		case Util.ACTION_MAP:
			action_map();
			break;
		default:
		}			
		
	}

	private void action_favourite() {
        createGroupDialog();
	}

	private void action_settings() {
        Intent intent = new Intent(c, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
		
	}

	private void action_friends() {
        Intent intent = new Intent(c, FriendsActivity.class);
        c.startActivity(intent);
		
	}
	
	private void action_camera() {
	   //Intent intent = new Intent(c, SharePhotoActivity.class);
	   //c.startActivity(intent);	
	   createPhotoDialog();
	}

	private void action_archive() {
        Intent intent = new Intent(c, NewsActivity.class);
        c.startActivity(intent);		
	}

	private void action_map() {
        Intent intent = new Intent(c, MapActivity.class);
        c.startActivity(intent);		
	}
	
	public void createGroupDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle(c.getResources().getString(R.string.group_menu_dialog))
			 .setItems(R.array.group_dialog, new GroupListener());
		AlertDialog alert = builder.create();
		alert.show();
		
	}   
	
	public void createPhotoDialog() {

    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle(c.getResources().getString(R.string.camera_share_dialog))
			 .setItems(R.array.camera_dialog, new ShareListener());
		AlertDialog alert = builder.create();
		alert.show();
	}    
	
	public class ShareListener implements DialogInterface.OnClickListener {
		
        public void onClick(DialogInterface dialog, int which) {
     	   if(which == 0) {
     		   Intent intent = new Intent(c, SharePhotoActivity.class);
     		   c.startActivity(intent);
     	   } else {
     		   Toast t = Toast.makeText(c, "Select from saved photos", Toast.LENGTH_SHORT);
     		   t.show();
     	   }
        }
	 }
	
	public class GroupListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int which) {
     	   if(which == 0) {
     		   Intent intent = new Intent(c, GroupsActivity.class);
     		   c.startActivity(intent);
     	   } else {
     		   Intent intent = new Intent(c, SubscriptionsActivity.class);
     		   c.startActivity(intent);
     	   }
        }
	 }
};