package com.hmi.smartphotosharing.local;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.hmi.smartphotosharing.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class MyInfoWindowAdapter implements InfoWindowAdapter {

	private ImageLoader imageLoader;
	private LayoutInflater inflater=null;	
	private ImageView img;
	private TextView txt;

	View popup;
	private boolean flag;
	
	public MyInfoWindowAdapter(ImageLoader il, LayoutInflater inflater) {
		imageLoader = il;
	    this.inflater=inflater;
	    popup = inflater.inflate(R.layout.popup,null);
		img = (ImageView) popup.findViewById(R.id.image1);
		txt = (TextView) popup.findViewById(R.id.text1);
	}


	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
	

	@Override
	public View getInfoContents(final Marker marker) {
		
		String[] msg = marker.getSnippet().split(",");
		boolean isGroup = msg[0].equals(MapActivity.TYPE_GROUP);
		if (isGroup) {
			txt.setVisibility(TextView.VISIBLE);
			txt.setText(msg[2]);
		} else {
			txt.setVisibility(TextView.GONE);
		}
		
		if (!flag) {
			flag = true;
			
			imageLoader.displayImage(marker.getTitle(), img, new SimpleImageLoadingListener() {

				@Override
				public void onLoadingComplete(String imageUri, View view,
						Bitmap loadedImage) {
						if (flag) {
							Log.d("ImageLoader", "loading done");
							marker.hideInfoWindow();
							marker.showInfoWindow();
							flag = false;
						}
				}
			
			});
		}
		return popup;
	}
	
	/*
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
	}*/
}
