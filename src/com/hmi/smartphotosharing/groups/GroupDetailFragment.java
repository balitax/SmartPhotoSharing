package com.hmi.smartphotosharing.groups;

import com.hmi.smartphotosharing.PhotoDetailAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.R.id;
import com.hmi.smartphotosharing.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;

public class GroupDetailFragment extends Fragment {

	//variable for selection intent
	private final int PICKER = 1;
	//adapter for gallery view
	private PhotoDetailAdapter imgAdapt;
	//gallery object
	private Gallery picGallery;
	//image view for larger display
	private ImageView picView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.group_detail, container, false);

		// Get the large image view
        picView = (ImageView) view.findViewById(R.id.picture);
        
        // Get the gallery view
        picGallery = (Gallery) view.findViewById(R.id.gallery);
        
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
                
        // Create a new adapter
        imgAdapt = new PhotoDetailAdapter(getActivity());
        // Set the gallery adapter
        picGallery.setAdapter(imgAdapt);
            
        // Set the click listener for each item in the thumbnail gallery
        picGallery.setOnItemClickListener(new OnItemClickListener() {
        	
        	// Handle clicks
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	// Set the larger image view to display the chosen bitmap calling method of adapter class
                picView.setImageBitmap(imgAdapt.getPic(position));
            }
        });	
	
	}
	
	


}
