package com.hmi.smartphotosharing.subscriptions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.util.ImageLoader;
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
    private static final int GLOBE_WIDTH = 256;
    private static final Double LN2 = 0.6931471805599453;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Subscription data[] = null;	// A Group array that contains all list items
	ImageLoader dm;
		
	public SubscriptionAdapter(Context context, int resource, Subscription[] objects, ImageLoader dm) {
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
            
        // Show subscription as a person
        if (subscription.person != null) {
        	holder.txtTitle.setText(subscription.user.rname);
	        String url = Util.USER_DB + subscription.user.picture;
	        dm.DisplayImage(url, holder.imgIcon);
        } 
        
        // Show subscription as a location
        else {
        	holder.txtTitle.setText(subscription.name);
        	
        	String url = context.getResources().getString(R.string.subscription_static_map);
        	
        	double lat1 = Double.parseDouble(subscription.lat1);
        	double lat2 = Double.parseDouble(subscription.lat2);
        	double lon1 = Double.parseDouble(subscription.lon1);
        	double lon2 = Double.parseDouble(subscription.lon2);
        	
        	// Move to the center of the Group location
            double centerLat = ((lat1 + lat2)/2);
            double centerLong = ((lon1 + lon2)/2);

            // Calculate the zoom level from the GPS bounds
            Double angle = lon2 - lon1;
            if (angle < 0) {
              angle += 360;
            }
            
            int zoom = (int)(Math.log(100 * 360 / angle / GLOBE_WIDTH) / LN2);
            Log.d("ZOOM", Integer.toString(zoom));
        	String mapUrl = String.format(url, centerLat, centerLong, zoom, Util.API_KEY);
        	
        	Log.d("ZOOM", mapUrl);
        	dm.DisplayImage(mapUrl, holder.imgIcon);
        	
        }
                
        
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
