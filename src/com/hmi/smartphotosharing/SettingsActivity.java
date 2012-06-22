package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        // Replace the content of this Activity by the SettingsFragment
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
        
    }
    


}