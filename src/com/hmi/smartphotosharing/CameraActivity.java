package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;
/**
 * Activity that represents the Camera page.
 * Used for taking pictures and handling sharing.
 * @author Edwin
 *
 */
public class CameraActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Use the camera.xml layout.
		// Example
        setContentView(R.layout.camera);

    }
}