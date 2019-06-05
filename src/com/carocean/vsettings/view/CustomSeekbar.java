package com.carocean.vsettings.view;

import com.carocean.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @ClassName: CustomSeekbar
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.04.24
 **/
public class CustomSeekbar extends View {

	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;
	private int mOrientation = HORIZONTAL;
	private int ICON_THUMB_W = 200;
	private int ICON_THUMB_H = 40;
	public int SCREEN_LEFT = 0;
	public int SCREEN_TOP = 0;
	private int SEEKBAR_H;
	private int SEEKBAR_MAX = 100;
	private int SEEKBAR_W;

	BitmapDrawable mBackgroundDrawable;
	BitmapDrawable mProgressDrawable;
	BitmapDrawable mThumbDrawable[] = new BitmapDrawable[2];

	boolean mPress = false;

	private int mCurProgress = 0;
	private int mTargetW = 0;
	private int mTargetH = 0;
	private Paint paint;
	public float precent;

	public boolean isSliding = true;

	public CustomSeekbar(Context context) {
		super(context);
	}

	public CustomSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	public int correntSeekbarValue(int value) {
		if (value > SEEKBAR_MAX)
			value = SEEKBAR_MAX;
		else if (value < 0)
			value = 0;
		return value;
	}

	public void init(Context context, AttributeSet attrs) {
		if (null != attrs) {
			TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar);
			mBackgroundDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_background);
			mProgressDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_progress);
			if (mProgressDrawable == null)
				mProgressDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_background);
			mThumbDrawable[0] = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_thumb_n);
			mThumbDrawable[1] = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_thumb_p);
			if (mThumbDrawable[1] == null)
				mThumbDrawable[1] = (BitmapDrawable) typedArray.getDrawable(R.styleable.CustomSeekBar_csb_thumb_n);
			mOrientation = typedArray.getInt(R.styleable.CustomSeekBar_csb_orientation, HORIZONTAL);
			SEEKBAR_MAX = typedArray.getInt(R.styleable.CustomSeekBar_csb_max, SEEKBAR_MAX);
			typedArray.recycle();
		}
		ICON_THUMB_W = mThumbDrawable[0].getBitmap().getWidth();
		ICON_THUMB_H = mThumbDrawable[0].getBitmap().getHeight();
		SEEKBAR_W = mBackgroundDrawable.getBitmap().getWidth();
		SEEKBAR_H = mBackgroundDrawable.getBitmap().getHeight();
		setProgressMax(SEEKBAR_MAX);
		// setProgress(7);

		paint = new Paint();
		paint.setSubpixelText(true);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setColor(Color.WHITE);
		paint.setTextSize(17.0F);
		paint.setTextAlign(Paint.Align.CENTER);

	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		canvas.clipRect(SCREEN_LEFT, SCREEN_TOP, SCREEN_LEFT + SEEKBAR_W, SCREEN_TOP + SEEKBAR_H);
		canvas.drawBitmap(mBackgroundDrawable.getBitmap(), SCREEN_LEFT, SCREEN_TOP, paint);
		canvas.restore();
		canvas.save();
		if (mOrientation == HORIZONTAL) {
			canvas.clipRect(SCREEN_LEFT, SCREEN_TOP, SCREEN_LEFT + mTargetW + ICON_THUMB_W / 2, SCREEN_TOP + SEEKBAR_H);
		} else {
			canvas.clipRect(SCREEN_LEFT, SCREEN_TOP + mTargetH + ICON_THUMB_H / 2, SCREEN_LEFT + SEEKBAR_W,
					SCREEN_TOP + SEEKBAR_H);
		}
		canvas.drawBitmap(mProgressDrawable.getBitmap(), SCREEN_LEFT, SCREEN_TOP, paint);
		canvas.restore();
		canvas.save();
		if (mPress) {
			if (mOrientation == HORIZONTAL) {
				canvas.drawBitmap(mThumbDrawable[1].getBitmap(), SCREEN_LEFT + mTargetW, SCREEN_TOP, paint);
			} else {
				canvas.drawBitmap(mThumbDrawable[1].getBitmap(), SCREEN_LEFT, SCREEN_TOP + mTargetH, paint);
			}

		} else {
			if (mOrientation == HORIZONTAL) {
				canvas.drawBitmap(mThumbDrawable[0].getBitmap(), SCREEN_LEFT + mTargetW, SCREEN_TOP, paint);
			} else {
				canvas.drawBitmap(mThumbDrawable[0].getBitmap(), SCREEN_LEFT, SCREEN_TOP + mTargetH, paint);
			}
		}
		canvas.restore();
	}

	public boolean onTouchEvent(MotionEvent event) {
		int i = (int) event.getX();
		int j = (int) event.getY();

		// Log.d("demo", "...event.getX()..." + i + "...event.getY()..." + j);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getParent().requestDisallowInterceptTouchEvent(true);
		case MotionEvent.ACTION_MOVE:
			mPress = true;
			break;
		case MotionEvent.ACTION_UP:
			getParent().requestDisallowInterceptTouchEvent(false);
			mPress = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}
		int progress;
		if (mOrientation == HORIZONTAL) {
			progress = (int) ((i - SCREEN_LEFT) / precent);
		} else {
			progress = (int) ((j - SCREEN_TOP) / precent);
		}
		setProgress(progress);

		return true;
	}

	public void setProgress(int pos) {
		int i;
		pos = correntSeekbarValue(pos);
		mCurProgress = pos;
		if (mOrientation == HORIZONTAL) {
			i = (int) (pos * precent);
			if (mTargetW != i) {
				mTargetW = i;
				if (null != mOnProgressListener && mPress) {
					mOnProgressListener.onChanged(this);
				}
			}
		} else {
			pos = SEEKBAR_MAX - pos;
			i = (int) (pos * precent);
			if (mTargetH != i) {
				mTargetH = i;
				if (mPress) {
					mCurProgress = pos;
					if (null != mOnProgressListener) {
						mOnProgressListener.onChanged(this);
					}
				}
			}
		}
		invalidate();
	}

	public void setProgressMax(int value) {
		SEEKBAR_MAX = value;
		if (mOrientation == HORIZONTAL)
			precent = ((float) (SEEKBAR_W - ICON_THUMB_W) / SEEKBAR_MAX);
		else
			precent = ((float) (SEEKBAR_H - ICON_THUMB_H) / SEEKBAR_MAX);
	}

	/**
	 * Register a callback to be invoked when the progress changes.
	 *
	 * @param listener
	 *            the callback to call on progress change
	 */

	public int getProgress() {
		return mCurProgress;
	}

	public void setSliding(boolean isSliding) {
		this.isSliding = isSliding;
	}

	private OnProgressChangedListener mOnProgressListener;

	public void setOnProgressChangedListener(OnProgressChangedListener listener) {
		mOnProgressListener = listener;
	}

	public interface OnProgressChangedListener {
		void onChanged(CustomSeekbar seekBar);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (isSliding) {
			super.dispatchTouchEvent(event);
		}
		return true;
	}
}
