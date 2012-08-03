package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hmi.smartphotosharing.json.Group;

public class MySpinnerAdapter extends BaseAdapter {
	private Context mContext;
	private List<Group> data;
	
    public MySpinnerAdapter(Context c, List<Group> list) {
        mContext = c;
        this.data = list;
    }

    @Override
    public int getCount() {
    	if (data == null){
    		return 0;
    	} else {
    		return data.size();
    	}
    }
    
    @Override
    public Group getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView t = new TextView(mContext);
        t.setText(data.get(position).name);
        return t;
    }
    
}
