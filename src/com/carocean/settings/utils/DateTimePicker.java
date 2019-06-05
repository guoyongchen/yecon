package com.carocean.settings.utils;

import java.lang.reflect.Field;

import com.carocean.utils.sLog;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

/***************************** DatePicker&TimePicker ************************************/
/**
 * @ClassName: DateTimePicker
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class DateTimePicker {

	public static void setcolorfortimepickerdivider(TimePicker tp, int color) {
		Field[] myfs = null;
		try {
			myfs = tp.getClass().getDeclaredFields();
			for (Field field : myfs) {
				field.setAccessible(true);
				String name = field.getName();
				sLog.e("datetimedialog", "timepicker:" + name + "=" + "\n");
				if (name.equalsIgnoreCase("mAmPmSpinner") || name.equalsIgnoreCase("mHourSpinner")
						|| name.equalsIgnoreCase("mMinuteSpinner")) {
					NumberPicker pk = (NumberPicker) field.get(tp);
					if (pk != null) {
						Field dvfd = pk.getClass().getDeclaredField("mSelectionDivider");
						dvfd.setAccessible(true);
						dvfd.set(pk, new ColorDrawable(color));
					}
				} else if (name.equalsIgnoreCase("mDivider")) {
					TextView tv = (TextView) field.get(tp);
					if (tv != null) {
						tv.setText("");
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setcolorfordatepickerdivider(DatePicker dp, int color) {
		Field[] myfs = null;
		try {
			myfs = dp.getClass().getDeclaredFields();
			for (Field field : myfs) {
				field.setAccessible(true);
				String name = field.getName();
				sLog.e("datetimedialog", "datepicker:" + name + "=" + "\n");
				if (name.equalsIgnoreCase("mYearSpinner") || name.equalsIgnoreCase("mMonthSpinner")
						|| name.equalsIgnoreCase("mDaySpinner")) {
					NumberPicker pk = (NumberPicker) field.get(dp);
					Field dvfd = pk.getClass().getDeclaredField("mSelectionDivider");
					dvfd.setAccessible(true);
					dvfd.set(pk, new ColorDrawable(color));
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void settextsizefortimepicker(TimePicker tp, float size) {
		Field[] myfs = null;
		try {
			myfs = tp.getClass().getDeclaredFields();
			for (Field field : myfs) {
				field.setAccessible(true);
				String name = field.getName();
				sLog.e("datetimedialog", "timepicker:" + name + "=" + "\n");

				Object ob = field.get(tp);
				if (ob != null) {
					if (ob instanceof ViewGroup) {
						settextsize_viewgroup((ViewGroup) ob, size);
					} else if (ob instanceof View) {
						settextsize_view((View) ob, size);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void settextsizefordatepicker(DatePicker dp, float size) {
		Field[] myfs = null;
		try {
			myfs = dp.getClass().getDeclaredFields();
			for (Field field : myfs) {
				field.setAccessible(true);
				String name = field.getName();
				sLog.e("datetimedialog", "timepicker:" + name + "=" + "\n");

				Object ob = field.get(dp);
				if (ob != null) {
					if (ob instanceof ViewGroup) {
						((ViewGroup) ob).setMinimumWidth(150);
						settextsize_viewgroup((ViewGroup) ob, size);
					} else if (ob instanceof View) {
						settextsize_view((View) ob, size);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void settextsize_viewgroup(ViewGroup vg, float size) {
		for (int i = 0;; i++) {
			Object ob = vg.getChildAt(i);
			if (ob == null) {
				break;
			} else {
				if (ob instanceof ViewGroup) {
					settextsize_viewgroup((ViewGroup) ob, size);
				} else if (ob instanceof View) {
					settextsize_view((View) ob, size);
				}
			}
		}
		Field[] fs = vg.getClass().getDeclaredFields();
		for (Field field : fs) {
			field.setAccessible(true);
			String name = field.getName();
			if (name.contains("Paint")) {
				try {
					Paint pt = (Paint) field.get(vg);
					if (pt != null) {
						pt.setTextSize(size);
						pt.setColor(Color.WHITE);
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static void settextsize_view(View v, float size) {
		if (v instanceof EditText) {
			EditText et = (EditText) v;
			et.setTextSize(size);
			et.setTextColor(Color.WHITE);
			et.setFocusable(false);
			et.setEnabled(false);
		} else if (v instanceof TextView) {
			TextView et = (TextView) v;
			et.setTextSize(size);
		} else if (v instanceof Button) {
			Button et = (Button) v;
			et.setTextSize(size);
		} else if (v instanceof CheckBox) {
			CheckBox et = (CheckBox) v;
			et.setTextSize(size);
		} else {

			Field[] fs = v.getClass().getDeclaredFields();
			for (Field field : fs) {
				field.setAccessible(true);
				String name = field.getName();
				if (name.contains("Paint")) {
					try {
						Paint pt = (Paint) field.get(v);
						pt.setTextSize(size);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
