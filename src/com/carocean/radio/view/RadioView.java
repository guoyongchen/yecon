package com.carocean.radio.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RadioView extends View {
	private ArrayList<Integer> mDataList = new ArrayList<Integer>();

	private Paint mScaleLinePaint;// 刻度画笔
	private TextPaint mScaleTextPaint;// 刻度值画笔
	private final int mScaleLineColor = Color.WHITE;// 刻度颜色
	private final int mScaleTextColor = Color.WHITE;// 刻度文字颜色
	private int mProgress = 0;
	private boolean mAm = false;

	private RadioViewCallBack mRadioViewCallBack;

	public void setCallBack(RadioViewCallBack callBack) {
		mRadioViewCallBack = callBack;
	}

	public RadioView(Context context) {
		super(context);
	}

	public RadioView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	protected void onDraw(Canvas canvas) {
		drawScale(canvas);
	}

	private void initPaint() {

		mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mScaleLinePaint.setColor(mScaleLineColor);
		mScaleLinePaint.setStrokeCap(Paint.Cap.ROUND);
		mScaleLinePaint.setStyle(Paint.Style.STROKE);

		mScaleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mScaleTextPaint.setColor(mScaleTextColor);
		mScaleTextPaint.setTypeface(Typeface.DEFAULT);
		mScaleTextPaint.setTextSize(30);
	}

	public void initFmData() {
		mAm = false;
		mProgress = 0;
		if (!mDataList.isEmpty()) {
			mDataList.clear();
		}
		for (int i = 8750; i <= 10800; i += 10) {
			mDataList.add(i);
		}
		invalidate();
	}

	public void initAmData() {
		mAm = true;
		mProgress = 0;
		if (!mDataList.isEmpty()) {
			mDataList.clear();
		}
		for (int i = 531; i <= 1629; i += 9) {
			mDataList.add(i);
		}
		invalidate();
	}

	// 画刻度
	private void drawScale(Canvas canvas) {

		if (mDataList.isEmpty()) {
			return;
		}
		ArrayList<Integer> dataList = new ArrayList<Integer>();
		final int len = mDataList.size();
		int indexNum = mProgress - 13;

		for (int i = 0; i < len; i++, indexNum++) {
			if (indexNum >= 0) {
				indexNum = indexNum > (len - 1) ? 0 : indexNum;
				dataList.add(mDataList.get(indexNum));
			} else {
				final int size = len + indexNum;
				dataList.add(mDataList.get(size));
			}
		}

		for (int i = 0; i < dataList.size(); i++) {
			float startX = -20;
			float startY = i * 23;
			float endX = 120;
			float endY = startY;
			boolean bandFlag = false;

			switch (i) {
			case 1:
				startX = 4;
				break;
			case 2:
				startX = 18;
				break;
			case 3:
				startX = 23;
				break;
			case 4:
				startX = 28;
				break;
			case 5:
				startX = 30;
				break;
			case 6:
				startX = 34;
				break;
			case 7:
				startX = 38;
				break;
			case 8:
				startX = 42;
				break;
			case 9:
				startX = 44;
				break;
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
				startX = 45;
				break;
			case 16:
				startX = 43;
				break;
			case 17:
				startX = 41;
				break;
			case 18:
				startX = 44;
				break;
			case 19:
				startX = 40;
				break;
			case 20:
				startX = 42;
				break;
			case 21:
				startX = 38;
				break;
			case 22:
				startX = 35;
				break;
			case 23:
				startX = 32;
				break;
			case 24:
				startX = 30;
				break;
			}

			if (mAm) {
				if (dataList.get(i) == 1620) {
					bandFlag = false;
				} else {
					bandFlag = dataList.get(i) % 90 == 0;
				}
			} else {
				if (dataList.get(i) == 8800) {
					bandFlag = false;
				} else {
					bandFlag = dataList.get(i) % 100 == 0;
				}
			}

			if (bandFlag) {
				endX = 160;
				mScaleLinePaint.setAlpha((int) (255 * 0.65));
				mScaleLinePaint.setStrokeWidth(2);

				// 绘制频率文本
				canvas.save();
				String mScaleText = Float.toString(dataList.get(i) / 100.F);
				if (mAm) {
					mScaleText = Integer.toString(dataList.get(i));
				}
				canvas.drawText(mScaleText, endX + 50, endY + 13, mScaleTextPaint);
				canvas.restore();

			} else {

				switch (i) {
				case 0:
				case 24:
					mScaleLinePaint.setAlpha((int) (255 * 0.1));
					break;

				case 1:
				case 23:
					mScaleLinePaint.setAlpha((int) (255 * 0.15));
					break;

				case 2:
				case 22:
					mScaleLinePaint.setAlpha((int) (255 * 0.2));
					break;

				case 3:
				case 21:
					mScaleLinePaint.setAlpha((int) (255 * 0.25));
					break;

				case 4:
				case 20:
					mScaleLinePaint.setAlpha((int) (255 * 0.3));
					break;

				case 5:
				case 19:
					mScaleLinePaint.setAlpha((int) (255 * 0.35));
					break;

				case 6:
				case 18:
					mScaleLinePaint.setAlpha((int) (255 * 0.4));
					break;

				case 7:
				case 17:
					mScaleLinePaint.setAlpha((int) (255 * 0.45));
					break;

				case 8:
				case 16:
					mScaleLinePaint.setAlpha((int) (255 * 0.45));
					break;

				case 9:
				case 15:
					mScaleLinePaint.setAlpha((int) (255 * 0.5));
					break;

				case 10:
				case 14:
					mScaleLinePaint.setAlpha((int) (255 * 0.55));
					break;

				case 11:
				case 13:
					mScaleLinePaint.setAlpha((int) (255 * 0.6));
					break;

				case 12:
					mScaleLinePaint.setAlpha((int) (255 * 0.65));
					break;

				default:
					mScaleLinePaint.setColor(mScaleTextColor);
					mScaleLinePaint.setStrokeWidth(2);
					break;
				}
			}

			canvas.save();
			canvas.drawLine(startX, startY, startX + endX, endY, mScaleLinePaint);
			canvas.restore();
		}

	}

	public void perv() {
		final int size = mDataList.size() - 1;
		mProgress = --mProgress < 0 ? size : mProgress;
		if (null != mRadioViewCallBack && !mDataList.isEmpty()) {
			mRadioViewCallBack.prevProgress(mDataList.get(mProgress));
		}
		invalidate();
	}

	public void next() {
		final int size = mDataList.size() - 1;
		mProgress = ++mProgress > size ? 0 : mProgress;
		if (null != mRadioViewCallBack && !mDataList.isEmpty()) {
			mRadioViewCallBack.nextProgress(mDataList.get(mProgress));
		}
		invalidate();
	}

	public void resetProgress() {

		if (null != mRadioViewCallBack) {
			mRadioViewCallBack.resetProgress();
		}

		mProgress = 0;
		invalidate();
	}

	public void setProgress(int freq) {
		if (!mDataList.isEmpty()) {
			mProgress = mDataList.indexOf(freq);
		}
		invalidate();
	}

	private float mDownY = 0, mMoveY = 0;

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mDownY = event.getY();
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			mMoveY = event.getY();
			if (mDownY > mMoveY) {
				if (((mDownY - mMoveY) > 30)) {
					next();
					mDownY = mMoveY;
				}

			} else {
				if ((mMoveY - mDownY) > 30) {
					perv();
					mDownY = mMoveY;
				}
			}
		}
		return true;
	}
}
