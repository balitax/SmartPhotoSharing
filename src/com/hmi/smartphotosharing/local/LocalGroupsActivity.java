package com.hmi.smartphotosharing.local;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.GroupAdapter;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class LocalGroupsActivity extends NavBarListActivity implements OnDownloadListener {
	    
    public static final String LAT = "LATITUDE";
    public static final String LON = "LONGITUDE";
        
	private ImageLoader imageLoader;
	    
	private LatLng point;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.local_groups);
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
        
        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.local);
        Util.setSelectedBackground(getApplicationContext(), home);

        Intent intent = getIntent();
        point = new LatLng(intent.getDoubleExtra(LAT, 0), intent.getDoubleExtra(LON, 0));
        
        // Load data
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
	
	private void loadData() {

		// Get data about this point
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String url = Util.getUrl(getApplicationContext(),R.string.local_http_point);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
        	
			map.put("sid",new StringBody(hash));
			
			map.put("lat", new StringBody(Double.toString(point.latitude)));
			map.put("lon", new StringBody(Double.toString(point.longitude)));
			
		} catch (UnsupportedEncodingException e) {
			Log.e("Map activity", e.getMessage());
		}
        PostData pr = new PostData(url,map);
        new PostRequest(this).execute(pr);

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
		parseGroups(result);
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