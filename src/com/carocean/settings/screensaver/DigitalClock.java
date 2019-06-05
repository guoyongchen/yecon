/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carocean.settings.screensaver;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.settings.utils.timeUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class DigitalClock extends LinearLayout implements OnClickListener {
	private static final String TAG = "DigitalClock";
	private LinearLayout mContent;
	private boolean TIME_USE_BMP = false;

	private Canvas mTimeCanvas;
	private Paint mPaint;
	private Bitmap mTimeBitmap;
	private Bitmap mMaoHaoBitmap;
	private List<Bitmap> mNumBitmaps;
	private final int mGap = 2;
	private int[] mImageResArray = { R.drawable.launcher_widget_clock_t0, R.drawable.launcher_widget_clock_t1, R.drawable.launcher_widget_clock_t2,
			R.drawable.launcher_widget_clock_t3, R.drawable.launcher_widget_clock_t4, R.drawable.launcher_widget_clock_t5,
			R.drawable.launcher_widget_clock_t6, R.drawable.launcher_widget_clock_t7, R.drawable.launcher_widget_clock_t8,
			R.drawable.launcher_widget_clock_t9 };
	int mTime_w, mTime_h;
	TextView tv_week, tv_date, tv_time, tv_time_tips;
	ImageView iv_time;

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// final String action = intent.getAction();
			updateDateTime(context);
		}
	};

	public DigitalClock(Context context) {
		this(context, null);

	}

	public DigitalClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public DigitalClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mNumBitmaps = new ArrayList<Bitmap>();
		mMaoHaoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.launcher_widget_clock_maohao);
		for (int i = 0; i < mImageResArray.length; i++) {
			mNumBitmaps.add(BitmapFactory.decodeResource(getResources(), mImageResArray[i]));
		}

		mTime_w = (mNumBitmaps.get(0).getWidth() + mGap) * 4 + mMaoHaoBitmap.getWidth();
		mTime_h = mNumBitmaps.get(0).getHeight();

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setFilterBitmap(false);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		getContext().registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		resetLayout();
	}

	void resetLayout() {
		// mContent.removeAllViewsInLayout();
		Context context = getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.launcher_widget_clock_digital_layout, null);
		mContent = this;
		mContent.addView(view);

		tv_week = (TextView) mContent.findViewById(R.id.id_week);
		tv_date = (TextView) mContent.findViewById(R.id.id_date);

		iv_time = (ImageView) mContent.findViewById(R.id.iv_time);
		iv_time.setScaleType(ImageView.ScaleType.MATRIX);
		tv_time = (TextView) mContent.findViewById(R.id.id_time);
		tv_time_tips = (TextView) mContent.findViewById(R.id.id_time_tips);

		if (TIME_USE_BMP) {
			iv_time.setVisibility(View.VISIBLE);
			tv_time.setVisibility(View.GONE);
			tv_time_tips.setVisibility(View.GONE);
		} else {
			iv_time.setVisibility(View.GONE);
			tv_time.setVisibility(View.VISIBLE);

		}
		updateDateTime(context);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch (arg0.getId()) {

		// case R.id.id_datetime:
		// Function.onSetDatetime(getContext());
		// break;

		default:
			break;
		}
	}

	private void updateDateTime(Context context) {
		String time = timeUtils.getCurrentTime(context);// timeUtils.getHourMinute(timeUtils.is24HourFormat(context));
		String timetips = timeUtils.getCurrentTimeTips(context);
		String date = timeUtils.getCurrentDate(mContext);
		String week = timeUtils.getCurrentWeek(context);
		tv_date.setText(date);
		tv_week.setText(week);

		if (TIME_USE_BMP) {
			if (mTimeBitmap == null) {
				mTimeBitmap = Bitmap.createBitmap(mTime_w, mTime_h, Bitmap.Config.ARGB_8888);
				mTimeCanvas = new Canvas(mTimeBitmap);
			}

			iv_time.setImageBitmap(getTimeBitmap(mTimeBitmap, timeUtils.getHourMinute(context), context));
		} else {
			tv_time.setText(time);
			tv_time_tips.setText(timetips);
		}
		Log.i(TAG, "\ntime:" + time + " date:" + date + " date:" + week);
	}

	public Bitmap getTimeBitmap(Bitmap bitmap, String[] times, Context context) {
		if (bitmap != null) {
			mTimeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			// mTimeCanvas.drawColor(Color.BLACK);
			Bitmap[] bitmaps = getBitmapsByTime(times);
			int width[] = new int[bitmaps.length];
			int height[] = new int[bitmaps.length];
			for (int i = 0; i < bitmaps.length; i++) {

				width[i] = bitmaps[i].getWidth();
				height[i] = bitmaps[i].getHeight();

				int l = 0;

				if (i == 0) {
					l = 0;
				} else {
					for (int m = 0; m < i; m++) {
						l += width[m] + mGap;
					}
				}

				int t = 0;
				int r = l + width[i];
				int b = height[i] - t;

				Rect rect = new Rect(l, t, r, b);
				mTimeCanvas.drawBitmap(bitmaps[i], null, rect, mPaint);
			}
		}
		return bitmap;
	}

	private Bitmap[] getBitmapsByTime(String[] hour_minute) {
		String timeStr = hour_minute[0] + "a" + hour_minute[1];

		Bitmap[] bitmaps = new Bitmap[5];
		int num = 0;
		for (int i = 0; i < bitmaps.length; i++) {
			try {
				num = Integer.valueOf(String.valueOf(timeStr.charAt(i)));
				bitmaps[i] = mNumBitmaps.get(num);
			} catch (Exception e) {
				bitmaps[i] = mMaoHaoBitmap;
			}
		}
		return bitmaps;
	}

}
