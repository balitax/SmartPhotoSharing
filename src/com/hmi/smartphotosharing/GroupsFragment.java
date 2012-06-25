package com.hmi.smartphotosharing;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.hmi.smartphotosharing.GroupAdapter.GroupHolder;

public class GroupsFragment extends ListFragment {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		        	
		return inflater.inflate(R.layout.groups, container, false);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create an array of Group objects, can be replaced by a database query later on
        Group groups[] = new Group[] {
        		new Group(R.drawable.ic_unknown, "Lowlands"),
        		new Group(R.drawable.ic_unknown, "Utwente"),
        		new Group(R.drawable.ic_unknown, "Holiday 2011")
        };
        
        setListAdapter(new GroupAdapter(getActivity(), R.layout.list_item, groups));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        // Add a listener to each item to catch mouse clicks
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            // When clicked, show a toast with the TextView text
        	GroupHolder gh = (GroupHolder)view.getTag();
            Toast.makeText(getActivity(), gh.txtTitle.getText(),
                Toast.LENGTH_SHORT).show();
          }
        });
	}
	
}