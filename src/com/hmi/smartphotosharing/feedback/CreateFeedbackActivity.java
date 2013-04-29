package com.hmi.smartphotosharing.feedback;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.util.Util;


public class CreateFeedbackActivity extends Activity implements OnDownloadListener {

    private static final int CODE_UPLOAD = 1;
    
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    
    private boolean isRecording;
    private boolean isPlaying;
    
    private Button play;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.feedback_create);
        
        isRecording = true;
        isPlaying = true;
        
        play = (Button) findViewById(R.id.button_play);
        play.setEnabled(false);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Volume up/down button controls media instead of ringer volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
    	SimpleDateFormat format =
                new SimpleDateFormat("dd-MM-yyy_HH_mm_ss");
    	String fileName = "feedback_" + format.format(new Date()) + ".3gp";
    	
        mFileName = getExternalFilesDir(null).getAbsolutePath();
        mFileName += "/audio";

        File file = new File(mFileName);
        file.mkdirs();
        
        mFileName += "/" + fileName;
        
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    public void onClickRecord(View v) {
    	Button b = (Button)v;
    	
        onRecord(isRecording);
        if (isRecording) {
            b.setText("Stop recording");
            play.setEnabled(false);
        } else {
            b.setText("Start recording");
            play.setEnabled(true);
            ImageView ok = (ImageView) findViewById(R.id.record_ok);
            ok.setVisibility(ImageView.VISIBLE);
        }
        isRecording = !isRecording;
    }
    
       
    public void onClickPlay(View v) {
    	Button b = (Button)v;
    	
        onPlay(isPlaying);
        if (isPlaying) {
            b.setText("Stop playing");
        } else {
            b.setText("Start playing");
        }
        isPlaying = !isPlaying;
    }

    public void onClickSend(View v) {
    	// Get user session ID
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
		TextView text = (TextView) findViewById(R.id.message);
		String commentTxt = text.getText().toString();
		
		String shareUrl = Util.getUrl(this,R.string.feedback_http_add);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
        	
			map.put("sid", new StringBody(hash));
	        map.put("msg", new StringBody(commentTxt));
	        
	        if (mFileName != null) {
	    		File file = new File(mFileName);
	    		ContentBody cbFile = new FileBody(file, "audio/3gpp");
	    		map.put("audio", cbFile);
	        }

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(shareUrl,map);
        new PostRequest(this, CODE_UPLOAD,true).execute(pr);
    	
    }

	@Override
	public void parseJson(String json, int code) {

		switch(code){
			case(CODE_UPLOAD):
				parseUpload(json);
				break;
		default:
		}
		
	}
	
    private void parseUpload(String json) {
		Log.d("Json parse",json);     
				
		Gson gson = new Gson();
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK) {
        	Toast.makeText(this, "Feedback added", Toast.LENGTH_SHORT).show();
        	
    		setResult(RESULT_OK);
    		finish();
		} else {
        	Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();	
		}
		
	}
}

