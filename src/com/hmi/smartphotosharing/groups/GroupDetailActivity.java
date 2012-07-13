package com.hmi.smartphotosharing.groups;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.OnDownloadListener;
import com.hmi.smartphotosharing.DrawableManager;
import com.hmi.smartphotosharing.MyImageAdapter;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SmartPhotoSharing;
import com.hmi.smartphotosharing.photo.Photo;
import com.hmi.smartphotosharing.photo.PhotoContainer;
import com.hmi.smartphotosharing.photo.PhotoDetailActivity;
import com.hmi.smartphotosharing.photo.PhotoList;

public class GroupDetailActivity extends SherlockFragmentActivity implements OnDownloadListener {

	//adapter for gallery view
	private MyImageAdapter imgAdapt;
	//gallery object
	private GridView gridView;
	//image view for larger display
	private TextView textView;

	private DrawableManager dm;
	private List<Photo> mObjectList;
	
	private long id;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_detail);

        mObjectList = new ArrayList<Photo>();
        dm = new DrawableManager(this);
        
        textView = (TextView) findViewById(R.id.group_detail_intro);
        
        // Get the gallery view
        gridView = (GridView) findViewById(R.id.grid);

        textView.setText("Selected id was: " + String.valueOf(id));
        
        // Set the gallery adapter
        gridView.setAdapter(imgAdapt);

        // Set the click listener for each item in the thumbnail gallery
        gridView.setOnItemClickListener(new MyOnItemClickListener(this));       	
    }

    private class MyOnItemClickListener implements OnItemClickListener {
		private Context c;
		
		public MyOnItemClickListener(Context c) {
			this.c = c;
		}
		// Handle clicks
		@Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent intent = new Intent(c, PhotoDetailActivity.class);
	    	intent.putExtra("id", id);
	    	startActivity(intent);
	    }
    }
    
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.group_detail_menu, menu);
	    return true;
	}	
	
	@Override
	public void onStart() {
        super.onStart();
        
        loadData();
	}
	
	private boolean loadData() {
		boolean res = false;
        
        if (SmartPhotoSharing.refreshDisplay) {
    		new FetchJSON(this).execute(getResources().getString(R.string.popular_http));
    		res = true;
        } 		
        
        return res;
	}
	
	public void parseJson(String result) {
		Gson gson = new Gson();
		PhotoList list = gson.fromJson(result, PhotoList.class);
		
		List<PhotoContainer> group_list = list.getPostContainterList();
		PhotoContainer gc;
		for (int i = 0; i < group_list.size(); i++) {
		    gc = group_list.get(i);
		    mObjectList.add(gc.getPost());
		}
		
		gridView.setAdapter(
			new MyImageAdapter(
					this, 
					mObjectList.toArray(new Photo[group_list.size()]),
					dm
		));
		
		mObjectList.clear();
	}
}
