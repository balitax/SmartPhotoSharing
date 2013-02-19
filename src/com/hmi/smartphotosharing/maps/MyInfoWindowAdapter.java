package com.hmi.smartphotosharing.maps;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.hmi.smartphotosharing.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class MyInfoWindowAdapter implements InfoWindowAdapter {

	ImageLoader imageLoader;
	LayoutInflater inflater=null;
	
	public MyInfoWindowAdapter(ImageLoader il, LayoutInflater inflater) {
		imageLoader = il;
	    this.inflater=inflater;
	}


	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public View getInfoContents(Marker marker) {
		View popup = inflater.inflate(R.layout.popup,null);
		
		ImageView img = (ImageView) popup.findViewById(R.id.image1);
		//img.setLayoutParams(new Gallery.LayoutParams(32,32));
		imageLoader.displayImage(marker.getTitle(), img, 
				new SimpleImageLoadingListener() {
				    @Override
				    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				        // Do whatever you want with Bitmap
				    }
				});
		
		return popup;
	}

}
