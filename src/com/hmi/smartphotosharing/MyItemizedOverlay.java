package com.hmi.smartphotosharing;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
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
	
	private class RectangleOverlay extends Overlay {

		GeoPoint start, end;
		private Paint paint = new Paint();

		public RectangleOverlay(GeoPoint start, GeoPoint end) {
			this.start = start;
			this.end = end;
		}
		
	    public boolean onTouchEvent(MotionEvent e, MapView m){
	    	return false;
	    }

	    @Override
	    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
	            long when) {

	        if(start != null && end != null){
	            //get the 2 geopoints defining the area and transform them to pixels
	            //this way if we move or zoom the map rectangle will follow accordingly
	            Point screenPts1 = new Point();
	            mapView.getProjection().toPixels(start, screenPts1);
	            Point screenPts2 = new Point();
	            mapView.getProjection().toPixels(end, screenPts2);

	            //draw inner rectangle
	            paint.setColor(0x4435EF56);
	            paint.setStyle(Style.FILL);
	            canvas.drawRect(screenPts1.x, screenPts1.y, screenPts2.x, screenPts2.y, paint);
	            //draw outline rectangle
	            paint.setColor(0x88158923);
	            paint.setStyle(Style.STROKE);
	            canvas.drawRect(screenPts1.x, screenPts1.y, screenPts2.x, screenPts2.y, paint);
	        }
	        return true;
	    }

	}

}
