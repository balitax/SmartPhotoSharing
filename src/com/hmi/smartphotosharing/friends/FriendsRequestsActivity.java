package com.hmi.smartphotosharing.friends;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.SelectFriendsActivity;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserListResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class FriendsRequestsActivity extends NavBarListActivity implements OnDownloadListener {

	public static final String KEY_ID = "com.hmi.smartphotosharing.friends.uid";
	
    public static final int CODE_FRIENDS = 1;
    public static final int CODE_REQUESTS = 2;
    public static final int CODE_CONFIRM = 3;

	private static final int CODE_INVITE = 0;
	
    private ImageLoader imageLoader;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.friends_requests);
        super.onCreate(savedInstanceState);
        
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

        // Pauses the loading of image to get smoother scrolling
        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, true, true);
        getListView().setOnScrollListener(listener);
        
        loadData();
        
        // Show selection in nav bar
        ImageView img = (ImageView) findViewById(R.id.friends);
        Util.setSelectedBackground(getApplicationContext(), img);
        
    }
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
    public void onClickAdd(View view) {
    	Intent intent = new Intent(this, AddFriendsActivity.class);
    	intent.putExtra("type", AddFriendsActivity.TYPE_FRIENDS);
    	startActivityForResult(intent, CODE_INVITE);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == CODE_INVITE) {
        	if (resultCode == RESULT_OK) {
        		String friendIds = data.getStringExtra("friends");
	        	Log.d("LIST", "Friends: " + friendIds);
	        	
	    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
	    		String hash = settings.getString(Login.SESSION_HASH, null);

	            String url = Util.getUrl(this,R.string.friends_http_add);	
	            
	            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
	            try {
	    			map.put("sid", new StringBody(hash));
	    	        if (friendIds != "") {
	    	        	map.put("friends", new StringBody(friendIds));
	    	        }
	    		} catch (UnsupportedEncodingException e) {
	    			e.printStackTrace();
	    		}
	            
	            PostData pr = new PostData(url,map);
	    		new PostRequest(this,CODE_INVITE).execute(pr);
	        }
        }
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
        
        String url = String.format(Util.getUrl(this,R.string.friends_http_request),hash);		
        new FetchJSON(this,CODE_REQUESTS).execute(url);
		
	}

	@Override
	public void parseJson(String json, int code) {
		Log.d("FriendsActivity", json);
		switch (code) {
			case CODE_REQUESTS:
				parseRequests(json);
				break;

			case CODE_CONFIRM:
				parseConfirm(json);
				break;	
			default:
		}
		
	}
	
	private void parseConfirm(String json) {
		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json,  StringResponse.class);
		
		if (response != null) {
			Toast.makeText(getApplicationContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
		}
	}

	private void parseRequests(String result) {
		Gson gson = new Gson();
		UserListResponse response = gson.fromJson(result, UserListResponse.class);
		
		if (response != null) {
			List<User> user_list = response.getObject();
			
			if (user_list != null) {
				// Sort the group on newest
				FriendsRequestAdapter adapter = new FriendsRequestAdapter(
						this, 
						R.layout.friends_request_item, 
						user_list,
						imageLoader
					);
				adapter.sort(Sorter.USER_SORTER);
				setListAdapter(adapter);	
			}
		}
	}


}
