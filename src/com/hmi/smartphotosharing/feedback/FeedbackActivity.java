package com.hmi.smartphotosharing.feedback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Feedback;
import com.hmi.smartphotosharing.json.FeedbackListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FeedbackActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CODE_FEEDBACK 	= 1;
    public static final int CODE_CREATE 	= 2;
    public static final int CODE_DELETE 	= 3;
	
    private ImageLoader imageLoader;
    private Button requests;

    private MediaPlayer   mPlayer = null;
    private static String mFileName = null;
    private List<Feedback> mList;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.feedback);
        super.onCreate(savedInstanceState);
                
        loadData();
                
    }
	

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
    
    @Override
    public void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
    	if (mPlayer != null) {
    		stopPlaying();
    	}
    	
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("FeedbackActivity", "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.feedback_menu, menu);
		super.onCreateOptionsMenu(menu);

	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intent;
		switch (item.getItemId()) {

        	case R.id.feedback_add:
            	intent = new Intent(this, CreateFeedbackActivity.class);
            	startActivityForResult(intent, CODE_CREATE);
        		return true;
	        case R.id.refresh:
	        	loadData();
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	public void onClickAdd(View view) {
    	Intent intent = new Intent(this, CreateFeedbackActivity.class);
    	startActivityForResult(intent, CODE_CREATE);		
	}
	
	public void onClickHelp(View view) {
    	Util.createSimpleDialog(this, getResources().getString(R.string.feedback_intro));		
	}   
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == CODE_CREATE) {
        	if (resultCode == RESULT_OK) {
        		loadData();
	        }
        }
	}
	
	private void loadData() {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        String url = Util.getUrl(this,R.string.feedback_http);	
        PostData pr = new PostData(url,map);
		new PostRequest(this,CODE_FEEDBACK).execute(pr);
		
	}

	@Override
	public void parseJson(String json, int code) {

		Util.checkLogout(json,this);
		
		switch (code) {
			case CODE_FEEDBACK:
				parseFeedback(json);
				break;
			case CODE_DELETE:
				parseDelete(json);
			default:
		}
		
	}
	private void parseDelete(String json) {

		Gson gson = new Gson();
		FeedbackListResponse response = gson.fromJson(json, FeedbackListResponse.class);
		
		if (response != null) {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			loadData();
		}
		
	}	
	private void parseFeedback(String json) {

		Gson gson = new Gson();
		FeedbackListResponse response = gson.fromJson(json, FeedbackListResponse.class);
		
		if (response != null) {
			switch(response.getStatus()) {
			
				case(Util.STATUS_OK):
	
					// Sort the group on newest
					FeedbackAdapter adapter = new FeedbackAdapter(
							this, 
							R.layout.feedback_list_item, 
							response.getObject()
						);
					setListAdapter(adapter);	
				    registerForContextMenu(getListView());
					break;
									
				default:
					Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
			}
		}
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			Feedback f = (Feedback) getListAdapter().getItem(info.position);
			menu.setHeaderTitle("Feedback item");
			if (f.file != null && !f.file.equals(""))
				menu.add(Menu.NONE, 0, 0, "Play Audio");
			menu.add(Menu.NONE, 1, 1, "Delete");
			
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  long id = getListAdapter().getItemId(info.position);
	  
	  switch(menuItemIndex) {
	  	case 0:
	  		playAudio(info.position);
	  		break;
	  	case 1:
	  		deleteFeedback(id);
	  		break;
	  }

	  return true;
	}
	
	
	private void deleteFeedback(long id) {

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
			map.put("fid", new StringBody(Long.toString(id)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        String url = Util.getUrl(this,R.string.feedback_http_delete);	
        PostData pr = new PostData(url,map);
		new PostRequest(this,CODE_DELETE).execute(pr);
		
	}


	private void playAudio(int position) {
		Feedback f = (Feedback) getListAdapter().getItem(position);
		if (f.file != null && !f.file.equals("")) {
			mFileName = Util.getFeedbackUrl(f);
			Log.d("FeedbackActivity", "Playing " + mFileName);
			onPlay(true);
		}
	}
}
