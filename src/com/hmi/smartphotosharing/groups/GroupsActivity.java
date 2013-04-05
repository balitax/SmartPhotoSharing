package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.hmi.smartphotosharing.local.MapActivity;
import com.hmi.smartphotosharing.subscriptions.SubscriptionsActivity;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class GroupsActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_GROUPS = 0;
        
	private ImageLoader imageLoader;
	    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.groups);
        super.onCreate(savedInstanceState);
                        
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

        // Pauses the loading of image to get smoother scrolling
        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, true, true);
        getListView().setOnScrollListener(listener);
        
        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), home);
                
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

        // Load data
        loadData();
    }

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.group_menu, menu);
		super.onCreateOptionsMenu(menu);

	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intent;
		switch (item.getItemId()) {

        	case R.id.group_new:
                intent = new Intent(getApplicationContext(), GroupCreateActivity.class);
                startActivity(intent);	
        		return true;
        	case R.id.group_join:
                intent = new Intent(getApplicationContext(), GroupJoinActivity.class);
                startActivity(intent);	
        		return true;
	        case R.id.refresh:
	        	loadData();
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http),hash);
		new FetchJSON(this,CODE_GROUPS).execute(groupsUrl);

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            loadData();
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
			
		default:
		}
	}


	private void parseGroups(String result) {
		
		Gson gson = new Gson();
		GroupListResponse response = gson.fromJson(result, GroupListResponse.class);
		
		if (response != null) {
			List <Group> group_list = response.getObject();
			
			if (group_list == null) {
				ListView listView = getListView();
				TextView emptyView = (TextView) listView.getEmptyView();
				emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
				emptyView.setText(getResources().getString(R.string.groups_empty));
			} else {

				// Sort the group on newest
				GroupAdapter adapter = new GroupAdapter(
						this, 
						R.layout.group_item, 
						group_list,
						imageLoader
					);
				adapter.sort(Sorter.GROUP_SORTER_UPDATES);
				setListAdapter(adapter);	
			}
		}
	}
 
}