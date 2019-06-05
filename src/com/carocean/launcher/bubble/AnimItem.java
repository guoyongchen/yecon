package com.carocean.launcher.bubble;

import java.util.ArrayList;
import java.util.List;

import com.carocean.launcher.utils.launcherUtils;
import com.carocean.utils.sLog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class AnimItem implements Comparable<AnimItem> {
	int appId;
	int mCurrent;
	int mTarget;
	View mView;
	String pkg;

	public AnimItem(View paramView) {
		this(paramView, null);
	}

	public AnimItem(View paramView, String paramString) {
		mView = paramView;
		pkg = paramString;
	}

	public View getView() {
		return mView;
	}

	public void bringToFront() {
		mView.bringToFront();
	}

	public int compareTo(AnimItem paramAnimItem) {
		return paramAnimItem.mCurrent - mCurrent;
	}

	public List<Animator> getAnims(int paramInt) {
		mTarget = paramInt;
		if (mCurrent == mTarget) {
			return null;
		}
		ArrayList<Animator> localArrayList = new ArrayList<Animator>();
		Attr localAttr = AnimManager.getInstance().getAttr(paramInt);
		View localView1 = mView;
		float[] arrayOfFloat1 = new float[1];
		arrayOfFloat1[0] = (localAttr.x + (localAttr.width - mView.getWidth()) / 2.0F);
		localArrayList.add(ObjectAnimator.ofFloat(localView1, "x", arrayOfFloat1));
		View localView2 = mView;
		float[] arrayOfFloat2 = new float[1];
		arrayOfFloat2[0] = (localAttr.y + (localAttr.height - mView.getHeight()) / 2.0F);
		localArrayList.add(ObjectAnimator.ofFloat(localView2, "y", arrayOfFloat2));
		View localView3 = mView;
		float[] arrayOfFloat3 = new float[1];
		arrayOfFloat3[0] = (localAttr.width / mView.getWidth());
		localArrayList.add(ObjectAnimator.ofFloat(localView3, "scaleX", arrayOfFloat3));
		View localView4 = mView;
		float[] arrayOfFloat4 = new float[1];
		arrayOfFloat4[0] = (localAttr.height / mView.getHeight());
		localArrayList.add(ObjectAnimator.ofFloat(localView4, "scaleY", arrayOfFloat4));
		sLog.d("localAttr.x:" + localAttr.x + " localAttr.y:" + localAttr.y);
		sLog.d("mView.getWidth():" + mView.getWidth() + "mView.getHeight():" + mView.getHeight());
		return localArrayList;
	}

	public void setAttr(Attr paramAttr) {
		if (mView == null) {
			return;
		}
		mView.setX(paramAttr.x + (paramAttr.width - mView.getWidth()) / 2.0F);
		mView.setY(paramAttr.y + (paramAttr.height - mView.getHeight()) / 2.0F);
		mView.setScaleX(paramAttr.width / mView.getWidth());
		mView.setScaleY(paramAttr.height / mView.getHeight());
	}

	public void setCurrent(int paramInt) {
		mCurrent = paramInt;
		Attr localAttr = AnimManager.getInstance().getAttr(0);
		if ((mView instanceof TextView)) {
			((TextView) mView).setTextSize(0, 48 * mView.getWidth() / localAttr.width);
		}
	}

	public void setTarget(int paramInt) {
		mTarget = paramInt;
	}

	public void startActivity(Context context) {
		if (pkg.equalsIgnoreCase("navi")) {
			launcherUtils.startNavi();
		} else if (pkg.equalsIgnoreCase("radio")) {
			launcherUtils.startRadio();
		} else if (pkg.equalsIgnoreCase("media")) {
			launcherUtils.startMedia();
		} else if (pkg.equalsIgnoreCase("bt")) {
			launcherUtils.startBT();
		} else if (pkg.equalsIgnoreCase("settings")) {
			launcherUtils.startSettings();
		} else if (pkg.equalsIgnoreCase("screenlink")) {
			launcherUtils.startScreenLink();
		}
	}

	public void udPkg(String paramString) {
		pkg = paramString;
	}
}
