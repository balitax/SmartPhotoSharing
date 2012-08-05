package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.hmi.smartphotosharing.json.Photo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Base Adapter subclass creates Gallery view
 * - provides method for adding new images from user selection
 * - provides method to return bitmaps from array
 *
 */
public class MyGalleryAdapter extends BaseAdapter {


	private Context context;
	private ImageLoader imageLoader;
	private List<Photo> data;
	
	//use the default gallery background image
    int defaultItemBackground;
    
    //placeholder bitmap for empty spaces in gallery
    Bitmap placeholder;

    //constructor
    public MyGalleryAdapter(Context c, List<Photo> list, ImageLoader im) {
        context = c;
        this.data = list;
        this.imageLoader = ImageLoader.getInstance();
        
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
        .memoryCacheExtraOptions(50, 50)
	        .build();
        
        //Initialize ImageLoader with created configuration. Do it once.
        imageLoader.init(config);

        
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
        return getItem(position).getId();
    }

    //get view specifies layout and display options for each thumbnail in the gallery
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
       
        if(v == null) {
	    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        v = inflater.inflate(R.layout.simple_image, null);
	        //specify the bitmap at this position in the array

            ViewHolder h = new ViewHolder();
            h.imgIcon = (ImageView)v.findViewById(R.id.image1);
            v.setTag(h);
        }

        ViewHolder holder = (ViewHolder)v.getTag();

        //set layout options
        holder.imgIcon.setLayoutParams(new Gallery.LayoutParams(45, 45));
        
        Photo photo = data.get(position);
        imageLoader.displayImage(photo.thumb, holder.imgIcon);
        
        //scale type within view area
        holder.imgIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //set default gallery item background
        //imageView.setBackgroundResource(defaultItemBackground);
        holder.imgIcon.setPadding(2,2,2,2);
        if (getItem(position).isNew)
        	holder.imgIcon.setBackgroundColor(0xFFFF0000);
        //return the view
        
        //imageView.setOnLongClickListener(new OnItemClickListener(context, position));
        
        return v;
    }
    
    static class ViewHolder {
        ImageView imgIcon;
    }


}