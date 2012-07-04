package com.hmi.smartphotosharing;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

public class PopularFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	// This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    
    // The view that will contain the images
    private GridView gridView;

    // Directory of thumbnails
    public static final Uri SPS_DIR = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	View view = inflater.inflate(R.layout.popular, container, false);
    	
    	gridView = (GridView) view.findViewById(R.id.popularGrid);
        
    	return view;
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
                
        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new MyCursorAdapter(getActivity(),
                	R.layout.simple_image, 								// The target layout
                	null,												// Cursor should be null here
                	new String[] { MediaStore.Images.Thumbnails.DATA }, // Column names to bind to the UI
                	new int[] { R.id.image1 } 							// The views that should display column in the previous parameter
                	);													// Flags used to determine the behavior of the adapter.
        
        gridView.setAdapter(mAdapter);
     
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = {MediaStore.Images.Media.DATA};
        return new CursorLoader(getActivity(), 
        		SPS_DIR,									// Uri to query
                null, 										// Projection (which columns to select)
                null, 										// Selection (== SQL WHERE statment)
                null,										// Selection Args (if ?s are used)
                MediaStore.Images.Thumbnails.IMAGE_ID);		// Sort order (== SQL ORDER BY)
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
		// TODO
    	mAdapter.changeCursor(data);
        //mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    	// TODO
    	mAdapter.changeCursor(null);
        //mAdapter.swapCursor(null);
    }
}