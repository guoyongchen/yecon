package com.carocean.media.scan;

import java.util.ArrayList;
import java.util.List;

import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaScanConstans.YeconMediaAlbumColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaArtistColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaDirColumns;
import com.carocean.media.constants.MediaScanConstans.YeconMediaFilesColumns;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class MediaProvider extends ContentProvider {

	private static final String TAG = "MediaProvider";
	
	@Override
	public boolean onCreate() {
		Log.i(TAG, "MediaProvider onCreate");
		return false;
	}

	/**
	 * 获取需要操作的数据库
	 */
	@SuppressLint("DefaultLocale")
	public DatabaseHelper getDatabaseHelper(String strDBName) {
		synchronized (TAG) {
			DatabaseHelper db  = null;
			Log.e(TAG, "++getDatabaseHelper++");
			if (strDBName == null || strDBName.length() == 0) {
				Log.e(TAG, "getDatabaseHelper illegal parameter!");
			} else {
				db = DatabaseHelper.getInstance(getContext(), strDBName);
			}
			Log.e(TAG, "--getDatabaseHelper--");
			return db;
		}
	}

	/**
	 * Title: query Description:
	 * 
	 * @param uri
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		synchronized (TAG) {
			return query(uri, columns, selection, selectionArgs, sortOrder, false);
		}
	}

	public Cursor query(Uri uri, String[] columns, String selection,
			String[] selectionArgs, String sortOrder, boolean bDistinct) {
		synchronized (TAG) {
			Log.i(TAG, "++query++");
			long lStart = System.currentTimeMillis();
			Cursor c = null;
			List<String> ls = uri.getPathSegments();
			if (ls.size() >= 2) {
				DatabaseHelper helper = getDatabaseHelper(ls.get(0));
				String table = ls.get(1);
				try {
					if (helper != null) {
						c = helper.getWritableDatabase().query(bDistinct, table,
								columns, selection, selectionArgs, null, null, sortOrder,
								null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					helper = null;
				}
			} else {
				Log.e(TAG, "Unknown URI" + uri);
			}
			Log.i(TAG, "--query-- count:" + (c == null ? 0 : c.getCount())
					+ " cost time:" + (System.currentTimeMillis() - lStart));
			return c;
		}
	}

	/**
	 * Title: getType Description:
	 * 
	 * @param uri
	 * @return
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		return null;
	}

	/**
	 * Title: insert Description:
	 * 
	 * @param uri
	 * @param values
	 * @return
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 *      android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		synchronized (TAG) {
			List<String> ls = uri.getPathSegments();
			if (ls.size() >= 2) {
				DatabaseHelper helper = getDatabaseHelper(ls.get(0));
				String table = ls.get(1);
				try {
					if (helper != null) {
						long lRowID = helper.getWritableDatabase().insert(table, null,
								values);
						return Uri.parse(MediaScanConstans.CONTENT_URI + "/" + ls.get(0) + "/"
								+ ls.get(1) + "/" + lRowID);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Log.e(TAG, "Unknown URI" + uri);
			}
			return null;
		}
	}

	/**
	 * 
	 * @Title: insert
	 * @Description: 批量插入到数据库
	 * @param @param uri
	 * @param @param lsValues
	 */
	public long insert(Uri uri, List<ContentValues> lsValues) {
		synchronized (TAG) {
			long lStart = System.currentTimeMillis();
			Log.i(TAG, "++insert++");
			List<String> ls = uri.getPathSegments();
			long lOut = 0;
			if (ls.size() >= 2) {
				DatabaseHelper helper = getDatabaseHelper(ls.get(0));
				String table = ls.get(1);
				if (helper != null) {
					SQLiteDatabase db = helper.getWritableDatabase();
					try {
						db.beginTransaction();
						for (int i = 0; i < lsValues.size(); i++) {
							lOut = helper.getWritableDatabase().insert(table,
									null, lsValues.get(i));
						}
						db.setTransactionSuccessful();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.endTransaction();
					}
				}
			} else {
				Log.e(TAG, "Unknown URI" + uri);
			}
			Log.i(TAG, "--insert-- " + (System.currentTimeMillis() - lStart));
			return lOut;
		}
	}

	/**
	 * Title: delete Description:
	 * 
	 * @param uri
	 * @param selection
	 * @param selectionArgs
	 * @return
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 *      java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		synchronized (TAG) {
			List<String> ls = uri.getPathSegments();
			if (ls.size() >= 2) {
				DatabaseHelper helper = getDatabaseHelper(ls.get(0));
				if (helper != null) {
					String table = ls.get(1);
					try {
						return helper.getWritableDatabase().delete(table, selection,
								selectionArgs);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				Log.e(TAG, "Unknown URI" + uri);
			}
			return -1;
		}
	}

	/**
	 * Title: update Description:
	 * 
	 * @param uri
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @return
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 *      android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		synchronized (TAG) {
			List<String> ls = uri.getPathSegments();
			if (ls.size() >= 2) {
				DatabaseHelper helper = getDatabaseHelper(ls.get(0));
				if (helper != null) {
					String table = ls.get(1);
					try {
						return helper.getWritableDatabase().update(table, values,
								selection, selectionArgs);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				Log.e(TAG, "Unknown URI" + uri);
			}
			return -1;
		}
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {
		/*
		 * 创建用于在dir表有数据插入的时候自动更新files表中parent_id的trigger
		 */
		private static final String TRIGGER_DIR_INSERT = "create trigger trigger_insert_dir_id after insert on "
				+ MediaScanConstans.TABLE_DIR
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.PARENT_ID
				+ "=new."
				+ YeconMediaDirColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.PARENT
				+ "=new."
				+ YeconMediaDirColumns.DATA + "; end;";
		
		/*
		 * 创建用于在dir表有数据更新时候自动更新files表中的parent_id的trigger
		 */
		private static final String TRIGGER_DIR_UPDATE = "create trigger trigger_update_dir_id after update on "
				+ MediaScanConstans.TABLE_DIR
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.PARENT_ID
				+ "=new."
				+ YeconMediaDirColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.PARENT
				+ "=new."
				+ YeconMediaDirColumns.DATA + "; end;";
		
		/*
		 * 创建用于在album表有数据插入的时候自动更新files表中album_id的trigger
		 */
		private static final String TRIGGER_ALBUM_INSERT = "create trigger trigger_insert_album_id after insert on "
				+ MediaScanConstans.TABLE_ALBUM
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.ALBUM_ID
				+ "=new."
				+ YeconMediaAlbumColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.ALBUM
				+ "=new."
				+ YeconMediaAlbumColumns.NAME + "; end;";
		
		/*
		 * 创建用于在album表有数据更新的时候自动更新files表中album_id的trigger
		 */
		private static final String TRIGGER_ALBUM_UPDATE = "create trigger trigger_update_album_id after update on "
				+ MediaScanConstans.TABLE_ALBUM
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.ALBUM_ID
				+ "=new."
				+ YeconMediaAlbumColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.ALBUM
				+ "=new."
				+ YeconMediaAlbumColumns.NAME + "; end;";

		/*
		 * 创建用于在artist表有数据插入的时候自动更新files表中artist_id的trigger
		 */
		private static final String TRIGGER_ARTIST_INSERT = "create trigger trigger_insert_artist_id after insert on "
				+ MediaScanConstans.TABLE_ARTIST
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.ARTIST_ID
				+ "=new."
				+ YeconMediaArtistColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.ARTIST
				+ "=new."
				+ YeconMediaArtistColumns.NAME + "; end;";
		
		/*
		 * 创建用于在artist表有数据更新的时候自动更新files表中artist_id的trigger
		 */
		private static final String TRIGGER_ARTIST_UPDATE = "create trigger trigger_update_artist_id after update on "
				+ MediaScanConstans.TABLE_ARTIST
				+ " begin update "
				+ MediaScanConstans.TABLE_FILES
				+ " set "
				+ YeconMediaFilesColumns.ARTIST_ID
				+ "=new."
				+ YeconMediaArtistColumns._ID
				+ " where "
				+ YeconMediaFilesColumns.ARTIST
				+ "=new."
				+ YeconMediaArtistColumns.NAME + "; end;";
		/**
		 * 创建一个新的实例 DatabaseHelper. Title: Description:
		 * 
		 * @param context
		 * @param name
		 */
		private static ArrayList<DataBaseObject> instance = new ArrayList<DataBaseObject>();
		
		private DatabaseHelper(Context context, String name) {
			super(context, ConstructDBName(name), null,
					getDatabaseVersion(context));
			Log.e(TAG, "[DatabaseHelper] create DatabaseHelper");
		}


		public static DatabaseHelper getInstance(Context context, String name) {
			if (instance != null) {
				for (DataBaseObject dataBaseObject : instance) {
					if (dataBaseObject.getName() != null && dataBaseObject.getName().equals(name)) {
						Log.i(TAG, "[DatabaseHelper] find getInstance name:" + name);
						return dataBaseObject.getDataBaseHelper();
					}
				}
				DatabaseHelper databaseHelper = new DatabaseHelper(context, name);
				instance.add(new DataBaseObject(databaseHelper, name));
				Log.i(TAG, "[DatabaseHelper] new getInstance name:" + name);
				return databaseHelper;
			}
			Log.i(TAG, "[DatabaseHelper] getInstance null");
			return null;
		}


			
		@SuppressLint("DefaultLocale")
		private static String ConstructDBName(String strDBName) {
			if (strDBName != null) {
				strDBName = strDBName.toLowerCase();
				if (!strDBName.endsWith(".db")) {
					strDBName += ".db";
				}
			}
			return strDBName;
		}

		/**
		 * @Title: getDatabaseVersion
		 * @param @param context
		 */
		public static int getDatabaseVersion(Context context) {
			return 1;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.e(TAG, "[DatabaseHelper] ++onCreate++");
			synchronized (TAG) {
				// create tables
				db.execSQL(MediaScanConstans.YeconMediaFilesColumns.getCreateTableSQL(MediaScanConstans.TABLE_FILES));
				db.execSQL(MediaScanConstans.YeconMediaDirColumns.getCreateTableSQL(MediaScanConstans.TABLE_DIR));
				db.execSQL(MediaScanConstans.YeconMediaArtistColumns.getCreateTableSQL(MediaScanConstans.TABLE_ARTIST));
				db.execSQL(MediaScanConstans.YeconMediaAlbumColumns.getCreateTableSQL(MediaScanConstans.TABLE_ALBUM));
				// create triggers
				db.execSQL(TRIGGER_DIR_INSERT);
				db.execSQL(TRIGGER_ALBUM_INSERT);
				db.execSQL(TRIGGER_ARTIST_INSERT);
				db.execSQL(TRIGGER_DIR_UPDATE);
				db.execSQL(TRIGGER_ALBUM_UPDATE);
				db.execSQL(TRIGGER_ARTIST_UPDATE);
			}
			Log.e(TAG, "[DatabaseHelper] --onCreate--");
		}

		/**
		 * @Title: ClearTable
		 * @Description: 清空所有的表
		 */
		public void ClearTable() {
			Log.e(TAG, "[DatabaseHelper] ++ClearTable++");
			synchronized (TAG) {
				try {
					SQLiteDatabase db = getWritableDatabase();
					onUpgrade(db, 1, 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.e(TAG, "[DatabaseHelper] --ClearTable--");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			synchronized (TAG) {
				db.execSQL("drop table if exists " + MediaScanConstans.TABLE_FILES);
				db.execSQL("drop table if exists " + MediaScanConstans.TABLE_DIR);
				db.execSQL("drop table if exists " + MediaScanConstans.TABLE_ALBUM);
				db.execSQL("drop table if exists " + MediaScanConstans.TABLE_ARTIST);
				onCreate(db);
			}
		}

		/**
		 * @Title: excuteSQL
		 * @param 执行的sql语句
		 */
		public void excuteSQL(String strSQL) {
			synchronized (TAG) {
				getWritableDatabase().execSQL(strSQL);
			}
		}
		
		public long insert(String strTable, List<ContentValues> lsValues) {
			Log.e(TAG, "[DatabaseHelper] ++insert++");
			synchronized (TAG) {
				long lOut = 0;
				if (strTable != null) {
					SQLiteDatabase db = getWritableDatabase();
					try {
						db.beginTransaction();
						for (ContentValues values : lsValues) {
							lOut = db.insert(strTable, null, values);
						}
						db.setTransactionSuccessful();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.endTransaction();
					}
				}
				Log.e(TAG, "[DatabaseHelper] --insert-- :" + lOut);
				return lOut;
			}
		}
		
		public long update(String strTable, List<ContentValues> lsValues, String whereClause, String[] whereArgs) {
			Log.e(TAG, "[DatabaseHelper] ++update++");
			synchronized (TAG) {
				long lOut = 0;
				if (strTable != null) {
					SQLiteDatabase db = getWritableDatabase();
					try {
						db.beginTransaction();
						for (ContentValues values : lsValues) {
							db.update(strTable, values, whereClause, whereArgs);
						}
						db.setTransactionSuccessful();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.endTransaction();
					}
				}
				Log.e(TAG, "[DatabaseHelper] --update-- :" + lOut);
				return lOut;
			}
		}
		
		public Cursor query(String table, String[] columns, String selection, String[] selectionArgs) {
			Log.e(TAG, "[DatabaseHelper] ++query++");
			synchronized (TAG) {
				Cursor c = null;
				if (table != null) {
					SQLiteDatabase db = getWritableDatabase();
					try {
						c = db.query(table, columns, selection, selectionArgs,
								null, null, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Log.e(TAG, "[DatabaseHelper] --query--");
				return c;
			}
		}
		
		public void delete(String table, String whereClause, String[] whereArgs) {
			Log.e(TAG, "[DatabaseHelper] ++delete++");
			synchronized (TAG) {
				SQLiteDatabase db = getWritableDatabase();
				try {
					db.delete(table, whereClause, whereArgs);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.e(TAG, "[DatabaseHelper] --delete--");
			}
		}
		
		public Cursor execQuery(String sql) {
			Log.e(TAG, "[DatabaseHelper] ++execQuery++");
			synchronized (TAG) {
				Cursor c = null;
				if (sql != null) {
					SQLiteDatabase db = getWritableDatabase();
					try {
						c = db.rawQuery(sql, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Log.e(TAG, "[DatabaseHelper] --execQuery--");
				return c;
			}
		}
		
		@Override
		protected void finalize() throws Throwable {
			Log.e(TAG, "[DatabaseHelper] ++finalize++");
			synchronized (TAG) {
				try {
					this.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					super.finalize();
				}
			}
			Log.e(TAG, "[DatabaseHelper] --finalize--");
		}
	}
	
	static class  DataBaseObject {
		private DatabaseHelper mInstance = null;
		private String mName = "";
		public DataBaseObject(DatabaseHelper databaseHelper, String name) {
			mInstance = databaseHelper;
			mName = name;
		}
		
		public String getName() {
			return mName;
		}
		
		public DatabaseHelper getDataBaseHelper() {
			return mInstance;
		}
	}
}
