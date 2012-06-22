package com.hmi.smartphotosharing.examples;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpendableListAdapterExample extends BaseExpandableListAdapter {
	
	// Sample data set.  children[i] contains the children (String[]) for groups[i].
    private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
    private String[][] children = {
            { "Arnold", "Barry", "Chuck", "David" },
            { "Ace", "Bandit", "Cha-Cha", "Deuce" },
            { "Fluffy", "Snuggles" },
            { "Goldy", "Bubbles" }
    };

	Context context;		// The parenting Context that the Adapter is embedded in
	
	public ExpendableListAdapterExample(Context context) {
		super();
		
        this.context = context;
	}
	
	@Override
	public Object getChild(int group, int child) {
        return children[group][child];
	}

	@Override
	public long getChildId(int group, int child) {
		return child;
	}
	
	public TextView getGenericView(boolean group) {
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 64);

        TextView textView = new TextView(context);
        textView.setLayoutParams(lp);
        
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding((group ? 60 : 36), 0, 0, 0);
        return textView;
    }

	@Override
	public View getChildView(int group, int child, boolean isLastChild, View convertView,
			ViewGroup parent) {
        TextView textView = getGenericView(false);
        textView.setText(getChild(group, child).toString());
        return textView;

	}

	@Override
	public int getChildrenCount(int group) {
        return children[group].length;

	}

	@Override
	public Object getGroup(int group) {
        return groups[group];

	}

	@Override
	public int getGroupCount() {
        return groups.length;
	}

	@Override
	public long getGroupId(int group) {
		return group;
	}

	@Override
	public View getGroupView(int group, boolean isExpanded, View convertView, ViewGroup parent) {

        TextView textView = getGenericView(true);
        textView.setText(getGroup(group).toString());
        return textView;

	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

}
