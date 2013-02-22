package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.groups.GroupsActivity;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.LongResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Util;

public class Login extends Activity implements OnDownloadListener{
	
	public static int CORRECT = 200;
	public static int INCORRECT = 403;
	public static final String SESSION_PREFS = "session";
	public static final String SESSION_HASH = "sessionhash";
	public static final String SESSION_UID = "sessionuid";
	
	public static final int CODE_VALIDATE = 1;
	public static final int CODE_LOGIN = 2;

	private static final int CODE_REGISTER = 3;
	
	public static String SENDER_ID = "748116297344";
	
	EditText username;
	EditText password;
	
	@Override
	public void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Google Cloud Messaging 
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			Log.v("GCM", "Already registered");
		  
		}
		
		setContentView(R.layout.login);

		username = (EditText) findViewById(R.id.login_username);
		password = (EditText) findViewById(R.id.login_password);
		
		// TODO remove
		//username.setText("s0166049");
		//password.setText("changeme22");
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		if (hash != null) {
			String validateUrl = String.format(Util.getUrl(this,R.string.login_validate), hash);
	        new FetchJSON(this, CODE_VALIDATE).execute(validateUrl);
		}
		
		// Load preference defaults on first startup
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
		
	public void onClickLogin(View v) {
		
		if (hasConnection(this)) {
			String url = String.format(
					Util.getUrl(this, R.string.login_http), 
					username.getText().toString().trim(), 
					password.getText().toString()
					);
			Log.d("Json fetch",url);
	        new FetchJSON(this,CODE_LOGIN).execute(url);
		} else {
			Toast.makeText(this, this.getResources().getString(R.string.login_connection), Toast.LENGTH_SHORT).show();			
		}
	}

	public void onClickRegister(View v) {
		Uri uri = Uri.parse(Util.REGISTER_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
	}
	
	@Override
	public void parseJson(String json, int code) {
		switch(code) {
		case CODE_VALIDATE:
			parseValidate(json);
			break;
		case CODE_LOGIN:
			parseLogin(json);
			break;
		case CODE_REGISTER:
			parseRegister(json);
			break;
		default:
			
		}
	}

	private void parseRegister(String json) {
		Gson gson = new Gson();
		Log.d("JSON parse", json);
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
			//GCMRegistrar.setRegisteredOnServer(this, true);			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
			GCMRegistrar.unregister(this);
		}
	}

	private void parseValidate(String json) {
		
		Gson gson = new Gson();
		UserResponse response = gson.fromJson(json, UserResponse.class);
		
		if (response.getStatus() == CORRECT) {

			// Store the sessionhash in sharedpreferences
			SharedPreferences settings = getSharedPreferences(SESSION_PREFS,MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(SESSION_UID, response.getObject().getId());
			editor.commit();
			
			// GCM
			checkGCM();
			
			Intent intent = new Intent(this, GroupsActivity.class);
		    startActivity(intent);				
		}
		
	}

	private void parseLogin(String json) {
		Gson gson = new Gson();
		Log.i("Json fetch", json);
		
		LongResponse response = gson.fromJson(json, LongResponse.class);

		if (response.getStatus() == Util.STATUS_OK) {
			
			// GCM
			checkGCM();
			
			// Store the sessionhash in sharedpreferences
			SharedPreferences settings = getSharedPreferences(SESSION_PREFS,MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(SESSION_HASH, response.getMessage());
			editor.putLong(SESSION_UID, response.getObject());
			editor.commit();
			
			Intent intent = new Intent(this, GroupsActivity.class);
		    startActivity(intent);	
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	
		
		
	}

	private void checkGCM() {
		
			final String regId = GCMRegistrar.getRegistrationId(this);
			
			// Register with the server by sending the SENDER_ID
			Log.v("GCM", "Registering with server");
			SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
			String hash = settings.getString(Login.SESSION_HASH, null);
		
			String registerUrl = String.format(Util.getUrl(this,R.string.gcm_register),hash,regId);		
			new FetchJSON(this, CODE_REGISTER).execute(registerUrl);
		
	}	

	public boolean hasConnection(Context c) {
		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);;
		NetworkInfo i = cm.getActiveNetworkInfo();
		if (i == null)
			return false;
		if (!i.isConnected())
		    return false;
		if (!i.isAvailable())
		    return false;
		return true;
		
	}
}
