package com.airlocksoftware.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DbOpener extends SQLiteOpenHelper {

	private Context mContext;

	private static final String TAG = DbOpener.class.getSimpleName();;

	public DbOpener(Context context, String databaseName, int databaseVersion) {
		super(context, databaseName, null, databaseVersion);
		this.mContext = context;
	}

}
