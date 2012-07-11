package com.hmi.smartphotosharing.groups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SmartPhotoSharing;
import com.hmi.smartphotosharing.photo.Photo;
import com.hmi.smartphotosharing.photo.PhotoContainer;
import com.hmi.smartphotosharing.photo.PhotoDetailActivity;
import com.hmi.smartphotosharing.photo.PhotoList;

public class GroupDetailActivity extends SherlockFragmentActivity {

	//adapter for gallery view
	private MyImageAdapter imgAdapt;
	//gallery object
	private GridView gridView;
	//image view for larger display
	private TextView textView;

	private DrawableManager dm;
	private List<Photo> mObjectList;
	
	private long id;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_detail);

        mObjectList = new ArrayList<Photo>();
        dm = new DrawableManager(this);
        
        textView = (TextView) findViewById(R.id.group_detail_intro);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        textView.setText("Selected id was: " + String.valueOf(id));
        
        // Set the gallery adapter
        gridView.setAdapter(imgAdapt);

        // Set the click listener for each item in the thumbnail gallery
        gridView.setOnItemClickListener(new MyOnItemClickListener(this));       	
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
    		new DownloadText().execute(getResources().getString(R.string.popular_http));
    		res = true;
        } 		
        
        return res;
	}
	
	private void parseJson(String result) {
		Gson gson = new Gson();
		PhotoList list = gson.fromJson(result, PhotoList.class);
		
		List<PhotoContainer> group_list = list.getPostContainterList();
		PhotoContainer gc;
		for (int i = 0; i < group_list.size(); i++) {
		    gc = group_list.get(i);
		    mObjectList.add(gc.getPost());
		}
		
		gridView.setAdapter(
			new MyImageAdapter(
					this, 
					mObjectList.toArray(new Photo[group_list.size()]),
					dm
		));
		
		mObjectList.clear();
	}
	
	   private class DownloadText extends AsyncTask<String,Void,String> {
	        
	    	@Override
	    	protected String doInBackground(String... urls) {
	             
		       // params comes from the execute() call: params[0] is the url.
		       try {
		           return downloadUrl(urls[0]);
		       } catch (IOException e) {
		           return "Unable to retrieve web page. URL may be invalid.";
		       }
	    	}
	    	
	       // onPostExecute displays the results of the AsyncTask.
	    	@Override
	    	protected void onPostExecute(String result) {
	    		parseJson(result);
	    		//textView.setText(result);
	    	}
	    	
			// Given a URL, establishes an HttpUrlConnection and retrieves
	    	// the web page content as a InputStream, which it returns as
	    	// a string.
	    	private String downloadUrl(String myurl) throws IOException {
	    	    InputStream is = null;
	    	    // Only display the first 500 characters of the retrieved
	    	    // web page content.
	    	        
	    	    try {
	    	        URL url = new URL(myurl);
	    	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    	        conn.setReadTimeout(10000 /* milliseconds */);
	    	        conn.setConnectTimeout(15000 /* milliseconds */);
	    	        conn.setRequestMethod("GET");
	    	        conn.setDoInput(true);
	    	        // Starts the query
	    	        conn.connect();
	    	        int response = conn.getResponseCode();
	    	        is = conn.getInputStream();

	    	        // Convert the InputStream into a string
	    	        String contentAsString = readIt(is);
	    	        return contentAsString;
	    	        
	    	    // Makes sure that the InputStream is closed after the app is
	    	    // finished using it.
	    	    } finally {
	    	        if (is != null) {
	    	            is.close();
	    	        } 
	    	    }
	    	}
	    	
	    	// Reads an InputStream and converts it to a String.
	    	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
	    		    		
	    	    Reader reader = new InputStreamReader(stream, "UTF-8");  
	    		BufferedReader br = new BufferedReader(reader);
	    		StringBuilder sb = new StringBuilder();
	    		String line = null;
	    		while ((line = br.readLine()) != null) {
	    			sb.append(line + "\n");
	    		}
	    		return sb.toString();
	    	}
	    }
}
