package com.hmi.smartphotosharing.examples;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PictureDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION 			= 2;
    private static final String DATABASE_NAME			= "sps.db";
    private static final String PHOTOS_TABLE_NAME 		= "photos";
    
    private static final String KEY_ID					= "id";
    private static final String KEY_TITLE				= "title";
    private static final String KEY_LOCATION			= "location";
    private static final String KEY_LATITUDE			= "latitude";
    private static final String KEY_LONGTITUDE			= "longtitude";
    private static final String KEY_COMMENT				= "comment";
    private static final String KEY_DATE				= "date";
    
    private static final String PHOTOS_TABLE_CREATE =
                "CREATE TABLE " + PHOTOS_TABLE_NAME + " (" +
                KEY_ID  		+ " TEXT, " + 
                KEY_TITLE 		+ " TEXT, " +
                KEY_COMMENT 	+ " TEXT);";

    PictureDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PHOTOS_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
