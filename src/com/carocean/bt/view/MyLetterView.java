package com.carocean.bt.view;

import com.carocean.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyLetterView extends View {
	public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };

	private int choose = 0;
	private Paint paint = new Paint();
	private int mWidth = 0;
	private int mHeight = 0;

	private int normalTextSize = 16;
	private int normalTextColor;
	private int pressedTextColor;

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

	public MyLetterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public MyLetterView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		normalTextColor = context.getResources().getColor(R.color.white);
		pressedTextColor = context.getResources().getColor(R.color.bright_green);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint = reset(paint);

		int singleHeight = mHeight / b.length;

		for (int i = 0; i < b.length; i++) {
			paint.setColor(normalTextColor);

			if (i == choose) {
				paint.setColor(pressedTextColor);
				paint.setFakeBoldText(true);
			}

			float xPos = (mWidth - paint.measureText(b[i])) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
		}
	}

	private Paint reset(Paint paint) {
		paint.setAntiAlias(true);
		paint.setTextSize(normalTextSize);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		return paint;
	}
	public void setchoose(String c){
		for (int i = 0; i < b.length; i++) {
			if (b[i].equals(c)) {
				choose = i;
				invalidate();
				return;
			}
		}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c]);
					choose = c;
					invalidate();
				}
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c]);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			invalidate();
			break;
		}
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {
		void onTouchingLetterChanged(String s);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = this.getWidth();
		mHeight = this.getHeight();
	}
}
