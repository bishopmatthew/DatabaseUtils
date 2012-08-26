package com.airlocksoftware.database;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Manages the database. Also provides some methods for interacting with the
 * database without the need for cursors.
 * 
 * @author matthewbishop
 * 
 */
public class DbInterface {

	private SQLiteDatabase mDb;
	private Context mContext;
	
	// CONSTANTS
	private static final String TAG = DbInterface.class.getSimpleName();

	private DbInterface(Context context, DbOpener opener) {
		mContext = context;
		mDb = opener.getWritableDatabase();
		// Enable foreign key constraints
		if (!mDb.isReadOnly()) {
			mDb.execSQL("PRAGMA foreign_keys = ON;");
		}
	}

	/** For when you need direct access to the database **/
	public SQLiteDatabase getDb() {
		return mDb;
	}
	
	/** Gets the SQL query as a string from R.xml.sql_queries by it's 'name' attribute **/
	public String getSqlFromXml(String name) {
		XmlResourceParser parser = mContext.getResources().getXml(R.xml.sql_queries);
		String query = null;
		while (true) {
			try {
				int eventType = parser.next();
				if (eventType == XmlPullParser.END_DOCUMENT) {
					break;
				} else if (eventType == XmlPullParser.START_TAG
						&& parser.getName().equals("query")
						&& parser.getAttributeValue(null, "name").equals(name)
						&& parser.next() == XmlPullParser.TEXT) {

					query = parser.getText();
					break;
				}
			} catch (Exception e) {
				Log.e("TEST", "Error parsing sql_queries.xml for " + name);
				break;
			}
		}

		return query.trim().replace("/n", "").replace("/t", "");
	}

	/** Checks if any rows match the arguments **/
	public boolean exists(String table, String whereClause, String[] whereArgs) {
		Cursor cursor = mDb.rawQuery("SELECT 1 FROM " + table + " where " + whereClause, whereArgs);
		boolean exists = cursor.moveToFirst();
		cursor.close();
		return exists;
	}
	
	/** Checks if a given string exists in the specified table and column **/
	public boolean exists(String table, String column, String toCheck) {
		String[] selectionArgs = new String[] { toCheck };
		Cursor cursor = mDb.rawQuery("SELECT 1 FROM " + table + " where " + column + "=?", selectionArgs);
		boolean exists = cursor.moveToFirst();
		cursor.close();
		return exists;
	}

	/** Gets the maximum value stored in the db, for the given arguments **/
	public int getMax(String table, String column, String whereClause, String[] whereArgs) {
		Cursor cursor = mDb.query(table, new String[] {column}, whereClause, whereArgs, null, null, column + " ASC");
		if(cursor.moveToFirst()) {
			int maxValue = cursor.getInt(cursor.getColumnIndex(column));
			return maxValue;
		} else {
			cursor.close();
			return -1;
		}
	}
	
	/** Gets an array of longs matching the arguments **/
	public long[] getAllLongs(String table, String column) {
		Cursor c = mDb.query(table, new String[] { column }, null, null, null, null, column);
		if (!c.moveToFirst()) {
			Log.d(TAG, "Tried to load a long[], but there are none in " + table + "-" + column);
			c.close();
			return null;
		}
		long[] toReturn = new long[c.getCount()];
		for (int i = 0; i < c.getCount(); i++) {
			toReturn[i] = c.getLong(c.getColumnIndex(column));
			c.moveToNext();
		}
		c.close();
		return toReturn;
	}


}
