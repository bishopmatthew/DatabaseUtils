package com.airlocksoftware.database;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

/** Abstract class used to represent a row in a table. Has CRUD methods, as well as the ability to create the table.
 *  Public, non-transient fields of basic types are put into the database. **/
public abstract class SqlObject implements Serializable {

	// DATABASE FIELDS
	public long id;

	// DATABASE FIELD NAMES
	protected String ID = "id";

	// UNIMPORTANT CACHE DATA
	private int mColCount;
	private String[] mColNames;

	// CONSTANTS
	private static final String TAG = SqlObject.class.getSimpleName();
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

	// RAW QUERY CONSTANTS
	public static final String CHECK_TABLE_EXISTS = "SELECT distinct tbl_name from sqlite_master where tbl_name=?";

	// SQL TYPES
	public static final String INTEGER = "INTEGER";
	public static final String LONG = "INTEGER";
	public static final String BOOLEAN = "INTEGER";
	public static final String FLOAT = "FLOAT";
	public static final String STRING = "STRING";
	public static final String DATE = "STRING";
	public static final String OTHER = "STRING";

	// NAMES FOR CURSORS (these might need to be static)
	public String[] getColNames() {
		// CACHE
		if (mColNames != null) return mColNames;

		Field[] fields = getClass().getDeclaredFields();
		ArrayList<String> cols = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			try {
				int modifiers = fields[i].getModifiers();
				boolean shouldSave = !Modifier.isTransient(modifiers) && Modifier.isPublic(modifiers);
				if (shouldSave) cols.add(fields[i].getName());
			} catch (Exception e) {
				Log.d(TAG, "Error: couldn't get column names for object = " + toString());
			}
		}
		mColNames = (String[]) cols.toArray(new String[cols.size()]);
		return mColNames;
	}

	public String getTableName() {
		return this.getClass().getSimpleName();
	}

	// CRUD OPERATIONS

	/**
	 * Creates a new entry in the database. Will throw an exception if any data
	 * isn't set before now.
	 * Extend this method in subclass to do error checking / setup, then
	 * call super.create()
	 * 
	 * @throws Exception
	 **/
	protected boolean create(DbInterface db) throws Exception {
//		DbInterface db = DbInterface.getInstance();

		ContentValues values = new ContentValues();
		Field[] fields = getColFields();

		for (Field field : fields) {
			try {
				Object toSave = field.get(this);
				if (toSave instanceof Integer) {
					values.put(field.getName(), (Integer) toSave);
				} else if (toSave instanceof Boolean) {
					values.put(field.getName(), (Boolean) toSave);
				} else if (toSave instanceof Float) {
					values.put(field.getName(), (Float) toSave);
				} else if (toSave instanceof Long) {
					values.put(field.getName(), (Long) toSave);
				} else if (toSave instanceof String) {
					values.put(field.getName(), (String) toSave);
				} else if (toSave instanceof Date) {
					values.put(field.getName(), dateToString((Date) toSave));
				} else {
					values.put(field.getName(), toSave.toString());
				}

			} catch (Exception e) {
				Log.d(TAG, "Error saving object " + toString() + ": " + e.toString());
				throw new Exception("Error creating object=" + getTableName(), e);
			}
		}

		id = db.getDb().insert(getTableName(), null, values);
		return (id != -1);
	}

	public boolean read(DbInterface db, long id) {
		Cursor cursor = db.getDb().query(getTableName(), getColNames(), ID + "=?", new String[] { Long.toString(id) },
				null, null, null);

		if (cursor.moveToFirst()) {
			readFromCursor(cursor);
			cursor.close();
			return true;
		} else {
			Log.d(TAG, "Couldn't find id=" + id + " in the " + getTableName());
			cursor.close();
			return false;
		}
	}

	public void readFromCursor(Cursor cursor) {
		id = cursor.getLong(cursor.getColumnIndex(ID));
		Field[] fields = getColFields();

		for (Field field : fields) {
			try {
				if (field.getClass().isInstance(Integer.class)) {
					field.set(this, cursor.getInt(cursor.getColumnIndex(field.getName())));
				} else if (field.getClass().isInstance(Boolean.class)) {
					field.set(this, intToBool(cursor.getInt(cursor.getColumnIndex(field.getName()))));
				} else if (field.getClass().isInstance(Float.class)) {
					field.set(this, cursor.getFloat(cursor.getColumnIndex(field.getName())));
				} else if (field.getClass().isInstance(Long.class)) {
					field.set(this, cursor.getLong(cursor.getColumnIndex(field.getName())));
				} else if (field.getClass().isInstance(String.class)) {
					field.set(this, cursor.getString(cursor.getColumnIndex(field.getName())));
				} else if (field.getClass().isInstance(Date.class)) {
					field.set(this, stringToDate(cursor.getString(cursor.getColumnIndex(field.getName()))));
				} else {
					throw new Exception("Object isn't one of the supported type");
				}
			} catch (Exception e) {
				Log.d(TAG, "Error reading object " + toString() + ": " + e.toString());
			}
		}
	}

	// TODO I can probably abstract the creation of the content values out
	// between create and update
	public boolean update(DbInterface db) {

		ContentValues values = new ContentValues();
		Field[] fields = getColFields();

		for (Field field : fields) {
			try {
				Object toSave = field.get(this);
				if (toSave instanceof Integer) {
					values.put(field.getName(), (Integer) toSave);
				} else if (toSave instanceof Boolean) {
					values.put(field.getName(), (Boolean) toSave);
				} else if (toSave instanceof Float) {
					values.put(field.getName(), (Float) toSave);
				} else if (toSave instanceof Long) {
					values.put(field.getName(), (Long) toSave);
				} else if (toSave instanceof String) {
					values.put(field.getName(), (String) toSave);
				} else if (toSave instanceof Date) {
					values.put(field.getName(), dateToString((Date) toSave));
				} else {
					values.put(field.getName(), toSave.toString());
				}

			} catch (Exception e) {
				Log.d(TAG, "Error updating object " + toString() + ": " + e.toString());
			}
		}

		return db.getDb().update(getTableName(), values, ID + "=?", new String[] { Long.toString(id) }) > 0;
	}

	public boolean delete(DbInterface db) {
		if (!db.exists(getTableName(), ID, Long.toString(id))) {
			Log.d(TAG, "Tried to delete a nonexistent " + ID + "=" + id);
			return false;
		} else {
			return db.getDb().delete(getTableName(), ID + "=?", new String[] { Long.toString(id) }) == 1;
		}
	}

	// TABLE OPERATIONS
	// TODO ADD DROP TABLE SUPPORT (even better, add upgrade support)
	/** Creates a table for these objects if one doesn't already exist **/
	public boolean createTable(DbInterface db) {
		Field[] fields = getColFields();
		String statement = "CREATE TABLE IF NOT EXISTS " + getTableName() + " ( ";
		statement += "id INTEGER PRIMARY KEY, ";
		for (Field col : fields) {
			// skip id
			if (col.getName().equals(ID)) {
				continue;
			}
			// handle other types
			statement += col.getName() + " " + getFieldSqlType(col) + ", ";
		}
		// remove final comma (,)
		statement = statement.substring(0, statement.length() - 2);
		statement += " ) ";
		db.getDb().execSQL(statement);
		return true;
	}

	/** Check if the table this object defines exists **/
	public boolean tableExists(DbInterface db) {
		Cursor c = db.getDb().rawQuery(CHECK_TABLE_EXISTS, new String[] { getTableName() });
		return (c != null && c.moveToFirst());
	}

	// UTIL METHODS
	public static String dateToString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		return sdf.format(date);
	}

	public static Date stringToDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		Date toReturn = null;
		try {
			toReturn = sdf.parse(date);
		} catch (ParseException e) {
			Log.d(TAG, "Error parsing date=" + date);
		}
		return toReturn;
	}

	public int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	public boolean intToBool(int i) {
		return i == 1;
	}

	// PROTECTED UTILS
	protected boolean isColField(Field toCheck) {
		int modifiers = toCheck.getModifiers();
		return (!Modifier.isTransient(modifiers) && Modifier.isPublic(modifiers));
	}

	protected Field[] getColFields() {
		Field[] fields = getClass().getDeclaredFields();
		ArrayList<Field> colFields = new ArrayList<Field>();

		for (int i = 0; i < fields.length; i++) {
			try {
				Field field = fields[i];
				if (isColField(field)) colFields.add(field);
			} catch (Exception e) {
				Log.d(TAG, "Error getting ColFields " + toString() + ": " + e.toString());
			}
		}

		return (Field[]) colFields.toArray(new Field[colFields.size()]);
	}

	protected String getFieldSqlType(Field field) {
		try {
			Object toSave = field.get(this);
			if (toSave instanceof Integer) {
				return INTEGER;
			} else if (toSave instanceof Boolean) {
				return BOOLEAN;
			} else if (toSave instanceof Float) {
				return FLOAT;
			} else if (toSave instanceof Long) {
				return LONG;
			} else if (toSave instanceof String) {
				return STRING;
			} else if (toSave instanceof Date) {
				return DATE;
			} else {
				return OTHER;
			}
		} catch (Exception e) {
			Log.d(TAG, "Error getting FieldSqlType: " + field.toString());
			return OTHER;
		}
	}
}
