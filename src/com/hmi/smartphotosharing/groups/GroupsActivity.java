package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupListResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.User;
import com.hmi.json.UserResponse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SettingsActivity;
import com.hmi.smartphotosharing.camera.CameraActivity;
public class GroupsActivity extends ListActivity implements OnDownloadListener, OnGroupClickListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_PROFILE = 1;
    private static final int CODE_GROUPS = 2;
    private static final int CODE_PHOTO = 3;
    
    private static final int JOIN_GROUP = 6;
    
	private DrawableManager dm;
	private TextView name;
	private ImageView pic;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups);
        
        name = (TextView) findViewById(R.id.groups_name);
        pic = (ImageView) findViewById(R.id.groups_icon);
        
        dm = new DrawableManager(this);
        loadData(true, true);
        
    }
    	
	@Override
	public void onStart() {
        super.onStart();
        
	}
	
    @Override
    public void onResume() {
      super.onResume();
      
      // Refresh groups list
      loadData(false, true);
    }  
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.group_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
        switch (item.getItemId()) {
	        case R.id.camera:
				intent = new Intent(this, CameraActivity.class);
			    startActivityForResult(intent, CODE_PHOTO);	
	        	return true;
	        case R.id.settings:
	            intent = new Intent(this, SettingsActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
		        return true;
	        case R.id.refresh:
	        	loadData(false,true);
		    	return true;
	        case R.id.create_group:
	        	intent = new Intent(this, GroupCreateActivity.class);
	        	startActivityForResult(intent, CREATE_GROUP);
		    	return true;
	        case R.id.join_group:
	        	intent = new Intent(this, GroupJoinActivity.class);
	        	startActivityForResult(intent, JOIN_GROUP);
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void loadData(boolean profile, boolean groups) {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (profile) {
	        String profileUrl = String.format(getResources().getString(R.string.profile_http),hash);		
	        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);
		}

		if (groups) {
			String groupsUrl = String.format(getResources().getString(R.string.groups_http),hash);
			new FetchJSON(this,CODE_GROUPS).execute(groupsUrl);
		}

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            loadData(false,true);
        }
    }	

	/**
	 * Checks whether there is a network connection available
	 * @return true if the device is connected to a network
	 */
	public boolean hasNetwork() {
		// Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        return networkInfo != null && networkInfo.isConnected();
	}
		
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {
		
		switch (code) {
		case CODE_GROUPS:
			parseGroups(result);
			break;
			
		case CODE_PROFILE:
			parseProfile(result);
			break;
			
		default:
		}
	}

	private void parseProfile(String result) {
		Gson gson = new Gson();
		UserResponse response = gson.fromJson(result, UserResponse.class);
		User user = response.msg;
		
		// Set the user name
		name.setText(response.msg.getName());
		
		// Set the user icon
		String userPic = getResources().getString(R.string.group_http_logo) + user.picture;
		dm.fetchDrawableOnThread(userPic, pic);
	}

	private void parseGroups(String result) {
		
		Gson gson = new Gson();
		GroupListResponse gr = gson.fromJson(result, GroupListResponse.class);
		
		if (gr != null) {
			List <Group> group_list = gr.getGroupsList();
			if (group_list == null) group_list = new ArrayList<Group>();
			
			setListAdapter(new GroupAdapter(
								this, 
								R.layout.list_item, 
								group_list.toArray(new Group[group_list.size()]),
								dm,
								this
							));	
		}
	}
	
	@Override
	public void OnGroupClick(long id) {

    	Intent intent = new Intent(this, GroupDetailActivity.class);
    	intent.putExtra("id", id);
    	startActivity(intent);
		
	}
	

 
}