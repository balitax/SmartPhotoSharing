package com.hmi.smartphotosharing.subscriptions;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.SelectLocationActivity;
import com.hmi.smartphotosharing.groups.SelectSingleFriendActivity;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;

public class SubscriptionCreateActivity extends Activity implements OnDownloadListener {

	private static final int CODE_LOCATION = 1;
	private static final int CODE_INVITE = 2;
	
	private long friend;
	
	private double lat1, lon1, lat2, lon2;

	boolean locationSet, friendSet;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.subscription_create);
        
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        friendSet = false;
        locationSet = false;
    }

    public void onClickFriends(View view) {
    	Intent intent = new Intent(this,SelectSingleFriendActivity.class);
    	startActivityForResult(intent, CODE_INVITE);
    }
    
    public void onClickLocation(View view) {
    	Intent intent = new Intent(this,SelectLocationActivity.class);
    	startActivityForResult(intent, CODE_LOCATION);
    }	
    
	public void onCreateClick(View v) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		EditText nameView = (EditText) findViewById(R.id.sub_create_name);
		String name = nameView.getText().toString();
		
    	// Get subscription info
		String createUrl = Util.getUrl(this,R.string.subscriptions_http_create);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
	        map.put("name", new StringBody(name));

	        if (friendSet) {
		        map.put("person", new StringBody(Long.toString(friend)));
	        }
	        
	        if (locationSet) {
		        map.put("lat1", new StringBody(Double.toString(lat1)));
		        map.put("lat2", new StringBody(Double.toString(lat2)));
		        map.put("lon1", new StringBody(Double.toString(lon1)));
		        map.put("lon2", new StringBody(Double.toString(lon2)));
	        }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(createUrl,map);
		
		Log.d("SmarthPhotoSharing", "Create url: " + createUrl);
		new PostRequest(this).execute(pr);
		
	} 
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Button friendsBtn = (Button) findViewById(R.id.button_invite_friends);
    	
        if (requestCode == CODE_INVITE) {
        	if (resultCode == RESULT_OK) {
        		friendSet = true;
	        	friend = data.getLongExtra("friend", 0);
	        	Log.d("LIST", "Friend: " + friend);
	        	
	        	friendsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
	        	
        	} else {
        		friendSet = false;
	        	friendsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_invite,0,0,0);        		
        	}
        }
        
        else if (requestCode == CODE_LOCATION) {

        	Button locationBtn = (Button) findViewById(R.id.button_location);
        	
        	if (resultCode == RESULT_OK) {
        		locationSet = true;
	        	lat1 = data.getDoubleExtra("lat1", 0);
	        	lon1 = data.getDoubleExtra("lon1", 0);
	        	
	        	lat2 = data.getDoubleExtra("lat2", 0);
	        	lon2 = data.getDoubleExtra("lon2", 0);
	
	        	locationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
	        	
        	} else {
        		locationSet = false;
	        	locationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_mylocation,0,0,0);
        		
        	}
        }
        
    }	


	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		Log.i("Json parse", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
        	Toast.makeText(this, "Subscription created", Toast.LENGTH_SHORT).show();
        	Intent data = new Intent();

    		EditText nameView = (EditText) findViewById(R.id.sub_create_name);
    		String name = nameView.getText().toString();
        	data.putExtra("name", name);
        	
    		setResult(RESULT_OK, data);
    		finish();
		} else {
        	Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();	
		}
		
	}

}
