package com.hmi.smartphotosharing.friends;

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
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class FriendsAdapter extends ArrayAdapter<User> {
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<User> data;		// A User array that contains all list items
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
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.gallery = (LinearLayout)v.findViewById(R.id.gallery);
            v.setTag(holder);
        } else {
        	holder = (UserHolder)v.getTag();
        }
        
        holder.txtTitle.setText(user.rname);
                
        // Set the icon for this list item
        imageLoader.displayImage(Util.getThumbUrl(user), holder.imgIcon);
                
        holder.gallery.removeAllViews();
        if (user.newest_photos != null && user.newest_photos.size() > 0) {
			for (Photo p : user.newest_photos) {
				View imgView = getImageView(Util.getThumbUrl(p));
				imgView.setOnClickListener(new MyOnItemClickListener(context, p.getId()));
				holder.gallery.addView(imgView);
				
			}
        }        
        return v;
    }
	
    public View getImageView(String path){
                
        ImageView imageView = new ImageView(context);
        int size = Util.getThumbSize(context);
        imageView.setLayoutParams(new LayoutParams(size,size));
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
    static class UserHolder {
        ImageView imgIcon;
        TextView txtTitle;
        LinearLayout gallery;
    }	
    

    /**
	* Listener for clicking images on the gallery.
	* @author Edwin
	*
	*/
	private class MyOnItemClickListener implements OnClickListener{
        private Context context;
        long gid;
        
        public MyOnItemClickListener(Context context, long gid){
            this.context = context;
            this.gid = gid;
        }

		@Override
		public void onClick(View arg0) {
	        Intent intent = new Intent(context, PhotoDetailActivity.class);
			intent.putExtra("id", gid);
			context.startActivity(intent);
		}
        
	        
    }
}
