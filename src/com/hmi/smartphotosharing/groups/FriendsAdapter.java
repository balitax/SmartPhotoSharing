package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;

import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.User;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class FriendsAdapter extends ArrayAdapter<User>  {
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<User> data = null;	// A Group array that contains all list items
	ImageLoader imageLoader;
	ListView list;
	
	public FriendsAdapter(Context context, int resource, List<User> objects, ListView list) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        this.list = list;
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

        View row = convertView;
        GroupHolder holder = null;
       
        if(row == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new GroupHolder();
            holder.checkBox = (CheckBox)row.findViewById(R.id.checkbox);
            holder.imgIcon = (ImageView)row.findViewById(R.id.icon);
           
            row.setTag(holder);
        } else {
            holder = (GroupHolder)row.getTag();
        }
        
        User user = getItem(position);
        holder.checkBox.setText(user.rname);
        
        if(list.isItemChecked(position))
            holder.checkBox.setChecked(true);
        else
            holder.checkBox.setChecked(false);

        // Set the icon for this list item
        imageLoader.displayImage(user.thumb, holder.imgIcon);
                
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
    	CheckBox checkBox;
        ImageView imgIcon;
    }
	
 
}
