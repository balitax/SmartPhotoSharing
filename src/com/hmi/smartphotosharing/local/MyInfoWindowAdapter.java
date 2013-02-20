package com.hmi.smartphotosharing.local;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.hmi.smartphotosharing.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class MyInfoWindowAdapter implements InfoWindowAdapter {

	private ImageLoader imageLoader;
	private LayoutInflater inflater=null;	

    private Marker mSelectedMarker;
    private boolean mRefreshingInfoWindow;
    
	public MyInfoWindowAdapter(ImageLoader il, LayoutInflater inflater) {
		imageLoader = il;
	    this.inflater=inflater;
	}


	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
	

	@Override
	public View getInfoContents(Marker marker) {
		View popup = inflater.inflate(R.layout.popup,null);
		if (mRefreshingInfoWindow) {
            // Refresh your info window
        } else {
            mSelectedMarker = marker;
            // The info window had been shown due to a click on a marker
            // Do whatever you want (for instance start an async request
            // that will update the info window later)
    		ImageView img = (ImageView) popup.findViewById(R.id.image1);
    		imageLoader.displayImage(marker.getTitle(), img, new MyImageLoadingListener());
        }
		
		return popup;
	}
	
    private void refreshInfoWindow() {
        if (mSelectedMarker == null) {
            return;
        }
        mRefreshingInfoWindow = true;
        mSelectedMarker.showInfoWindow();
        mRefreshingInfoWindow = false;
    }
               
	private class MyImageLoadingListener implements ImageLoadingListener {

		
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			Log.d("ImageLoader", "Done loading ");
			refreshInfoWindow();
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
			
		}
	}
}
