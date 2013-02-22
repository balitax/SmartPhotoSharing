package com.hmi.smartphotosharing;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	public static final String KEY_NOT_PHOTO_UPLOAD = "pref_key_not_photo_upload";
	public static final String KEY_NOT_PHOTO_COMMENT = "pref_key_not_photo_comment";
	public static final String KEY_NOT_PHOTO_LIKE = "pref_key_not_photo_like";
	public static final String KEY_NOT_SUB = "pref_key_not_sub";
	public static final String KEY_NOT_FRIENDS = "pref_key_not_friends";
	public static final String KEY_NOT_INVITE = "pref_key_not_invite";

	public static final String KEY_NOT_SOUND = "pref_key_not_use_sound";
	public static final String KEY_NOT_VIBRATE = "pref_key_not_use_vibrate";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
    }
    
}