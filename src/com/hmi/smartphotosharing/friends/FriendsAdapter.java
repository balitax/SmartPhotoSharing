package com.hmi.smartphotosharing.friends;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SinglePhotoDetail;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.json.User;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class FriendsAdapter extends ArrayAdapter<User> {

    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector gestureDetector;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<User> data;	// A Group array that contains all list items
	ImageLoader imageLoader;
		
	public FriendsAdapter(Context context, int resource, List<User> objects, ImageLoader im) {
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
    public User getItem(int position) {
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

        User user = getItem(position);
        
        View v = convertView;
        UserHolder holder;
        
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new UserHolder();
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.privateIcon = (ImageView)v.findViewById(R.id.private_icon);
            holder.locationIcon = (ImageView)v.findViewById(R.id.location_icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.totalNew = (TextView)v.findViewById(R.id.total_new);
            holder.picGallery = (Gallery)v.findViewById(R.id.gallery);
            v.setTag(holder);
        } else {
        	holder = (UserHolder)v.getTag();
        }
        
        holder.txtTitle.setText(user.rname);
                
        // Set the icon for this list item
        imageLoader.displayImage(user.thumb, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        v.setOnClickListener(new MyOnClickListener(position));
        
        // Set the adapter for the gallery
        holder.picGallery.setAdapter(
				new MyGalleryAdapter(
						context, 
						user.newest_photos,
						imageLoader
			));
		
        // Make the gallery start from the left
        if (user.newest_photos != null && user.newest_photos.size() > 1)
        	holder.picGallery.setSelection(1);

		// GestureDetector to detect swipes on the gallery
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        // Detect clicking an image
        holder.picGallery.setOnItemClickListener(new MyOnItemClickListener(context,user.getId()));
        
        // Detect swipes
        holder.picGallery.setOnTouchListener(gestureListener);
        
        return v;
    }
	
	/**
	 * The Groupholder class is used to cache the Views
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class UserHolder {
        ImageView imgIcon;
        ImageView privateIcon;
        ImageView locationIcon;
        TextView txtTitle;
        TextView totalNew;
        Gallery picGallery;
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
	 * Gesture detector needed to detect swipes
	 * This is needed in combination with onItemClickListener to
	 * enable both swiping the gallery and clicking imageviews.
	 * @author Edwin
	 *
	 */
    private class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }

    /**
     * Listener for clicking images on the gallery.
     * @author Edwin
     *
     */
    private class MyOnItemClickListener implements OnItemClickListener{    
        private Context context;
        long gid;
        
        public MyOnItemClickListener(Context context, long gid){
            this.context = context;
            this.gid = gid;
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
        	Intent intent = new Intent(context, SinglePhotoDetail.class);
		    intent.putExtra("id", id);
		    context.startActivity(intent);
        	
        }

    }
 
}
