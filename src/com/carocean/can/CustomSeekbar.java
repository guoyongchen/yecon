package com.carocean.can;

import com.carocean.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CustomSeekbar extends View {
	private final String TAG = "CustomSeekbar";
	private Bitmap mBackground;
	private Bitmap mBackgroundFocus;
	private Drawable mDrawableBKF;
	private Path   mPath = new Path();    
	Bitmap mThumb;
	Paint  mPaint = new Paint();
	private int mCurrentPos = 0;
	private float mSectionWidth = (float)25;
	private float mSectionDis = (float)4.5;
	private Rect mThumbRect = new Rect();
	private OnSelectedChangeListener mOnSelectedChangeListener;
	public CustomSeekbar(Context context) {
		super(context);
	}

	public CustomSeekbar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.can_custom_seekbar);
		Drawable drawable = typedArray.getDrawable(R.styleable.can_custom_seekbar_thumb);
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable colordDrawable = (BitmapDrawable) drawable;
			mThumb = colordDrawable.getBitmap();
			
			if(null != mThumb) {
				mThumbRect.set(0, 0, mThumb.getWidth(), mThumb.getHeight());
			}
		}
		drawable = null;
		drawable = typedArray.getDrawable(R.styleable.can_custom_seekbar_background_n);
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable colordDrawable = (BitmapDrawable) drawable;
			mBackground = colordDrawable.getBitmap();
		}
		drawable = null;
		mDrawableBKF = typedArray.getDrawable(R.styleable.can_custom_seekbar_background_f);
		if (mDrawableBKF instanceof BitmapDrawable) {
			BitmapDrawable colordDrawable = (BitmapDrawable) mDrawableBKF;
			mBackgroundFocus = colordDrawable.getBitmap();
			mDrawableBKF.setBounds(0, 0, mBackgroundFocus.getWidth(), mBackgroundFocus.getHeight());
		}
		
		typedArray.recycle();
		mPaint.setAntiAlias(true);
	}
	private int getIndex(int x) {
		int index = 0;
		//index *(mSectionWidth + mSectionDis)- mSectionDis = x;
		if(null != mThumb) {
			x -= mThumb.getWidth()/2;
		}
		if(x > mSectionWidth) {
			index = (int)((x + mSectionDis)/(mSectionWidth + mSectionDis));
		}
		return index;
	}
	private int getPossition(int index) {
		int posX = 0;
		posX = (int)(index *(mSectionWidth + mSectionDis)- mSectionDis);
		//posX += mSectionWidth/2;

		return posX;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int minimumWidth = getSuggestedMinimumWidth();
		final int minimumHeight = getSuggestedMinimumHeight();
		Log.e("YView", "---minimumWidth = " + minimumWidth + "");
		Log.e("YView", "---minimumHeight = " + minimumHeight + "");
		int width = measureWidth(minimumWidth, widthMeasureSpec);
		int height = measureHeight(minimumHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	private int measureWidth(int defaultWidth, int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		Log.e("YViewWidth", "---speSize = " + specSize + "");
		
		switch (specMode) {
		case MeasureSpec.AT_MOST:
			if(null != mBackground) {
				defaultWidth = (int) mBackground.getWidth() + getPaddingLeft() + getPaddingRight();
			}
			Log.e("YViewWidth", "---speMode = AT_MOST");
			break;
		case MeasureSpec.EXACTLY:
			Log.e("YViewWidth", "---speMode = EXACTLY");
			defaultWidth = specSize;
			break;
		case MeasureSpec.UNSPECIFIED:
			Log.e("YViewWidth", "---speMode = UNSPECIFIED");
			defaultWidth = Math.max(defaultWidth, specSize);
		}
		return defaultWidth;
	}

	private int measureHeight(int defaultHeight, int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		Log.e("YViewHeight", "---speSize = " + specSize + "");

		switch (specMode) {
		case MeasureSpec.AT_MOST:
			if(null != mBackground) {
				defaultHeight = mBackground.getHeight() + getPaddingTop() + getPaddingBottom();
			}
			
			if(null != mThumb) {
				if(defaultHeight < mThumb.getWidth()) {
					defaultHeight += mThumb.getWidth();
				}
			}
			Log.e("YViewHeight", "---speMode = AT_MOST");
			break;
		case MeasureSpec.EXACTLY:
			defaultHeight = specSize;
			Log.e("YViewHeight", "---speSize = EXACTLY");
			break;
		case MeasureSpec.UNSPECIFIED:
			defaultHeight = Math.max(defaultHeight, specSize);
			Log.e("YViewHeight", "---speSize = UNSPECIFIED");

			break;
		}
		return defaultHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int left = 0;
		
		if(null != mBackground) {
			int top = (getHeight() - mBackground.getHeight())/2;
			canvas.drawBitmap(mBackground, left, top, mPaint);
		}
		if(null != mThumb) {
			left = mCurrentPos;
			canvas.drawBitmap(mThumb, left, 0, mPaint);
		}
		
		if(null != mBackgroundFocus) {
			left = mCurrentPos;
			mThumbRect.set(0, 0, mCurrentPos, mBackgroundFocus.getHeight());

			canvas.drawBitmap(mBackgroundFocus, mThumbRect, mThumbRect, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			responseTouch(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			responseTouch(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
		{
			int index = getIndex((int)event.getX());
			
			if(null != mOnSelectedChangeListener) {
				mOnSelectedChangeListener.OnSelectedChange(index);
			}
			int x = getPossition(index);
			responseTouch(x, 0);
		}
			break;
		}
		return true;
	}

	private void responseTouch(float x, float y) {
		mCurrentPos = (int)x;
		invalidate();
	}

	public void setOnSelectedChangeListener(OnSelectedChangeListener response) {
		mOnSelectedChangeListener = response;
	}

	public void setProgress(int progress) {
		int x = getPossition(progress);
		responseTouch(x, 0);
		invalidate();
	}

	public interface OnSelectedChangeListener {
		void OnSelectedChange(int index);
	}
}