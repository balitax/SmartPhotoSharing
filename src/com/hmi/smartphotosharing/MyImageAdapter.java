package com.hmi.smartphotosharing;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.photo.Photo;

public class MyImageAdapter extends BaseAdapter {
	private Context mContext;
	private DrawableManager dm;
	private Photo[] data;
	
    public MyImageAdapter(Context c, Photo[] list, DrawableManager dm) {
        mContext = c;
        this.dm = dm;
        this.data = list;
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        Photo photo = data[position];
        dm.fetchDrawableOnThread(photo.src, imageView);
        
        return imageView;
    }
    
    public View getEmptyView() {
    	LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.popular, null);
    	
    	TextView textView = (TextView) view.findViewById(R.id.empty_list_view);
    	
    	return textView;
    }
    
}
