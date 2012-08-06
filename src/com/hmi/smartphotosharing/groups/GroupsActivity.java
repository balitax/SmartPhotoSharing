package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GroupsActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_PROFILE = 1;
    private static final int CODE_GROUPS = 2;
        
	private ImageLoader imageLoader;
	
	private ImageView add,groups;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.groups);
        super.onCreate(savedInstanceState);
        
        add = (ImageView) findViewById(R.id.group_add);
        add.setOnClickListener(new MyClickListener(this,GroupCreateActivity.class));
        groups = (ImageView) findViewById(R.id.all_groups);
        groups.setOnClickListener(new MyClickListener(this,GroupJoinActivity.class));
                
        imageLoader = ImageLoader.getInstance();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.ic_launcher)
            .cacheInMemory()
            .cacheOnDisc()
            	.build();

        // Set the config for the ImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .threadPoolSize(5)
        .threadPriority(Thread.MIN_PRIORITY+2)
        .defaultDisplayImageOptions(options)
        .build();

        // Init ImageLoader
        imageLoader.init(config);
        
        // Load data
        loadData(true, true);
        
    }
    
    public class MyClickListener implements OnClickListener {
    	private Context c;
    	private Class<? extends Activity> cls;
    	
    	public MyClickListener(Context c, Class<? extends Activity> cls) {
    		this.c = c;
    		this.cls = cls;
    	}
    	
    	@Override
    	public void onClick(View v) {	
    		Intent intent = new Intent(c,cls);
    		c.startActivity(intent);
    	}

    }

	@Override
	public void onStart() {
        super.onStart();
        
	}
	
    @Override
    public void onResume() {
      super.onResume();
      
      // Refresh groups list
      loadData(false, true);
    }  
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.group_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        case R.id.refresh:
	        	loadData(false,true);
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void loadData(boolean profile, boolean groups) {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (profile) {
	        String profileUrl = String.format(Util.getUrl(this,R.string.profile_http),hash);		
	        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);
		}

		if (groups) {
			String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http),hash);
			new FetchJSON(this,CODE_GROUPS).execute(groupsUrl);
		}

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            loadData(false,true);
        }
    }	

	/**
	 * Checks whether there is a network connection available
	 * @return true if the device is connected to a network
	 */
	public boolean hasNetwork() {
		// Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        return networkInfo != null && networkInfo.isConnected();
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
			
		case CODE_PROFILE:
			parseProfile(result);
			break;
			
		default:
		}
	}

	private void parseProfile(String result) {
		Gson gson = new Gson();
		UserResponse response = gson.fromJson(result, UserResponse.class);
		User user = response.getObject();
		
		// Set the user name
		TextView name = (TextView) findViewById(R.id.groups_name);
		name.setText(user.getName());
		
		// Set the user name
		TextView stats = (TextView) findViewById(R.id.stats);
		stats.setText(String.format(this.getResources().getString(R.string.profile_stats), user.groups, user.photos));
		
		// Set the user icon
		String userPic = Util.USER_DB + user.picture;
		ImageView pic = (ImageView) findViewById(R.id.groups_icon);
		//dm.fetchDrawableOnThread(userPic, pic);
		imageLoader.displayImage(userPic, pic);
	}

	private void parseGroups(String result) {
		
		Gson gson = new Gson();
		GroupListResponse gr = gson.fromJson(result, GroupListResponse.class);
		
		if (gr != null) {
			List <Group> group_list = gr.getObject();
			
			// Sort the group on newest
			
			if (group_list == null) group_list = new ArrayList<Group>();
			
			GroupAdapter adapter = new GroupAdapter(
					this, 
					R.layout.group_item, 
					group_list,
					imageLoader
				);
			adapter.sort(Sorter.GROUP_SORTER);
			setListAdapter(adapter);	
		}
	}
 
}