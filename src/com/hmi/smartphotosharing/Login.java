package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.LoginResponse;
import com.hmi.json.OnDownloadListener;

public class Login extends Activity implements OnDownloadListener{
	
	public static int INCORRECT = 403;
	
	EditText username;
	EditText password;
	
	@Override
	public void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		setContentView(R.layout.login);

		username = (EditText) findViewById(R.id.login_username);
		password = (EditText) findViewById(R.id.login_password);
		
		// TODO remove
		username.setText("s0166049");
		password.setText("changeme22");
	}
		
	public void onClickLogin(View v) {

			
		String url = String.format(
				getResources().getString(R.string.login_http), 
				username.getText().toString(), 
				password.getText().toString()
				);

        new FetchJSON(this).execute(url);
	}

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		LoginResponse response = gson.fromJson(json, LoginResponse.class);

		if (response.status != INCORRECT) {
			Intent intent = new Intent(this, SmartPhotoSharing.class);
		    startActivity(intent);	
		} else {
			Toast.makeText(this, "Login incorrect", Toast.LENGTH_SHORT).show();
		}
		
	}	

}
