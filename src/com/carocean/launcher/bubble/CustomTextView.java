package com.carocean.launcher.bubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class CustomTextView extends TextView {

	private Bitmap bitmap;

	public CustomTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return (vaild(event)) && (super.dispatchTouchEvent(event));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return (vaild(event)) && (super.onTouchEvent(event));
	}

	public void upData(int resid) {
		setText(resid);
	}

	public boolean vaild(MotionEvent event) {
		if (event.getAction() == 0) {
			int i = (int) event.getX();
			int j = (int) event.getY();
			int k = getWidth();
			int m = getHeight();
			Drawable localDrawable = getBackground().getCurrent();
			this.bitmap = Bitmap.createBitmap(k, m, Bitmap.Config.ARGB_8888);
			Canvas localCanvas = new Canvas(this.bitmap);
			localDrawable.setBounds(0, 0, k, m);
			localDrawable.draw(localCanvas);
			if ((this.bitmap == null) || (i < 0) || (j < 0) || (i >= k) || (j >= m)) {
			}
			while (this.bitmap.getPixel(i, j) == 0) {
				return false;
			}
		}
		return true;
	}
}
