package com.airlocksoftware.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DbOpener extends SQLiteOpenHelper {

	private Context mContext;
	private DbInterface mDbInterface;

	/** The name of the database file **/
	/* TODO make this work with a library project 
	 * define DATABASE_NAME as an attribute OR
	 * make this an abstract class and force subclass to overide it
	 */
	private static final String DATABASE_NAME = "airlock_database.db";

	/** The database version **/
	private static final int DATABASE_VERSION = 1;
	
	private static final String TAG = DbOpener.class.getSimpleName();;

	public DbOpener(Context context, DbInterface dbInterface) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
		this.mDbInterface = dbInterface;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	private void createTables(SQLiteDatabase db) {
		db.execSQL(mDbInterface.getSqlFromXml("create_table_types"));
		db.execSQL(mDbInterface.getSqlFromXml("create_table_categories"));
		db.execSQL(mDbInterface.getSqlFromXml("create_table_transactions"));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// CODE TO HANDLE UPGRADES TO DB GOES HERE
		
		// TODO while in development, drop the tables when upgrading db
		//db.execSQL("DROP TABLE IF EXISTS Types");
		//db.execSQL("DROP TABLE IF EXISTS Categories");
		//db.execSQL("DROP TABLE IF EXISTS Transactions");
	}

	

}
