package com.airlocksoftware.database;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Abstract class used to represent a row in a table. Has CRUD methods, as well as the ability to create the table.
 * Public, non-transient fields of basic types are used as columns.
 **/
public abstract class SqlObject implements Serializable {

	// DATABASE FIELDS
	public long id;

	// DATABASE FIELD NAMES
	protected String ID = "id";

	// UNIMPORTANT CACHE DATA
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
	public static final String ENUM = "STRING";
	public static final String FLOAT = "FLOAT";
	public static final String STRING = "STRING";
	public static final String DATE = "STRING";
	public static final String OTHER = "STRING";

	public String[] getColNames() {
		// cached value
		if (mColNames != null) return mColNames;

		Field[] fields = getClass().getDeclaredFields();
		ArrayList<String> cols = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			try {
				Field field = fields[i];
				if (isColField(field)) cols.add(field.getName());
			} catch (Exception e) {
				Log.d(TAG, "Error: couldn't get column names for object = " + toString());
			}
		}
		// add ID field
		cols.add(ID);
		mColNames = (String[]) cols.toArray(new String[cols.size()]);
		return mColNames;
	}

	public String getTableName() {
		return this.getClass()
								.getSimpleName();
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
	protected boolean create(SQLiteDatabase db) {
		ContentValues values = this.toContentValues();
		// we're not setting id when we create this
		values.remove(ID);

		id = db.insert(getTableName(), null, values);
		return (id != -1);
	}

	public boolean read(SQLiteDatabase db, long idToRead) {
		Cursor cursor = db.query(getTableName(), getColNames(), ID + "=?", new String[] { Long.toString(idToRead) }, null,
				null, null);

		if (cursor.moveToFirst()) {
			readFromCursor(cursor);
			cursor.close();
			return true;
		} else {
			Log.d(TAG, "Couldn't find id=" + id + " in the " + getTableName() + " table");
			cursor.close();
			return false;
		}
	}

	public void readFromCursor(Cursor cursor) {
		id = cursor.getLong(cursor.getColumnIndex(ID));
		Field[] fields = getColFields();

		for (Field field : fields) {
			try {
				Class<?> type = field.getType();
				if (type.isAssignableFrom(Integer.TYPE)) {
					field.set(this, cursor.getInt(cursor.getColumnIndex(field.getName())));
				} else if (type.isAssignableFrom(Boolean.TYPE)) {
					int bool = cursor.getInt(cursor.getColumnIndex(field.getName()));
					boolean val = intToBool(bool);
					field.set(this, val);
				} else if (type.isAssignableFrom(Float.TYPE)) {
					field.set(this, cursor.getFloat(cursor.getColumnIndex(field.getName())));
				} else if (type.isAssignableFrom(Long.TYPE)) {
					field.set(this, cursor.getLong(cursor.getColumnIndex(field.getName())));
				} else if (type.isEnum()) {
					Method valueOf = type.getMethod("valueOf", String.class);
					String enumName = cursor.getString(cursor.getColumnIndex(field.getName()));
					if (enumName != null) field.set(this, valueOf.invoke(null, enumName));
				} else if (type.isAssignableFrom(String.class)) {
					field.set(this, cursor.getString(cursor.getColumnIndex(field.getName())));
				} else if (type.isAssignableFrom(Date.class)) {
					field.set(this, stringToDate(cursor.getString(cursor.getColumnIndex(field.getName()))));
				} else {
					throw new Exception("Object: " + field.toString() + " isn't one of the supported type");
				}
			} catch (Exception e) {
				Log.d(TAG, "Error reading object\n" + toString() + ": \n\n" + e.toString());
			}
		}
	}

	public boolean update(SQLiteDatabase db) {
		ContentValues values = this.toContentValues();
		return db.update(getTableName(), values, ID + "=?", new String[] { Long.toString(id) }) > 0;
	}

	public boolean delete(SQLiteDatabase db) {
		if (!DbUtils.exists(db, getTableName(), ID, Long.toString(id))) {
			Log.d(TAG, "Tried to delete a nonexistent " + ID + "=" + id);
			return false;
		} else {
			return db.delete(getTableName(), ID + "=?", new String[] { Long.toString(id) }) == 1;
		}
	}

	// TABLE OPERATIONS

	/** Creates a table for these objects if one doesn't already exist **/
	public boolean createTable(SQLiteDatabase db) {
		Field[] fields = getColFields();
		String statement = "CREATE TABLE IF NOT EXISTS " + getTableName() + " ( ";
		statement += ID + " INTEGER PRIMARY KEY, ";
		for (Field col : fields) {
			// skip id (although I don't think it will show up in subclasses)
			if (col.getName()
							.equals(ID)) continue;

			// handle other types
			statement += col.getName() + " " + getFieldSqlType(col) + ", ";
		}
		// remove final comma (,)
		statement = statement.substring(0, statement.length() - 2);
		statement += " ) ";
		db.execSQL(statement);
		return true;
	}

	/** Check if the table this object defines exists **/
	public boolean tableExists(SQLiteDatabase db) {
		Cursor c = db.rawQuery(CHECK_TABLE_EXISTS, new String[] { getTableName() });
		return (c != null && c.moveToFirst());
	}

	// TODO add drop table support (even better, add upgrade support)

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

	private boolean isColField(Field toCheck) {
		int modifiers = toCheck.getModifiers();
		return (!Modifier.isTransient(modifiers) && Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers));
	}

	private Field[] getColFields() {
		// Field[] fields = getClass().getDeclaredFields();
		Field[] fields = getClass().getFields();
		List<Field> colFields = new ArrayList<Field>();

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
			} else if (toSave.getClass()
												.isEnum()) {
				return ENUM;
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

	protected ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		Field[] fields = getColFields();

		for (Field field : fields) {
			try {
				Object toSave = field.get(this);
				if (toSave == null) continue; // object is null, don't put it in ContentValues

				if (toSave instanceof Integer) {
					values.put(field.getName(), (Integer) toSave);
				} else if (toSave.getClass()
													.isEnum()) {
					values.put(field.getName(), toSave.toString());
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
				throw new RuntimeException("Error creating object=" + getTableName(), e);
			}
		}

		// put id in
		values.put(ID, id);

		return values;
	}
}
