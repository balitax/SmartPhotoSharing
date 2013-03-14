package com.hmi.smartphotosharing.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SinglePhotoDetail;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.json.News;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class NewsAdapter extends ArrayAdapter<News> {

	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	List<News> data;	// A Group array that contains all list items
	ImageLoader imageLoader;
		
	public NewsAdapter(Context context, int resource, List<News> objects, ImageLoader im) {
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
    public News getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    public String bold(String s) {
    	return "<b>" + s + "</b>";
    }
	/**
	 * This method overrides the inherited getView() method.
	 * It is called for every ListView item to create the view with
	 * the properties that we want.
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        News news = getItem(position);
        
        View v = convertView;
        NewsHolder holder;
        
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new NewsHolder();
            holder.icon = (ImageView)v.findViewById(R.id.icon);
            holder.photo = (ImageView)v.findViewById(R.id.photo);
            holder.name = (TextView)v.findViewById(R.id.name);
            holder.time = (TextView)v.findViewById(R.id.time);
            holder.text = (TextView)v.findViewById(R.id.text);
            v.setTag(holder);
        } else {
        	holder = (NewsHolder)v.getTag();
        }
                
        // Set the icon for this list item
        holder.name.setText(news.uname);
        
        Date time = new Date(news.time*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String datum = sdf.format(time);
        
        holder.time.setText(datum);
        
        imageLoader.displayImage(news.thumb, holder.icon);
        imageLoader.displayImage(news.photo, holder.photo);
        
        switch (news.type) {
        
	        case NewsActivity.TYPE_GROUP:
	        	Spanned groupTxt = Html.fromHtml(String.format(context.getResources().getString(R.string.news_txt_group), bold(news.rname), bold(news.name)));
	        	holder.text.setText(groupTxt);
	        	v.setOnClickListener(new MyGroupClickListener(position));
	        	break;
	        
	        case NewsActivity.TYPE_PHOTO:
	        	Spanned photoTxt = Html.fromHtml(String.format(context.getResources().getString(R.string.news_txt_photo), bold(news.rname), bold(news.name)));
	        	holder.text.setText(photoTxt);
	        	v.setOnClickListener(new MyPhotoClickListener(position));
	        	break;
	        default:
	        	break;
        }
        
        
        return v;
    }
	
	/**
	 * The Groupholder class is used to cache the Views
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class NewsHolder {
        ImageView icon;
        ImageView photo;
        TextView name;
        TextView text;
        TextView time;
    }	
    
    private class MyGroupClickListener implements OnClickListener{       
        private int mPosition;
        
        public MyGroupClickListener(int position){
            mPosition = position;
        }
        
        @Override
        public void onClick(View arg0) {
        	//groupClickListener.OnGroupClick(getItemId(mPosition));
        	Intent intent = new Intent(context, GroupDetailActivity.class);
        	intent.putExtra("id", getItem(mPosition).gid);
        	context.startActivity(intent);
        }       
    }
    
    private class MyPhotoClickListener implements OnClickListener{       
        private int mPosition;
        
        public MyPhotoClickListener(int position){
            mPosition = position;
        }
        
        @Override
        public void onClick(View arg0) {
        	//groupClickListener.OnGroupClick(getItemId(mPosition));
        	Intent intent = new Intent(context, SinglePhotoDetail.class);
        	intent.putExtra("id", getItem(mPosition).iid);
        	context.startActivity(intent);
        }       
    }
}
