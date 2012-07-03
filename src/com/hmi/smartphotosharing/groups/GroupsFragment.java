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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.smartphotosharing.OnLoadDataListener;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SmartPhotoSharing;

public class GroupsFragment extends ListFragment {
	
	private List<Group> mObjectList = new ArrayList<Group>() ;
	
    private static final String DEBUG_TAG = "HttpExample";
    
    private OnLoadDataListener mListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups, container, false);
        
		Button createBtn = (Button) view.findViewById(R.id.button_create_group);	
		createBtn.setOnClickListener(mCreateGroupOnClickListener);
        return view;
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
        		new DownloadText().execute(getActivity().getResources().getString(R.string.groups_http));
        		res = true;
        	} else {
		        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();		        
        	}
        } 		
        
        return res;
	}

	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		
  		Fragment newFragment = new GroupDetailFragment(id);
      	FragmentTransaction ft = getFragmentManager().beginTransaction();

      	// Replace whatever is in the fragment_container view with this fragment,
      	// and add the transaction to the back stack
      	ft.replace(android.R.id.content, newFragment, "GroupDetailFragment");
      	ft.addToBackStack(null);

      	// Commit the transaction
      	ft.commit();	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.refresh:
		        Toast.makeText(getActivity(), "Refresh!", Toast.LENGTH_SHORT).show();	
        		
        		return loadData();
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }
	
	Button.OnClickListener mCreateGroupOnClickListener = 
		new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), GroupCreateActivity.class);
				getActivity().startActivity(intent);
			}
	};	
	/**
	 * This method binds the JSON data to a GroupList.
	 * @param jsonString The serialized JSON data
	 * @return GroupList object that contains the JSON data as Group objects
	 */
	protected <T>T deserialize(String jsonString, Class<T> classOfT){
		Gson gson = new Gson();
		return gson.fromJson(jsonString, classOfT);
	}


	/**
	 * Checks whether there is a network connection available
	 * @return true if the device is connected to a network
	 */
	public boolean hasNetwork() {
		// Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) 
            getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        return networkInfo != null && networkInfo.isConnected();
	}
		
	/**
	 * This method converts the GroupList object to an array of Group objects and sets the list adapter.
	 * @param result
	 */
	private void parseJson(String result) {
		GroupList list = deserialize(result, GroupList.class);
		List <GroupContainer> group_list = list.getPostContainterList();
		GroupContainer gc;
		for (int i = 0; i < group_list.size(); i++) {
		    gc = group_list.get(i);
		    mObjectList.add(gc.getPost());
		}
		
		setListAdapter(new GroupAdapter(
							getActivity(), 
							R.layout.list_item, 
							mObjectList.toArray(new Group[group_list.size()]),
							mListener.getDrawableManager()
						));
		mObjectList.clear();
	}
	
	// Uses AsyncTask to create a task away from the main UI thread. This task takes a 
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
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
    	        Log.d(DEBUG_TAG, "The response is: " + response);
    	        is = conn.getInputStream();

    	        // Convert the InputStream into a string
    	        String contentAsString = readIt(is);
    	        is.close();
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