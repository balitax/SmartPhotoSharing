package com.hmi.smartphotosharing.news;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarListActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.GroupAdapter;
import com.hmi.smartphotosharing.json.News;
import com.hmi.smartphotosharing.json.NewsListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Sorter;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class NewsActivity extends NavBarListActivity implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;

    private static final int CODE_NEWS = 1;
    private static final int CODE_USER = 2;

	public static final int TYPE_GROUP = 0;
	public static final int TYPE_PHOTO = 1;
	public static final int TYPE_LIKE = 2;
	public static final int TYPE_FRIEND = 3;
	public static final int TYPE_CHECKIN = 4;
	public static final int TYPE_COMMENT = 5;
	
	private ImageLoader imageLoader;
	private ImageView icon;
	private TextView title;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.news);
        super.onCreate(savedInstanceState);
                        
        imageLoader = ImageLoader.getInstance();
        icon = (ImageView)findViewById(R.id.app_icon);
        title = (TextView)findViewById(R.id.header_title);
        
        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.ic_launcher)
            .cacheInMemory()
            .cacheOnDisc()
            	.build();

        // Set the config for the ImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .threadPoolSize(5)
        .threadPriority(Thread.MIN_PRIORITY+2)
        .defaultDisplayImageOptions(options)
        .build();

        // Init ImageLoader
        imageLoader.init(config);
        
        // Show selection in nav bar
        ImageView home = (ImageView) findViewById(R.id.home);
        Util.setSelectedBackground(getApplicationContext(), home);
        
        // Load data
        loadData();
        
        
    }
    	    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {

		super.onCreateOptionsMenu(menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        case R.id.refresh:
	        	loadData();
		    	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	
	
	private void loadData() {
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String url = Util.getUrl(this,R.string.news_http);		

        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(url,map);
		new PostRequest(this, CODE_NEWS,false).execute(pr);
		
		url = Util.getUrl(this,R.string.profile_http);
		HashMap<String,ContentBody> map2 = new HashMap<String,ContentBody>();
        try {
			map2.put("sid", new StringBody(hash));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        PostData pr2 = new PostData(url,map2);
		new PostRequest(this, CODE_USER,false).execute(pr2);
	}
		
		
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	@Override
	public void parseJson(String result, int code) {
		
		switch(code) {
			case CODE_NEWS:
				parseNews(result);
				break;
			case CODE_USER:
				parseUser(result);
				break;
			default:
		}
		
	}

	private void parseUser(String result) {

		Gson gson = new Gson();
		UserResponse response = gson.fromJson(result, UserResponse.class);
		
		if (response != null) {
			User user = response.getObject();
			
			if (user != null) {
				imageLoader.displayImage(Util.getThumbUrl(user), icon);
				title.setText(user.rname);
			}			
		}		
	}

	private void parseNews(String result) {
		
		Gson gson = new Gson();
		NewsListResponse response = gson.fromJson(result, NewsListResponse.class);
		
		if (response != null) {
			List <News> news_list = response.getObject();
			
			if (news_list == null || news_list.size() == 0) {
				ListView listView = getListView();
				TextView emptyView = (TextView) listView.getEmptyView();
				emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
				emptyView.setText(getResources().getString(R.string.news_empty));
			} else {

				// Sort the group on newest
				NewsAdapter adapter = new NewsAdapter(
						this, 
						R.layout.news_item, 
						news_list,
						imageLoader
					);
				adapter.sort(Sorter.NEWS_SORTER_TIME);
				setListAdapter(adapter);	
			}
		}
	}
 
}