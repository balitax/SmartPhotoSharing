package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.hmi.smartphotosharing.json.Comment;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MyPagerAdapter extends PagerAdapter {

	
	private static final int CODE_COMMENT_ADD = 2;
	private static final int CODE_LIKE = 3;
	private static final int CODE_COMMENT_REMOVE = 5;
	
	private Context context;
	private List<Photo> data;
	private ImageLoader imageLoader;
	private LinearLayout list;

	private int screenWidth, margin;
	
	public MyPagerAdapter(Context c, List<Photo> data) {
		this.context = c;
		this.data = data;
		this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        screenWidth = (int)(display.getWidth()*0.95);
        margin = (int)((display.getWidth() - screenWidth)/2);
	}
	
	@Override
    public int getCount() {
        return data.size();
    }

	@Override
    public Object instantiateItem(ViewGroup collection, int position) {

        Photo p = data.get(position);
        
        String url = Util.IMG_DB + p.name;
        
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.photo_detail_item, null);
        list = (LinearLayout) view.findViewById(R.id.comments);

        ImageView back = (ImageView) view.findViewById(R.id.back);
        back.setVisibility(ImageView.VISIBLE);
        
        ImageView icon = (ImageView) view.findViewById(R.id.app_icon);
        TextView title = (TextView) view.findViewById(R.id.header_title);
        TextView sub = (TextView) view.findViewById(R.id.header_subtext);
        Util.showSubHeader(title, sub);
        
        // Buttons
        Button button = (Button)view.findViewById(R.id.add_comment);
        EditText commentInput = (EditText)view.findViewById(R.id.edit_message);
        button.setOnClickListener(new CommentClickListener(position,p.getId(),commentInput));
        
        // User icon
		ImageView userIcon = (ImageView) view.findViewById(R.id.app_icon);
		imageLoader.displayImage(Util.getThumbUrl(Util.USER_DB, p.picture), userIcon);
        
		// User name
		TextView name = (TextView) view.findViewById(R.id.header_title);
		name.setText(p.rname);
		
		// Picture date
        TextView date = (TextView) view.findViewById(R.id.header_subtext);
        Date time = new Date(Long.parseLong(p.time)*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String datum = sdf.format(time);
        date.setText(datum);
        
        /*TextView group = (TextView)view.findViewById(R.id.photo_detail_group);
		TextView by = (TextView)view.findViewById(R.id.photo_detail_name);
		
		
		ImageView myLike = (ImageView) view.findViewById(R.id.like);

        
        // GroupText
        String groupTxt = context.getResources().getString(R.string.photo_detail_group);
        group.setText(String.format(groupTxt, p.groupname));
        
        // Update user icon
		String userPic = Util.USER_DB + p.picture;
		imageLoader.displayImage(userPic, userIcon);
        
		// Update the 'Taken by' text
        String byTxt = context.getResources().getString(R.string.photo_detail_name);
        by.setText(String.format(byTxt, p.rname));

        
        
        myLike.setOnClickListener(new LikeClickListener(p.getId(), p.me));
        */
        
        // 'Likes'
		TextView likes = (TextView) view.findViewById(R.id.like_txt);
        int numLikes = p.getLikes();
    	likes.setText(p.likes);
        /*myLike.setImageResource(R.drawable.like);
    	
        if (p.me){ 
        	if (numLikes > 1) 
	        	likes.setText(String.format(context.getResources().getString(R.string.like_txt_multiple), Integer.toString(p.getLikes()-1)));
        	else
	        	likes.setText(context.getResources().getString(R.string.like_txt_you));
        } else {
        	likes.setText(String.format(context.getResources().getString(R.string.like_txt), p.likes));
        }*/
        
        // Load the actual picture in the imageView
		ImageView image = (ImageView) view.findViewById(R.id.picture);
		image.setOnClickListener(new PictureClickListener(context, url));
		
        LayoutParams params = (LayoutParams) image.getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth;
        params.setMargins(0, margin, 0, margin);
        image.setLayoutParams(params);
        
		DisplayImageOptions roundOptions = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(8)).build();
        imageLoader.displayImage(Util.IMG_DB + p.name, image, roundOptions);
		      
        // Load all the comments
        setComments(p.comments);
        
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

	public void setComments(List<Comment> comments) {
		list.removeAllViews();
		if (comments != null) {
			for (int i=0; i<comments.size(); i++) {
				
			  LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			  View vi = inflater.inflate(R.layout.comment, null);
			  Comment comment = comments.get(i);
			  
			  // Icon
			  ImageView img = (ImageView)vi.findViewById(R.id.comment_icon);	
			  String userPic = Util.USER_DB + comment.picture;
			  imageLoader.displayImage(userPic, img);
	
			  // Delete
			  SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Activity.MODE_PRIVATE);
			  long uid = settings.getLong(Login.SESSION_UID, 0);
			  
			  ImageView delete = (ImageView)vi.findViewById(R.id.comment_delete);	
			  long commentUid = comment.getUid();
			  if (commentUid == uid) {
				  delete.setVisibility(ImageView.VISIBLE);
				  delete.setOnClickListener(new DeleteClickListener(comment.getId()));
			  } else {
				  delete.setVisibility(ImageView.GONE);
			  }
			  
			  // Comment text
			  TextView txt = (TextView)vi.findViewById(R.id.comment_txt);
			  txt.setText(comment.comment);
			  
			  // Get the timestamp
	          Date time = new Date(Long.parseLong(comment.time)*1000);
	          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	          String datum = sdf.format(time);
	          				  
			  // Comment username
			  TextView user = (TextView)vi.findViewById(R.id.comment_user);
			  user.setText(comment.rname + " (" + datum + ")");
			  
			  list.addView(vi);
			}
		} else {
	
			TextView txt = new TextView(context);
			txt.setText("No comments for this photo");
			list.addView(txt);
		}
	}
	
    private class CommentClickListener implements OnClickListener{    
        private EditText e;
        private long iid;
        public CommentClickListener(int position, long iid, EditText e){
            this.e = e;
            this.iid = iid;
        }
        
        @Override
        public void onClick(View arg0) {
    		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
    		
            String commentUrl = Util.getUrl(context,R.string.photo_detail_addcomment);
            
            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
            try {
            	String commentTxt = e.getEditableText().toString();
    			map.put("sid", new StringBody(hash));
    	        map.put("iid", new StringBody(Long.toString(iid)));
    	        if (!commentTxt.equals(""))
    	        	map.put("comment", new StringBody(commentTxt));
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            PostData pr = new PostData(commentUrl,map);
            new PostRequest(context, CODE_COMMENT_ADD).execute(pr);
        }       
    }
 
    private class LikeClickListener implements OnClickListener{    

        private long iid;
        private boolean myLike;
        
        public LikeClickListener(long iid, boolean me){
            this.iid = iid;
            this.myLike = me;
        }
        
        @Override
        public void onClick(View arg0) {
    		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
    		String hash = settings.getString(Login.SESSION_HASH, null);
    		
    		
    		String like = "";
    		if (myLike)
    			like = Util.getUrl(context,R.string.unlike_http);
    		else
    			like = Util.getUrl(context,R.string.like_http);
    			
    		myLike = !myLike;
    		
            HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
            try {
    			map.put("sid", new StringBody(hash));
    	        map.put("iid", new StringBody(Long.toString(iid)));
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            PostData pr = new PostData(like,map);
            new PostRequest(context, CODE_LIKE).execute(pr);
        }       
    }
    
    private class DeleteClickListener implements OnClickListener{    

        private long cid;
        
        public DeleteClickListener(long cid){
            this.cid = cid;
        }
        
        @Override
        public void onClick(View arg0) {
        	confirmDeleteCommentDialog(context, cid);
        	
        }       
    }
    
    private class PictureClickListener implements OnClickListener {
    	
    	private String url;
    	private Context c;
    	
    	private PictureClickListener(Context c, String url) {
    		this.url = url;
    		this.c = c;
    	}

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(c, FullscreenImageActivity.class);
			intent.putExtra(Util.URL_MESSAGE, url);
			c.startActivity(intent);
		}
    	
    }
    
    private void confirmDeleteCommentDialog(final Context c, final long cid) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to delete this comment?")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {

			       		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
			       		String hash = settings.getString(Login.SESSION_HASH, null);
			       		
			       		String deleteUrl = Util.getUrl(context,R.string.photo_detail_removecomment);
			       			
			               HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
			               try {
			       			map.put("sid", new StringBody(hash));
			       	        map.put("cid", new StringBody(Long.toString(cid)));
			       		} catch (UnsupportedEncodingException e) {
			       			e.printStackTrace();
			       		}
		               
		                PostData pr = new PostData(deleteUrl,map);
		                new PostRequest(context, CODE_COMMENT_REMOVE).execute(pr);
		           }
		       })
		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		
	}
}
