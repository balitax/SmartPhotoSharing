package com.hmi.smartphotosharing.maps;
import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public MyItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
		
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	@Override
	public int size() {
		  return mOverlays.size();
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView)  {
		
		/*
		mOverlays.clear();
        OverlayItem overlayitem = new OverlayItem(p, null, null);
        addOverlay(overlayitem);
        mapView.invalidate();
        */
        
        // Make rectangle
		mapView.getOverlays().clear();
        GeoPoint topLeft = mapView.getProjection().fromPixels(0, 0);
        GeoPoint bottomRight = mapView.getProjection().fromPixels(mapView.getWidth()-1, mapView.getHeight()-1);
        RectangleOverlay rect = new RectangleOverlay(topLeft,bottomRight);
        mapView.getOverlays().add(rect);
        
        return true;
	}
	
}
