package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.hmi.json.Photo;

/**
 * Base Adapter subclass creates Gallery view
 * - provides method for adding new images from user selection
 * - provides method to return bitmaps from array
 *
 */
public class MyGalleryAdapter extends BaseAdapter {

	private Context context;
	private DrawableManager dm;
	private List<Photo> data;
	
	//use the default gallery background image
    int defaultItemBackground;
    
    //placeholder bitmap for empty spaces in gallery
    Bitmap placeholder;

    //constructor
    public MyGalleryAdapter(Context c, List<Photo> list, DrawableManager dm) {
        context = c;
        this.dm = dm;
        this.data = list;
    }

    //BaseAdapter methods
    
    @Override
    public int getCount() {
    	if (data == null) {
    		return 0;
    	} else {
    		return data.size();
    	}
    }

    @Override
    public Photo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    //get view specifies layout and display options for each thumbnail in the gallery
    public View getView(int position, View convertView, ViewGroup parent) {

    	//create the view
        ImageView imageView = new ImageView(context);
        //specify the bitmap at this position in the array

        Photo photo = data.get(position);
        
        String url = photo.thumb;
        dm.fetchDrawableOnThread(url, imageView);
        
        //set layout options
        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        //scale type within view area
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //set default gallery item background
        imageView.setBackgroundResource(defaultItemBackground);
        //return the view
        
        imageView.setOnClickListener(new OnItemClickListener(context, position));
        
        return imageView;
    }
    
    private class OnItemClickListener implements OnClickListener{    
        private Context context;
        private int position;
        
        public OnItemClickListener(Context context, int position){
            this.context = context;
            this.position = position;
        }
        
        @Override
        public void onClick(View arg0) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
        	intent.putExtra("id", getItemId(position));
        	context.startActivity(intent);
        }       
    }
 
}