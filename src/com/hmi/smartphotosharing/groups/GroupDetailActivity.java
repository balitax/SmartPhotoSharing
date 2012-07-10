package com.hmi.smartphotosharing.groups;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class GroupDetailActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO fix id
  		Fragment newFragment = new GroupDetailFragment(0);
      	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

      	// Replace whatever is in the fragment_container view with this fragment,
      	// and add the transaction to the back stack
      	ft.replace(android.R.id.content, newFragment, "GroupDetailFragment");
      	ft.addToBackStack(null);

      	// Commit the transaction
      	ft.commit();	
    }

}
