package com.hmi.smartphotosharing.groups;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.UserDetailActivity;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GroupInfoActivity extends NavBarListActivity implements OnDownloadListener {

	private static final int CODE_GROUP = 1;
	
	
	public static final String KEY_ID = "id";
			
	//private ImageView privateIcon, locationIcon;
	
	private ImageLoader imageLoader;
			
	private long id;	
	private Group group;

	private TextView groupMembers, groupName;
	private ImageView groupIcon;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_info);
        super.onCreate(savedInstanceState);

        groupName = (TextView) findViewById(R.id.header_title);
        groupIcon = (ImageView) findViewById(R.id.app_icon);
        groupMembers = (TextView) findViewById(R.id.header_subtext);
        
        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);

        ImageView home = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), home);
        
        Util.showSubHeader(groupName, groupMembers);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
    
    @Override
    public void onResume() {
      super.onResume();
      
      // Refresh groups list
      loadData();
    }  
    	    
    public void onClickMap(View view) {

		Intent intent = new Intent(this,ShowLocationActivity.class);
		intent.putExtra("lat1", group.latstart);
		intent.putExtra("lat2", group.latend);
		intent.putExtra("lon1", group.longstart);
		intent.putExtra("lon2", group.longend);
		startActivity(intent);
    }
    
    protected void onListItemClick (ListView l, View v, int position, long id) {
    	Intent intent = new Intent(this, UserDetailActivity.class);
    	intent.putExtra("id", id);
    	startActivity(intent);
    }
    
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
			map.put("gid", new StringBody(Long.toString(id)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        String usersUrl = Util.getUrl(this,R.string.group_http_detail);	        

        PostData pr = new PostData(usersUrl,map);
		new PostRequest(this,CODE_GROUP).execute(pr);

	}
				
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {

		Util.checkLogout(result,this);
		
		switch (code) {
		case CODE_GROUP:
			parseGroup(result);
			break;
						
		default:
		}
	}
	
	public void parseGroup(String json) {

		Gson gson = new Gson();
		GroupResponse response = gson.fromJson(json, GroupResponse.class);
		group = response.getObject();

		groupName.setText(group.name);
		imageLoader.displayImage(Util.getThumbUrl(group), groupIcon);
		
		if (!group.isLocationLocked()) {
			ImageButton map = (ImageButton)findViewById(R.id.btn_map);
			map.setVisibility(ImageButton.INVISIBLE);
		}
		
		groupMembers.setText(group.isPrivate() ? "Private group" : "Public group");
		
		TextView desc = (TextView) findViewById(R.id.group_desc);
		desc.setText("Owner: " + group.owner);
		
		if (response != null) {
			
			UserAdapter adapter = new UserAdapter(
					this, 
					R.layout.select_friend_item, 
					group.users
				);
			
			adapter.sort(Sorter.USER_SORTER);
			setListAdapter(adapter);	
			
		}
	}
}
