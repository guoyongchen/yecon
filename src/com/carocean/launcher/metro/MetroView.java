package com.carocean.launcher.metro;

import com.carocean.R;
import com.carocean.page.SimpleAnimtionListener;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MetroView extends FrameLayout {

	AnimatorSet mAnimatorSet;

	public MetroView(Context paramContext) {
		super(paramContext);
	}

	public MetroView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public MetroView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	AnimatorSet setAminmator(final boolean bValue) {
		float f1 = 1.0F;
		float f2 = 0.8F;
		if (!bValue) {
			f2 = 1.0F;
		}
		float f3 = getScaleX();
		long l = (long) (200.0F * (f1 * Math.abs(f2 - f3)) / 0.2F);
		Object[] arrayOfObject = new Object[2];
		arrayOfObject[0] = Float.valueOf(f2);
		arrayOfObject[1] = Float.valueOf(f3);
		AnimatorSet localAnimatorSet = new AnimatorSet();
		Animator[] arrayOfAnimator = new Animator[2];
		arrayOfAnimator[0] = ObjectAnimator.ofFloat(this, "scaleX", new float[] { f2 });
		arrayOfAnimator[1] = ObjectAnimator.ofFloat(this, "scaleY", new float[] { f2 });

		localAnimatorSet.playTogether(arrayOfAnimator);
		localAnimatorSet.setDuration(l);
		localAnimatorSet.addListener(new SimpleAnimtionListener() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if ((!bValue) && (MetroView.this.getScaleX() == 1.0F)) {
					performClick();
					super.onAnimationEnd(arg0);
				}
			}
		});

		localAnimatorSet.start();
		return localAnimatorSet;
	}

	void handlePressed(final boolean paramBoolean) {
		if (mAnimatorSet != null) {
			if (mAnimatorSet.isRunning()) {
				mAnimatorSet.cancel();
			}
			mAnimatorSet = null;
		}
		mAnimatorSet = setAminmator(paramBoolean);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		getParent().requestDisallowInterceptTouchEvent(true);

		switch (arg0.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handlePressed(true);
			break;
		case MotionEvent.ACTION_UP:
			handlePressed(false);
			break;

		default:
			break;
		}
		return true;
		// return super.onTouchEvent(arg0);
	}

	@Override
	public void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		super.setPressed(pressed);
		TextView localTextView = (TextView) findViewById(R.id.item_title);
		if (localTextView != null) {
			localTextView.setPressed(pressed);
		}
	}

	public void setup(int paramInt1, int paramInt2) {
		View localView = findViewById(R.id.item_icon);
		if (localView != null) {
			localView.setBackgroundResource(paramInt1);
		}
		TextView localTextView = (TextView) findViewById(R.id.item_title);
		if (localTextView != null) {
			localTextView.setClickable(false);
			localTextView.setText(paramInt2);
		}
		setLongClickable(false);
	}
}
