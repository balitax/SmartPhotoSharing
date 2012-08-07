package com.hmi.smartphotosharing.groups;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GroupManageActivity extends NavBarActivity implements OnDownloadListener {
	
	private static final int CODE_GROUP_DETAILS = 1;
    private static final int TAKE_PICTURE = 5;

	public static final String KEY_ID = "id";
    private ImageLoader imageLoader;
	private long id;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_manage);
        super.onCreate(savedInstanceState);
        
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        loadData();
    }
	
    @Override
    public void onResume() {
      super.onResume();
      
      loadData();
    }  
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) { 
			loadData();
	    } 
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String detailUrl = String.format(Util.getUrl(this,R.string.group_http_detail),hash,id);
		new FetchJSON(this,CODE_GROUP_DETAILS).execute(detailUrl);
		
	}
	
	public void onClickPicture(View view) {
		Intent intent = new Intent(this, ChangeGroupPictureActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	@Override
	public void parseJson(String json, int code) {

		switch (code) {
			case CODE_GROUP_DETAILS:
				parseGroup(json);
				break;
				
			default:
		}
		
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		
		GroupResponse response = gson.fromJson(result, GroupResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Group group = response.getObject();

	        TextView groupName = (TextView) findViewById(R.id.group_detail_name);
	        ImageView groupIcon = (ImageView) findViewById(R.id.group_detail_icon);
	        TextView groupPhotos = (TextView) findViewById(R.id.group_detail_photos);
	        TextView groupMembers = (TextView) findViewById(R.id.group_detail_members);
	        ImageView privateIcon = (ImageView) findViewById(R.id.private_icon);
	        ImageView locationIcon = (ImageView) findViewById(R.id.location_icon);
	        
			groupName.setText(group.name);
			String logoUrl = Util.GROUP_DB + group.logo;
			imageLoader.displayImage(logoUrl, groupIcon);
	
			if (!group.isPrivate()) {
				privateIcon.setVisibility(ImageView.GONE);
			}

			if (!group.isLocationLocked()) {
				locationIcon.setVisibility(ImageView.GONE);
			}
			
			// Set the string telling how many members the group has
			String photos = String.format(getResources().getString(R.string.group_detail_members), group.members);
			groupMembers.setText(photos);
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
}
