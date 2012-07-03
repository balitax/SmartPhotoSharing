package com.hmi.smartphotosharing;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.photo_detail, container, false);

		// Get the large image view
        imgView = (ImageView) view.findViewById(R.id.picture);
        
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dm = mListener.getDrawableManager();
        dm.fetchDrawableOnThread(getActivity().getResources().getString(R.string.photo_detail_url), imgView);
	
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.group_menu, menu);
	}	


}
