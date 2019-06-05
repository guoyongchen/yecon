package com.carocean.media;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class HorizontalScrollTextView extends TextView {
    private float textLength = 0f;// 文本长度
    private float step = 0f;// 文本的横坐标
    private float y = 0f;// 文本的纵坐标
    public boolean isStarting = false;// 是否开始滚动
    private TextPaint mMyPaint = null;
    private String text = "";// 文本内容
    private OnScrollCompleteListener onScrollCompleteListener;//滚动结束监听

    public HorizontalScrollTextView(Context context) {
        super(context);
    }

    public HorizontalScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OnScrollCompleteListener getOnSrollCompleteListener() {
        return onScrollCompleteListener;
    }

    public void setOnScrollCompleteListener(OnScrollCompleteListener  onScrollCompleteListener){
        this.onScrollCompleteListener = onScrollCompleteListener;
    }
    public void init() {
    	mMyPaint = getPaint();
        text = getText().toString();
        textLength = mMyPaint.measureText(text);
        y = getTextSize() + getPaddingTop();
    }
    
    @Override
    protected void drawableStateChanged() {
    	super.drawableStateChanged();
    	mMyPaint.setColor(getCurrentTextColor());
    	invalidate();
    }
    
    //开启滚动
    public void startScroll() {
        if (textLength >= 480) {
            isStarting = true;
            invalidate();
        }
    }
    //停止滚动
    public void stopScroll() {
        isStarting = false;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
    	mMyPaint.drawableState = getDrawableState();
        if (!isStarting) {
            canvas.drawText(text,  0, y + 22, mMyPaint);
            return;
        }

        canvas.drawText(text,  - step, y + 22, mMyPaint);

        step += 0.3;// 2.0为文字的滚动速度
        //判断是否滚动结束
        if (step > textLength){
            step = 0;
            if (onScrollCompleteListener != null) {
                onScrollCompleteListener.onScrollComplete();
            }
        }
        invalidate();
    }

    public void setTextColor(int color) {
    	
    }
    
    public interface OnScrollCompleteListener {
        void onScrollComplete();
    }
}
