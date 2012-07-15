package com.hmi.smartphotosharing;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hmi.json.FetchJSON;
import com.hmi.json.JSONResponse;
import com.hmi.json.OnDownloadListener;

public class ProfileFragment extends ListFragment implements OnDownloadListener {

    private OnLoadDataListener mListener;
    
    private TextView username;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.profile, container, false);
		
		username = (TextView) view.findViewById(R.id.profile_name);
		return view;
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoadDataListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }	
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

	}
	
	@Override
	public void onStart() {
        super.onStart();
        
        new FetchJSON(this).execute(getActivity().getResources().getString(R.string.profile_http));
	
	}

	@Override
	public void parseJson(String json, int code) {
		Gson gson = new Gson();
		JSONResponse response = gson.fromJson(json, JSONResponse.class);
		
		username.setText(response.msg.rname);
		
	}
	
}