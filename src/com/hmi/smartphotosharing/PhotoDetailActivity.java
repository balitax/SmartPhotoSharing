package com.hmi.smartphotosharing;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PhotoResponse;
import com.hmi.smartphotosharing.util.Util;

public class PhotoDetailActivity extends NavBarActivity implements OnDownloadListener {
	
	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_LIKE = 3;
	private static final int CODE_PHOTOS = 4;
	private static final int CODE_COMMENT_REMOVE = 5;
	
	public static final String KEY_ID = "id";
	public static final String KEY_GID = "gid";
	public static final String KEY_SSID = "ssid";
	
	private long id, gid, ssid;
	private ViewPager vp;
	
	private int currentPage;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.photo_detail);
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        gid = intent.getLongExtra(KEY_GID, 0);    
        ssid = intent.getLongExtra(KEY_SSID, 0); 
        vp = (ViewPager) findViewById(R.id.viewpager);    

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);
        
        if (id != 0) {
			loadData();
        } else {
        	Log.e("SmartPhotoSharing", "Photo id was 0, url was probably incorrect");
        }
        
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putInt("current", currentPage);
		
		// Store comment text
		View currentView = vp.getChildAt(currentPage);
		if (currentView != null) {
			EditText edit = (EditText) currentView.findViewById(R.id.edit_message);
			savedInstanceState.putString("comment", edit.getEditableText().toString());
		}
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentPage = savedInstanceState.getInt("current");
		
		// Set comment text
		String commentTxt = savedInstanceState.getString("comment");
		View currentView = vp.getChildAt(currentPage);
		EditText edit = (EditText) currentView.findViewById(R.id.edit_message);
		edit.setText(commentTxt, TextView.BufferType.EDITABLE);

	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        id = intent.getLongExtra(KEY_ID, 0);
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

	public void loadData() {
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
		case(CODE_LIKE):
			parseLike(json);
			break;
		case(CODE_COMMENT_REMOVE):
			parseCommentRemove(json);
			break;
		default:
		}
        
	}

	private void parseCommentRemove(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
			vp.setCurrentItem(currentPage);
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseLike(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
			vp.setCurrentItem(currentPage);
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
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
			
			boolean found = false;
			for (int i = 0; i < photo_list.size() & !found; i++) {
				if (photo_list.get(i).getId() == id) {
					currentPage = i;
					found = true;
				}
			}
			
			MyPagerAdapter adapter = new MyPagerAdapter(this,photo_list);
			PageListener pageListener = new PageListener();
			vp.setOnPageChangeListener(pageListener);

			vp.setAdapter(adapter);
			
			vp.setCurrentItem(currentPage);
			
		} else {
			Toast.makeText(this, list.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	

	
	private void parseCommentAdd(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
			vp.setCurrentItem(currentPage);
		} else if (pr.getStatus() == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
    private class PageListener extends SimpleOnPageChangeListener{
        public void onPageSelected(int position) {
            Log.i("PAGER", "page selected " + position);
               currentPage = position;
    }
}

}
