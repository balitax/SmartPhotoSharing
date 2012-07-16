package com.hmi.smartphotosharing;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Base Adapter subclass creates Gallery view
 * - provides method for adding new images from user selection
 * - provides method to return bitmaps from array
 *
 */
public class MyGalleryAdapter extends BaseAdapter {

	private int currentPic = 0;
	
	//use the default gallery background image
    int defaultItemBackground;
    
    //gallery context
    private Context context;

    //array to store bitmaps to display
    private Bitmap[] imageBitmaps;
    //placeholder bitmap for empty spaces in gallery
    Bitmap placeholder;

    //constructor
    public MyGalleryAdapter(Context c) {
    	
    	//instantiate context
    	context = c;
    	
    	//create bitmap array
        imageBitmaps  = new Bitmap[10];
        //decode the placeholder image
        placeholder = BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher);
        
        //set placeholder as all thumbnail images in the gallery initially
        for(int i=0; i<imageBitmaps.length; i++)
        	imageBitmaps[i]=placeholder;
        
        //get the styling attributes - use default Andorid system resources
        TypedArray styleAttrs = context.obtainStyledAttributes(R.styleable.PicGallery);
        //get the background resource
        defaultItemBackground = styleAttrs.getResourceId(
        		R.styleable.PicGallery_android_galleryItemBackground, 0);
        //recycle attributes
        styleAttrs.recycle();
    }

    //BaseAdapter methods
    
    //return number of data items i.e. bitmap images
    public int getCount() {
        return imageBitmaps.length;
    }

    //return item at specified position
    public Object getItem(int position) {
        return position;
    }

    //return item ID at specified position
    public long getItemId(int position) {
        return position;
    }

    //get view specifies layout and display options for each thumbnail in the gallery
    public View getView(int position, View convertView, ViewGroup parent) {

    	//create the view
        ImageView imageView = new ImageView(context);
        //specify the bitmap at this position in the array
        imageView.setImageBitmap(imageBitmaps[position]);
        //set layout options
        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        //scale type within view area
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //set default gallery item background
        imageView.setBackgroundResource(defaultItemBackground);
        //return the view
        
        imageView.setOnClickListener(new OnItemClickListener(context,position));
        
        return imageView;
    }
    
    private class OnItemClickListener implements OnClickListener{       
        private int mPosition;
        private Context context;
        
        public OnItemClickListener(Context context, int position){
            mPosition = position;
            this.context = context;
        }
        
        @Override
        public void onClick(View arg0) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
        	context.startActivity(intent);
        }       
    }
    //custom methods for this app
    
    //helper method to add a bitmap to the gallery when the user chooses one
    public void addPic(Bitmap newPic) {
    	//set at currently selected index
    	imageBitmaps[currentPic] = newPic;
    }
    
    //return bitmap at specified position for larger display
    public Bitmap getPic(int posn) {
    	//return bitmap at posn index
    	return imageBitmaps[posn];
    }
    
    public void setPic(int pos) {
    	currentPic = pos;
    }
}