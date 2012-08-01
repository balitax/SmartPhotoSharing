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

import com.hmi.smartphotosharing.json.Photo;

public class MyImageAdapter extends BaseAdapter {
	
	private static int DIM = 46;
	
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
        View v = convertView;
        ViewHolder holder;
        
        if (v == null) {  // if it's not recycled, initialize some attributes
        	LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	v = vi.inflate(R.layout.gridview_item, null);
            
            holder = new ViewHolder();
            holder.img = (ImageView) v.findViewById(R.id.icon);
            
            v.setTag(holder);
        } else {
        	holder = (ViewHolder) v.getTag();
        }

        holder.img.setLayoutParams(new GridView.LayoutParams(DIM, DIM));
        holder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        if (getItem(position).isNew)
        	holder.img.setBackgroundColor(0xFFFF0000);
        
        Photo photo = data.get(position);
        
        String url = photo.thumb;
        dm.fetchDrawableOnThread(url, holder.img);
        
        return v;
    }
    
    static class ViewHolder {
    	ImageView img;
    }
        
}
