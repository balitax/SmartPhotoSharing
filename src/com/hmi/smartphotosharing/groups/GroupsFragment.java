package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.GroupsResponse;
import com.hmi.json.OnDownloadListener;
import com.hmi.smartphotosharing.OnLoadDataListener;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SmartPhotoSharing;

public class GroupsFragment extends ListFragment implements OnDownloadListener {
	
    public static final int CREATE_GROUP = 4;
    
    private OnLoadDataListener mListener;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.groups, container, false);
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        		new FetchJSON(this).execute(getActivity().getResources().getString(R.string.groups_http));
        		res = true;
        	} else {
		        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();		        
        	}
        } 		
        
        return res;
	}
		
	public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP) {
            if (resultCode == Activity.RESULT_OK) {
            	// TODO : refresh group list
            	Toast.makeText(getActivity(), "Group Created", Toast.LENGTH_SHORT).show();
            }
        }
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
	@Override
	public void parseJson(String result, int code) {
		
		Gson gson = new Gson();
		GroupsResponse gr = gson.fromJson(result, GroupsResponse.class);
		
		List <Group> group_list = gr.getGroupsList();
		
		setListAdapter(new GroupAdapter(
							getActivity(), 
							R.layout.list_item, 
							group_list.toArray(new Group[group_list.size()]),
							mListener.getDrawableManager()
						));
	}
 
}