package com.hmi.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.content.ContentBody;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PostRequest extends AsyncTask<PostData,Void,String> {
    
	private Context c;
	private int code;
	private OnDownloadListener dl;
	private ProgressDialog pd;
	
	public PostRequest(Context c) {
		this.c = c;
		this.code = 0;
		this.dl = (OnDownloadListener) c;
	}
	
	/**
	 * Creates a FetchJSON object with the listener and a request code.
	 * The request code can be used when multiple requests are done from one activity.
	 * A switch statement can be used in the listener's parseJson method to distinguish between request codes.
	 * @param dl The listener object
	 * @param code The request code
	 */
	public PostRequest(Context c, int code) {
		this.c = c;
		this.code = code;
		this.dl = (OnDownloadListener) c;
		
	}
	
	@Override
    protected void onPreExecute() {
		pd = new ProgressDialog(c);
		pd.setMessage("Loading...");
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.show();
    }
    
	@Override
	protected String doInBackground(PostData... data) {
         
       // params comes from the execute() call: params[0] is the url.
       try {
           return sendPost(data[0]);
       } catch (IOException e) {
    	   ErrorResponse err = new ErrorResponse();
    	   
           return new Gson().toJson(err);
       }
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			dl.parseJson(result, code);
		} catch (JsonSyntaxException e) {
			Log.e("JSON", "Json syntax exception: " + e.getMessage());
			Log.e("JSON", result);
		}
		if (pd != null) pd.dismiss();
	}
	
	public String sendPost(PostData pr) throws IOException, ClientProtocolException  {
		
		HttpClient httpclient = new DefaultHttpClient();

		HttpPost httppost = new HttpPost(pr.url);
		    		
		MultipartEntity mpEntity = new MultipartEntity();
		
		// For each <String,ContentBody> pair in the map, add it as an entity to the post entity
	    Iterator<Map.Entry<String,ContentBody>> it = pr.map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String,ContentBody> pairs = (Map.Entry<String,ContentBody>)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());

			mpEntity.addPart(pairs.getKey(), pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	
		httppost.setEntity(mpEntity);
		Log.d("sendPost","executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		Log.d("sendPost",""+response.getStatusLine());
		
		String res = null;
		if (resEntity != null) {
			res = EntityUtils.toString(resEntity);
			Log.d("sendPost",res);
			resEntity.consumeContent();
		}
		httpclient.getConnectionManager().shutdown();
		return res;
	}
}