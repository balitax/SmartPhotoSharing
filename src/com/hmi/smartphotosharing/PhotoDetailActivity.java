package com.hmi.smartphotosharing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.Comment;
import com.hmi.json.CommentListResponse;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PhotoResponse;

public class PhotoDetailActivity extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_PHOTO = 1;
	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_COMMENT_LOAD = 3;
	
	public static final String KEY_ID = "id";
	
	private long id;
	private DrawableManager dm;
	private EditText commentInput;
	
	private LinearLayout list;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.photo_detail);
        super.onCreate(savedInstanceState);

        commentInput = (EditText) findViewById(R.id.edit_message);
        list = (LinearLayout) findViewById(R.id.comments);
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);

        dm = new DrawableManager(this);
        if (id != 0) {
			loadData(true,true);
        } else {
        	Log.e("SmartPhotoSharing", "Photo id was 0, url was probably incorrect");
        }
        
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

	public void onCommentClick(View v) {
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		String commentTxt = commentInput.getText().toString();
        String commentUrl = String.format(Util.getUrl(this,R.string.photo_detail_addcomment),hash,id,commentTxt);		
        new FetchJSON(this, CODE_COMMENT_ADD).execute(commentUrl);
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
        
		if (photo) {
	        String photoUrl = String.format(Util.getUrl(this,R.string.photo_detail_url),hash,id);
	        
			new FetchJSON(this,CODE_PHOTO).execute(photoUrl);
		}
		
		if (comments) {
	        String commentsUrl = String.format(Util.getUrl(this,R.string.photo_detail_comments),hash,id);
	        
			new FetchJSON(this,CODE_COMMENT_LOAD).execute(commentsUrl);
		}
	}
	
	@Override
	public void parseJson(String json, int code) {
		
		Log.i("JSON parse", json);
		
		switch(code){
		case(CODE_PHOTO):
			parsePhoto(json);
			break;
		case(CODE_COMMENT_ADD):
			parseCommentAdd(json);
			break;
		case(CODE_COMMENT_LOAD):
			parseCommentLoad(json);
			break;
		default:
		}
        
	}

	private void parseCommentLoad(String json) {
		Gson gson = new Gson();
		CommentListResponse response = gson.fromJson(json, CommentListResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			
			List<Comment> comments = response.getObject();
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			
			if (comments != null) {
				list.removeAllViews();
				for (int i=0; i<comments.size(); i++) {
				  View vi = inflater.inflate(R.layout.comment, null);
				  Comment comment = comments.get(i);
				  
				  //Icon
				  ImageView img = (ImageView)vi.findViewById(R.id.comment_icon);
				  String userPic = Util.USER_DB + comment.picture;
				  dm.fetchDrawableOnThread(userPic, img);

				  // Comment text
				  TextView txt = (TextView)vi.findViewById(R.id.comment_txt);
				  txt.setText(comment.comment);
				  
				  
				  // Get the timestamp
		          Date time = new Date(Long.parseLong(comment.time)*1000);
		          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		          String datum = sdf.format(time);
		          				  
				  // Comment username
				  TextView user = (TextView)vi.findViewById(R.id.comment_user);
				  user.setText(comment.rname + " (" + datum + ")");
				  
				  
				  list.addView(vi);
				}
			} else {

				TextView txt = new TextView(this);
				txt.setText("No comments for this photo");
				list.addView(txt);
			}
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();			
		}
	}

	private void parseCommentAdd(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			loadData(false,true);
			commentInput.getEditableText().clear();
		} else if (pr.getStatus() == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parsePhoto(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			Photo p = pr.getObject();
			
			String uri = p.getUrl();

	        ImageView photo = (ImageView) findViewById(R.id.picture);
	        
	        dm.fetchDrawableOnThread(uri, photo);

	        // Update user icon
	        ImageView pic = (ImageView) findViewById(R.id.photo_detail_icon);
			String userPic = Util.USER_DB + p.picture;
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
		} else if (pr.getStatus() == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
		
	}	


}
