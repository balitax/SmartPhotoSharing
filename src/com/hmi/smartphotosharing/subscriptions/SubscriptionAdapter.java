package com.hmi.smartphotosharing.subscriptions;

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

import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.util.Util;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class SubscriptionAdapter extends ArrayAdapter<Subscription> {

    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector gestureDetector;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Subscription data[] = null;	// A Group array that contains all list items
	DrawableManager dm;
		
	public SubscriptionAdapter(Context context, int resource, Subscription[] objects, DrawableManager dm) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.dm = dm;
	}
	
    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Subscription getItem(int position) {
        return data[position];
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

        View v = convertView;
        SubscriptionHolder holder = null;
       
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new SubscriptionHolder();
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.totalNew = (TextView)v.findViewById(R.id.total_new);
            holder.picGallery = (Gallery) v.findViewById(R.id.gallery);
            v.setTag(holder);
        } else {
            holder = (SubscriptionHolder)v.getTag();
        }
                        
        Subscription subscription = data[position];
        holder.txtTitle.setText(subscription.name);
                
        // Set the icon for this list item
        
        // TODO icon
        
        if (subscription.person != null) {
	        String url = Util.USER_DB + subscription.icon;
	        dm.fetchDrawableOnThread(url, holder.imgIcon);
        } else {
        	holder.imgIcon.setImageResource(R.drawable.ic_menu_mapmode);
        }
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        v.setOnClickListener(new MyOnClickListener(position));
        
        
        if (subscription.totalnew == 0) {
        	holder.totalNew.setVisibility(TextView.INVISIBLE);
        } else {
            holder.totalNew.setVisibility(TextView.VISIBLE); // Needed because of the holder pattern
            holder.totalNew.setText(Integer.toString(subscription.totalnew));
        }
        
        // Set the adapter for the gallery
        
		holder.picGallery.setAdapter(
				new MyGalleryAdapter(
						context, 
						subscription.photos,
						dm
			));
		
		// GestureDetector to detect swipes on the gallery
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        // Detect clicking an image
        holder.picGallery.setOnItemClickListener(new MyOnItemClickListener(context));
        
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
    static class SubscriptionHolder {
        ImageView imgIcon;
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
        
        public MyOnItemClickListener(Context context){
            this.context = context;
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
		    intent.putExtra("id", id);
		    context.startActivity(intent);
        	
        }

    }
 
}