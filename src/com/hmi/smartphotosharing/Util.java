package com.hmi.smartphotosharing;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class Util {
	/**
	 * Replaces the current tab contents by a new instance of the given Fragment subclass.
	 * The current fragment is added to the back stack.
	 * @param cls The subclass of Fragment that replaces the current tab contents.
	 */
	public static void replaceTab(Activity ac, Class<? extends Fragment> cls) {
    	
		Fragment newFragment = Fragment.instantiate(ac, cls.getName());
    	FragmentTransaction ft = ac.getFragmentManager().beginTransaction();

    	// Replace whatever is in the fragment_container view with this fragment,
    	// and add the transaction to the back stack
    	ft.replace(android.R.id.content, newFragment);
    	ft.addToBackStack(null);

    	// Commit the transaction
    	ft.commit();
		
	}
}
