package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.json.Photo;

public class MyImageAdapter extends BaseAdapter {
	private Context mContext;
	private DrawableManager dm;
	private List<Photo> data;
	
    public MyImageAdapter(Context c, List<Photo> list, DrawableManager dm) {
        mContext = c;
        this.dm = dm;
        this.data = list;
    }

    @Override
    public int getCount() {
    	if (data == null){
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        Photo photo = data.get(position);
        
        String url = photo.thumb;
        dm.fetchDrawableOnThread(url, imageView);
        
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
