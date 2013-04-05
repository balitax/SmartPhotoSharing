package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyImageAdapter extends BaseAdapter {
		
	private Context mContext;
	private ImageLoader imageLoader;
	private List<Photo> data;
	
    public MyImageAdapter(Context c, List<Photo> list) {
        mContext = c;
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
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
        View v = convertView;
        ViewHolder holder;
        Photo photo = getItem(position);
        
        if (v == null) {  // if it's not recycled, initialize some attributes
        	LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	v = vi.inflate(R.layout.gridview_item, null);
            
            holder = new ViewHolder();
            holder.img = (ImageView) v.findViewById(R.id.icon);
            
            v.setTag(holder);
        } else {
        	holder = (ViewHolder) v.getTag();
        }

        holder.img.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        if (photo.isNew)
        	holder.img.setBackgroundColor(0xFFFF0000);
               
        String url = Util.getThumbUrl(photo);
        imageLoader.displayImage(url, holder.img);
        
        return v;
    }
    
    static class ViewHolder {
    	ImageView img;
    }
        
}
