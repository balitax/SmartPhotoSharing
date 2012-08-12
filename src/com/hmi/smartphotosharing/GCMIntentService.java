package com.hmi.smartphotosharing;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.StringRepsonse;
import com.hmi.smartphotosharing.util.Util;

public class GCMIntentService extends GCMBaseIntentService implements OnDownloadListener {

	private static final int ACTION_DEFAULT 			= 0;
	private static final int ACTION_PHOTO_UPLOAD 		= 10;
	private static final int ACTION_PHOTO_COMMENT 		= 11;
	private static final int ACTION_PHOTO_LIKE			= 12;
	private static final int ACTION_GROUP_INVITE 		= 20;
	private static final int ACTION_SUBSCRIPTION_UPDATE = 30;
	
	private static final int CODE_REGISTER = 1;
	private static final int CODE_UNREGISTER = 2;

	public static String SENDER_ID = "748116297344";

    private static final String LOG_TAG = "GCM";

	public GCMIntentService(){
		super(SENDER_ID);

	}
		
	@Override
	protected void onError(Context context, String errorId) {
        Log.i( LOG_TAG, "GCMIntentService onError called: " + errorId );

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		
		String actionStr = intent.getStringExtra("action");
		String valueStr = intent.getStringExtra("value");
		
		// Read preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean notPhotoUpload = sharedPref.getBoolean(SettingsActivity.KEY_NOT_PHOTO_UPLOAD,true);
		boolean notPhotoComment = sharedPref.getBoolean(SettingsActivity.KEY_NOT_PHOTO_COMMENT,true);
		boolean notPhotoLike = sharedPref.getBoolean(SettingsActivity.KEY_NOT_PHOTO_LIKE, true);
		boolean notInvite = sharedPref.getBoolean(SettingsActivity.KEY_NOT_INVITE,true);
		boolean notSub = sharedPref.getBoolean(SettingsActivity.KEY_NOT_SUB,true);
		
		boolean notSound = sharedPref.getBoolean(SettingsActivity.KEY_NOT_SOUND,false);
		boolean notVibrate = sharedPref.getBoolean(SettingsActivity.KEY_NOT_VIBRATE,true);
		
		// Get the message contents
		// See the protocol on dropbox
		int id = Integer.parseInt(intent.getStringExtra("id"));
		String title = intent.getStringExtra("title");
		String content = intent.getStringExtra("content");
		int action = Integer.parseInt(actionStr);
		long value = Long.parseLong(valueStr);
		
        Log.d( LOG_TAG, "Title: " + title + " / Action: " + actionStr + " / Value: " + valueStr);

        // Create notification
		String ns = Context.NOTIFICATION_SERVICE;
		int icon = R.drawable.picalilly;
		CharSequence tickerText = title;
		long when = System.currentTimeMillis();
		
		// Get notification manager reference
		Notification notification = new Notification(icon, tickerText, when);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		// Sound, vibration, light settings
		if (notSound)
			notification.defaults |= Notification.DEFAULT_SOUND;
		
		if (notVibrate)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		// Set notification contents
		Context mcontext = this;
		CharSequence contentTitle = title;
		CharSequence contentText = content;
		
		Intent notificationIntent = null;

		// Create intent based on the action received
		switch(action) {

			case ACTION_GROUP_INVITE:
				if (!notInvite) break;
				notificationIntent = new Intent(this, GroupDetailActivity.class);
				notificationIntent.putExtra(GroupDetailActivity.KEY_ID, value);		
				break;
			case ACTION_PHOTO_LIKE:
				if(!notPhotoLike) break;
			case ACTION_PHOTO_UPLOAD:
				if (!notPhotoUpload) break;
			case ACTION_PHOTO_COMMENT:
				if (!notPhotoComment) break;
			case ACTION_SUBSCRIPTION_UPDATE:
				if (!notSub) break;
				notificationIntent = new Intent(this, SinglePhotoDetail.class);	
				notificationIntent.putExtra(SinglePhotoDetail.KEY_ID, value);	
				break;
			case ACTION_DEFAULT:
			default:
				notificationIntent = new Intent(this, GroupsActivity.class);
		}

		if (notificationIntent != null) {
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	
			notification.setLatestEventInfo(mcontext, contentTitle, contentText, contentIntent);
	        
			mNotificationManager.notify(id, notification);
		}
	}
	

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.i(LOG_TAG, "Register called: " + regId);
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
        String registerUrl = String.format(Util.getUrl(this,R.string.gcm_register),hash,regId);		
        new FetchJSON(this, CODE_REGISTER).execute(registerUrl);

	}

	@Override
	protected void onUnregistered(Context context, String regId) {

        Log.i( LOG_TAG, "Unregister: " + regId );

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
        String unregisterUrl = String.format(Util.getUrl(this,R.string.gcm_unregister),hash,regId);		
        new FetchJSON(this, CODE_UNREGISTER).execute(unregisterUrl);

	}

	@Override
	public void parseJson(String json, int code) {

		Log.i("GCM JSON Parse", json);
		
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		switch(code){
		
		case CODE_REGISTER:
			break;
		case CODE_UNREGISTER:
			break;
		default:
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
		
	}

}
