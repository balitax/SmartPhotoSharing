package com.hmi.smartphotosharing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.analytics.tracking.android.EasyTracker;
import com.hmi.smartphotosharing.util.Util;

public class FullscreenImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        Intent intent = getIntent();
        String url = intent.getStringExtra(Util.URL_MESSAGE);

        WebView w = (WebView)findViewById(R.id.webview);
        w.setBackgroundColor(Color.BLACK);
        WebSettings ws = w.getSettings();
        ws.setBuiltInZoomControls(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        
        // Maybe replace by cached image
        w.loadUrl(url);
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
}