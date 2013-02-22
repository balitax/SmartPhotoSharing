package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class GroupJoinActivity extends NavBarListActivity implements OnDownloadListener{

	public static final int CODE_GROUPS = 1;
	public static final int CODE_JOIN = 2;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.join_group);
        super.onCreate(savedInstanceState);
        
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
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.group_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        case R.id.refresh:
	        	loadData();
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
    	SharedPreferences settings = this.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String joinUrl = String.format(Util.getUrl(this,R.string.groups_http_join),hash,id);		
        new FetchJSON(this, GroupJoinActivity.CODE_JOIN).execute(joinUrl);
    }    
    
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http_public),hash);		
        new FetchJSON(this, CODE_GROUPS).execute(groupsUrl);

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
			
		case CODE_JOIN:
			parseJoin(result);
			break;
			
		default:
		}
	}
	
	private void parseJoin(String json) {

		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
			case(Util.STATUS_OK):
				Toast.makeText(this, "Joined group", Toast.LENGTH_SHORT).show();
	    		setResult(RESULT_OK);
	    		finish();
				break;
								
			default:
				Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}

	public void parseGroups(String json) {

		Log.d("JOIN", json);
		Gson gson = new Gson();
		GroupListResponse response = gson.fromJson(json, GroupListResponse.class);
		
		if (response != null) {
			List <Group> group_list = response.getObject();
			if (group_list == null) group_list = new ArrayList<Group>();

			GroupJoinAdapter adapter = new GroupJoinAdapter(
											this, 
											R.layout.join_group_item,
											group_list);
			adapter.sort(Sorter.GROUP_SORTER_ABC);
			setListAdapter(adapter);	
		}
	}

}
