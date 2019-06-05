package com.carocean.settings.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import com.carocean.R;
import com.carocean.utils.sLog;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.widget.SimpleAdapter;

/**
 * @ClassName: TimeZoneSet
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class TimeZoneSet {

	public static final String KEY_ID = "id"; // value: String
	public static final String KEY_DISPLAYNAME = "name"; // value: String
	public static final String KEY_GMT = "gmt"; // value: String
	public static final String KEY_OFFSET = "offset"; // value: int
														// (Integer)
	public static final String XMLTAG_TIMEZONE = "timezone";
	public static final int HOURS_1 = 60 * 60000;

	public static TimeZoneSet mInstance;

	public TimeZoneSet(Context context) {

	}

	public static TimeZoneSet getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new TimeZoneSet(context);
		}
		return mInstance;
	}

	public List<HashMap<String, Object>> getZones(Context context) {
		final List<HashMap<String, Object>> myData = new ArrayList<HashMap<String, Object>>();
		final long date = Calendar.getInstance().getTimeInMillis();
		try {
			XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
			while (xrp.next() != XmlResourceParser.START_TAG)
				continue;
			xrp.next();
			while (xrp.getEventType() != XmlResourceParser.END_TAG) {
				while (xrp.getEventType() != XmlResourceParser.START_TAG) {
					if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
						return myData;
					}
					xrp.next();
				}
				if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
					String id = xrp.getAttributeValue(0);
					String displayName = xrp.nextText();
					addItem(myData, id, displayName, date);
				}
				while (xrp.getEventType() != XmlResourceParser.END_TAG) {
					xrp.next();
				}
				xrp.next();
			}
			xrp.close();
		} catch (XmlPullParserException xppe) {
			sLog.e("Ill-formatted timezones.xml file");
		} catch (java.io.IOException ioe) {
			sLog.e("Unable to read timezones.xml file");
		}

		return myData;
	}

	private void addItem(List<HashMap<String, Object>> myData, String id, String displayName, long date) {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_ID, id);
		map.put(KEY_DISPLAYNAME, displayName);
		final TimeZone tz = TimeZone.getTimeZone(id);
		final int offset = tz.getOffset(date);
		final int p = Math.abs(offset);
		final StringBuilder name = new StringBuilder();
		name.append("GMT");

		if (offset < 0) {
			name.append('-');
		} else {
			name.append('+');
		}

		name.append(p / (HOURS_1));
		name.append(':');

		int min = p / 60000;
		min %= 60;

		if (min < 10) {
			name.append('0');
		}
		name.append(min);

		map.put(KEY_GMT, name.toString());
		map.put(KEY_OFFSET, offset);

		myData.add(map);
	}

	/**
	 * Searches {@link TimeZone} from the given {@link SimpleAdapter} object,
	 * and returns the index for the TimeZone.
	 * 
	 * @param adapter
	 *            SimpleAdapter constructed by
	 *            {@link #constructTimezoneAdapter(Context, boolean)}.
	 * @param tz
	 *            TimeZone to be searched.
	 * @return Index for the given TimeZone. -1 when there's no corresponding
	 *         list item. returned.
	 */
	public int getTimeZoneIndex(List<HashMap<String, Object>> list, String defaultId) {
		// final String defaultId = tz.getID();
		final int listSize = list.size();
		for (int i = 0; i < listSize; i++) {
			// Using HashMap<String, Object> induces unnecessary warning.
			final HashMap<?, ?> map = (HashMap<?, ?>) list.get(i);
			final String id = (String) map.get(KEY_ID);
			if (defaultId.equals(id)) {
				// If current timezone is in this list, move focus to it
				return i;
			}
		}
		return -1;
	}

	public static class MyComparator implements Comparator<HashMap<?, ?>> {
		private String mSortingKey;

		public MyComparator(String sortingKey) {
			mSortingKey = sortingKey;
		}

		public void setSortingKey(String sortingKey) {
			mSortingKey = sortingKey;
		}

		public int compare(HashMap<?, ?> map1, HashMap<?, ?> map2) {
			Object value1 = map1.get(mSortingKey);
			Object value2 = map2.get(mSortingKey);

			/*
			 * This should never happen, but just in-case, put non-comparable
			 * items at the end.
			 */
			if (!isComparable(value1)) {
				return isComparable(value2) ? 1 : 0;
			} else if (!isComparable(value2)) {
				return -1;
			}

			return ((Comparable) value1).compareTo(value2);
		}

		private boolean isComparable(Object value) {
			return (value != null) && (value instanceof Comparable);
		}
	}

}
