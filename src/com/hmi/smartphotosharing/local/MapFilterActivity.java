package com.hmi.smartphotosharing.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hmi.smartphotosharing.R;

public class MapFilterActivity extends Activity {

    public static final String FILTER_PREFS = "MAPFILTER";

    public static final String TYPE 			= "type";
    public static final String DATE_TYPE 		= "dateType";
    public static final String DATE_VALUE 		= "dateValue";
    public static final String BORDERS 			= "borders";
    
	// Filter settings
	private boolean filterShowGroupBorders;
	private int filterType, filterDate;
	
	private CheckBox cBorders;
	private RadioGroup rShow, rDate;
	private List<Integer> ids, dates;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.local_map_filter);

    	/*SharedPreferences s = getSharedPreferences(FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = s.edit();
        editor.clear();
        editor.commit();*/
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(FILTER_PREFS,MODE_PRIVATE);
        filterType = settings.getInt(TYPE, 0);
        filterDate = settings.getInt(DATE_TYPE, 0);
        filterShowGroupBorders = settings.getBoolean("borders", true);

        // Store all the checkbox IDs in an arraylist
        // The index will represent the value of filterType
        ids = new ArrayList<Integer>();
        Collections.addAll(ids, 
        		R.id.radio_map_filter_all, 
        		R.id.radio_map_filter_friends, 
        		R.id.radio_map_filter_groups);
        
        dates = new ArrayList<Integer>();
        Collections.addAll(dates,
        		R.id.radio_map_filter_all_time,
        		R.id.radio_map_filter_yesterday,
        		R.id.radio_map_filter_last_week,
        		R.id.radio_map_filter_last_month);
        
        rShow = (RadioGroup) findViewById(R.id.radio_filter_show);
        rShow.setOnCheckedChangeListener(new MyTypeListener());
        
        rDate = (RadioGroup) findViewById(R.id.radio_filter_date);
        rDate.setOnCheckedChangeListener(new MyDateListener());
        
        cBorders = (CheckBox) findViewById(R.id.checkbox_borders);

        // Set to stored data
    	if (filterShowGroupBorders) cBorders.setChecked(true);
    	
		rShow.check(ids.get(filterType));
		rDate.check(dates.get(filterDate));
    }
    
    public void onSubmitClick(View view) {
    	boolean borders = cBorders.isChecked();
    	SharedPreferences settings = getSharedPreferences(FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(BORDERS, borders);
        editor.commit();
    	
    	setResult(Activity.RESULT_OK);
    	finish();
    }
    
    public void setType(int type) {
    	SharedPreferences settings = getSharedPreferences(FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TYPE, type);
        editor.commit();
    }
    
    public void setDate(int dateType, int dateValue) {
    	SharedPreferences settings = getSharedPreferences(FILTER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putInt(DATE_TYPE, dateType);
        editor.putInt(DATE_VALUE, dateValue);
        editor.commit();
    }
    
    public class MyTypeListener implements RadioGroup.OnCheckedChangeListener {

    	@Override
    	public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
            
            RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(checkedId);
            // If the radiobutton that has changed in check state is now checked...
            if (checkedRadioButton.isChecked()) {
                filterType = ids.indexOf(checkedId);
                setType(filterType);
            }
        }
    }
    
    public class MyDateListener implements RadioGroup.OnCheckedChangeListener {
    	
    	@Override
        public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
            
            RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(checkedId);
            // If the radiobutton that has changed in check state is now checked...
            if (checkedRadioButton.isChecked()) {
            	
            	filterDate = dates.indexOf(checkedId);
            	
            	int date = 0;
            	switch(checkedId) {
            	case R.id.radio_map_filter_last_month:
            		date = 60*60*24*31;
            		break;
            	case R.id.radio_map_filter_last_week:
            		date = 60*60*24*7;
            		break;
            	case R.id.radio_map_filter_yesterday:
            		date = 60*60*24;
            		break;
            	default:
            		date = (int) (System.currentTimeMillis() / 1000);
                }
                setDate(filterDate, date);
            }
        }
    }
}
