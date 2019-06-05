package com.carocean.can;

import java.util.ArrayList;
import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class PMGraphaView extends View {
    private float lineSmoothness = 0.3f;
    private List<Point> mPointList;
    private List<Integer> mPmValueList = new ArrayList<Integer>();
    private Path mPath = new Path();
    private Path mAssistPath = new Path();
    private float drawScale = 1f;
    private int mColor = 0x616161;
    private PathMeasure mPathMeasure;

    public PMGraphaView(Context context) {
        super(context);
    }

    public PMGraphaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public int getPoints() {
    	return 11;
    }
    public void setPointList(List<Point> pointList) {
        mPointList = pointList;
        measurePath();
    }
    public void setPmValue(int value) {
        if(getPoints() == mPmValueList.size()) {
        	mPmValueList.remove(0);
        }
        mPmValueList.add(value);
        invalidate();
    }

    public void setLineSmoothness(float lineSmoothness) {
        if (lineSmoothness != this.lineSmoothness) {
            this.lineSmoothness = lineSmoothness;
            measurePath();
            postInvalidate();
        }
    }

    public void setDrawScale(float drawScale) {
        this.drawScale = drawScale;
        postInvalidate();
    }

    public void startAnimation(long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "drawScale", 0f, 1f);
        animator.setDuration(duration);
        animator.start();
    }
    private Paint mPaint = new Paint();
    private Path  mDstPath = new Path();
    @Override
    protected void onDraw(Canvas canvas) {
        if (mPointList == null)
            return;
       /* mPaint.reset();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mDstPath.reset();
        mDstPath.rLineTo(0, 0);
        float distance = mPathMeasure.getLength() * drawScale;
        if (mPathMeasure.getSegment(0, distance, mDstPath, true)) {
            canvas.drawPath(mDstPath, mPaint);
            float[] pos = new float[2];
            mPathMeasure.getPosTan(distance, pos, null);
            drawPoint(canvas,pos);
        }*/
        
        if(1 == mPointList.size()) {
        	drawPoint(canvas, mPointList.get(0));
        }else {
        	drawBrokenLine(canvas);
        }
        drawPmValue(canvas);
    }
    private void drawBrokenLine(Canvas canvas) {
    	mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
    	for(int i = 0; i < mPointList.size() - 1; i ++) {
    		float startX = mPointList.get(i).x;
    		float startY = mPointList.get(i).y;
    		float stopX = mPointList.get(i+1).x;
    		float stopY = mPointList.get(i+1).y;
    		canvas.drawLine(startX, startY, stopX, stopY, mPaint);
    	}
    	//canvas.drawPath(mPath, mPaint);
    }
    private void drawPmValue(Canvas canvas) {
    	mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(25);
    	for(int i = 0; i < mPointList.size() - 1; i ++) {
    		String text = String.valueOf(mPmValueList.get(i));
    		int offY = 20;
    		
    		if(mPointList.get(i + 1).y - mPointList.get(i).y > 0) {
    			offY = -5;
    		}
    		canvas.drawText(text, mPointList.get(i).x + 5, mPointList.get(i).y + offY, mPaint);
    	}
    }
    private void drawPoint(Canvas canvas, final float[] pos){
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);
        boolean drawAllPoint = false;
        if(drawAllPoint) {
	        for (Point point : mPointList) {
	            if (point.x > pos[0]) {
	                break;
	            }
	            canvas.drawCircle(point.x, point.y, 10, mPaint);
	        }
        }else {
        	Point point = mPointList.get(mPointList.size() - 1);
        	canvas.drawCircle(point.x, point.y, 10, mPaint);
        }
    }
    private void drawPoint(Canvas canvas, Point point){
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(point.x, point.y, 10, mPaint);
    }
    private void measurePath() {
        mPath.reset();
        mAssistPath.reset();
        
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;

        final int lineSize = mPointList.size();
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            if (Float.isNaN(currentPointX)) {
                Point point = mPointList.get(valueIndex);
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX)) {
                if (valueIndex > 0) {
                    Point point = mPointList.get(valueIndex - 1);
                    previousPointX = point.x;
                    previousPointY = point.y;
                } else {
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                if (valueIndex > 1) {
                    Point point = mPointList.get(valueIndex - 2);
                    prePreviousPointX = point.x;
                    prePreviousPointY = point.y;
                } else {
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }
            if (valueIndex < lineSize - 1) {
                Point point = mPointList.get(valueIndex + 1);
                nextPointX = point.x;
                nextPointY = point.y;
            } else {
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                mPath.moveTo(currentPointX, currentPointY);
                mAssistPath.moveTo(currentPointX, currentPointY);
            } else {
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (lineSmoothness * firstDiffX);
                final float firstControlPointY = previousPointY + (lineSmoothness * firstDiffY);
                final float secondControlPointX = currentPointX - (lineSmoothness * secondDiffX);
                final float secondControlPointY = currentPointY - (lineSmoothness * secondDiffY);
                mPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
                mAssistPath.lineTo(firstControlPointX, firstControlPointY);
                mAssistPath.lineTo(secondControlPointX, secondControlPointY);
                mAssistPath.lineTo(currentPointX, currentPointY);
            }
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
        mPathMeasure = new PathMeasure(mPath, false);
    }

}
