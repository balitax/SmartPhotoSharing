package com.hmi.smartphotosharing;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.hmi.smartphotosharing.GroupAdapter.GroupHolder;

public class GroupsActivity extends ListActivity {
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create an array of Group objects, can be replaced by a database query later on
        Group groups[] = new Group[] {
        		new Group(R.drawable.unknown, "Lowlands"),
        		new Group(R.drawable.unknown, "Utwente"),
        		new Group(R.drawable.unknown, "Holiday 2011")
        };
        
        setListAdapter(new GroupAdapter(this, R.layout.list_item, groups));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        // Add a listener to each item to catch mouse clicks
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            // When clicked, show a toast with the TextView text
        	GroupHolder gh = (GroupHolder)view.getTag();
            Toast.makeText(getApplicationContext(), gh.txtTitle.getText(),
                Toast.LENGTH_SHORT).show();
          }
        });


    }
}