package com.airlocksoftware.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is an example of the way SQLiteOpenHelper should be used.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static DatabaseHelper mInstance = null;

	private Context mContext;

	public static DatabaseHelper getInstance(Context context, String databaseName, int databaseVersion) {
		/**
		 * use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information:
		 * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
		 */
		if (mInstance == null) {
			mInstance = new DatabaseHelper(context.getApplicationContext(), databaseName, databaseVersion);
		}
		return mInstance;
	}

	/**
	 * constructor should be private to prevent direct instantiation.
	 * make call to static factory method "getInstance()" instead.
	 */
	private DatabaseHelper(Context context, String databaseName, int databaseVersion) {
		super(context, databaseName, null, databaseVersion);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}