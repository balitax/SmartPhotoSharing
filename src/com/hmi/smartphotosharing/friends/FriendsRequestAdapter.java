package com.hmi.smartphotosharing.friends;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class FriendsRequestAdapter extends ArrayAdapter<User> {

	OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<User> data;		// A User array that contains all list items
	ImageLoader imageLoader;
		
	public FriendsRequestAdapter(Context context, int resource, List<User> objects, ImageLoader im) {
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
            holder.btnAccept = (ImageButton)v.findViewById(R.id.btn_accept);
            holder.btnDecline = (ImageButton)v.findViewById(R.id.btn_decline);
            
            v.setTag(holder);
        } else {
        	holder = (UserHolder)v.getTag();
        }
        
        holder.txtTitle.setText(user.rname);
        holder.btnAccept.setOnClickListener(new MyOnClickListener(user.getId(), true));  
        holder.btnDecline.setOnClickListener(new MyOnClickListener(user.getId(), false));      
        
        // Set the icon for this list item
        imageLoader.displayImage(Util.getThumbUrl(user), holder.imgIcon);
        
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
        TextView txtTitle;
        ImageButton btnAccept;
        ImageButton btnDecline;
    }	
    
    private class MyOnClickListener implements OnClickListener{       
        private long uid;
        private int accept;
        
        public MyOnClickListener(long uid, boolean accept){
            this.uid = uid;
            this.accept = accept ? 1 : 0;  
        }
        
        @Override
        public void onClick(View arg0) {

    		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Activity.MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
            String url = String.format(Util.getUrl(context,R.string.friends_http_confirm),hash, uid, accept);		
            new FetchJSON(context,FriendsRequestsActivity.CODE_CONFIRM).execute(url);
        }       
    }
 
}
