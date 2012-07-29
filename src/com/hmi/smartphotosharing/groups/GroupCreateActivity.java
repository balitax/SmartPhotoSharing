package com.hmi.smartphotosharing.groups;

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
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.StringRepsonse;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.Util;

public class GroupCreateActivity extends Activity implements OnDownloadListener {

	private static final int CODE_LOCATION = 1;
	private static final int CODE_INVITE = 2;
	
	private long[] friends;
	
	private double lat1, lon1, lat2, lon2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create);
        
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        friends = null;
    }
    	    
    public void onClickFriends(View view) {
    	Intent intent = new Intent(this,SelectFriendsActivity.class);
    	startActivityForResult(intent, CODE_INVITE);
    }
    
    public void onClickLocation(View view) {
    	Intent intent = new Intent(this,SelectLocationActivity.class);
    	startActivityForResult(intent, CODE_LOCATION);
    }	
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Button friendsBtn = (Button) findViewById(R.id.button_invite_friends);
    	
        if (requestCode == CODE_INVITE) {
        	if (resultCode == RESULT_OK) {
	        	friends = data.getLongArrayExtra("friends");
	        	Log.d("LIST", "Friends: " + friends.length);
	        	
	        	friendsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
	        	
        	} else {
	        	friendsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_invite,0,0,0);        		
        	}
        }
        
        else if (requestCode == CODE_LOCATION) {

        	Button locationBtn = (Button) findViewById(R.id.button_location);
        	
        	if (resultCode == RESULT_OK) {
	        	lat1 = data.getDoubleExtra("lat1", 0);
	        	lon1 = data.getDoubleExtra("lon1", 0);
	        	
	        	lat2 = data.getDoubleExtra("lat2", 0);
	        	lon2 = data.getDoubleExtra("lon2", 0);
	
	        	locationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
	        	
        	} else {
	        	locationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_mylocation,0,0,0);
        		
        	}
        }
        
    }	
	
	public void onCreateClick(View v) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		Log.d("Session ID", hash);
		EditText nameView = (EditText) findViewById(R.id.group_create_name);
		String name = nameView.getText().toString();

		EditText descView = (EditText) findViewById(R.id.group_create_desc);
		String desc = descView.getText().toString();
				
    	// Get group info
		String createUrl = String.format(Util.getUrl(this,R.string.groups_http_create),hash,name,desc,lat1,lon1,lat2,lon2);
		Log.d("SmarthPhotoSharing", "Create url: " + createUrl);
		new FetchJSON(this).execute(createUrl);
		
	}

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		Log.i("Json parse", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
        	Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show();
        	Intent data = new Intent();

    		EditText nameView = (EditText) findViewById(R.id.group_create_name);
    		String name = nameView.getText().toString();
        	data.putExtra("name", name);
        	
    		setResult(RESULT_OK, data);
    		finish();
		} else {
        	Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();	
		}
		
	}

}
