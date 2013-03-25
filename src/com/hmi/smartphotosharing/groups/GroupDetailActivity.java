package com.hmi.smartphotosharing.groups;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.friends.AddFriendsActivity;
import com.hmi.smartphotosharing.json.BooleanResponse;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GroupDetailActivity extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_GROUP_DETAILS = 1;
	private static final int CODE_GROUP_PHOTOS = 2;
	private static final int CODE_JOIN = 3;
	private static final int CODE_LEAVE = 4;
	private static final int CODE_INVITE = 5;
	private static final int CODE_OWNER = 6;
	
	private static final int DIALOG_INFO = 0;
	
	public static final String KEY_ID = "id";
	
	// update group info after 1s
	public static final int UPDATE_AFTER = 2000; //ms
	private Handler mHandler = new Handler();
	
	//gallery object
	private GridView gridView;
	//image view for larger display
	
	//private TextView groupPhotos;
	private TextView groupMembers;
	private TextView groupName;
	private ImageView groupIcon;

	// container for the group members
	private String members;
	
	//private ImageView privateIcon, locationIcon;
	
	private ImageLoader imageLoader;
			
	private long id;
	private boolean isMember;
	
	private Group group;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_detail);
        super.onCreate(savedInstanceState);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        groupName = (TextView) findViewById(R.id.header_title);
        groupIcon = (ImageView) findViewById(R.id.app_icon);
        //groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
        groupMembers = (TextView) findViewById(R.id.header_subtext);
        groupMembers.setText(getResources().getString(R.string.group_detail_info));
        
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);
        
        //privateIcon = (ImageView) findViewById(R.id.private_icon);
        //locationIcon = (ImageView) findViewById(R.id.location_icon);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        // Set the click listener for each item in the thumbnail gallery     
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);

        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), home);
        
        isMember = true;
        group = null;
        
        // Show the subtext in the header
        InfoClickListener icl = new InfoClickListener();
        groupMembers.setOnClickListener(icl);
        groupName.setOnClickListener(icl);
        Util.showSubHeader(groupName, groupMembers);
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
	public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.group_detail_menu, menu);
		super.onCreateOptionsMenu(menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
        switch (item.getItemId()) {
	        case R.id.invite:
	        	intent = new Intent(this, AddFriendsActivity.class);
	        	intent.putExtra("id", id);
	        	intent.putExtra("type", AddFriendsActivity.TYPE_GROUP);
	        	startActivityForResult(intent, CODE_INVITE);
	        	return true;
	        case R.id.manage:
	        	intent = new Intent(this, GroupManageActivity.class);
	        	intent.putExtra("id", id);
	        	startActivity(intent);	        	
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }
	
	/*
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
    }*/
    
    public void onClickCamera(View view) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
        String ownerUrl = String.format(Util.getUrl(this,R.string.groups_http_owner),hash,id);		
        new FetchJSON(this, CODE_OWNER).execute(ownerUrl);
    	
    	
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

    public void onLocationHelp(View v) {
    	Util.createSimpleDialog(this, getResources().getString(R.string.dialog_location));
    }
    
    public void onPrivateHelp(View v) {
    	Util.createSimpleDialog(this, getResources().getString(R.string.dialog_private));
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
        
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        if (requestCode == CODE_INVITE) {
        	if (resultCode == RESULT_OK) {
        		String friendIds = data.getStringExtra("friends");
	        	Log.d("LIST", "Friends: " + friendIds);
	        	
	    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
	    		String hash = settings.getString(Login.SESSION_HASH, null);

	            String inviteUrl = Util.getUrl(this,R.string.groups_http_invite);	
	            
	            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
	            try {
	    			map.put("sid", new StringBody(hash));
	    	        map.put("gid", new StringBody(Long.toString(group.getId())));
	    	        if (friendIds != "") {
	    	        	map.put("members", new StringBody(friendIds));
	    	        }
	    		} catch (UnsupportedEncodingException e) {
	    			e.printStackTrace();
	    		}
	            
	            PostData pr = new PostData(inviteUrl,map);
	    		new PostRequest(this,CODE_INVITE).execute(pr);
	        }
        }
    }
    
    private class InfoClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
	    	showDialog(DIALOG_INFO);			
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
	    	intent.putExtra("gid", group.getId());
	    	startActivity(intent);
	    }
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
			case CODE_INVITE:
				parseInvite(result);
				break;
			case CODE_LEAVE:
				parseLeave(result);
				break;
			case CODE_OWNER:
				parseOwner(result);
				break;
			default:
		}
	}
	
	private void parseOwner(String json) {
		Gson gson = new Gson();
		BooleanResponse response = gson.fromJson(json, BooleanResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Intent intent = new Intent(this,GroupManageActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseInvite(String json) {

		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
			case(Util.STATUS_OK):
				Toast.makeText(this, "Users invited to group", Toast.LENGTH_SHORT).show();
				loadData();
				break;
								
			default:
				Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}
	
	private void parseLeave(String json) {

		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
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
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
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
					photo_list
		));

        gridView.setOnItemClickListener(new MyOnItemClickListener(this)); 
        
		// Set the string telling how many photos the group has
		//String photos = String.format(getResources().getString(R.string.group_detail_photos), photo_list.size());
		//groupPhotos.setText(photos);
		
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		
		GroupResponse gdr = gson.fromJson(result, GroupResponse.class);
		
		if (gdr.getStatus() == Util.STATUS_OK) {
			group = gdr.getObject();
	
			groupName.setText(group.name);
			imageLoader.displayImage(Util.getThumbUrl(group), groupIcon);
	
			/*
			if (!group.isPrivate()) {
				privateIcon.setVisibility(ImageView.GONE);
			}

			if (!group.isLocationLocked()) {
				locationIcon.setVisibility(ImageView.GONE);
			}
			*/
			
			// Set the button text join/leave group
			if (group.member == 0) {
				isMember = false;
			} else {
				isMember = true;				
			}
			
			if (group.users != null) {
				if (group.users.size() > 0) {
					
					StringBuilder b = new StringBuilder();
					for(User u : group.users){
						if (b.length() > 0) b.append(", ");
						b.append(u.rname);
					}
					members = b.toString();

		            mHandler.removeCallbacks(mUpdateTimeTask);
		            mHandler.postDelayed(mUpdateTimeTask, UPDATE_AFTER);
				}
			}
			// Set the string telling how many members the group has
			//String photos = String.format(getResources().getString(R.string.group_detail_members), group.members);
			//groupMembers.setText(photos);
		} else {
			Toast.makeText(this, gdr.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
	
	private Runnable mUpdateTimeTask = new Runnable() {
	   public void run() {
	       groupMembers.setText(members);		     
	   }
	};
}
