package com.hmi.smartphotosharing.groups;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Util;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class GroupJoinAdapter extends ArrayAdapter<Group> {

    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector gestureDetector;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Group data[] = null;	// A Group array that contains all list items
	ImageLoader dm;
		
	public GroupJoinAdapter(Context context, int resource, Group[] objects, ImageLoader dm) {
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
    public Group getItem(int position) {
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
        GroupHolder holder = null;
       
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new GroupHolder();
            holder.privateIcon = (ImageView)v.findViewById(R.id.private_icon);
            holder.locationIcon = (ImageView)v.findViewById(R.id.location_icon);
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.totalNew = (TextView)v.findViewById(R.id.total_new);
            holder.picGallery = (Gallery) v.findViewById(R.id.gallery);
            v.setTag(holder);
        } else {
            holder = (GroupHolder)v.getTag();
        }
                        
        Group group = data[position];
        holder.txtTitle.setText(group.name);

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
        if (group.totalnew == 0) 
        	holder.totalNew.setVisibility(View.GONE);
        else
        	holder.totalNew.setText(Integer.toString(group.totalnew));
        
        // Set the icon for this list item
        String url = Util.GROUP_DB + group.logo;
        dm.DisplayImage(url, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        v.setOnClickListener(new MyOnClickListener(position));

        // Set the adapter for the gallery
        
		holder.picGallery.setAdapter(
				new MyGalleryAdapter(
						context, 
						group.photos,
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
        holder.picGallery.setOnItemClickListener(new MyOnItemClickListener(context,group.getId()));
        
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
    static class GroupHolder {
        public ImageView locationIcon;
		public ImageView privateIcon;
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
        	SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);

            String joinUrl = String.format(Util.getUrl(context,R.string.groups_http_join),hash,getItemId(mPosition));		
            new FetchJSON(context, GroupJoinActivity.CODE_JOIN).execute(joinUrl);
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
        private long gid;
        
        public MyOnItemClickListener(Context context, long gid){
        	this.gid = gid;
            this.context = context;
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
        	intent.putExtra("gid", gid);
		    intent.putExtra("id", id);
		    context.startActivity(intent);
        	
        }

    }
 
}
