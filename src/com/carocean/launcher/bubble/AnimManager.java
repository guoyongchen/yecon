package com.carocean.launcher.bubble;

import java.util.ArrayList;
import java.util.List;

import com.carocean.page.SimpleAnimtionListener;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

public class AnimManager {
	static AnimManager instance;
	AnimatorSet mAnimatorSet = null;
	List<Animator> mAnims = new ArrayList<Animator>();
	public SparseArray<Attr> mAttrs = new SparseArray<Attr>();
	int mFront = 0;
	Handler mHandler = new Handler(Looper.getMainLooper());
	public List<AnimItem> mItems = new ArrayList<AnimItem>();

	public static AnimManager getInstance() {
		if (null == instance) {
			instance = new AnimManager();
		}
		return instance;
	}

	public static AnimManager initialize() {
		if (instance == null) {
			instance = new AnimManager();
		}
		return instance;
	}

	public Attr getAttr(int paramInt) {
		if (mAttrs.indexOfKey(paramInt) >= 0) {
			return mAttrs.get(paramInt);
		}
		return null;
	}

	public AnimItem getmItems(int paramInt) {
		return mItems.get(paramInt);
	}

	public void initAttr(int paramInt, Attr paramAttr) {
		mAttrs.put(paramInt, paramAttr);
	}

	public void initItem(int paramInt, AnimItem paramAnimItem) {
		mItems.add(paramInt, paramAnimItem);
	}

	public void restart() {
		int i = mItems.size();
		for (int j = 0;; j++) {
			if (j >= i) {
				return;
			}
			AnimItem localAnimItem = mItems.get(j);
			Attr localAttr = mAttrs.get(j);
			if ((localAnimItem != null) && (localAttr != null)) {
				localAnimItem.setAttr(localAttr);
			}
		}
	}

	void swap(int paramInt1, int paramInt2) {
		if (paramInt1 == paramInt2) {
			return;
		}
		AnimItem localAnimItem1 = mItems.get(paramInt1);
		AnimItem localAnimItem2 = mItems.get(paramInt2);
		mItems.set(paramInt2, localAnimItem1);
		localAnimItem1.setCurrent(paramInt2);
		mItems.set(paramInt1, localAnimItem2);
		localAnimItem2.setCurrent(paramInt1);
	}

	public void toFront(int paramInt) {
		toFront(paramInt, null);
	}

	public void toFront(int paramInt, final Runnable paramRunnable) {
		if (mFront == paramInt) {
			if (paramRunnable != null) {
				paramRunnable.run();
				return;
			}
		}

		if (mAnimatorSet != null) {
			if (mAnimatorSet.isRunning()) {
				mAnimatorSet.cancel();
			}
			mAnimatorSet = null;
		}

		mAnims.clear();
		ArrayList<AnimItem> localArrayList = new ArrayList<AnimItem>(mItems);
		List<Animator> localList1 = localArrayList.get(paramInt).getAnims(mFront);
		if ((localList1 != null) && (localList1.size() > 0)) {
			mAnims.addAll(localList1);
		}
		List<Animator> localList2 = localArrayList.get(mFront).getAnims(paramInt);
		if ((localList2 != null) && (localList2.size() > 0)) {
			mAnims.addAll(localList2);
		}

		Log.d("demo", "......paramInt:" + paramInt + "......mFront:" + mFront + "......mAnims.size():" + mAnims.size());

		swap(paramInt, mFront);
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playTogether(mAnims);
		if (paramRunnable != null) {
			mAnimatorSet.addListener(new SimpleAnimtionListener() {
				public void onAnimationEnd(Animator paramAnonymousAnimator) {
					super.onAnimationEnd(paramAnonymousAnimator);
					paramRunnable.run();
				}
			});
		}
		mAnimatorSet.setDuration(300L);
		mAnimatorSet.start();
		final Attr localAttr = AnimManager.getInstance().getAttr(0);
		for (int i = mItems.size() - 1; i >= 0; i--) {
			final AnimItem localAnimItem = (AnimItem) mItems.get(i);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					((TextView) localAnimItem.getView()).setTextSize(0,
							48.01F * localAnimItem.getView().getWidth() / localAttr.width);
					localAnimItem.bringToFront();
				}
			}, 50L);
		}
	}
}
