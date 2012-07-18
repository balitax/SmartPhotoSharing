package com.hmi.smartphotosharing.groups;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.Group;
import com.hmi.json.OnDownloadListener;
import com.hmi.json.Photo;
import com.hmi.json.PopularResponse;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.MyGalleryAdapter;
import com.hmi.smartphotosharing.R;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class GroupAdapter extends ArrayAdapter<Group> implements OnDownloadListener{
	
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Group data[] = null;	// A Group array that contains all list items
	DrawableManager dm;

	private Gallery[] picGallery;
		
	public GroupAdapter(Context context, int resource, Group[] objects, DrawableManager dm) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.dm = dm;
        picGallery = new Gallery[data.length];
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
        
        picGallery[position] = (Gallery) row.findViewById(R.id.gallery);
        
	    // Create a new adapter
	    //imgAdapt = new MyGalleryAdapter(context);
	    
	    // Set the gallery adapter
	    //picGallery.setAdapter(imgAdapt);
        
        Group group = data[position];
        holder.txtTitle.setText(group.name);
        
        // Set the icon for this list item
        String url = context.getResources().getString(R.string.group_http_logo) + group.logo;
        dm.fetchDrawableOnThread(url, holder.imgIcon);
        
        // We need to set the onClickListener here to make sure that
        // the row can also be clicked, in addition to the gallery photos
        row.setOnClickListener(new OnItemClickListener(context,position));

		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Activity.MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

        String galleryUrl = String.format(context.getResources().getString(R.string.groups_http_gallery), hash, getItemId(position));
		new FetchJSON(this,position).execute(galleryUrl);
		
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
        	intent.putExtra("id", getItemId(mPosition));
        	context.startActivity(intent);
        }       
    }

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		PopularResponse list = gson.fromJson(json, PopularResponse.class);
		
		List<Photo> photo_list = list.msg;
		
		picGallery[code].setAdapter(
			new MyGalleryAdapter(
					context, 
					photo_list,
					dm
		));
	}

}
