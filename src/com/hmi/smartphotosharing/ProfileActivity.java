package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;

public class ProfileActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Use the profile.xml layout.
        setContentView(R.layout.profile);
    }
}