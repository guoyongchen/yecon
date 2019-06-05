package com.carocean.utils;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @ClassName: MarqueeTextView
 * @Description: TODO
 * @author: LZY
 * @date: 2018.04.24
 **/
public class MarqueeTextView extends TextView {
	public MarqueeTextView(Context paramContext) {
		super(paramContext);
		init();
	}

	public MarqueeTextView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init();
	}

	public MarqueeTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		init();
	}

	private void init() {
		setSingleLine();
		setEllipsize(TextUtils.TruncateAt.MARQUEE);
		setMarqueeRepeatLimit(-1);
	}

	public boolean isFocused() {
		return true;
	}
}