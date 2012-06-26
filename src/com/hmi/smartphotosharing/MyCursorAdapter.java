package com.hmi.smartphotosharing;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MyCursorAdapter extends SimpleCursorAdapter {

	Context context;
	
	int layout;

	public MyCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.layout = layout;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
        int nameCol = c.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

        String name = c.getString(nameCol);

        /**
         * Next set the name of the entry.
         */     
        ImageView img = (ImageView) v.findViewById(R.id.image1);
        if (img != null) {
            img.setImageURI(Uri.parse(name));
        }
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();
		
		final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int nameCol = c.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

        String name = c.getString(nameCol);

        /**
         * Next set the name of the entry.
         */     
        ImageView img = (ImageView) v.findViewById(R.id.image1);
        if (img != null) {
            img.setImageURI(Uri.parse(name));
        }

        return v;
	}
}
