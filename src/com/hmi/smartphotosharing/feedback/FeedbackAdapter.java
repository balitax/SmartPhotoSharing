package com.hmi.smartphotosharing.feedback;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.UserDetailActivity;
import com.hmi.smartphotosharing.json.Feedback;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.util.DateUtil;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class FeedbackAdapter extends ArrayAdapter<Feedback> {
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<Feedback> data;		// A User array that contains all list items
		
	public FeedbackAdapter(Context context, int resource, List<Feedback> objects) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
	}
	
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Feedback getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
    
	/**
	 * This method overrides the inherited getView() method.
	 * It is called for every ListView item to create the view with
	 * the properties that we want.
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Feedback f = getItem(position);
        
        View v = convertView;
        FeedbackHolder holder;
        
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new FeedbackHolder();
            holder.play = (ImageView)v.findViewById(R.id.play);
            holder.msg = (TextView)v.findViewById(R.id.item_text);
            holder.date = (TextView)v.findViewById(R.id.item_date);
            v.setTag(holder);
        } else {
        	holder = (FeedbackHolder)v.getTag();
        }

        Date time = new Date(Long.parseLong(f.date)*1000);
        
        holder.date.setText(DateUtil.formatTime(time));
        holder.msg.setText(f.message);
        
        if (f.file == null || f.file.equals("")) {
        	holder.play.setVisibility(ImageView.INVISIBLE);  
        } else {      	
        	holder.play.setVisibility(ImageView.VISIBLE);
        }
          
        return v;
    }
	    
	/**
	 * The Groupholder class is used to cache the Views
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class FeedbackHolder {
    	ImageView play;
        TextView msg;
        TextView date;
    }	
    
}
