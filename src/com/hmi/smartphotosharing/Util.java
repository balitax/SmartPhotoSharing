package com.hmi.smartphotosharing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;


public class Util {
	/**
	 * Replaces the current tab contents by a new instance of the given Fragment subclass.
	 * The current fragment is added to the back stack.
	 * @param cls The subclass of Fragment that replaces the current tab contents.
	 */
	public static void replaceTab(SherlockFragmentActivity ac, Class<? extends Fragment> cls) {
    	
		Fragment newFragment = Fragment.instantiate(ac, cls.getName());
    	FragmentTransaction ft = ac.getSupportFragmentManager().beginTransaction(); 

    	// Replace whatever is in the fragment_container view with this fragment,
    	// and add the transaction to the back stack
    	ft.replace(android.R.id.content, newFragment);
    	ft.addToBackStack(null);

    	// Commit the transaction
    	ft.commit();
		
	}
}
