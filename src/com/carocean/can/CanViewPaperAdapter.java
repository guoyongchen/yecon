package com.carocean.can;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class CanViewPaperAdapter extends PagerAdapter {
	List<View> mChildList;

	CanViewPaperAdapter(List<View> list) {
		mChildList = list;
	}

	@Override
	public int getCount() {
		return mChildList.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		position %= mChildList.size();
		if (position < 0) {
			position = mChildList.size() + position;
		}
		View view = mChildList.get(position);

		ViewParent vp = view.getParent();
		if (vp != null) {
			ViewGroup parent = (ViewGroup) vp;
			parent.removeView(view);
		}
		container.addView(view);

		return view;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {

	}
}