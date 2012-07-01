package com.hmi.smartphotosharing.groups;

import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.R.id;
import com.hmi.smartphotosharing.R.layout;

import android.app.Fragment;
import android.app.FragmentTransaction;
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

public class GroupDetailFragment extends Fragment {

	//variable for selection intent
	private final int PICKER = 1;
	//adapter for gallery view
	private MyGalleryAdapter imgAdapt;
	//gallery object
	private Gallery picGallery;
	//image view for larger display
	private ImageView picView;
	private TextView textView;
	
	private long id;
	
	public GroupDetailFragment(long id) {
		this.id = id;
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
        picView = (ImageView) view.findViewById(R.id.picture);
        textView = (TextView) view.findViewById(R.id.group_detail_intro);
        // Get the gallery view
        picGallery = (Gallery) view.findViewById(R.id.gallery);
        
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         
        textView.setText("Selected id was: " + String.valueOf(id));
        
        // Create a new adapter
        imgAdapt = new MyGalleryAdapter(getActivity());
        // Set the gallery adapter
        picGallery.setAdapter(imgAdapt);
            
        // Set the click listener for each item in the thumbnail gallery
        picGallery.setOnItemClickListener(new OnItemClickListener() {
        	
        	// Handle clicks
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	// Set the larger image view to display the chosen bitmap calling method of adapter class
          		Fragment newFragment = new GroupDetailFragment(id);
              	FragmentTransaction ft = getFragmentManager().beginTransaction();

              	// Replace whatever is in the fragment_container view with this fragment,
              	// and add the transaction to the back stack
              	ft.replace(android.R.id.tabcontent, newFragment);
              	ft.addToBackStack(null);

              	// Commit the transaction
              	ft.commit();
            }
        });	
	
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.group_menu, menu);
	}	


}
