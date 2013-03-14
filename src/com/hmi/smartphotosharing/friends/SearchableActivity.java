package com.hmi.smartphotosharing.friends;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.hmi.smartphotosharing.R;

public class SearchableActivity extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.invite_friends);
	    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	doMySearch(query);
	    }
	}
	
	@Override
	public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     
	     //@TODO PUT GPS DATA IN HERE
	     //appData.putBoolean(SearchableActivity.JARGON, true);
	     
	     startSearch(null, false, appData, false);
	     return true;
	 }
	
	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		
	}
}
