package com.hmi.smartphotosharing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RectangleOverlay extends Overlay {

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
            paint.setColor(0x2235EF56);
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