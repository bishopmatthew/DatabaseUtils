package com.airlocksoftware.database;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Static helper methods for interating with a SQLiteDatabase
 * 
 * @author matthewbishop
 * 
 */
public class DbUtils {

	private SQLiteDatabase mDb;
	private Context mContext;

	// CONSTANTS
	private static final String TAG = DbUtils.class.getSimpleName();

	/** Gets the SQL query as a string from R.xml.sql_queries by it's 'name' attribute **/
	public static String getSqlFromXml(Context context, String name) {
		XmlResourceParser parser = context.getResources()
																			.getXml(R.xml.sql_queries);
		String query = null;
		while (true) {
			try {
				int eventType = parser.next();
				if (eventType == XmlPullParser.END_DOCUMENT) {
					break;
				} else if (eventType == XmlPullParser.START_TAG && parser.getName()
																																	.equals("query")
						&& parser.getAttributeValue(null, "name")
											.equals(name) && parser.next() == XmlPullParser.TEXT) {

					query = parser.getText();
					break;
				}
			} catch (Exception e) {
				Log.e("TEST", "Error parsing sql_queries.xml for " + name);
				break;
			}
		}

		return query.trim()
								.replace("/n", "")
								.replace("/t", "");
	}

	/** Checks if any rows match the arguments **/
	public static boolean exists(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + table + " where " + whereClause, whereArgs);
		boolean exists = cursor.moveToFirst();
		cursor.close();
		return exists;
	}

	/** Checks if a given string exists in the specified table and column **/
	public static boolean exists(SQLiteDatabase db, String table, String column, String toCheck) {
		String[] selectionArgs = new String[] { toCheck };
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + table + " where " + column + "=?", selectionArgs);
		boolean exists = cursor.moveToFirst();
		cursor.close();
		return exists;
	}

	/** Gets the id of the first row whose column matches toMatch **/
	public static long getId(SQLiteDatabase db, String table, String column, String toMatch) {
		String[] selectionArgs = new String[] { toMatch };
		Cursor cursor = db.rawQuery("SELECT id FROM " + table + " where " + column + "=? LIMIT 1", selectionArgs);

		long id = -1;

		if (cursor.moveToFirst()) {
			id = cursor.getLong(cursor.getColumnIndex("id"));
		}
		cursor.close();
		return id;
	}

	/** Gets the maximum value stored in the db, for the given arguments **/
	public static int getMax(SQLiteDatabase db, String table, String column, String whereClause, String[] whereArgs) {
		Cursor cursor = db.query(table, new String[] { column }, whereClause, whereArgs, null, null, column + " ASC");
		if (cursor.moveToFirst()) {
			int maxValue = cursor.getInt(cursor.getColumnIndex(column));
			return maxValue;
		} else {
			cursor.close();
			return -1;
		}
	}

	/** Gets an array of longs matching the arguments **/
	public static long[] getAllLongs(SQLiteDatabase db, String table, String column) {
		Cursor c = db.query(table, new String[] { column }, null, null, null, null, column);
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
