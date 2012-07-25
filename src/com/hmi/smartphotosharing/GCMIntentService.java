package com.hmi.smartphotosharing;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.StringRepsonse;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;


public class GCMIntentService extends GCMBaseIntentService implements OnDownloadListener {

	private static final int ACTION_DEFAULT = 0;
	private static final int ACTION_PHOTO_UPLOAD = 10;
	private static final int ACTION_GROUP_INVITE = 20;
	
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
		
		// Get the message contents
		// See the protocol on dropbox
		int id = Integer.parseInt(intent.getStringExtra("id"));
		String title = intent.getStringExtra("title");
		String content = intent.getStringExtra("content");
		int action = Integer.parseInt(intent.getStringExtra("action"));
		long value = Long.parseLong(intent.getStringExtra("value"));
		
        Log.d( LOG_TAG, "Title: " + title + " / Action: " + action + " / Value: " + value);

        // Create notification
		String ns = Context.NOTIFICATION_SERVICE;
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = title;
		long when = System.currentTimeMillis();
		
		// Get notification manager reference
		Notification notification = new Notification(icon, tickerText, when);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		// Set notification contents
		Context mcontext = getApplicationContext();
		CharSequence contentTitle = title;
		CharSequence contentText = content;
		
		Intent notificationIntent;

		// Create intent based on the action received
		switch(action) {

		case ACTION_PHOTO_UPLOAD:
			notificationIntent = new Intent(this, PhotoDetailActivity.class);	
			notificationIntent.putExtra("id", value);
			break;
		case ACTION_GROUP_INVITE:
			notificationIntent = new Intent(this, GroupDetailActivity.class);
			notificationIntent.putExtra("id", value);		
			break;
		default:
			notificationIntent = new Intent(this, GroupsActivity.class);
		}
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(mcontext, contentTitle, contentText, contentIntent);
        
		mNotificationManager.notify(id, notification);
	}
	

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.i(LOG_TAG, "Register called: " + regId);
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
        String registerUrl = String.format(getResources().getString(R.string.gcm_register),hash,regId);		
        new FetchJSON(this, CODE_REGISTER).execute(registerUrl);

	}

	@Override
	protected void onUnregistered(Context context, String regId) {

        Log.i( LOG_TAG, "Unregister: " + regId );

		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
        String unregisterUrl = String.format(getResources().getString(R.string.gcm_unregister),hash,regId);		
        new FetchJSON(this, CODE_UNREGISTER).execute(unregisterUrl);

	}

	@Override
	public void parseJson(String json, int code) {

		Log.i("GCM JSON Parse", json);
		
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		switch(code){
		
		case CODE_REGISTER:
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			break;
		case CODE_UNREGISTER:
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			break;
		default:
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
		
	}

}