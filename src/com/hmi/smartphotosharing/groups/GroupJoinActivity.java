package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.StringRepsonse;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Util;

public class GroupJoinActivity extends ListActivity implements OnDownloadListener{

	private ImageLoader dm;
	public static final int CODE_GROUPS = 1;
	public static final int CODE_JOIN = 2;
	
	private static final int STATUS_OK = 200;
	private static final int STATUS_FORBIDDEN = 403;
	private static final int STATUS_404 = 404;
	private static final int STATUS_FAILED = 500;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group);
        dm = new ImageLoader(this);
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
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
			case(STATUS_OK):
				Toast.makeText(this, "Joined group", Toast.LENGTH_SHORT).show();
	    		setResult(RESULT_OK);
	    		finish();
				break;
				
			case(STATUS_404):
				Toast.makeText(this, "Group does not exist", Toast.LENGTH_SHORT).show();
				break;
				
			case(STATUS_FORBIDDEN):
				Toast.makeText(this, "This group is private, you need an invite", Toast.LENGTH_SHORT).show();
				break;	
				
			case(STATUS_FAILED):
				Toast.makeText(this, "You already are a member", Toast.LENGTH_SHORT).show();
				break;	
				
			default:
				Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}

	public void parseGroups(String json) {

		Gson gson = new Gson();
		GroupListResponse gr = gson.fromJson(json, GroupListResponse.class);
		
		if (gr != null) {
			List <Group> group_list = gr.getObject();
			if (group_list == null) group_list = new ArrayList<Group>();
			
			setListAdapter(new GroupJoinAdapter(
								this, 
								R.layout.list_item, 
								group_list.toArray(new Group[group_list.size()]),
								dm
							));	
		}
	}

}
