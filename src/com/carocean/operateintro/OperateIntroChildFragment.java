package com.carocean.operateintro;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OperateIntroChildFragment extends Fragment implements OnPageChangeListener {
	private ViewPager mViewPaper;
	private TextView mHintTv;
	private List<View> mPageList;
	private int mCurrentPage = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_operateintro_child, container, false);
		initViews(view);
		return view;
	}

	private void initViews(View parent) {
		if (null == parent) {
			return;
		}
		mHintTv = (TextView) parent.findViewById(R.id.operate_intro_page_hint);
		mViewPaper = (ViewPager) parent.findViewById(R.id.operate_intro_viewpaper);
		mViewPaper.setOnPageChangeListener(this);
		mPageList = new ArrayList<View>();
		for (int i = 0; i < 8; i++) {
			TextView child = (TextView) getActivity().getLayoutInflater()
					.inflate(R.layout.activity_operateintro_icon_view, null);
			String title = "the " + Integer.toString(i + 1) + " page";
			child.setText(title);
			mPageList.add(child);
		}
		mPageList.add(0, mPageList.get(mPageList.size() - 1));
		mPageList.add(mPageList.size(), mPageList.get(0));
		OperateIntroViewPaperAdapter adapter = new OperateIntroViewPaperAdapter(mPageList);
		mViewPaper.setAdapter(adapter);
		setCurrentItem(1);

	}

	private void setCurrentItem(int item) {
		if (null != mViewPaper) {
			mViewPaper.setCurrentItem(item);
		}
	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	public void onPageSelected(int arg0) {
		mCurrentPage = arg0;

		if (0 == arg0) {
			arg0 = mPageList.size() - 2;
		} else if (mPageList.size() - 1 == arg0) {
			arg0 = 1;
		}
		String hint = String.valueOf(arg0) + "/" + String.valueOf(mPageList.size() - 2);
		mHintTv.setText(hint);
	}

	public void onPageScrollStateChanged(int arg0) {
		if (arg0 != ViewPager.SCROLL_STATE_IDLE) {
			return;
		}

		if (0 == mCurrentPage) {
			mViewPaper.setCurrentItem(mPageList.size() - 2, false);

		} else if (mCurrentPage == mPageList.size() - 1) {
			mViewPaper.setCurrentItem(1, false);
		}

	}
}
