package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class GroupJoinAdapter extends ArrayAdapter<Group> {
        
    private Context context;		// The parenting Context that the Adapter is embedded in
	private int rowResource, dropDownResource;	// The xml layout file for each ListView item
	private List<Group> data;	// A Group array that contains all list items
	private ImageLoader imageLoader;
	
	public GroupJoinAdapter(Context context, int rowResource, int dropDownResource, List<Group> objects) {
		super(context, rowResource, objects);
		
        this.rowResource = rowResource;
        this.dropDownResource = dropDownResource;
        this.context = context;
        this.data = objects;
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
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
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        GroupHolder holder = null;
       
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(rowResource, null);
           
            holder = new GroupHolder();
            holder.privateIcon = (ImageView)v.findViewById(R.id.private_icon);
            holder.locationIcon = (ImageView)v.findViewById(R.id.location_icon);
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            v.setTag(holder);
        } else {
            holder = (GroupHolder)v.getTag();
        }
                        
        Group group = getItem(position);
        holder.txtTitle.setText(group.name);

        // Private icon
        if(group.isPrivate()) {
        	holder.privateIcon.setVisibility(ImageView.VISIBLE);
        } else {
        	holder.privateIcon.setVisibility(ImageView.INVISIBLE);
        }

        // Private icon
        if(group.isLocationLocked()) {
        	holder.locationIcon.setVisibility(ImageView.VISIBLE);
        } else {
        	holder.locationIcon.setVisibility(ImageView.INVISIBLE);
        }
        
        // Set the icon for this list item
        String url = Util.GROUP_DB + group.logo;
        imageLoader.displayImage(url, holder.imgIcon);
                
        return v;
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
            v = inflater.inflate(dropDownResource, null);
           
            holder = new GroupHolder();
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            v.setTag(holder);
        } else {
            holder = (GroupHolder)v.getTag();
        }
                        
        Group group = getItem(position);
        holder.txtTitle.setText(group.name);
                
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
    }	
    

	 
}
