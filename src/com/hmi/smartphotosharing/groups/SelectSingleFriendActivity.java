package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserListResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class SelectSingleFriendActivity extends NavBarListActivity implements OnDownloadListener {

	private long id;
	
	private static final int CODE_USERS = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.select_friend);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        
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

        String usersUrl = String.format(Util.getUrl(this,R.string.groups_http_users_invite),hash,id);		
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
						
		default:
		}
	}
	
	public void parseUsers(String json) {

		Gson gson = new Gson();
		UserListResponse response = gson.fromJson(json, UserListResponse.class);
		
		if (response != null) {
			List<User> userList = response.getObject();
			if (userList == null) userList = new ArrayList<User>();
			
			UserAdapter adapter = new UserAdapter(
					this, 
					R.layout.select_friend, 
					userList
				);
			
			adapter.sort(Sorter.USER_SORTER);
			setListAdapter(adapter);	
		}
	}

	@Override
	protected void onListItemClick (ListView l, View v, int position, long uid) {
    	Intent data = new Intent();

    	data.putExtra("friend", uid);
    	this.setResult(RESULT_OK, data);
    	this.finish();
		
	}
}
