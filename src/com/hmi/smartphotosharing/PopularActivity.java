package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;

public class PopularActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        // Use the popular.xml layout.
        setContentView(R.layout.popular);

    }
}