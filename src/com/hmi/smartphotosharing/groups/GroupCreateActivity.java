package com.hmi.smartphotosharing.groups;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;

public class GroupCreateActivity extends Activity implements OnDownloadListener {

	private static final int CODE_LOCATION = 1;
	private static final int CODE_INVITE = 2;
	
	private String friendIds;
	
	private double lat1, lon1, lat2, lon2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_create);
        
        // Make the Dialog style appear fullscreen
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        friendIds = "";
    }
    	    
    public void onClickFriends(View view) {
    	Intent intent = new Intent(this,SelectFriendsActivity.class);
    	startActivityForResult(intent, CODE_INVITE);
    }
    
    public void onClickLocation(View view) {
    	Intent intent = new Intent(this,SelectLocationActivity.class);
    	startActivityForResult(intent, CODE_LOCATION);
    }	
    
    public void onLocationHelp(View v) {
    	Util.createSimpleDialog(this, getResources().getString(R.string.dialog_location));
    }
    
    public void onPrivateHelp(View v) {
    	Util.createSimpleDialog(this, getResources().getString(R.string.dialog_private));
    }
    
	public void onCreateClick(View v) {

		CheckBox locationCheck = (CheckBox) findViewById(R.id.checkbox_location);
		boolean locationLocked = locationCheck.isChecked();
		
		boolean noLocationSelected = lat1 == 0 && lat2 == 0 && lon1 == 0 && lon2 == 0;
		if (locationLocked && noLocationSelected) {
			Util.createSimpleDialog(this, getResources().getString(R.string.dialog_location_error));
		} else {
			postData();
		}
	} 
	
	private void postData() {
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		EditText nameView = (EditText) findViewById(R.id.group_create_name);
		String name = nameView.getText().toString();

		EditText descView = (EditText) findViewById(R.id.group_create_desc);
		String desc = descView.getText().toString();
			
		CheckBox privateCheck = (CheckBox) findViewById(R.id.checkbox_private);
		String isPrivate = privateCheck.isChecked() ? "1" : "0";

		CheckBox locationCheck = (CheckBox) findViewById(R.id.checkbox_location);
		String locationLocked = locationCheck.isChecked() ? "1" : "0";
		
    	// Get group info
		String createUrl = Util.getUrl(this,R.string.groups_http_create);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
	        map.put("name", new StringBody(name));
	        map.put("desc", new StringBody(desc));
	        map.put("lat1", new StringBody(Double.toString(lat1)));
	        map.put("lat2", new StringBody(Double.toString(lat2)));
	        map.put("lon1", new StringBody(Double.toString(lon1)));
	        map.put("lon2", new StringBody(Double.toString(lon2)));
	        map.put("private", new StringBody(isPrivate));
	        map.put("locationlink", new StringBody(locationLocked));
	        
	        if (!friendIds.equals("")) {
	        	map.put("members", new StringBody(friendIds));
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
        		friendIds = data.getStringExtra("friends");
	        	Log.d("LIST", "Friends: " + friendIds);
	        		        	
	        	friendsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on,0,0,0);
	        	
        	} else {
        		friendIds = "";
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
        		lat1 = lat2 = lon1 = lon2 = 0;
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
