package com.hmi.smartphotosharing;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.photo.Photo;
import com.hmi.smartphotosharing.photo.PhotoContainer;
import com.hmi.smartphotosharing.photo.PhotoDetailActivity;
import com.hmi.smartphotosharing.photo.PhotoList;

public class PopularFragment extends Fragment {
	
	// This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    
    // The view that will contain the images
    private GridView gridView;

	private List<Photo> mObjectList;

    private OnLoadDataListener mListener;
    
    // Directory of thumbnails
    public static final Uri SPS_DIR = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mObjectList = new ArrayList<Photo>();
        setHasOptionsMenu(true);
    }        
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	View view = inflater.inflate(R.layout.popular, container, false);
    	
    	gridView = (GridView) view.findViewById(R.id.popularGrid);
        
    	return view;
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	gridView.setOnItemClickListener(new MyOnItemClickListener(getActivity()));
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoadDataListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }	
	
	@Override
	public void onStart() {
        super.onStart();
        
        loadData();
	}
	
	private boolean loadData() {
		boolean res = false;
		mListener.onLoadData();
        
        if (SmartPhotoSharing.refreshDisplay) {
        	if (mListener.canLoad()) {
        		new DownloadText().execute(getActivity().getResources().getString(R.string.popular_http));
        		res = true;
        	} else {
		        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();		        
        	}
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
					getActivity(), 
					mObjectList.toArray(new Photo[group_list.size()]),
					mListener.getDrawableManager()
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