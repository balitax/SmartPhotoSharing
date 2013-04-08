package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

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

import com.google.gson.Gson;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class UserDetailActivity extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_USER_DETAILS = 1;
		
	//gallery object
	private GridView gridView;
	//image view for larger display
	
	//private TextView groupPhotos;
	private TextView groupName;
	private ImageView groupIcon;
	
	//private ImageView privateIcon, locationIcon;
	
	private ImageLoader imageLoader;
			
	private long id;	
	private User user;
	private int type;
	public static final String KEY_ID = "id";
	
	private final int TYPE_PHOTO 	= 0;
	private final int TYPE_LIKES	= 1;
	private final int TYPE_SPOTS	= 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.user_detail);
        super.onCreate(savedInstanceState);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        groupName = (TextView) findViewById(R.id.header_title);
        groupIcon = (ImageView) findViewById(R.id.app_icon);
        //groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
        
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);
        
        //privateIcon = (ImageView) findViewById(R.id.private_icon);
        //locationIcon = (ImageView) findViewById(R.id.location_icon);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        // Set the click listener for each item in the thumbnail gallery     
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);

        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.favourite);
        Util.setSelectedBackground(getApplicationContext(), home);
        
        user = null;
        type = TYPE_PHOTO;
        
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        id = intent.getLongExtra(KEY_ID, 0);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (id != 0) {
			loadData();
        }
    }
    
    public void onClickPhotos(View v) {
    	type = TYPE_PHOTO;
    	loadData();
    }
    
    public void onClickLikes(View v) {
    	type = TYPE_LIKES;
    	loadData();
    }
    
    public void onClickSpots(View v) {
    	type = TYPE_SPOTS;
    	loadData();
    }
    	
	private void loadData() {
    	
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
			map.put("uid", new StringBody(Long.toString(id)));
			map.put("type", new StringBody(Integer.toString(type)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        String usersUrl = Util.getUrl(this,R.string.profile_http);	        

        PostData pr = new PostData(usersUrl,map);
		new PostRequest(this,CODE_USER_DETAILS).execute(pr);
				        
	}
	
	public void parseJson(String result, int code) {
		
		switch (code) {
			case CODE_USER_DETAILS:
				parseUser(result);
				break;
			default:
		}
	}
	
	
	private void parseUser(String result) {
		Gson gson = new Gson();
		
		UserResponse response = gson.fromJson(result, UserResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			user = response.getObject();
	
			groupName.setText(user.rname);
			imageLoader.displayImage(Util.getThumbUrl(user), groupIcon);
			
			if (!user.isFriend() && user.isPrivate()) {
				TextView empty = (TextView) findViewById(R.id.grid_empty);
				empty.setVisibility(TextView.VISIBLE);
				empty.setText(R.string.group_detail_private);
			} else {
				setPhotos();
			}
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private void setPhotos() {
		List<Photo> photo_list = user.newest_photos;
		
		if (photo_list == null || photo_list.isEmpty()) {
			TextView empty = (TextView) findViewById(R.id.grid_empty);
			empty.setVisibility(TextView.VISIBLE);
		} else {		
			gridView.setAdapter(
				new MyImageAdapter(
						this, 
						photo_list
			));
	
	        gridView.setOnItemClickListener(new MyOnItemClickListener()); 
		}		
	}
	
	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
			
			Intent intent = new Intent(UserDetailActivity.this, PhotoDetailActivity.class);
			intent.putExtra("id", id);
			intent.putExtra("uid", user.getId());
			intent.putExtra("type", type);
			UserDetailActivity.this.startActivity(intent);
		}
		
	}

}
