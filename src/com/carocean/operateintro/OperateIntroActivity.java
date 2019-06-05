package com.carocean.operateintro;

import com.carocean.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

public class OperateIntroActivity extends Activity{
	private Context mContext;
	private FragmentManager mFragmentManager;
	private OperateIntroMainFragment mOperateIntroMainFragment;
	private OperateIntroChildFragment mOperateIntroChildFragment;
	private Fragment                  mCurrentFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_operateintro);
		mContext = this;

		mFragmentManager = getFragmentManager();
		selectFragment(0);
		
	}
	@Override
	public void onBackPressed() {
		if(mCurrentFragment == mOperateIntroChildFragment) {
			selectFragment(0);
			return ;
		}
		super.onBackPressed();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void HideFragment(FragmentTransaction fragmentTransaction) {
		if (null != mOperateIntroMainFragment) {
			fragmentTransaction.hide(mOperateIntroMainFragment);
		}

		if(null != mOperateIntroChildFragment) {
			fragmentTransaction.hide(mOperateIntroChildFragment);
		}
	}

	private void selectFragment(int index) {
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

		HideFragment(fragmentTransaction);
		switch (index) {
		case 0:
			if (mOperateIntroMainFragment == null) {
				mOperateIntroMainFragment = new OperateIntroMainFragment();
				fragmentTransaction.add(R.id.operate_intro_content, mOperateIntroMainFragment);
			}
			mCurrentFragment = mOperateIntroMainFragment;
			fragmentTransaction.show(mCurrentFragment);
			break;
		case 1:
			if(null == mOperateIntroChildFragment) {
				mOperateIntroChildFragment = new OperateIntroChildFragment();
				fragmentTransaction.add(R.id.operate_intro_content, mOperateIntroChildFragment);
			}
			mCurrentFragment = mOperateIntroChildFragment;
			fragmentTransaction.show(mCurrentFragment);
			break;

		}
		fragmentTransaction.commit();
	}

	public void onItemClick(int index) {
		selectFragment(1);
	}
	
}
