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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserListResponse;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class SelectFriendsActivity extends ListActivity implements OnDownloadListener {
	private ImageLoader dm;
	private ListView listView;
		
	private static final int CODE_USERS = 1;
	private long gid;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.invite_friends);
        dm = new ImageLoader(this);
        
        Intent intent = getIntent();
        gid = intent.getLongExtra("gid", 0);
        
        listView = (ListView) findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    	
	@Override
	public void onStart() {
        super.onStart();
        loadData();
        
        Util.createSimpleDialog(this, getResources().getString(R.string.dialog_friends));
	}
	
    @Override
    public void onResume() {
      super.onResume();
      
      // Refresh groups list
    }  
    
	@Override
	public void onBackPressed() {
	    this.setResult(RESULT_CANCELED);
	    finish();
	}
	
    public void onSendClick(View view) {
    	Intent data = new Intent();
    	
    	SparseBooleanArray items = listView.getCheckedItemPositions();
    	
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for (int i = 0; i < items.size(); i++) {
    		if (items.valueAt(i)) {
    			if (first)
    				first = false;
    			else
    				sb.append(",");
    			sb.append(listView.getItemIdAtPosition(items.keyAt(i)));
    		}
    	}
    	
    	data.putExtra("friends", sb.toString());
    	this.setResult(RESULT_OK, data);
    	this.finish();
    }
    
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String usersUrl = Util.getUrl(this,R.string.groups_http_users);
        
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
	        if (gid != 0) {
	        	map.put("gid", new StringBody(Long.toString(gid)));
	        }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
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

        Log.d("FRIENDS", json);
		Gson gson = new Gson();
		UserListResponse response = gson.fromJson(json, UserListResponse.class);
		
		if (response != null) {
			List<User> userList = response.getObject();
			if (userList == null) userList = new ArrayList<User>();
			
			FriendsAdapter adapter = new FriendsAdapter(
					this, 
					R.layout.friends_item, 
					userList,
					dm,
					listView
				);
			
			adapter.sort(Sorter.USER_SORTER);
			setListAdapter(adapter);	
		}
	}
	
}
