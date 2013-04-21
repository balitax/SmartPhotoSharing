package com.hmi.smartphotosharing.groups;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GroupManageActivity extends NavBarActivity implements OnDownloadListener {
	
	private static final int CODE_GROUP_DETAILS = 1;
	private static final int CODE_GROUP_REMOVE = 2;
    private static final int TAKE_PICTURE = 5;

	public static final String KEY_ID = "id";
    private ImageLoader imageLoader;
	private long id;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_manage);
        super.onCreate(savedInstanceState);
        
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        loadData();
        
        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), home);
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
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
			map.put("gid", new StringBody(Long.toString(id)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String detailUrl = Util.getUrl(this,R.string.group_http_detail);
		
        PostData pr = new PostData(detailUrl,map);
        new PostRequest(this, CODE_GROUP_DETAILS,false).execute(pr);
		
	}
	
	public void onClickPicture(View view) {
		Intent intent = new Intent(this, ChangeGroupPictureActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, TAKE_PICTURE);
	}
	
	public void onClickDelete(View view) {
		confirmDeleteCommentDialog(id);
	}

	@Override
	public void parseJson(String json, int code) {

		switch (code) {
			case CODE_GROUP_DETAILS:
				parseGroup(json);
				break;
			
			case CODE_GROUP_REMOVE:
				parseRemove(json);
				break;
			default:
		}
		
	}

	private void parseRemove(String json) {
		Gson gson = new Gson();
		
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
		if (response.getStatus() == Util.STATUS_OK) {
			Intent intent = new Intent(this, GroupsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} 
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		
		GroupResponse response = gson.fromJson(result, GroupResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Group group = response.getObject();

	        TextView groupName = (TextView) findViewById(R.id.header_title);
	        ImageView groupIcon = (ImageView) findViewById(R.id.app_icon);
	        //TextView groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
	        //TextView groupMembers = (TextView) findViewById(R.id.group_detail_members);
	        //ImageView privateIcon = (ImageView) findViewById(R.id.private_icon);
	        //ImageView locationIcon = (ImageView) findViewById(R.id.location_icon);
	        
			groupName.setText(group.name);
			String logoUrl = Util.getThumbUrl(group);
			imageLoader.displayImage(logoUrl, groupIcon);
	
			/*
			if (!group.isPrivate()) {
				privateIcon.setVisibility(ImageView.GONE);
			}

			if (!group.isLocationLocked()) {
				locationIcon.setVisibility(ImageView.GONE);
			}*/
			
			// Set the string telling how many members the group has
			//String photos = String.format(getResources().getString(R.string.group_detail_members), group.members);
			//groupMembers.setText(photos);
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
	
    private void confirmDeleteCommentDialog(final long gid) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this group? All photos will be removed!")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {

			       		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
			       		String hash = settings.getString(Login.SESSION_HASH, null);
			       		
			       		String deleteUrl = Util.getUrl(GroupManageActivity.this,R.string.groups_http_remove);
			       			
			               HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
			               try {
			       			map.put("sid", new StringBody(hash));
			       	        map.put("gid", new StringBody(Long.toString(gid)));
			       		} catch (UnsupportedEncodingException e) {
			       			e.printStackTrace();
			       		}
		               
		                PostData pr = new PostData(deleteUrl,map);
		                new PostRequest(GroupManageActivity.this, CODE_GROUP_REMOVE).execute(pr);
		           }
		       })
		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		
	}
}
