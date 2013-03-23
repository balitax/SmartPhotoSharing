package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class GroupAdapter extends ArrayAdapter<Group> {

    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector gestureDetector;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<Group> data;	// A Group array that contains all list items
	ImageLoader imageLoader;
		
	public GroupAdapter(Context context, int resource, List<Group> objects, ImageLoader im) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.imageLoader = im;
	}
	
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Group getItem(int position) {
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

        Group group = getItem(position);
        
        View v = convertView;
        GroupHolder holder;
        
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new GroupHolder();
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.privateIcon = (ImageView)v.findViewById(R.id.private_icon);
            holder.locationIcon = (ImageView)v.findViewById(R.id.location_icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.totalNew = (TextView)v.findViewById(R.id.total_new);
            holder.gallery = (LinearLayout)v.findViewById(R.id.gallery);
            v.setTag(holder);
        } else {
        	holder = (GroupHolder)v.getTag();
        }
        
        holder.txtTitle.setText(group.name);
                
        // Set the icon for this list item
        String url = Util.getThumbUrl(group);
        imageLoader.displayImage(url, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        v.setOnClickListener(new MyOnClickListener(position));
        
        // Private icon
        if(group.isPrivate()) {
        	holder.privateIcon.setVisibility(ImageView.VISIBLE);
        } else {
        	holder.privateIcon.setVisibility(ImageView.GONE);
        }

        // Private icon
        if(group.isLocationLocked()) {
        	holder.locationIcon.setVisibility(ImageView.VISIBLE);
        } else {
        	holder.locationIcon.setVisibility(ImageView.GONE);
        }
        
        // Number of updates
        if (group.totalnew == 0) {
        	holder.totalNew.setVisibility(TextView.INVISIBLE);
        } else {
            holder.totalNew.setVisibility(TextView.VISIBLE); // Needed because of the holder pattern
            holder.totalNew.setText(Integer.toString(group.totalnew));
        }

        holder.gallery.removeAllViews();
        if (group.photos != null && group.photos.size() > 0) {
			for (Photo p : group.photos) {
				View imgView = getImageView(Util.getThumbUrl(p));
				imgView.setOnClickListener(new MyOnItemClickListener(context, getItemId(position), p.getId()));
				holder.gallery.addView(imgView);
				
			}
        }    
        
        return v;
    }

    public View getImageView(String path){
                
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(60, 60));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        imageLoader.displayImage(path, imageView);
        return imageView;
    }
    
	/**
	 * The Groupholder class is used to cache the Views
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class GroupHolder {
        ImageView imgIcon;
        ImageView privateIcon;
        ImageView locationIcon;
        TextView txtTitle;
        TextView totalNew;
        LinearLayout gallery;
    }	
    
    private class MyOnClickListener implements OnClickListener{       
        private int mPosition;
        
        public MyOnClickListener(int position){
            mPosition = position;
        }
        
        @Override
        public void onClick(View arg0) {
        	//groupClickListener.OnGroupClick(getItemId(mPosition));
        	Intent intent = new Intent(context, GroupDetailActivity.class);
        	intent.putExtra("id", getItemId(mPosition));
        	context.startActivity(intent);
        }       
    }
	
    /**
     * Listener for clicking images on the gallery.
     * @author Edwin
     *
     */
    private class MyOnItemClickListener implements OnClickListener{    
        private Context context;
        long gid, iid;
        
        public MyOnItemClickListener(Context context, long gid, long iid){
            this.context = context;
            this.gid = gid;
            this.iid = iid;
        }

		@Override
		public void onClick(View v) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
        	//Intent intent = new Intent(context, SinglePhotoDetail.class);
		    intent.putExtra("gid", gid);
		    intent.putExtra("id", iid);
		    context.startActivity(intent);
			
		}
        

    }
 
}
