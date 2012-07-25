package com.hmi.smartphotosharing;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PhotoResponse;
import com.hmi.json.PhotoListResponse;

public class PhotoDetailActivity extends Activity implements OnDownloadListener {

	private ImageView imgView;
	
	private long id;
	private DrawableManager dm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail);

        imgView = (ImageView) findViewById(R.id.picture);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        
        if (id != 0) {
			SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
			String hash = settings.getString(Login.SESSION_HASH, null);
	        
	        String photoUrl = String.format(getResources().getString(R.string.photo_detail_url),hash,id);
	        Log.i("JSON parse", photoUrl);
			new FetchJSON(this).execute(photoUrl);
        } else {
        	Log.e("SmartPhotoSharing", "Photo id was 0, url was probably incorrect");
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

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		Log.i("JSON parse", json);
		
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.status == Util.STATUS_OK) {
			Photo p = pr.getObject();
			
			String uri = p.getUrl();
			        
	        dm = new DrawableManager(this);
	        dm.fetchDrawableOnThread(uri, imgView);
	        
	        // Update user icon
	        ImageView pic = (ImageView) findViewById(R.id.photo_detail_icon);
			String userPic = getResources().getString(R.string.group_http_logo) + p.picture;
			dm.fetchDrawableOnThread(userPic, pic);
	        
			// Update the 'Taken by' text
	        TextView by = (TextView)findViewById(R.id.photo_detail_name);
	        String byTxt = getResources().getString(R.string.photo_detail_name);
	        by.setText(String.format(byTxt, p.rname));
	
	        // Update the timestamp
	        TextView date = (TextView)findViewById(R.id.photo_detail_date);
	        // Convert Unix timestamp to Date
	        Date time = new Date(Long.parseLong(p.time)*1000);
	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        String datum = sdf.format(time);
	        date.setText(datum);
	        
	        // Update the group text
	        TextView group = (TextView)findViewById(R.id.photo_detail_group);
	        String groupTxt = getResources().getString(R.string.photo_detail_group);
	        group.setText(String.format(groupTxt, p.groupname));
		} else if (pr.status == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
        
	}	


}
