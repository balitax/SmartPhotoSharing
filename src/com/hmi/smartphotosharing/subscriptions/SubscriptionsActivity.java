package com.hmi.smartphotosharing.subscriptions;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.json.SubscriptionListResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SubscriptionsActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_SUBSCRIPTS = 2;
    private static final int CODE_SUB_REMOVE = 3;

	private ImageLoader imageLoader;
		    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.subscriptions);
        super.onCreate(savedInstanceState);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        loadData();

        // Show selection in nav bar
        ImageView fav = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), fav);
        
    }
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.subscription_menu, menu);
		super.onCreateOptionsMenu(menu);

	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intent;
		switch (item.getItemId()) {

        	case R.id.subscription_new:
                intent = new Intent(getApplicationContext(), SubscriptionCreateActivity.class);
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

		String subsUrl = String.format(Util.getUrl(this,R.string.subscriptions_http),hash);
		new FetchJSON(this,CODE_SUBSCRIPTS).execute(subsUrl);

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            loadData();
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
				
			case CODE_SUB_REMOVE:
				parseSubRemove(result);
				break;
			default:
		}
	}

	private void parseSubRemove(String result) {

		Gson gson = new Gson();
		StringResponse response = gson.fromJson(result, StringResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseSubscripts(String result) {

		Log.d("JSON Parse", result);
		Gson gson = new Gson();
		SubscriptionListResponse response = gson.fromJson(result, SubscriptionListResponse.class);
		
		if (response != null) {
			List <Subscription> subscription_list = response.getObject();
			if (subscription_list == null) {
				ListView listView = getListView();
				TextView emptyView = (TextView) listView.getEmptyView();
				emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
				emptyView.setText(getResources().getString(R.string.subscriptions_empty));
				
			} else {
				
				SubscriptionAdapter adapter = new SubscriptionAdapter(
						this, 
						R.layout.subscription_item, 
						subscription_list.toArray(new Subscription[subscription_list.size()]),
						imageLoader
					);
				
				adapter.sort(Sorter.SUBSCRIPTIONS_SORTER);
				setListAdapter(adapter);	

			}
		}
	}
 
	private class MyLongClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			createItemDialog(position);
		}
		
	}
	
	public void createItemDialog(int pos) {
		ListView list = getListView();
		Subscription s = (Subscription)list.getAdapter().getItem(pos);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(s.name)
			 .setItems(R.array.subscription_dialog, new ItemDialog(s.getId()));
		AlertDialog alert = builder.create();
		alert.show();
		
	}  
	
	public class ItemDialog implements DialogInterface.OnClickListener {
		private long ssid;
		
		public ItemDialog(long ssid){
			this.ssid = ssid;
		}
		
		public void onClick(DialogInterface dialog, int which) {
     	   if(which == 0) {
     		   confirmDeleteCommentDialog(ssid);
     	   }
        }
	}
	
    private void confirmDeleteCommentDialog(final long ssid) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this subscription?")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {

			       		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
			       		String hash = settings.getString(Login.SESSION_HASH, null);
			       		
			       		String deleteUrl = Util.getUrl(SubscriptionsActivity.this,R.string.subscriptions_http_remove);
			       			
			               HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
			               try {
			       			map.put("sid", new StringBody(hash));
			       	        map.put("ssid", new StringBody(Long.toString(ssid)));
			       		} catch (UnsupportedEncodingException e) {
			       			e.printStackTrace();
			       		}
		               
		                PostData pr = new PostData(deleteUrl,map);
		                new PostRequest(SubscriptionsActivity.this, CODE_SUB_REMOVE).execute(pr);
		           }
		       })
		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		
	}
}