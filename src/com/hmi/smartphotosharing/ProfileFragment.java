package com.hmi.smartphotosharing;

import com.hmi.smartphotosharing.groups.Group;
import com.hmi.smartphotosharing.groups.GroupAdapter;
import com.hmi.smartphotosharing.groups.GroupDetailFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ProfileFragment extends ListFragment {

    private OnLoadDataListener mListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.profile, container, false);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create an array of Group objects, can be replaced by a database query later on
        Group groups[] = new Group[] {
        		//TODO 
        		//new Group(R.drawable.ic_unknown, "Lowlands"),
        		//new Group(R.drawable.ic_unknown, "Utwente"),
        		//new Group(R.drawable.ic_unknown, "Holiday 2011")
        };
        
        setListAdapter(new GroupAdapter(getActivity(), R.layout.list_item, groups, mListener.getDrawableManager()));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        // Add a listener to each item to catch mouse clicks
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {

	      		Fragment newFragment = Fragment.instantiate(getActivity(), GroupDetailFragment.class.getName());
	          	FragmentTransaction ft = getFragmentManager().beginTransaction();
	
	          	// Replace whatever is in the fragment_container view with this fragment,
	          	// and add the transaction to the back stack
	          	ft.replace(android.R.id.content, newFragment);
	          	ft.addToBackStack(null);
	
	          	// Commit the transaction
	          	ft.commit();
          }
        });
	}
	
}