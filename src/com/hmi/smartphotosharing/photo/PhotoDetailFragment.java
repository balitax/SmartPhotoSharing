package com.hmi.smartphotosharing.photo;

import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.OnLoadDataListener;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SharePhotoActivity;
import com.hmi.smartphotosharing.R.id;
import com.hmi.smartphotosharing.R.layout;
import com.hmi.smartphotosharing.R.menu;
import com.hmi.smartphotosharing.R.string;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoDetailFragment extends Fragment {

	private ImageView imgView;
	
	private long id;
	private DrawableManager dm;
    private OnLoadDataListener mListener;
	
	public PhotoDetailFragment(long id) {
		this.id = id;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.share:
        		// TODO : fix working uri
        		String uri = getActivity().getResources().getString(R.string.photo_detail_url);
        		
        		Intent intent = new Intent(getActivity(),SharePhotoActivity.class);
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.photo_detail, container, false);

		// Get the large image view
        imgView = (ImageView) view.findViewById(R.id.picture);
        
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO : fix url
        String uri = getActivity().getResources().getString(R.string.photo_detail_url);
        
        dm = mListener.getDrawableManager();
        dm.fetchDrawableOnThread(uri, imgView);
	
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.photo_menu, menu);
	}	


}
