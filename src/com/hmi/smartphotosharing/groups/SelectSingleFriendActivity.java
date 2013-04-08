package com.hmi.smartphotosharing.groups;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.ListActivity;
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
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserListResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

@Deprecated
public class SelectSingleFriendActivity extends ListActivity implements OnDownloadListener {

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

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        String usersUrl = String.format(Util.getUrl(this,R.string.friends_http));	        

        PostData pr = new PostData(usersUrl,map);
		new PostRequest(this,CODE_USERS).execute(pr);

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
					R.layout.select_friend_item, 
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
