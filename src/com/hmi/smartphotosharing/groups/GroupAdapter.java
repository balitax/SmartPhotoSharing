package com.hmi.smartphotosharing.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.R;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class GroupAdapter extends ArrayAdapter<Group> {
	// TODO : implements Filterable - voor type searching
	
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Group data[] = null;	// A Group array that contains all list items
	DrawableManager dm;

	private Gallery picGallery;
	private MyGalleryAdapter imgAdapt;
	
	public GroupAdapter(Context context, int resource, Group[] objects, DrawableManager dm) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.dm = dm;
        
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
        
        picGallery = (Gallery) row.findViewById(R.id.gallery);
        
	    // Create a new adapter
	    imgAdapt = new MyGalleryAdapter(context);
	    
	    // Set the gallery adapter
	    picGallery.setAdapter(imgAdapt);
        
        Group group = data[position];
        holder.txtTitle.setText(group.title);
        
        // Set the icon for this list item
        dm.fetchDrawableOnThread(group.icon, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        row.setOnClickListener(new OnItemClickListener(context,position));

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
    
    private class OnItemClickListener implements OnClickListener{       
        private int mPosition;
        private Context context;
        
        public OnItemClickListener(Context context, int position){
            mPosition = position;
            this.context = context;
        }
        
        @Override
        public void onClick(View arg0) {
        	Intent intent = new Intent(context, GroupDetailActivity.class);
        	intent.putExtra("id", mPosition);
        	context.startActivity(intent);
        }       
    }

}
