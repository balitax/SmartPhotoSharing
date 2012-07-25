package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.StringRepsonse;
import com.hmi.smartphotosharing.groups.GroupsActivity;

public class Login extends Activity implements OnDownloadListener{
	
	public static int CORRECT = 200;
	public static int INCORRECT = 403;
	public static final String SESSION_PREFS = "session";
	public static final String SESSION_HASH = "sessionhash";
	
	public static final int CODE_VALIDATE = 1;
	public static final int CODE_LOGIN = 2;

	private static final int CODE_REGISTER = 3;
	
	public static String SENDER_ID = "748116297344";
	
	EditText username;
	EditText password;
	
	@Override
	public void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		
		// Google Cloud Messaging 
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		} else {
		  
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				Log.v("GCM", "Already registered final");
			} else {
				
				// Register with the server by sending the SENDER_ID
				Log.v("GCM", "Registering with server");
				SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
				String hash = settings.getString(Login.SESSION_HASH, null);
			
				String registerUrl = String.format(getResources().getString(R.string.gcm_register),hash,regId);		
				new FetchJSON(this, CODE_REGISTER).execute(registerUrl);
			}
		}
		
		setContentView(R.layout.login);

		username = (EditText) findViewById(R.id.login_username);
		password = (EditText) findViewById(R.id.login_password);
		
		// TODO remove
		username.setText("s0166049");
		password.setText("changeme22");
		
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		String validateUrl = String.format(getResources().getString(R.string.login_validate), hash);

        new FetchJSON(this, CODE_VALIDATE).execute(validateUrl);
	}
		
	public void onClickLogin(View v) {
			
		String url = String.format(
				getResources().getString(R.string.login_http), 
				username.getText().toString(), 
				password.getText().toString()
				);
		Log.d("Json fetch",url);
        new FetchJSON(this,CODE_LOGIN).execute(url);
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
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show();
		
		if (response.status != CORRECT) {
			GCMRegistrar.unregister(this);			
		}
	}

	private void parseValidate(String json) {
		
		Gson gson = new Gson();
		StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
		
		if (response.status == CORRECT) {
			Intent intent = new Intent(this, GroupsActivity.class);
		    startActivity(intent);				
		}
		
	}

	private void parseLogin(String json) {
		Gson gson = new Gson();
		Log.i("Json fetch", json);
		try {
			StringRepsonse response = gson.fromJson(json, StringRepsonse.class);
	
			if (response.status != INCORRECT) {
				// Store the sessionhash in sharedpreferences
				SharedPreferences settings = getSharedPreferences(SESSION_PREFS,MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(SESSION_HASH, response.msg);
				editor.commit();
				
				Intent intent = new Intent(this, GroupsActivity.class);
			    startActivity(intent);	
			} else {
				Toast.makeText(this, "Login incorrect", Toast.LENGTH_SHORT).show();
			}
	
		} catch (JsonSyntaxException e) {
			Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
			Log.e("JSON", e.getMessage());
		}
		
	}	

}
