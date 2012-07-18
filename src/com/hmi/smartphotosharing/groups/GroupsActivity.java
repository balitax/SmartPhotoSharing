package com.hmi.smartphotosharing.groups;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupsResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.ProfileResponse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SettingsActivity;
import com.hmi.smartphotosharing.camera.CameraActivity;

public class GroupsActivity extends ListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_PROFILE = 1;
    private static final int CODE_GROUPS = 2;
    
	private DrawableManager dm;
	private TextView name;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups);
        
        name = (TextView) findViewById(R.id.groups_name);
        dm = new DrawableManager(this);
        loadData();
    }
    	
	@Override
	public void onStart() {
        super.onStart();
        
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
			    startActivity(intent);	
		        return true;
	        case R.id.settings:
	            intent = new Intent(this, SettingsActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
		        return true;
	        case R.id.create_group:
	        	intent = new Intent(this, GroupCreateActivity.class);
	        	startActivityForResult(intent, CREATE_GROUP);
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String profileUrl = String.format(getResources().getString(R.string.profile_http),hash);		
        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);

        String groupsUrl = String.format(getResources().getString(R.string.groups_http),hash);
		new FetchJSON(this,CODE_GROUPS).execute(groupsUrl);

	}
		
	public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP) {
            if (resultCode == Activity.RESULT_OK) {
            	// TODO : refresh group list
            	Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show();
            }
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
		ProfileResponse response = gson.fromJson(result, ProfileResponse.class);
		
		name.setText(response.msg.getName());
		
	}

	private void parseGroups(String result) {
		
		Gson gson = new Gson();
		GroupsResponse gr = gson.fromJson(result, GroupsResponse.class);
		
		List <Group> group_list = gr.getGroupsList();
		
		setListAdapter(new GroupAdapter(
							this, 
							R.layout.list_item, 
							group_list.toArray(new Group[group_list.size()]),
							dm
						));
			
	}
 
}