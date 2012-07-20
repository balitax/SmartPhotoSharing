package com.hmi.smartphotosharing.groups;

import android.app.Activity;
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

import com.hmi.json.Group;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;

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
    OnGroupClickListener groupClickListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Group data[] = null;	// A Group array that contains all list items
	DrawableManager dm;

	private Gallery picGallery;
		
	public GroupAdapter(Context context, int resource, Group[] objects, DrawableManager dm, OnGroupClickListener groupClickListener) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.dm = dm;
        this.groupClickListener = groupClickListener;
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

        View row = convertView;
        GroupHolder holder = null;
       
        if(row == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new GroupHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.icon);
            holder.txtTitle = (TextView)row.findViewById(R.id.item_text);
           
            row.setTag(holder);
        } else {
            holder = (GroupHolder)row.getTag();
        }
                        
        Group group = data[position];
        holder.txtTitle.setText(group.name);
        
        // Set the icon for this list item
        String url = context.getResources().getString(R.string.group_http_logo) + group.logo;
        dm.fetchDrawableOnThread(url, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        row.setOnClickListener(new MyOnClickListener(context,position));

        // Set the adapter for the gallery
        picGallery = (Gallery) row.findViewById(R.id.gallery);
		picGallery.setAdapter(
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
        picGallery.setOnItemClickListener(new MyOnItemClickListener(context));
        
        // Detect swipes
        picGallery.setOnTouchListener(gestureListener);
        
        return row;
    }
	
	/**
	 * The Groupholder class is used to cache the ImageView and Textview
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class GroupHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }	
    
    private class MyOnClickListener implements OnClickListener{       
        private int mPosition;
        private Context context;
        
        public MyOnClickListener(Context context, int position){
            mPosition = position;
            this.context = context;
        }
        
        @Override
        public void onClick(View arg0) {
        	groupClickListener.OnGroupClick(getItemId(mPosition));
        	
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
