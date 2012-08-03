package com.hmi.smartphotosharing.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class FetchJSON extends AsyncTask<String,Void,String> {
    
	private int code;
	private OnDownloadListener dl;
	private ProgressDialog pd;
	
	public FetchJSON(Object o) {
		this.code = 0;
		this.dl = (OnDownloadListener) o;
	}
	
	/**
	 * Creates a FetchJSON object with the listener and a request code.
	 * The request code can be used when multiple requests are done from one activity.
	 * A switch statement can be used in the listener's parseJson method to distinguish between request codes.
	 * @param dl The listener object
	 * @param code The request code
	 */
	public FetchJSON(Object o, int code) {
		this.code = code;
		this.dl = (OnDownloadListener) o;
		
	}
	
	@Override
	protected String doInBackground(String... urls) {
         
       // params comes from the execute() call: params[0] is the url.
       try {
           return downloadUrl(urls[0]);
       } catch (IOException e) {
    	   ErrorResponse err = new ErrorResponse();
    	   
           return new Gson().toJson(err);
       }
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			if (result != "") {
				dl.parseJson(result, code);
			} else {
	    	   ErrorResponse err = new ErrorResponse();
	           dl.parseJson(new Gson().toJson(err), code);
			}
		} catch (JsonSyntaxException e) {
			Log.e("JSON", "Json syntax exception: " + e.getMessage());
			Log.e("JSON", result);
		}
		if (pd != null) pd.dismiss();
	}
	
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private String downloadUrl(String myurl) throws IOException {
	    InputStream is = null;
	    // Only display the first 500 characters of the retrieved
	    // web page content.
	        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = readIt(is);
	        return contentAsString;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	// Reads an InputStream and converts it to a String.
	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
		    		
	    Reader reader = new InputStreamReader(stream, "UTF-8");  
		BufferedReader br = new BufferedReader(reader);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		br.close();
		reader.close();
		return sb.toString();
	}
}