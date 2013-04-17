package com.hmi.smartphotosharing.friends;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.GroupCreateActivity;
import com.hmi.smartphotosharing.groups.GroupJoinActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserListResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class AddFriendsActivity extends ListActivity implements OnDownloadListener {
	private ListView listView;
		
	public static final int CODE_USERS = 1;
	private long gid;
	private long type;
	
	public static final int TYPE_GROUP = 1;
	public static final int TYPE_FRIENDS = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.invite_friends);
                
        Intent intent = getIntent();
        
        type = intent.getIntExtra("type", 0);
        
        if (type == TYPE_GROUP)
        	gid = intent.getLongExtra("gid", 0);
        	
        listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        
        listView.setOnItemClickListener (new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
				listView.invalidateViews();
			}
        	
        });
	    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	    // Get the intent, verify the action and get the query
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY).trim();
	    	doMySearch(query);
	    }
    }
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.friend_add_menu, menu);
		super.onCreateOptionsMenu(menu);

	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {

        	case R.id.search:
        		onSearchRequested();
        		return true;
	        case R.id.refresh:
	        	loadData();
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	public void onClickSearch(View view) {
		onSearchRequested();
	}
	
	@Override
	public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     
	     //@TODO PUT GPS DATA IN HERE
	     //appData.putBoolean(SearchableActivity.JARGON, true);
	     
	     startSearch(null, false, appData, false);
	     return true;
	 }
	
	private void doMySearch(String query) {
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		loadFriendsData(hash, query);
	}	
	
	@Override
	public void onStart() {
        super.onStart();
        loadData();
        
        if (type == TYPE_GROUP)
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
    	
    	String friends = sb.toString();
    	Log.d("Friends", friends);
    	data.putExtra("friends", friends);
    	this.setResult(RESULT_OK, data);
    	this.finish();
    }
    
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (type == TYPE_GROUP)
			loadGroupData(hash);
		else if (type == TYPE_FRIENDS)
			loadFriendsData(hash);

	}
	
	private void loadFriendsData(String hash) {
		loadFriendsData(hash, null);
	}
	
	private void loadFriendsData(String hash, String query) {

        String usersUrl = Util.getUrl(this,R.string.friends_http_invite);
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
			
			if (query != null) {
				map.put("q", new StringBody(query));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(usersUrl,map);
		new PostRequest(this,CODE_USERS).execute(pr);
		
	}

	private void loadGroupData(String hash) {

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
			
			FriendsInviteAdapter adapter = new FriendsInviteAdapter(
					this, 
					R.layout.friends_item, 
					userList,
					listView
				);
			
			adapter.sort(Sorter.USER_SORTER);
			setListAdapter(adapter);	
		}
	}
	
}
