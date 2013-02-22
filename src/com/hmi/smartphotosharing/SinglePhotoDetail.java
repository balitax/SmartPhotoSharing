package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

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
import android.view.View.OnClickListener;
import android.widget.EditText;
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
import com.hmi.smartphotosharing.json.PhotoResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SinglePhotoDetail extends NavBarActivity implements OnDownloadListener {

	private static final int CODE_PHOTO = 1;
	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_COMMENT_LOAD = 3;
	private static final int CODE_LIKE = 4;
	
	public static final String KEY_ID = "id";
	
	private long id;
	private ImageLoader imageLoader;
	private EditText commentInput;
	
	private LinearLayout list;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.photo_detail_single);
        super.onCreate(savedInstanceState);

        commentInput = (EditText) findViewById(R.id.edit_message);
        list = (LinearLayout) findViewById(R.id.comments);
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
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
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.photo_menu, menu);
	    return true;
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
        String commentUrl = Util.getUrl(this,R.string.photo_detail_addcomment);
        
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
	        map.put("iid", new StringBody(Long.toString(id)));
	        map.put("comment", new StringBody(commentTxt));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(commentUrl,map);
        new PostRequest(this, CODE_COMMENT_ADD).execute(pr);
	}
	
    private class PictureClickListener implements OnClickListener {
    	
    	private String url;
    	private Context c;
    	
    	private PictureClickListener(Context c, String url) {
    		this.url = url;
    		this.c = c;
    	}

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(c, FullscreenImageActivity.class);
			intent.putExtra(Util.URL_MESSAGE, url);
			startActivity(intent);
		}
    	
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
		case(CODE_LIKE):
			parseLike(json);
			break;
		default:
		}
        
	}

	private void parseLike(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData(true,true);
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseCommentLoad(String json) {
		Gson gson = new Gson();
		CommentListResponse response = gson.fromJson(json, CommentListResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			
			List<Comment> comments = response.getObject();
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			list.removeAllViews();
			if (comments != null && comments.size() > 0) {
				for (int i=0; i<comments.size(); i++) {
				  View vi = inflater.inflate(R.layout.comment, null);
				  Comment comment = comments.get(i);
				  
				  //Icon
				  ImageView img = (ImageView)vi.findViewById(R.id.comment_icon);
				  String userPic = Util.USER_DB + comment.picture;
				  imageLoader.displayImage(userPic, img);

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
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData(false,true);
			commentInput.getEditableText().clear();
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parsePhoto(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			Photo p = pr.getObject();
			
			
			String uri = p.getUrl();

			
	        ImageView photo = (ImageView) findViewById(R.id.picture);
			photo.setOnClickListener(new PictureClickListener(getApplicationContext(), uri));
	        
	        imageLoader.displayImage(uri, photo);
	        /*
	        // Update user icon
	        ImageView pic = (ImageView) findViewById(R.id.photo_detail_icon);
			String userPic = Util.USER_DB + p.picture;
			imageLoader.displayImage(userPic, pic);
	        
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
			 */
			
	        // 'Likes'
			ImageView myLike = (ImageView) findViewById(R.id.like);
			TextView likes = (TextView) findViewById(R.id.like_txt);

	        myLike.setOnClickListener(new LikeClickListener(this,p.getId(), p.me));
	        
	        likes.setText(p.likes);
	        /*
	        int numLikes = p.getLikes();
	    	myLike.setImageResource(R.drawable.like);
	    	
	        if (p.me){ 
	        	if (numLikes > 1) 
		        	likes.setText(String.format(getResources().getString(R.string.like_txt_multiple), Integer.toString(p.getLikes()-1)));
	        	else
		        	likes.setText(getResources().getString(R.string.like_txt_you));
	        } else {
	        	likes.setText(String.format(getResources().getString(R.string.like_txt), p.likes));
	        }
	        
	        // Update the group text
	        TextView group = (TextView)findViewById(R.id.photo_detail_group);
	        String groupTxt = getResources().getString(R.string.photo_detail_group);
	        group.setText(String.format(groupTxt, p.groupname));*/
	        
		} else if (pr.getStatus() == Util.STATUS_LOGIN){
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
		
	}	

   private class LikeClickListener implements OnClickListener{    

        private long iid;
        private boolean myLike;
        private Context context;
        
        public LikeClickListener(Context context, long iid, boolean me){
            this.iid = iid;
            this.myLike = me;
            this.context = context;
        }
        
        @Override
        public void onClick(View arg0) {
    		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
    		
    		String like = "";
    		if (myLike)
    			like = Util.getUrl(context,R.string.unlike_http);
    		else
    			like = Util.getUrl(context,R.string.like_http);
    			
    		myLike = !myLike;
    		
            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
            try {
    			map.put("sid", new StringBody(hash));
    	        map.put("iid", new StringBody(Long.toString(iid)));
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            PostData pr = new PostData(like,map);
            new PostRequest(context, CODE_LIKE).execute(pr);
        }       
    }
}
