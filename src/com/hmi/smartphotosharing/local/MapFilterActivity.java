package com.hmi.smartphotosharing.local;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hmi.smartphotosharing.R;

public class MapFilterActivity extends Activity {

	// Filter settings
	private boolean filterShowGroupBorders;
	private int filterType;
	
	private CheckBox cBorders;
	private RadioGroup rShow;
	private RadioButton rFriends, rGroups, rAll;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.local_map_filter);
                
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(MapActivity.FILTER_PREFS,MODE_PRIVATE);
        filterType = settings.getInt("type", 0);
        filterShowGroupBorders = settings.getBoolean("borders", true);

        rShow = (RadioGroup) findViewById(R.id.radio_filter_show);
        rFriends = (RadioButton) findViewById(R.id.radio_map_filter_friends);
        rGroups = (RadioButton) findViewById(R.id.radio_map_filter_groups);
        rAll = (RadioButton) findViewById(R.id.radio_map_filter_all);
        cBorders = (CheckBox) findViewById(R.id.checkbox_borders);
        
        selectType();
    }
    
    private void selectType() {
    	
    	if (filterShowGroupBorders) cBorders.setChecked(true);
    	
		switch (filterType) {
		case MapActivity.FILTER_GROUPS:
			rShow.check(rGroups.getId());
			break;
		case MapActivity.FILTER_FRIENDS:
			rShow.check(rFriends.getId());
			break;
		case MapActivity.FILTER_ALL:
			rShow.check(rAll.getId());
			break;
		}
		
	}

    public void onSubmitClick(View view) {
    	boolean borders = cBorders.isChecked();
    	SharedPreferences settings = getSharedPreferences(MapActivity.FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("borders", borders);
        editor.commit();
    	
    	setResult(Activity.RESULT_OK);
    	finish();
    }
    
	public void onFriendsClick(View view) {
    	setType(MapActivity.FILTER_FRIENDS);    	
    }
    
    public void onGroupsClick(View view) {
    	setType(MapActivity.FILTER_GROUPS);
    }
    
    public void onAllClick(View view) {
    	setType(MapActivity.FILTER_ALL);
    }
    
    public void setType(int type) {
    	SharedPreferences settings = getSharedPreferences(MapActivity.FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("type", type);
        editor.commit();
    }
}
