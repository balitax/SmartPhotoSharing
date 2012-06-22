package com.hmi.smartphotosharing.examples;

import android.app.ExpandableListActivity;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;

public class ExpendableListExample extends ExpandableListActivity {
    
public static String NAME = "NAME";
public static String IS_EVEN = "IS_EVEN";

/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    
        super.onCreate(savedInstanceState);
                
        setListAdapter(new ExpendableListAdapterExample(this));

        setContentView(R.layout.settings_list);
    }
    


}