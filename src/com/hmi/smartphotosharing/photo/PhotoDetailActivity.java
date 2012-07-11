package com.hmi.smartphotosharing.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SharePhotoActivity;

public class PhotoDetailActivity extends SherlockFragmentActivity {

	private ImageView imgView;
	
	private long id;
	private DrawableManager dm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail);
        
		// Get the large image view
        imgView = (ImageView) findViewById(R.id.picture);
        
        String uri = getResources().getString(R.string.photo_detail_url);
        
        dm = new DrawableManager(this);
        dm.fetchDrawableOnThread(uri, imgView);
        
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.share:
        		// TODO : fix working uri
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
    	MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.photo_menu, menu);
	    return true;
	}	


}
