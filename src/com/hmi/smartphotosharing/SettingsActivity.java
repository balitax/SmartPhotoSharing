package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;

public class SettingsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Use the settings.xml layout.
        setContentView(R.layout.settings);

    }
}