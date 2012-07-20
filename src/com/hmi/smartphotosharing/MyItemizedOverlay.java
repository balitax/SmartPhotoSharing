package com.hmi.smartphotosharing;
import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private MapsListener listener;
	
	public MyItemizedOverlay(Drawable defaultMarker, MapsListener listener) {
		  super(boundCenterBottom(defaultMarker));
		  this.listener = listener;
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
		listener.updateGPS(p.getLatitudeE6()/1E6,p.getLongitudeE6()/1E6);
		mOverlays.clear();
        OverlayItem overlayitem = new OverlayItem(p, null, null);
        addOverlay(overlayitem);
        mapView.invalidate();
        return true;
	}
}
