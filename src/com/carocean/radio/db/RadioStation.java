package com.carocean.radio.db;

import java.util.ArrayList;

import com.carocean.radio.constants.RadioConstants;
import com.carocean.radio.constants.RadioFreqInfo;
import com.carocean.utils.Constants;
import com.carocean.utils.DataShared;
import com.carocean.utils.SourceManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class RadioStation {
	private static final String TAG = "RadioStation";

	// authority use composite content provider uri
	public static final String AUTHORITY = "com.carocean.radio";
	// use to composite content provider uri
	public static final String STATION = "station";
	// store current station in share preference with this key
	public static final String FM_CURRENT_STATION = "fm_curent_station";
	public static final String AM_CURRENT_STATION = "am_curent_station";

	public static final String FMRADIO_BAND = "fmradio_band";

	public static final String[] COLUMNS = new String[] { Station._ID, Station.FREQUENCY, Station.BAND,
			Station.IS_PRESET_FREQ, Station.IS_FAVORITE };

	public static final class Station implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + STATION);
		// 频率
		public static final String FREQUENCY = "frequency";
		// 频道
		public static final String BAND = "band";
		// 预置
		public static final String IS_PRESET_FREQ = "is_preset_freq";
		// 喜爱
		public static final String IS_FAVORITE = "is_favorite";
	}

	// 设置当前电台
	public static void setCurrentFreq(Context context, int frequency) {
		if (null != context) {
			String flag = FM_CURRENT_STATION;
			if (getRadioBand(context) == RadioConstants.BAND_ID_AM) {
				flag = AM_CURRENT_STATION;
			}
			DataShared.getInstance(context).putInt(flag, frequency);
			DataShared.getInstance(context).commit();
		}
	}

	// 获取当前电台
	public static int getCurrentFreq(Context context) {
		if (null != context) {
			String flag = FM_CURRENT_STATION;
			int freq = RadioConstants.DEFAULT_FM_STATION_MIN;
			if (getRadioBand(context) == RadioConstants.BAND_ID_AM) {
				flag = AM_CURRENT_STATION;
				freq = RadioConstants.DEFAULT_AM_STATION_MIN;
			}
			return DataShared.getInstance(context).getInt(flag, freq);
		}
		return 0;
	}

	// 获取频道
	public static int getRadioBand(Context context) {
		if (null != context) {
			return DataShared.getInstance(context).getInt(FMRADIO_BAND, RadioConstants.BAND_ID_FM);
		}
		return 0;
	}

	// 设置频道
	public static void setRadioBand(Context context, int band) {
		if (null != context) {
			DataShared.getInstance(context).putInt(FMRADIO_BAND, band);
			DataShared.getInstance(context).commit();
			SourceManager.acquireSource(context, band == RadioConstants.BAND_ID_FM ?
					Constants.KEY_SRC_MODE_FM : Constants.KEY_SRC_MODE_AM);
		}
	}

	// 新增预置电台
	public static void addPresetFreq(Context context, int freq) {
		if (null != context) {
			if (isFreqExist(context, freq)) {
				ContentValues values = new ContentValues(1);
				values.put(Station.IS_PRESET_FREQ, true);
				context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
						new String[] { String.valueOf(freq) });
			} else {
				ContentValues values = new ContentValues(3);
				values.put(Station.FREQUENCY, freq);
				values.put(Station.BAND, getRadioBand(context));
				values.put(Station.IS_PRESET_FREQ, true);
				context.getContentResolver().insert(Station.CONTENT_URI, values);
			}
		}
	}

	// 删除预置电台
	public static void removePresetFreq(Context context, int band, int freq) {
		if (null != context) {
			ArrayList<RadioFreqInfo> mFreqList = getAllPresetFreqInfo(context, band);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					if (d.getFreq() == freq) {
						if (d.getFavorite() == 0 && d.getPreset() == 1) {
							context.getContentResolver().delete(Station.CONTENT_URI, Station.FREQUENCY + "=?",
									new String[] { String.valueOf(freq) });
						} else {
							ContentValues values = new ContentValues(1);
							values.put(Station.IS_PRESET_FREQ, false);
							context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
									new String[] { String.valueOf(freq) });
						}
						break;
					}
				}
			}
		}
	}

	// 删除所有预置电台
	public static void removeAllPresetFreq(Context context, int band) {
		if (null != context) {
			ArrayList<RadioFreqInfo> mFreqList = getAllPresetFreqInfo(context, band);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					if (d.getFavorite() == 0 && d.getPreset() == 1) {
						context.getContentResolver().delete(Station.CONTENT_URI, Station.FREQUENCY + "=?",
								new String[] { String.valueOf(d.getFreq()) });
					} else {
						ContentValues values = new ContentValues(1);
						values.put(Station.IS_PRESET_FREQ, false);
						context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
								new String[] { String.valueOf(d.getFreq()) });
					}
				}
			}
		}
	}

	// 新增喜爱FM电台
	public static void addFavorite(Context context, int freq) {
		if (null != context) {
			if (isFreqExist(context, freq)) {
				ContentValues values = new ContentValues(1);
				values.put(Station.IS_FAVORITE, true);
				context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
						new String[] { String.valueOf(freq) });
			} else {
				ContentValues values = new ContentValues(3);
				values.put(Station.FREQUENCY, freq);
				values.put(Station.BAND, getRadioBand(context));
				values.put(Station.IS_FAVORITE, true);
				context.getContentResolver().insert(Station.CONTENT_URI, values);
			}
		}
	}

	// 删除喜爱电台
	public static void removeFavorite(Context context, int band, int freq) {
		if (null != context) {
			ArrayList<RadioFreqInfo> mFreqList = getAllFavoriteFreqInfo(context, band);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					if (d.getFreq() == freq) {
						if (d.getFavorite() == 1 && d.getPreset() == 0) {
							context.getContentResolver().delete(Station.CONTENT_URI, Station.FREQUENCY + "=?",
									new String[] { String.valueOf(freq) });
						} else {
							ContentValues values = new ContentValues(1);
							values.put(Station.IS_FAVORITE, false);
							context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
									new String[] { String.valueOf(freq) });
						}
						break;
					}
				}
			}
		}
	}

	// 删除所有喜爱电台
	public static void removeBandAllFavorite(Context context, int band) {
		if (null != context) {
			ArrayList<RadioFreqInfo> mFreqList = getAllFavoriteFreqInfo(context, band);
			if (null != mFreqList && !mFreqList.isEmpty()) {
				for (RadioFreqInfo d : mFreqList) {
					if (d.getFavorite() == 1 && d.getPreset() == 0) {
						context.getContentResolver().delete(Station.CONTENT_URI, Station.FREQUENCY + "=?",
								new String[] { String.valueOf(d.getFreq()) });
					} else {
						ContentValues values = new ContentValues(1);
						values.put(Station.IS_FAVORITE, false);
						context.getContentResolver().update(Station.CONTENT_URI, values, Station.FREQUENCY + "=?",
								new String[] { String.valueOf(d.getFreq()) });
					}
				}
			}
		}
	}

	// 检查电台是否存在
	private static boolean isFreqExist(Context context, int freq) {
		boolean isExist = false;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Station.CONTENT_URI, new String[] { Station.FREQUENCY },
					Station.FREQUENCY + "=?", new String[] { String.valueOf(freq) }, RadioStation.Station.FREQUENCY);
			if (cursor != null && cursor.moveToFirst() && cursor.getColumnCount() > 0) {
				isExist = true;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return isExist;
	}

	// 获取所有喜爱/预置频率电台
	public static ArrayList<Integer> getAllBandFreqInfo(Context context, int band) {
		ArrayList<Integer> favoriteList = new ArrayList<Integer>();
		if (null != context) {

			Cursor cursor = context.getContentResolver().query(RadioStation.Station.CONTENT_URI, RadioStation.COLUMNS,
					RadioStation.Station.BAND + "=?", new String[] { String.valueOf(band) },
					RadioStation.Station.FREQUENCY);

			if (null != cursor) {
				Log.e(TAG, "getAllBandFreqInfo::moveToFirst=" + cursor.moveToFirst());
			}

			if (null != cursor && cursor.moveToFirst()) {
				do {
					int sqlFreq = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.FREQUENCY));
					favoriteList.add(sqlFreq);
					Log.i(TAG, "getAllBandFreqInfo::sqlFreq = " + sqlFreq);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return favoriteList;
	}

	// 获取所有预存电台
	public static ArrayList<RadioFreqInfo> getAllPresetFreqInfo(Context context, int band) {
		ArrayList<RadioFreqInfo> favoriteList = new ArrayList<RadioFreqInfo>();
		if (null != context) {

			Cursor cursor = context.getContentResolver().query(RadioStation.Station.CONTENT_URI, RadioStation.COLUMNS,
					RadioStation.Station.IS_PRESET_FREQ + "=? and " + RadioStation.Station.BAND + "=?",
					new String[] { String.valueOf(1), String.valueOf(band) }, RadioStation.Station.FREQUENCY);

			if (null != cursor) {
				Log.e(TAG, "getAllPresetFreqInfo::moveToFirst=" + cursor.moveToFirst());
			}

			if (null != cursor && cursor.moveToFirst()) {
				do {
					int sqlFreq = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.FREQUENCY));
					int sqlband = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.BAND));
					int sqlfavorite = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_FAVORITE));
					int sqlpreset = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_PRESET_FREQ));
					favoriteList.add(new RadioFreqInfo(sqlFreq, sqlband, sqlfavorite, sqlpreset));
					Log.i(TAG, "getAllPresetFreqInfo " + sqlFreq + " - " + sqlband + " - " + sqlfavorite + " "
							+ sqlpreset);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return favoriteList;
	}

	// 获取所有喜爱电台
	public static ArrayList<RadioFreqInfo> getAllFavoriteFreqInfo(Context context, int band) {
		ArrayList<RadioFreqInfo> favoriteList = new ArrayList<RadioFreqInfo>();
		if (null != context) {

			Cursor cursor = context.getContentResolver().query(RadioStation.Station.CONTENT_URI, RadioStation.COLUMNS,
					RadioStation.Station.IS_FAVORITE + "=? and " + RadioStation.Station.BAND + "=?",
					new String[] { String.valueOf(1), String.valueOf(band) }, RadioStation.Station.FREQUENCY);

			if (null != cursor) {
				Log.e(TAG, "moveToFirst=" + cursor.moveToFirst());
			}

			if (null != cursor && cursor.moveToFirst()) {
				do {
					int sqlFreq = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.FREQUENCY));
					int sqlband = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.BAND));
					int sqlfavorite = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_FAVORITE));
					int sqlpreset = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_PRESET_FREQ));
					favoriteList.add(new RadioFreqInfo(sqlFreq, sqlband, sqlfavorite, sqlpreset));
					Log.i(TAG, "getAllFavoriteFreqInfo " + sqlFreq + " - " + sqlband + " - " + sqlfavorite + " "
							+ sqlpreset);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return favoriteList;
	}

	// 获取所有喜爱电台,不分FM和AM
	public static ArrayList<RadioFreqInfo> getAllFavoriteFreqInfoList(Context context) {
		ArrayList<RadioFreqInfo> favoriteList = new ArrayList<RadioFreqInfo>();
		if (null != context) {

			Cursor cursor = context.getContentResolver().query(RadioStation.Station.CONTENT_URI, RadioStation.COLUMNS,
					RadioStation.Station.IS_FAVORITE + "=?", new String[] { String.valueOf(1) },
					RadioStation.Station.FREQUENCY);

			if (null != cursor) {
				Log.e(TAG, "moveToFirst=" + cursor.moveToFirst());
			}

			if (null != cursor && cursor.moveToFirst()) {
				do {
					int sqlFreq = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.FREQUENCY));
					// int sqlband =
					// cursor.getInt(cursor.getColumnIndex(RadioStation.Station.BAND));
					int sqlfavorite = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_FAVORITE));
					int sqlpreset = cursor.getInt(cursor.getColumnIndex(RadioStation.Station.IS_PRESET_FREQ));
					favoriteList.add(new RadioFreqInfo(sqlFreq, 0, sqlfavorite, sqlpreset));
					Log.i(TAG, "getAllFavoriteFreqInfo " + sqlFreq + " - " + 0 + " - " + sqlfavorite + " " + sqlpreset);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return favoriteList;
	}
}
