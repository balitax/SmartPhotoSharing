package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.json.User;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.R;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class UserAdapter extends ArrayAdapter<User> {
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<User> data = null;	// A Group array that contains all list items
	DrawableManager dm;

	public UserAdapter(Context context, int resource, List<User> objects, DrawableManager dm) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.dm = dm;
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
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new GroupHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.icon);
            holder.txtTitle = (TextView)row.findViewById(R.id.item_text);
           
            row.setTag(holder);
        } else {
            holder = (GroupHolder)row.getTag();
        }
                        
        User user = getItem(position);
        holder.txtTitle.setText(user.rname);
        
        // Set the icon for this list item
        String url = context.getResources().getString(R.string.group_http_logo) + user.picture;
        dm.fetchDrawableOnThread(url, holder.imgIcon);
                
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
 
}
