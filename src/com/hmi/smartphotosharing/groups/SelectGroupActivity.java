package com.hmi.smartphotosharing.groups;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class SelectGroupActivity extends ListActivity implements OnDownloadListener {

	private static final int TEN_SECONDS = 10 * 1000;
	private static final int CODE_GROUPS = 2;

    private LocationManager mLocationManager;
	private Location gpsLocation;
	private MergeAdapter adapter;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

		// GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
        	Util.createGpsDisabledAlert(this);
        }
        
        setupGps();
        loadData();
    }

    @Override
    public void onPause() {
      super.onPause();
      
      mLocationManager.removeUpdates(listener);
    }
    
	@Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(listener);
    }  
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long gid) {
		Group group = (Group) adapter.getItem(position);
    	String groupName = group.name;

		Intent data = new Intent();
    	data.putExtra("group", gid);
    	data.putExtra("groupName", groupName);
    	this.setResult(RESULT_OK, data);
    	this.finish();
		
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		String groupsUrl = String.format(Util.getUrl(this,R.string.groups_http_locate), hash);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();

    	double lat = 0;
    	double lon = 0;
    	
        if (gpsLocation != null ) {
	        lat = gpsLocation.getLatitude();
	        lon = gpsLocation.getLongitude();
        }
        
        try {
			map.put("sid", new StringBody(hash));
	        map.put("lat", new StringBody(Double.toString(lat)));
	        map.put("lon", new StringBody(Double.toString(lon)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        PostData pr = new PostData(groupsUrl,map);
		
		new PostRequest(this,CODE_GROUPS).execute(pr);		
	}
	
	@Override
	public void parseJson(String result, int code) {
		switch (code) {
		case CODE_GROUPS:
			parseGroups(result);
			break;
						
		default:
		}
	}
	
	private void parseGroups(String json) {

		ListView listView = getListView();
		Gson gson = new Gson();
		GroupListResponse response = gson.fromJson(json, GroupListResponse.class);
		List<Group> list = response.getObject();

		adapter = new MergeAdapter();

		boolean found = false;
		boolean hasLocationLocked = false;
		int otherStart = 0;
		
		// Make sure the list is sorted
		if (list != null) {
			Collections.sort(list, Sorter.GROUP_SORTER_LOC);
				
			// Find where to split the list
			for (int i = 0; !found && i < list.size(); i++) {
				if (list.get(i).isLocationLocked()) {
					hasLocationLocked = true;
					otherStart++;
				} else {
					found = true;
				}
			}
		}
		adapter.addView(buildLabel(R.string.locations));
		
		if (hasLocationLocked) {
			adapter.addAdapter(buildLocAdapter(list.subList(0, otherStart)));
		} else {
			adapter.addView(buildText(R.string.groups_no_location));
		}

		adapter.addView(buildLabel(R.string.other_groups));
		
		if (found) {
			adapter.addAdapter(buildOtherAdapter(list.subList(otherStart, list.size())));		
		} else {
			adapter.addView(buildText(R.string.groups_no_other));
		}
		
		listView.setAdapter(adapter);

		/*
		for(int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i).name.equals(newGroupName)) {
				spinner.setSelection(i);
			}
		}*/
	}
	
	private ListAdapter buildLocAdapter(List<Group> list) {

		return new GroupJoinAdapter(this,R.layout.join_group_item,list);
	}

	private ListAdapter buildOtherAdapter(List<Group> list) {
		return new GroupJoinAdapter(this,R.layout.join_group_item,list);
	}

	private View buildLabel(int res) {
	    TextView result = new TextView(this);
	    result.setText(res);
	    result.setBackgroundColor(getResources().getColor(R.color.header_bg));
	    result.setTextColor(getResources().getColor(R.color.header_text));
	    result.setTextSize(20);
	    result.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	    result.setGravity(Gravity.CENTER_HORIZONTAL);
	    return(result);
	}
	
	private View buildText(int res) {
	    TextView result = new TextView(this);
	    result.setText(res);
	    result.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	    result.setGravity(Gravity.CENTER_HORIZONTAL);
	    return(result);
	}	
	
    private void setupGps() { 
        // Request updates from just the fine (gps) provider.
    	gpsLocation = requestUpdatesFromProvider();
    	 
    }

    private Location requestUpdatesFromProvider() {
        Location location = null;
        
        // Network
        String networkProvider = LocationManager.NETWORK_PROVIDER;

        if (mLocationManager.isProviderEnabled(networkProvider)) {
        	mLocationManager.requestLocationUpdates(networkProvider, TEN_SECONDS, 0, listener);
        }
        
        // GPS
        String gpsProvider = LocationManager.GPS_PROVIDER;
        
        if (mLocationManager.isProviderEnabled(gpsProvider)) {
            mLocationManager.requestLocationUpdates(gpsProvider, TEN_SECONDS, 0, listener);
            location = mLocationManager.getLastKnownLocation(gpsProvider);
        }
        
        return location;
    }
    
    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        	boolean isBetter = Util.isBetterLocation(location, gpsLocation);
        	
        	// Check if we should update or not
        	if (isBetter) {
        		gpsLocation = location;
            	loadData();
        	}

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
}
