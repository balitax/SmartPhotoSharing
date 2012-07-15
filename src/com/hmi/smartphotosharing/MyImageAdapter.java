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

import com.hmi.json.PhotoMessage;

public class MyImageAdapter extends BaseAdapter {
	private Context mContext;
	private DrawableManager dm;
	private List<PhotoMessage> data;
	
    public MyImageAdapter(Context c, List<PhotoMessage> list, DrawableManager dm) {
        mContext = c;
        this.dm = dm;
        this.data = list;
    }

    public int getCount() {
        return data.size();
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

        PhotoMessage photo = data.get(position);
        
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
