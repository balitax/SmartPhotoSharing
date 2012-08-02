package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.NavBarActivity;
import com.hmi.smartphotosharing.PhotoDetailActivity2;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.StringRepsonse;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Util;

public class GroupDetailActivity extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_GROUP_DETAILS = 1;
	private static final int CODE_GROUP_PHOTOS = 2;
	private static final int CODE_JOIN = 3;
	private static final int CODE_LEAVE = 4;
	private static final int CODE_INVITE = 5;
	
	private static final int DIALOG_INFO = 0;
	
	public static final String KEY_ID = "id";
	
	//gallery object
	private GridView gridView;
	//image view for larger display
	
	private TextView groupPhotos;
	private TextView groupMembers;
	private TextView groupName;
	private ImageView groupIcon;

	private ImageView privateIcon, locationIcon;
	
	private ImageLoader dm;
			
	private long id;
	private boolean isMember;
	
	private Group group;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_detail);
        super.onCreate(savedInstanceState);

        dm = new ImageLoader(this);
        
        groupName = (TextView) findViewById(R.id.group_detail_name);
        groupIcon = (ImageView) findViewById(R.id.group_detail_icon);
        groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
        groupMembers = (TextView) findViewById(R.id.group_detail_members);
        privateIcon = (ImageView) findViewById(R.id.private_icon);
        locationIcon = (ImageView) findViewById(R.id.location_icon);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        // Set the click listener for each item in the thumbnail gallery     
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        
        isMember = true;
        group = null;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        id = intent.getLongExtra(KEY_ID, 0);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (id != 0) {
			loadData();
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_INFO:
        	dialog = new Dialog(this);

        	dialog.setContentView(R.layout.group_info);
        	dialog.setTitle(group.name);

        	TextView text = (TextView) dialog.findViewById(R.id.text);
        	text.setText(group.description);

        	text = (TextView) dialog.findViewById(R.id.members);
        	text.setText("Members: " + Integer.toString(group.members));

        	text = (TextView) dialog.findViewById(R.id.photos);
        	text.setText("Photos: " + group.numphotos);
        	
        	text = (TextView) dialog.findViewById(R.id.owner);
        	text.setText("Owner: " + group.owner);

            break;
        default:
            dialog = null;
        }
        return dialog;
    }
    
    public void onClickJoinGroup(View view) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (isMember) {
			confirmLeaveDialog(this, hash);
		} else {
	        String joinUrl = String.format(Util.getUrl(this,R.string.groups_http_join),hash,id);		
	        new FetchJSON(this, CODE_JOIN).execute(joinUrl);
		}
    }
    
    public void onClickShowLocation(View view) {
    	Intent intent = new Intent(this,ShowLocationActivity.class);
    	intent.putExtra("lat1", group.latstart);
    	intent.putExtra("lat2", group.latend);
    	intent.putExtra("lon1", group.longstart);
    	intent.putExtra("lon2", group.longend);
    	startActivity(intent);
    }
    
    private void confirmLeaveDialog(final Context c, final String hash) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to leave this group?")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {
			   	        String leaveUrl = String.format(Util.getUrl(c,R.string.groups_http_leave),hash,id);		
			   	        new FetchJSON(c, CODE_LEAVE).execute(leaveUrl);
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
    public void onClickInfo(View view) {
    	showDialog(DIALOG_INFO);
    }
    
    public void onClickInvite(View view) {
    	Intent intent = new Intent(this, SelectSingleFriendActivity.class);
    	intent.putExtra("id", id);
    	startActivityForResult(intent, CODE_INVITE);
    }
    
    private class MyOnItemClickListener implements OnItemClickListener {
		private Context c;
		
		public MyOnItemClickListener(Context c) {
			this.c = c;
		}
		// Handle clicks
		@Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent intent = new Intent(c, PhotoDetailActivity2.class);
	    	intent.putExtra("id", id);
	    	intent.putExtra("gid", group.getId());
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
	        	intent = new Intent(this, SelectSingleFriendActivity.class);
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
		String detailUrl = String.format(Util.getUrl(this,R.string.group_http_detail),hash,id);
		new FetchJSON(this,CODE_GROUP_DETAILS).execute(detailUrl);
		
		// Get list of photos
		String photosUrl = String.format(Util.getUrl(this,R.string.group_http_detail_photos),hash,id);
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
			
			case(Util.STATUS_OK):
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
			
			case(Util.STATUS_OK):
				if (isMember) { // If user PREVIOUSLY was a member...
					isMember = false;
				} else {
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
		
		if (gdr.getStatus() == Util.STATUS_OK) {
			group = gdr.getObject();
	
			groupName.setText(group.name);
			String logoUrl = Util.GROUP_DB + group.logo;
			dm.DisplayImage(logoUrl, groupIcon);
	
			if (!group.isPrivate()) {
				privateIcon.setVisibility(ImageView.GONE);
			}

			if (!group.isLocationLocked()) {
				locationIcon.setVisibility(ImageView.GONE);
			}
			
			// Set the button text join/leave group
			if (group.member == 0) {
				isMember = false;
			} else {
				isMember = true;				
			}
			
			// Set the string telling how many members the group has
			String photos = String.format(getResources().getString(R.string.group_detail_members), group.members);
			groupMembers.setText(photos);
		} else {
			Toast.makeText(this, gdr.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
}
