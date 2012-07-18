package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupDetailResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PopularResponse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SmartPhotoSharing;

public class GroupDetailActivity extends SherlockFragmentActivity implements OnDownloadListener {

	private static final int CODE_GROUP_DETAILS = 1;
	private static final int CODE_GROUP_PHOTOS = 2;
	
	//gallery object
	private GridView gridView;
	//image view for larger display
	
	private TextView groupPhotos;
	private TextView groupMembers;
	private TextView groupName;
	private ImageView groupIcon;
	
	private DrawableManager dm;
			
	private long id;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_detail);

        dm = new DrawableManager(this);
        
        groupName = (TextView) findViewById(R.id.group_detail_name);
        groupIcon = (ImageView) findViewById(R.id.group_detail_icon);
        groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
        groupMembers = (TextView) findViewById(R.id.group_detail_members);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        // Set the click listener for each item in the thumbnail gallery     
        
        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
    }

    private class MyOnItemClickListener implements OnItemClickListener {
		private Context c;
		
		public MyOnItemClickListener(Context c) {
			this.c = c;
		}
		// Handle clicks
		@Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent intent = new Intent(c, PhotoDetailActivity.class);
	    	intent.putExtra("id", id);
	    	startActivity(intent);
	    }
    }
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.group_detail_menu, menu);
	    return true;
	}	
	
	@Override
	public void onStart() {
        super.onStart();
        
        loadData();
	}
	
	private boolean loadData() {
		boolean res = false;
        
        if (SmartPhotoSharing.refreshDisplay) {
        	
    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
        	// Get group info
    		String detailUrl = String.format(getResources().getString(R.string.group_http_detail),hash,id);
    		new FetchJSON(this,CODE_GROUP_DETAILS).execute(detailUrl);
    		
    		// Get list of photos
    		String photosUrl = String.format(getResources().getString(R.string.group_http_detail_photos),hash,id);
    		new FetchJSON(this,CODE_GROUP_PHOTOS).execute(photosUrl);
    		res = true;
        } 		
        
        return res;
	}
	
	public void parseJson(String result, int code) {
		
		switch (code) {
			case CODE_GROUP_DETAILS:
				parseGroup(result);
				break;
			case CODE_GROUP_PHOTOS:
				parsePhoto(result);
				break;
			default:
		}
	}

	private void parsePhoto(String result) {
		Gson gson = new Gson();
		PopularResponse list = gson.fromJson(result, PopularResponse.class);
		
		List<Photo> photo_list = list.msg;
		
		gridView.setAdapter(
			new MyImageAdapter(
					this, 
					photo_list,
					dm
		));

        gridView.setOnItemClickListener(new MyOnItemClickListener(this)); 
        
		// Set the string telling how many photos the group has
		String photos = String.format(getResources().getString(R.string.group_detail_photos), photo_list.size());
		groupPhotos.setText(photos);
		
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		
		try {
			GroupDetailResponse gdr = gson.fromJson(result, GroupDetailResponse.class);
			Group g = gdr.msg;

			groupName.setText(g.name);
			String logoUrl = getResources().getString(R.string.group_http_logo) + g.logo;
			dm.fetchDrawableOnThread(logoUrl, groupIcon);

			// Set the string telling how many members the group has
			String photos = String.format(getResources().getString(R.string.group_detail_members), 0);
			groupMembers.setText(photos);
		} catch (JsonSyntaxException e) {
			Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
		}

		
	}
}
