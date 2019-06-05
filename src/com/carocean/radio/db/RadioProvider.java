package com.carocean.radio.db;

import com.carocean.utils.sLog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RadioProvider extends ContentProvider {
	private static final String TAG = "RadioProvider";

	// database instance use to operate the database
	private SQLiteDatabase mSqlDb = null;
	// database helper use to get database instance
	private DatabaseHelper mDbHelper = null;
	// database name
	private static final String DATABASE_NAME = "FmRadio.db";
	// database version
	private static final int DATABASE_VERSION = 1;
	// table name
	private static final String TABLE_NAME = "StationList";

	// URI match code
	private static final int STATION_FREQ = 1;
	// URI match code
	private static final int STATION_FREQ_ID = 2;
	// use to match URI
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	// match URI with station frequency or station frequency id
	static {
		URI_MATCHER.addURI(RadioStation.AUTHORITY, RadioStation.STATION, STATION_FREQ);
		URI_MATCHER.addURI(RadioStation.AUTHORITY, RadioStation.STATION + "/#", STATION_FREQ_ID);
	}

	/**
	 * Helper to operate database
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			sLog.i(TAG, "FMRadioProvider - DatabaseHelper - onCreate");
			db.execSQL("CREATE TABLE " + TABLE_NAME + "("
					+ RadioStation.Station._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ RadioStation.Station.FREQUENCY + " INTEGER UNIQUE,"
					+ RadioStation.Station.BAND + " INTEGER DEFAULT 0,"
					+ RadioStation.Station.IS_PRESET_FREQ + " INTEGER DEFAULT 0,"
					+ RadioStation.Station.IS_FAVORITE + " INTEGER DEFAULT 0"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			sLog.i(TAG, "onUpgrade, upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

	}

	@Override
	public boolean onCreate() {
		Log.i(TAG, "onCreate");
		mDbHelper = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri rowUri = null;
		mSqlDb = mDbHelper.getWritableDatabase();
		ContentValues v = new ContentValues(values);

		long rowId = mSqlDb.insert(TABLE_NAME, null, v);
		if (rowId <= 0) {
			sLog.i(TAG, "insert, failed to insert row into " + uri + " error = " + rowId);
		}
		rowUri = ContentUris.appendId(RadioStation.Station.CONTENT_URI.buildUpon(), rowId).build();
		getContext().getContentResolver().notifyChange(rowUri, null);
		// mSqlDb.close();
		return rowUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rows = 0;
		mSqlDb = mDbHelper.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case STATION_FREQ:
			rows = mSqlDb.delete(TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		case STATION_FREQ_ID:
			String stationID = uri.getPathSegments().get(1);
			rows = mSqlDb.delete(TABLE_NAME, RadioStation.Station._ID + "=" + stationID
					+ (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		default:
			sLog.i(TAG, "delete, unkown URI to delete: " + uri);
			break;
		}
		// mSqlDb.close();
		return rows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rows = 0;
		mSqlDb = mDbHelper.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case STATION_FREQ:
			rows = mSqlDb.update(TABLE_NAME, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case STATION_FREQ_ID:
			String stationID = uri.getPathSegments().get(1);
			rows = mSqlDb.update(TABLE_NAME, values, RadioStation.Station._ID + "=" + stationID
					+ (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		default:
			sLog.i(TAG, "update, unkown URI to update: " + uri);
			break;
		}
		// mSqlDb.close();
		return rows;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		qb.setTables(TABLE_NAME);

		int match = URI_MATCHER.match(uri);

		if (STATION_FREQ_ID == match) {
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
		}

		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		if (null != c) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		// db.close();
		// c.close();
		return c;
	}

	public String getType(Uri uri) {
		return null;
	}

}
