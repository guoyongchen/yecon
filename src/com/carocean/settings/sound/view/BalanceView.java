package com.carocean.settings.sound.view;

import com.carocean.R;
import com.carocean.settings.sound.SoundMethodManager;
import com.carocean.settings.sound.SoundUtils;
import com.carocean.settings.sound.SoundUtils.AUDParaT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * set the background bitmap let the icon on background diff (x,y) to distance
 * with top,left,right,bottom
 * 
 * deside the sound
 * 
 */

public class BalanceView extends View {

	private Paint mPoint;
	private Bitmap bmpPoint, bmpPointP;
	private Bitmap bmpBG;
	private Bitmap bmpRulerV, bmpRulerH;
	private float backGroundHeight, backGroundWidth;
	private float pointX, pointY;
	private float mWidthParent, mHeightParent;
	boolean mShowRuler = false;
	boolean mPress = false;

	// get the values for back front and right or left
	private int mValueX, mValueY;
	int w_ajust, y_ajust;

	SoundMethodManager mAudMethodManager;

	Paint mPaint = null;

	public int[] getValue() {
		int value[] = { mValueX, mValueY };
		return value;
	}

	public BalanceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BalanceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(18);
		mPaint.setStrokeWidth(2);
		mAudMethodManager = SoundMethodManager.getInstance(context);
		initPaints();
	}

	public void changeBalanceValue() {
		if (SoundUtils.mOnBalanceListener != null) {
			SoundUtils.mOnBalanceListener.onChangeBalance(mValueX, mValueY);
		}
		SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()] = mValueX;
		SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()] = mValueY;
	}

	private void initPaints() {
		mPoint = new Paint();
		mPoint.setAntiAlias(true);
		mPoint.setDither(true);//
		// mPoint.setColor(mCircleColor);
		// mPoint.setStrokeWidth(mCircleStrokeWidth);
		// setBackgroundResource(R.drawable.balance_sound_view);
		mPoint.setStyle(Paint.Style.STROKE);
		mPoint.setStrokeJoin(Paint.Join.ROUND);
		mPoint.setStrokeCap(Paint.Cap.ROUND);
		bmpPoint = BitmapFactory.decodeResource(getResources(), R.drawable.setting_sound_balance_point);
		bmpPointP = BitmapFactory.decodeResource(getResources(), R.drawable.setting_sound_balance_point_p);
		bmpBG = BitmapFactory.decodeResource(getResources(), R.drawable.setting_sound_balance_bg);
		if (mShowRuler) {
			bmpRulerV = BitmapFactory.decodeResource(getResources(), R.drawable.setting_sound_balance_ruler_v);
			bmpRulerH = BitmapFactory.decodeResource(getResources(), R.drawable.setting_sound_balance_ruler_h);
		}
		backGroundHeight = bmpBG.getHeight();
		backGroundWidth = bmpBG.getWidth();
		w_ajust = bmpPoint.getWidth() / 2 /* + 44 */;
		y_ajust = bmpPoint.getHeight() / 2 /* + 44 */;
	}

	private void setBalanceValues(float pointX2, float pointY2) {
		// TODO Auto-generated method stub
		mValueX = (int) (40 * (pointX2 - w_ajust) / mWidthParent) - 20;
		mValueY = 20 - (int) ((40 * (pointY2 - y_ajust)) / mHeightParent);
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawPoint(canvas);
	}

	public void addPointX() {
		if (mValueX >= 20)
			return;
		mValueX += 1;
		pointX = getWidth() / 2 + mValueX * mWidthParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

	public void cutPointX() {
		if (mValueX <= -20)
			return;
		mValueX -= 1;
		pointX = getWidth() / 2 + mValueX * mWidthParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

	public void addPointY() {
		if (mValueY <= -20)
			return;
		mValueY -= 1;
		pointY = getHeight() / 2 - mValueY * mHeightParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

	public void cutPointY() {
		if (mValueY >= 20)
			return;
		mValueY += 1;
		pointY = getHeight() / 2 - mValueY * mHeightParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

	@Override
	protected void onFinishInflate() {

		super.onFinishInflate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// will compare to the xml settings
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		// int min = Math.min(width, height);
		// setMeasuredDimension(min, min);
		mWidthParent = width - 2 * w_ajust;
		mHeightParent = height - 2 * y_ajust;

		mValueX = SoundUtils.audio[AUDParaT.BALANCE_X.ordinal()];
		mValueY = SoundUtils.audio[AUDParaT.BALANCE_Y.ordinal()];

		// pointX = getWidth() / 2 + mValueX * mWidthParent / 40;
		// pointY = getHeight() / 2 - mValueY * mHeightParent / 40;
		pointX = width / 2 + mValueX * mWidthParent / 40;
		pointY = height / 2 - mValueY * mHeightParent / 40;

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			mPress = true;
			calculatePointerXYPosition(x, y);
			setBalanceValues(pointX, pointY);
			break;
		case MotionEvent.ACTION_MOVE:
			mPress = true;
			calculatePointerXYPosition(x, y);
			setBalanceValues(pointX, pointY);
			break;

		case MotionEvent.ACTION_UP:
			mPress = false;
			break;
		}
		invalidate();
		return true;
		// super.onTouchEvent(event);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable("PARENT");
		super.onRestoreInstanceState(superState);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable("PARENT", superState);
		return state;
	}

	String strValue;
	int gap = 0;// 56;
	int gap1 = 20;
	int gap2 = 22;

	public void drawPoint(Canvas canvas) {

		if (mShowRuler) {
			canvas.drawBitmap(bmpRulerH, gap1, pointY - bmpRulerH.getHeight() / 2, mPaint);
			canvas.drawBitmap(bmpRulerV, pointX - bmpRulerV.getWidth() / 2, gap1, mPaint);
		}

		mPaint.setColor(Color.GRAY);

		canvas.drawLine(0 + gap, pointY, backGroundWidth - gap, pointY, mPaint);
		canvas.drawLine(pointX, 0 + gap, pointX, backGroundHeight - gap, mPaint);

		if (mShowRuler) {
			mPaint.setColor(Color.WHITE);
			Rect bounds = new Rect();
			strValue = mValueX + "";
			mPaint.getTextBounds(strValue, 0, strValue.length(), bounds);
			canvas.drawText(strValue, pointX - bounds.width() / 2, bounds.height() + gap2, mPaint);

			strValue = mValueY + "";
			mPaint.getTextBounds(strValue, 0, strValue.length(), bounds);
			canvas.drawText(strValue, 0 + gap2, pointY + bounds.height() / 2, mPaint);
		}
		Bitmap bmpThumb;
		if (mPress)
			bmpThumb = bmpPointP;
		else
			bmpThumb = bmpPoint;
		canvas.drawBitmap(bmpThumb, pointX - bmpThumb.getWidth() / 2, pointY - bmpThumb.getHeight() / 2, mPaint);
	}

	private void calculatePointerXYPosition(float x, float y) {
		pointX = x;
		pointY = y;

		if (x < w_ajust) {
			pointX = w_ajust;
		} else if (x > this.getWidth() - w_ajust) {
			pointX = this.getWidth() - w_ajust;
		}

		if (y < y_ajust) {
			pointY = y_ajust;
		} else if (y > this.getHeight() - y_ajust) {
			pointY = this.getHeight() - y_ajust;
		}
	}

	public void onChangeBalanceMode(int value) {
		// TODO Auto-generated method stub
		// sLog.v("onChangeBalanceMode................................getWidth()
		// = " + getWidth());

		switch (value) {
		case 0:
			mValueX = 0;
			mValueY = 0;
			break;
		case 1:
			mValueX = 0;
			mValueY = 20;
			break;
		case 2:
			mValueX = 0;
			mValueY = -20;
			break;
		case 3:
			mValueX = -20;
			mValueY = 0;
			break;
		case 4:
			mValueX = 20;
			mValueY = 0;
			break;
		case 5:
			mValueX = -20;
			mValueY = 20;
			break;
		case 6:
			mValueX = 20;
			mValueY = 20;
			break;
		case 7:
			mValueX = -20;
			mValueY = -20;
			break;
		case 8:
			mValueX = 20;
			mValueY = -20;
			break;

		}

		pointX = getWidth() / 2 + mValueX * mWidthParent / 40;
		pointY = getHeight() / 2 - mValueY * mHeightParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}
	
	public void onChangeBalance(int valueX ,int valueY) {
		mValueX = valueX;
		mValueY = valueY;
		
		pointX = getWidth() / 2 + mValueX * mWidthParent / 40;
		pointY = getHeight() / 2 - mValueY * mHeightParent / 40;
		invalidate();
		mAudMethodManager.mMtksetting.setBalance(mValueX, mValueY);
		changeBalanceValue();
	}

}
