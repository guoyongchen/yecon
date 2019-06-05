package com.carocean.utils;

import com.carocean.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ListViewItem extends LinearLayout {
	// X轴偏移坐标
	private float mdeviationX = 0;
	// listView高度
	private int mListHeight = 0;
	public ListViewItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListViewItem);
		mdeviationX = typedArray.getFloat(R.styleable.ListViewItem_item_deviation_x, 0);
		mListHeight = typedArray.getInt(R.styleable.ListViewItem_item_list_height, 0);
		typedArray.recycle();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		// 此处获取到的top是每个该控件，也就是每个item的上边缘的值
		int top = getTop();
		// 这个方法应该是根据这个top也就是说item的Y轴位置来计算它的X轴偏移量
		float trans = calculatetrans(top);
		Matrix m = canvas.getMatrix();
		m.postTranslate(trans, 0);
		canvas.concat(m);
		super.dispatchDraw(canvas);
		canvas.restore();
	}

	private float calculatetrans(int top) {
		float result = 0f;
		result = ((float)(mListHeight-top)/getHeight() - 1) * mdeviationX;
		return result;
	}
}
