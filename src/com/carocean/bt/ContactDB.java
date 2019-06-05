package com.carocean.bt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

public class ContactDB {
	String TAG = "ContactDB";
	private SQLiteDatabase database;
	private static final ContactDB contactdb = new ContactDB();
	private SQLiteDatabase db;

	public final String PACKAGE_NAME = "com.carocean";
	public final String DB_NAME = "contact.db";
	String PATH_DB = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/" + PACKAGE_NAME + "/" + DB_NAME;

	private Context context;
	private DatabaseHelper DBHelper;
	private boolean mOpened = false;

	public static final String TYPE_CONTACT = "1";
	public static final String TYPE_CALLOUT = "4";
	public static final String TYPE_CALLIN = "5";
	public static final String TYPE_MISSED = "6";
	
	private String TABLE_NAME = "PBAPTABLE";
	public final String KEY_ROWID = "_id";
	public final String KEY_TYPE = "type";
	public final String KEY_PERSONNAME = "name";
	public final String KEY_PHONENUMBER = "num";
	public final String KEY_CALLTIME = "time";

	private String PBAP_DB_CREATE = "create table " + TABLE_NAME + " (" + KEY_ROWID + " integer primary key," + KEY_TYPE + " Text,"
			+ KEY_PERSONNAME + " Text," + KEY_PHONENUMBER + " Text," + KEY_CALLTIME + " Text)";

	public ContactDB() {
	}

	public static ContactDB getinstance() {
		return contactdb;
	}

	private boolean iscontactsexist() {
		return new File(PATH_DB).exists();
	}

	public long insertOneRecord(String type, String name, String num, String time) {
		Log.e(TAG, "insertOneRecord()");
		// String phoneNumTrimed = phonenum.replaceAll("\\D+", "");
		String phoneNumTrimed = num;
		if (mOpened == false) {
			Log.e(TAG, "error! database not open!");
			return 0;
		}
		db = DBHelper.getWritableDatabase();
		if (type.equals(TYPE_CONTACT)) {
			Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + KEY_TYPE + "=" + type + " and " + KEY_PHONENUMBER + "=? and "
					+ KEY_PERSONNAME + "=?", new String[] { num, name});
			Log.e(TAG, "cursor.getCount()=" + cursor.getCount());
			if (cursor.getCount() > 0) {
			//	cursor.moveToFirst();
				Log.e(TAG, "repeat data");
				cursor.close();
				return -1;
			} else {
				cursor.close();
			}
		} else {
			Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + KEY_TYPE + "=" + type + " and " + KEY_PHONENUMBER + "=? and "
					+ KEY_PERSONNAME + "=? and " + KEY_CALLTIME + "=?", new String[] { num, name, time });

			if (cursor.getCount() > 0) {
				Log.e(TAG, "repeat data");
				cursor.close();
				return -1;
			} else {
				cursor.close();
			}
		}

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_PERSONNAME, name);
		initialValues.put(KEY_PHONENUMBER, phoneNumTrimed);
		initialValues.put(KEY_CALLTIME, time);
		return db.insert(TABLE_NAME, null, initialValues);
	}

	public ContactDB open(String tablename) throws SQLException {
		Log.e(TAG, "open() mOpened = " + mOpened);
		if (!mOpened) {
			TABLE_NAME = "_" + tablename;
			PBAP_DB_CREATE = "create table " + TABLE_NAME + " (" + KEY_ROWID + " integer primary key," + KEY_TYPE + " Text,"
					+ KEY_PERSONNAME + " Text," + KEY_PHONENUMBER + " Text," + KEY_CALLTIME + " Text)";
			
			DBHelper = new DatabaseHelper(context);
			mOpened = true;
		}
		return this;
	}

	public void close() {
		Log.e(TAG, "close()");
		if (mOpened) {
			DBHelper.close();
			mOpened = false;
		}
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		String TAG = "DatabaseHelper";

		DatabaseHelper(Context context) {
			super(context, null, null, 1);
			Log.e(TAG, "DatabaseHelper()");
		}

		public void onCreate(SQLiteDatabase db) {
			Log.e(TAG, "onCreate()");
			db.execSQL(PBAP_DB_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.e(TAG, "onUpgrade()");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

	public void flushTable() {
		Log.e(TAG, "flushTable");
		long pre = SystemClock.elapsedRealtime();
		SQLiteDatabase dbObj = null;
		if (mOpened == false) {
			Log.e(TAG, "error! database not open!");
			return;
		}

		db = DBHelper.getReadableDatabase();
		try {
			dbObj = SQLiteDatabase.openOrCreateDatabase(PATH_DB, null);
			dbObj.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			dbObj.execSQL(PBAP_DB_CREATE);
		} catch (SQLException ex) {
			Log.e(TAG, "flushPhonebookTable, exception1");
			return;
		} finally {
			try {
				dbObj.close();
			} catch (SQLException ex) {
			}
		}
		try {
			db.execSQL("ATTACH DATABASE '" + PATH_DB + "' as dbObj");
			db.execSQL("INSERT INTO dbObj." + TABLE_NAME + " SELECT * FROM " + TABLE_NAME);
		} catch (SQLException ex) {
			Log.e(TAG, "flushPhonebookTable, exception2");
		} finally {
			try {
				db.execSQL("DETACH DATABASE dbObj");
			} catch (SQLException ex) {
			}
			long cur = SystemClock.elapsedRealtime();
			Log.e(TAG, "flushTable time=" + (cur - pre));
			return;
		}
	}

	public void loadTable() {
		Log.e(TAG, "loadTable");
		long pre = SystemClock.elapsedRealtime();
		if (mOpened == false) {
			Log.e(TAG, "error! database not open!");
			return;
		}

		db = DBHelper.getWritableDatabase();
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL(PBAP_DB_CREATE);
			db.execSQL("ATTACH DATABASE '" + PATH_DB + "' as dbObj");
			db.execSQL("INSERT INTO " + TABLE_NAME + " SELECT * FROM dbObj." + TABLE_NAME);
		} catch (SQLException ex) {
			Log.e(TAG, "loadPhonebookTable, exception");
		} finally {
			try {
				db.execSQL("DETACH DATABASE dbObj");
			} catch (SQLException ex) {
			}
			long cur = SystemClock.elapsedRealtime();
			Log.e(TAG, "loadTable time=" + (cur - pre));
			return;
		}
	}
    public void getPbRecords(ArrayList<HashMap<String, String>> contactlist, ArrayList<HashMap<String, String>> recordlist) {
        Log.e(TAG, "getPbRecords");
		long pre = SystemClock.elapsedRealtime();
        if (mOpened == false) {
            Log.e(TAG, "error! database not open!");
            return;
        }

        db = DBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        if( cursor != null){
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                	HashMap<String, String> map = new HashMap<String, String>();
                	String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
                	String name = cursor.getString(cursor.getColumnIndex(KEY_PERSONNAME));
                	String num = cursor.getString(cursor.getColumnIndex(KEY_PHONENUMBER));
                	String time = cursor.getString(cursor.getColumnIndex(KEY_CALLTIME));
					map.put("name", name);
					map.put("num", num);
					if (type.equals(ContactDB.TYPE_CONTACT)) {
						contactlist.add(map);
					}else{
    					map.put("type", type);
    					map.put("time", time);
    					map.put("time_f", Bluetooth.formatcallhistorytime(time));
    					recordlist.add(map);
					}
                } while(cursor.moveToNext());
            }
            cursor.close();            
        }
		long cur = SystemClock.elapsedRealtime();
		Log.e(TAG, "loadTable time=" + (cur - pre));
        Log.e(TAG, "getPbRecords contactlist.size=" + contactlist.size() + " recordlist.size=" + recordlist.size());
    }
}
