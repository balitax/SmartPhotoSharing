package com.hmi.smartphotosharing.subscriptions;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.json.SubscriptionListResponse;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;

public class SubscriptionsActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_PROFILE = 1;
    private static final int CODE_SUBSCRIPTS = 2;

	private ImageLoader dm;
		    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.subscriptions);
        super.onCreate(savedInstanceState);

        dm = new ImageLoader(this);
        loadData(true, true);
        
    }
    
	public void onClickCreateSubscription(View v) {	
		Intent intent = new Intent(this,SubscriptionCreateActivity.class);
		this.startActivity(intent);
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
    	
	private void loadData(boolean profile, boolean subs) {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (profile) {
	        String profileUrl = String.format(Util.getUrl(this,R.string.profile_http),hash);		
	        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);
		}

		if (subs) {
			String subsUrl = String.format(Util.getUrl(this,R.string.subscriptions_http),hash);
			new FetchJSON(this,CODE_SUBSCRIPTS).execute(subsUrl);
		}

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            loadData(false,true);
        }
    }	
		
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {
		
		switch (code) {
		case CODE_SUBSCRIPTS:
			parseSubscripts(result);
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
		stats.setText(String.format(this.getResources().getString(R.string.profile_follows), user.following, user.followers));
		
		// Set the user icon
		String userPic = Util.USER_DB + user.picture;
		ImageView pic = (ImageView) findViewById(R.id.groups_icon);
		dm.DisplayImage(userPic, pic);
	}

	private void parseSubscripts(String result) {

		Log.d("JSON Parse", result);
		Gson gson = new Gson();
		SubscriptionListResponse gr = gson.fromJson(result, SubscriptionListResponse.class);
		
		if (gr != null) {
			List <Subscription> subscription_list = gr.getObject();
			if (subscription_list == null) subscription_list = new ArrayList<Subscription>();
			
			SubscriptionAdapter adapter = new SubscriptionAdapter(
					this, 
					R.layout.subscription_item, 
					subscription_list.toArray(new Subscription[subscription_list.size()]),
					dm
				);
			
			adapter.sort(Sorter.SUBSCRIPTIONS_SORTER);
			setListAdapter(adapter);	
		}
	}
 
}