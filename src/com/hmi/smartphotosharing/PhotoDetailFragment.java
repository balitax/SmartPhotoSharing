package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.R.id;
import com.hmi.smartphotosharing.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoDetailFragment extends Fragment {

	private ImageView imgView;
	
	private long id;
	private DrawableManager dm;
	
	public PhotoDetailFragment(long id, DrawableManager dm) {
		this.id = id;
		this.dm = dm;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.group_detail, container, false);

		// Get the large image view
        imgView = (ImageView) view.findViewById(R.id.picture);
        
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         
        dm.fetchDrawableOnThread(getActivity().getResources().getString(R.string.photo_detail_url), imgView);
	
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.group_menu, menu);
	}	


}
