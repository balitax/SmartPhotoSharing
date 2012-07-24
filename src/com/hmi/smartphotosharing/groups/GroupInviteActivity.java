package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.StringRepsonse;
import com.hmi.json.User;
import com.hmi.json.UserListResponse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;

public class GroupInviteActivity extends ListActivity implements OnDownloadListener {

	private DrawableManager dm;
	private long id;
	
	private static final int CODE_USERS = 1;
	private static final int CODE_INVITE = 2;
	
	private static final int STATUS_OK = 200;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        
        setContentView(R.layout.join_group);
        dm = new DrawableManager(this);
    }
    	
	@Override
	public void onStart() {
        super.onStart();
        
	}
	
    @Override
    public void onResume() {
      super.onResume();
      
      // Refresh groups list
      loadData();
    }  
    	    
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String usersUrl = String.format(getResources().getString(R.string.groups_http_users),hash,id);		
        new FetchJSON(this, CODE_USERS).execute(usersUrl);

	}
				
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {
		switch (code) {
		case CODE_USERS:
			parseUsers(result);
			break;
			
		case CODE_INVITE:
			parseInvite(result);
			break;
			
		default:
		}
	}
	
	private void parseInvite(String json) {

		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response != null) {
			switch(response.status) {
			
			case(STATUS_OK):
				Toast.makeText(this, "User invited to group", Toast.LENGTH_SHORT).show();
	    		setResult(RESULT_OK);
	    		finish();
				break;
								
			default:
				Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}

	public void parseUsers(String json) {

		Gson gson = new Gson();
		UserListResponse response = gson.fromJson(json, UserListResponse.class);
		
		if (response != null) {
			List<User> userList = response.getUserList();
			if (userList == null) userList = new ArrayList<User>();
			
			setListAdapter(new UserAdapter(
								this, 
								R.layout.list_item, 
								userList,
								dm
							));	
		}
	}

	@Override
	protected void onListItemClick (ListView l, View v, int position, long uid) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		Log.d("GroupInvite", "User ID: " + uid);
        String inviteUrl = String.format(getResources().getString(R.string.groups_http_invite),hash,id,uid);		
        new FetchJSON(this, CODE_INVITE).execute(inviteUrl);
		
	}
}
