package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.ImageLoader;
import com.hmi.smartphotosharing.util.Util;

public class MyPagerAdapter extends PagerAdapter {

	private static final int CODE_PHOTO = 1;
	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_COMMENT_LOAD = 3;
	
	private Context context;
	private List<Photo> data;
	private long id;
	private ImageLoader dm;
	private EditText commentInput;
	
	/*
	private LinearLayout list;
	private TextView by;
	private TextView group;
	private TextView date;
	private ImageView image;
	private ImageView userIcon;
	*/
	
	public MyPagerAdapter(Context c, List<Photo> data, ImageLoader dm) {
		this.context = c;
		this.data = data;
		this.dm = dm;
		id = 1;
	}
	
	@Override
    public int getCount() {
        return data.size();
    }

	@Override
    public Object instantiateItem(ViewGroup collection, int position) {

        Photo p = data.get(position);
        
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.photo_detail_item, null);
		
		TextView date = (TextView)view.findViewById(R.id.photo_detail_date);
		TextView group = (TextView)view.findViewById(R.id.photo_detail_group);
		TextView by = (TextView)view.findViewById(R.id.photo_detail_name);
		ImageView image = (ImageView) view.findViewById(R.id.picture);
		ImageView userIcon = (ImageView) view.findViewById(R.id.photo_detail_icon);
        EditText commentInput = (EditText) view.findViewById(R.id.edit_message);
        LinearLayout list = (LinearLayout) view.findViewById(R.id.comments);

        // Update user icon
		String userPic = Util.USER_DB + p.picture;
		dm.DisplayImage(userPic, userIcon);
        
		// Update the 'Taken by' text
        String byTxt = context.getResources().getString(R.string.photo_detail_name);
        by.setText(String.format(byTxt, p.rname));

        // Convert Unix timestamp to Date
        Date time = new Date(Long.parseLong(p.time)*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String datum = sdf.format(time);
        date.setText(datum);
        

        dm.DisplayImage(p.location + p.name, image);
			
        ((ViewPager) collection).addView(view, 0);

        return view;
    }
	
    @Override
    public void destroyItem(ViewGroup collection, int position, Object o) {
        View view = (View)o;
        ((ViewPager) collection).removeView(view);
        view = null;
    }


    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);

    }

    @Override
    public Parcelable saveState() {
        return null;
    }
    	
	public void onCommentClick(View v) {
		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);

		String commentTxt = commentInput.getText().toString();
        String commentUrl = Util.getUrl(context,R.string.photo_detail_addcomment);
        
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
			map.put("sid", new StringBody(hash));
	        map.put("iid", new StringBody(Long.toString(id)));
	        map.put("comment", new StringBody(commentTxt));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        PostData pr = new PostData(commentUrl,map);
        new PostRequest(context, CODE_COMMENT_ADD).execute(pr);
	}
	
}
