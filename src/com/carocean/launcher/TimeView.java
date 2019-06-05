package com.carocean.launcher;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class TimeView extends TextView {
	SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd E");
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
			TimeView.this.removeCallbacks(TimeView.this.refresh);
			TimeView.this.postDelayed(TimeView.this.refresh, 100L);
		}
	};
	Runnable refresh = new Runnable() {
		public void run() {
			TimeView.this.setDate();
		}
	};

	public TimeView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TimeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDate();
		IntentFilter localIntentFilter = new IntentFilter("android.intent.action.TIME_SET");
		localIntentFilter.addAction("android.intent.action.TIME_TICK");
		localIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
		localIntentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		getContext().registerReceiver(this.mReceiver, localIntentFilter);
	}

	protected void onDetachedFromWindow() {
		getContext().unregisterReceiver(this.mReceiver);
		super.onDetachedFromWindow();
	}

	void setDate() {
		Calendar localCalendar = Calendar.getInstance();
		setText(this.format.format(localCalendar.getTime()));
	}
}
