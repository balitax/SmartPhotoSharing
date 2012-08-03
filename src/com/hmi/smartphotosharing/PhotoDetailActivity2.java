package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.json.Comment;
import com.hmi.smartphotosharing.json.CommentListResponse;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PhotoResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Util;

public class PhotoDetailActivity2 extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_COMMENT_LOAD = 3;
	private static final int CODE_PHOTOS = 4;
	
	public static final String KEY_ID = "id";
	public static final String KEY_GID = "gid";
	public static final String KEY_SSID = "ssid";
	
	private long id, gid, ssid;
	private ImageLoader dm;
	
	private ViewPager vp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.photo_detail);
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        gid = intent.getLongExtra(KEY_GID, 0);    
        ssid = intent.getLongExtra(KEY_SSID, 0); 
        vp = (ViewPager) findViewById(R.id.viewpager);    
        dm = new ImageLoader(this);
        if (id != 0) {
			loadData(true,true);
        } else {
        	Log.e("SmartPhotoSharing", "Photo id was 0, url was probably incorrect");
        }
        
    }
    
    @Override
    protected void onStart() {

		loadData(true,true);
		super.onStart();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        id = intent.getLongExtra(KEY_ID, 0);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (id != 0) {
			loadData(true,true);
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.share:

        		String uri = getResources().getString(R.string.photo_detail_url);
        		
        		Intent intent = new Intent(this,SharePhotoActivity.class);
				intent.setType("image/jpeg");

				// Add the Uri of the current photo as extra value
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
				
				// Create and start the chooser
				startActivity(intent);
				return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }

	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.photo_menu, menu);
	    return true;
	}

	public void loadData(boolean photo, boolean comments) {
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
        
		if (gid != 0) {
			// Get list of photos
			String photosUrl = String.format(Util.getUrl(this,R.string.group_http_detail_photos),hash,gid);
			Log.d("JSON", photosUrl);
			new FetchJSON(this,CODE_PHOTOS).execute(photosUrl);
		}
		
		else if (ssid != 0) {
			String photosUrl = String.format(Util.getUrl(this,R.string.subscriptions_http_photos),hash,ssid);
			Log.d("JSON", photosUrl);
			new FetchJSON(this,CODE_PHOTOS).execute(photosUrl);
		}
		
		else {
			// TODO ERROR
		}
		
	}
	
	@Override
	public void parseJson(String json, int code) {
		
		Log.i("JSON parse", json);
		
		switch(code){
		case(CODE_PHOTOS):
			parsePhoto(json);
			break;
		case(CODE_COMMENT_ADD):
			parseCommentAdd(json);
			break;
		default:
		}
        
	}

	private void parsePhoto(String result) {
		Gson gson = new Gson();
		PhotoListResponse list = gson.fromJson(result, PhotoListResponse.class);
			
		if (list.getStatus() == Util.STATUS_OK) {
			List<Photo> photo_list = list.getObject();
			
			// JSON will return null if there are no photos in this group
			if (photo_list == null)
				photo_list = new ArrayList<Photo>();
			
			int pos = 0;
			boolean found = false;
			for (int i = 0; i < photo_list.size() & !found; i++) {
				if (photo_list.get(i).getId() == id) {
					pos = i;
					found = true;
				}
			}
			
			MyPagerAdapter adapter = new MyPagerAdapter(this,photo_list,dm);
			
			vp.setAdapter(adapter);
			vp.setCurrentItem(pos);
			
		} else {
			Toast.makeText(this, list.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	

	
	private void parseCommentAdd(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			loadData(false,true);
			MyPagerAdapter adapter = (MyPagerAdapter)vp.getAdapter();
			adapter.notifyDataSetChanged();
		} else if (pr.getStatus() == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
}
