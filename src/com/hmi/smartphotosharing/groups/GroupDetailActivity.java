package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PhotoListResponse;
import com.hmi.json.StringRepsonse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;

public class GroupDetailActivity extends Activity implements OnDownloadListener {

	private static final int CODE_GROUP_DETAILS = 1;
	private static final int CODE_GROUP_PHOTOS = 2;
	private static final int CODE_JOIN = 3;
	private static final int CODE_LEAVE = 4;
	private static final int CODE_INVITE = 5;
	
	private static final int STATUS_OK = 200;
	
	//gallery object
	private GridView gridView;
	//image view for larger display
	
	private TextView groupPhotos;
	private TextView groupMembers;
	private TextView groupName;
	private ImageView groupIcon;
	private Button groupJoinBtn;
	
	private DrawableManager dm;
			
	private long id;
	private boolean isMember;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_detail);

        dm = new DrawableManager(this);
        
        groupName = (TextView) findViewById(R.id.group_detail_name);
        groupIcon = (ImageView) findViewById(R.id.group_detail_icon);
        groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
        groupMembers = (TextView) findViewById(R.id.group_detail_members);
        groupJoinBtn = (Button) findViewById(R.id.join_group);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        // Set the click listener for each item in the thumbnail gallery     
        
        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        
        isMember = true;
    }

    public void onClickJoinGroup(View view) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (isMember) {
	        String leaveUrl = String.format(getResources().getString(R.string.groups_http_leave),hash,id);		
	        new FetchJSON(this, CODE_LEAVE).execute(leaveUrl);
		} else {
	        String joinUrl = String.format(getResources().getString(R.string.groups_http_join),hash,id);		
	        new FetchJSON(this, CODE_JOIN).execute(joinUrl);
		}
    }
    
    private class MyOnItemClickListener implements OnItemClickListener {
		private Context c;
		
		public MyOnItemClickListener(Context c) {
			this.c = c;
		}
		// Handle clicks
		@Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent intent = new Intent(c, PhotoDetailActivity.class);
	    	intent.putExtra("id", id);
	    	startActivity(intent);
	    }
    }
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.group_detail_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
        switch (item.getItemId()) {
	        case R.id.invite:
	        	intent = new Intent(this, GroupInviteActivity.class);
	        	intent.putExtra("id", id);
	        	startActivityForResult(intent, CODE_INVITE);
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onStart() {
        super.onStart();
        
        loadData();
	}
	
	private void loadData() {
    	
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String detailUrl = String.format(getResources().getString(R.string.group_http_detail),hash,id);
		new FetchJSON(this,CODE_GROUP_DETAILS).execute(detailUrl);
		
		// Get list of photos
		String photosUrl = String.format(getResources().getString(R.string.group_http_detail_photos),hash,id);
		new FetchJSON(this,CODE_GROUP_PHOTOS).execute(photosUrl);
		        
	}
	
	public void parseJson(String result, int code) {
		
		switch (code) {
			case CODE_GROUP_DETAILS:
				parseGroup(result);
				break;
			case CODE_GROUP_PHOTOS:
				parsePhoto(result);
				break;
			case CODE_JOIN:
				parseJoin(result);
				break;
			case CODE_LEAVE:
				parseLeave(result);
				break;
			default:
		}
	}
	
	private void parseLeave(String json) {

		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
			case(STATUS_OK):
				Toast.makeText(this, "Group left", Toast.LENGTH_SHORT).show();
		    	Intent intent = new Intent(this, GroupsActivity.class);
		    	startActivity(intent);
				break;
				
			default:
				Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}

	private void parseJoin(String json) {

		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
			case(STATUS_OK):
				if (isMember) { // If user PREVIOUSLY was a member...
					groupJoinBtn.setText("Join group");
					isMember = false;
				} else {
					groupJoinBtn.setText("Leave group");
					isMember = true;				
				}
				break;
				
			default:
				Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}
	private void parsePhoto(String result) {
		Gson gson = new Gson();
		PhotoListResponse list = gson.fromJson(result, PhotoListResponse.class);
		
		List<Photo> photo_list = list.getObject();
		
		// JSON will return null if there are no photos in this group
		if (photo_list == null)
			photo_list = new ArrayList<Photo>();
		
		gridView.setAdapter(
			new MyImageAdapter(
					this, 
					photo_list,
					dm
		));

        gridView.setOnItemClickListener(new MyOnItemClickListener(this)); 
        
		// Set the string telling how many photos the group has
		String photos = String.format(getResources().getString(R.string.group_detail_photos), photo_list.size());
		groupPhotos.setText(photos);
		
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		
		GroupResponse gdr = gson.fromJson(result, GroupResponse.class);
		
		if (gdr.getStatus() == STATUS_OK) {
			Group g = gdr.getObject();
	
			groupName.setText(g.name);
			String logoUrl = getResources().getString(R.string.group_http_logo) + g.logo;
			dm.fetchDrawableOnThread(logoUrl, groupIcon);
	
			// Set the button text join/leave group
			if (g.member == 0) {
				groupJoinBtn.setText("Join group");
				isMember = false;
			} else {
				groupJoinBtn.setText("Leave group");
				isMember = true;				
			}
			
			// Set the string telling how many members the group has
			String photos = String.format(getResources().getString(R.string.group_detail_members), g.members);
			groupMembers.setText(photos);
		} else {
			Toast.makeText(this, gdr.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
}
