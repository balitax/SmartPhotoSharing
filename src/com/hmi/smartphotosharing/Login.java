package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.LoginResponse;
import com.hmi.json.OnDownloadListener;

public class Login extends Activity implements OnDownloadListener{
	
	public static int INCORRECT = 403;
	
	public TextView status;
	
	@Override
	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
	
		setContentView(R.layout.login);
		
		status = (TextView) findViewById(R.id.login_status);
	}
	
	public void onClickLogin(View v) {

		EditText username = (EditText) findViewById(R.id.login_username);
		EditText password = (EditText) findViewById(R.id.login_password);
			
		String url = String.format(
				getResources().getString(R.string.login_http), 
				username.getText().toString(), 
				password.getText().toString()
				);

        new FetchJSON(this).execute(url);
	}

	@Override
	public void parseJson(String json) {
		Gson gson = new Gson();
		LoginResponse response = gson.fromJson(json, LoginResponse.class);

		if (response.status != INCORRECT) {
			Intent intent = new Intent(this, SmartPhotoSharing.class);
		    startActivity(intent);	
		} else {
			status.setText("Login incorrect");
		}
		
	}	

}
