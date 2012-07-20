package com.hmi.smartphotosharing.groups;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupsResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.ProfileResponse;
import com.hmi.json.User;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SettingsActivity;
import com.hmi.smartphotosharing.SharePhotoActivity;
import com.hmi.smartphotosharing.camera.AlbumStorageDirFactory;
import com.hmi.smartphotosharing.camera.BaseAlbumDirFactory;
import com.hmi.smartphotosharing.camera.FroyoAlbumDirFactory;
public class GroupsActivity extends ListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_PROFILE = 1;
    private static final int CODE_GROUPS = 2;
    private static final int ACTION_TAKE_PHOTO = 5;
    
	private DrawableManager dm;
	private TextView name;
	private ImageView pic;

	private String mCurrentPhotoPath;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups);
        
        name = (TextView) findViewById(R.id.groups_name);
        pic = (ImageView) findViewById(R.id.groups_icon);
        
        dm = new DrawableManager(this);
        loadData(true, true);
        
		// Check the Android version and decide which album path settings to use
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
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
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.group_menu, menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
        switch (item.getItemId()) {
	        case R.id.camera:
	        	dispatchTakePhoto();
				//intent = new Intent(this, CameraActivity.class);
			    //startActivity(intent);	
		        //return true;
	        	return true;
	        case R.id.settings:
	            intent = new Intent(this, SettingsActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
		        return true;
	        case R.id.refresh:
	        	loadData(false,true);
		    	return true;
	        case R.id.create_group:
	        	intent = new Intent(this, GroupCreateActivity.class);
	        	startActivityForResult(intent, CREATE_GROUP);
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void dispatchTakePhoto() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File f = null;
		
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}

		// Handle the result of the Intent
		startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
		
	}

	private void loadData(boolean profile, boolean groups) {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		if (profile) {
	        String profileUrl = String.format(getResources().getString(R.string.profile_http),hash);		
	        new FetchJSON(this,CODE_PROFILE).execute(profileUrl);
		}

		if (groups) {
			String groupsUrl = String.format(getResources().getString(R.string.groups_http),hash);
			new FetchJSON(this,CODE_GROUPS).execute(groupsUrl);
		}

	}
		
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_GROUP && resultCode == Activity.RESULT_OK) {
            // TODO : refresh group list
            Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show();
        } else if (requestCode == ACTION_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
			handleCameraPhoto();
		}
    }	

	/**
	 * Checks whether there is a network connection available
	 * @return true if the device is connected to a network
	 */
	public boolean hasNetwork() {
		// Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        return networkInfo != null && networkInfo.isConnected();
	}
		
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {
		
		switch (code) {
		case CODE_GROUPS:
			parseGroups(result);
			break;
			
		case CODE_PROFILE:
			parseProfile(result);
			break;
			
		default:
		}
	}

	private void parseProfile(String result) {
		Gson gson = new Gson();
		ProfileResponse response = gson.fromJson(result, ProfileResponse.class);
		User user = response.msg;
		
		// Set the user name
		name.setText(response.msg.getName());
		
		// Set the user icon
		String userPic = getResources().getString(R.string.group_http_logo) + user.picture;
		dm.fetchDrawableOnThread(userPic, pic);
	}

	private void parseGroups(String result) {
		
		Gson gson = new Gson();
		GroupsResponse gr = gson.fromJson(result, GroupsResponse.class);
		
		if (gr != null) {
			List <Group> group_list = gr.getGroupsList();
			if (group_list == null) group_list = new ArrayList<Group>();
			
			setListAdapter(new GroupAdapter(
								this, 
								R.layout.list_item, 
								group_list.toArray(new Group[group_list.size()]),
								dm
							));	
		}
	}
	
	/**
	 *  Photo album for this application 
	 */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
	
	/**
	 * Tries to create the album directory
	 * @return The created directory or null when it cannot create the directory.
	 */
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	/**
	 * Sets the filename and actually creates the file.
	 * @return
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	/**
	 * Creates the file and sets the path for the photo
	 * @return The File where the image is stored.
	 * @throws IOException Thrown when writing is not possible.
	 */
	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void handleCameraPhoto() {

		if (mCurrentPhotoPath != null) {
						
			//setPic();
			galleryAddPic();
			
			// Send the intent to the class that can handle incoming photos
			Intent intent = new Intent(this,SharePhotoActivity.class);
			intent.setType("image/jpeg");

			// Add the Uri of the current photo as extra value
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mCurrentPhotoPath));
			
			// Create and start the chooser
			startActivity(intent);
			//mCurrentPhotoPath = null;
		} else {
			Log.w("Camera result", "Resulting photo path is null");
		}

	}
	
	/**
	 * Method for adding the current photo to the gallery.
	 * Creates an Intent to rescan the media gallery.
	 */
	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    sendBroadcast(mediaScanIntent);
	}
	

 
}