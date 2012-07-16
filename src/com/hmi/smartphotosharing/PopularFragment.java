package com.hmi.smartphotosharing;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PopularResponse;

public class PopularFragment extends Fragment implements OnDownloadListener {
	
	// This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    
    // The view that will contain the images
    private GridView gridView;


    private OnLoadDataListener mListener;
    
    // Directory of thumbnails
    public static final Uri SPS_DIR = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        		new FetchJSON(this).execute(getActivity().getResources().getString(R.string.popular_http));
        		res = true;
        	} else {
		        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();		        
        	}
        } 		
        
        return res;
	}
	
	public void parseJson(String result, int code) {
		Gson gson = new Gson();
		PopularResponse list = gson.fromJson(result, PopularResponse.class);
		
		List<Photo> photo_list = list.msg;
		
		gridView.setAdapter(
			new MyImageAdapter(
					getActivity(), 
					photo_list,
					mListener.getDrawableManager()
		));
		
	}
	
}