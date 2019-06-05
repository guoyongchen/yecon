package com.carocean.vmedia.t19can;

import java.util.Locale;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.page.SimpleAnimtionListener;
import com.carocean.t19can.T19CanRx.CarStatusInfo;
import com.carocean.t19can.T19CanRx.EnergyInfo;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.time.PageTime;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class PageCarInfo implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {

	public static ViewGroup vPage;
	public static Context mContext;
	private ViewGroup mRootView;
	private static PageCarInfo mPageCarInfo = null;

	private final IPage PAGE_CARSTATUS = new PageCarStatus();
	private final IPage PAGE_ENERGY = new PageEnergy();
	private final IPage PAGE_CARHELP = new PageCarHelp();

	AnimatorSet mAnimSet;
	private View mPageContent;
	private View mNextPageContent;
	private int mCurPageid;

	final int ID_BUTTON[] = {  R.id.page_energy, R.id.page_carhelp,R.id.page_carstatus };
	Button mButton[] = new Button[ID_BUTTON.length];
	final int ID_DRAWABLE_N[] = {  R.drawable.caninfo_energy_n,
			R.drawable.caninfo_assistance_n,R.drawable.caninfo_carstatus_n };
	final int ID_DRAWABLE_P[] = { R.drawable.caninfo_energy_d,
			R.drawable.caninfo_assistance_d,R.drawable.caninfo_carstatus_d};

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_carinfo, null));
			init(context);
			initView(mRootView);

			Animation localAnimation = AnimationUtils.loadAnimation(mContext, R.anim.app_start);
			vPage.startAnimation(localAnimation);

		} else {
			if ("is_setTime".equals(MediaActivity.mActivity.switchType)) {
				onClick(mRootView.findViewById(R.id.page_carhelp));
			} else if ("is_carInfo".equals(MediaActivity.mActivity.switchType)) {
//				onClick(mRootView.findViewById(R.id.page_carstatus));
				onClick(mRootView.findViewById(R.id.page_energy));
			} else {
				onClick(mRootView.findViewById(R.id.page_energy));
			}
		}

		return mRootView;
	}

	void init(Context context) {
		mPageCarInfo = this;
	}

	void initView(ViewGroup root) {
		boolean bChangeTextSize = false;
		Locale curLocale = mContext.getResources().getConfiguration().locale;
		if (curLocale.equals(Locale.SIMPLIFIED_CHINESE) || curLocale.equals(Locale.TAIWAN)) {
			bChangeTextSize = true;
		}

		int i = 0;
		for (Button btn : mButton) {
			mButton[i] = (Button) root.findViewById(ID_BUTTON[i]);
			mButton[i].setOnClickListener(this);
			if (bChangeTextSize) {
				mButton[i].setTextSize(32.0f);
				mButton[i].setPadding(65, 0, 0, 0);
			}
			i++;
		}

		vPage = (ViewGroup) root.findViewById(R.id.page_view);

		if ("is_setTime".equals(MediaActivity.mActivity.switchType)) {
			mCurPageid = R.id.page_carhelp;
		} else if ("is_carInfo".equals(MediaActivity.mActivity.switchType)) {
//			mCurPageid = R.id.page_carstatus;
			mCurPageid = R.id.page_energy;
		} else {
			mCurPageid = R.id.page_energy;
		}

		mButton[0].setBackgroundResource(ID_DRAWABLE_P[0]);
		fixPage(mCurPageid);
		refreshModUI(mCurPageid, root);
	}

	private void fixPage(int rid) {
		resetPageContent();
		vPage.removeAllViews();
		mPageContent = getPage(rid).getContentView(mContext, true);
		vPage.addView(mPageContent);
		if (mCurPageid != rid) {
			getPage(mCurPageid).removeNotify();
			getPage(rid).addNotify();
			mCurPageid = rid;
		}
	}

	void refreshModUI(int rid, ViewGroup root) {

		for (int i = 0; i < mButton.length; i++) {
			if (rid == ID_BUTTON[i]) {
				((Button) root.findViewById(ID_BUTTON[i])).setBackgroundResource(ID_DRAWABLE_P[i]);
			} else {
				((Button) root.findViewById(ID_BUTTON[i])).setBackgroundResource(ID_DRAWABLE_N[i]);
			}
		}
	}

	private IPage getPage(int paramInt) {
		switch (paramInt) {
		case R.id.page_carstatus:
//			return PAGE_CARSTATUS;
			return PAGE_ENERGY;
		case R.id.page_energy:
			return PAGE_ENERGY;
		case R.id.page_carhelp:
			return PAGE_CARHELP;
		default:
			return PAGE_ENERGY;
		}
	}

	private void resetPageContent() {
		if (mPageContent != null) {
			mPageContent.setVisibility(View.VISIBLE);
			mPageContent.setAnimation(null);
		}
		if (mNextPageContent != null) {
			mNextPageContent.setVisibility(View.VISIBLE);
			mNextPageContent.setAnimation(null);
			mNextPageContent = null;
		}
	}

	private void pageAnim(int rid) {
		mPageContent = getPage(mCurPageid).getContentView(mContext, false);
		mNextPageContent = getPage(rid).getContentView(mContext, true);

		vPage.removeAllViews();
		vPage.addView(mPageContent);
		vPage.addView(mNextPageContent);
		doAnim(mPageContent, mNextPageContent, mCurPageid, rid);
	}

	private void doAnim(final View view1, View view2, int rid1, final int rid2) {
		if ((mAnimSet != null) && (mAnimSet.isRunning())) {
			mAnimSet.cancel();
		}

		mAnimSet = new AnimatorSet();
		final int i = 0;
		int j = 559;
		Log.d("demo", "paramView1.getLeft():" + view1.getLeft() + "...paramView1.getRight():" + view1.getWidth());
		Animator[] arrayOfAnimator = new Animator[4];
		// view1.setScaleX(1.0F);
		// view1.setScaleY(1.0F);
		// view1.setAlpha(1.0F);
		view2.setScaleX(1.0F);
		view2.setScaleY(1.0F);
		view2.setAlpha(1.0F);
		if (rid2 < rid1) {
			arrayOfAnimator[0] = ObjectAnimator.ofFloat(view1, "scaleX", new float[] { 1.0F, 0.5F });
			arrayOfAnimator[1] = ObjectAnimator.ofFloat(view1, "scaleY", new float[] { 1.0F, 0.5F });
			arrayOfAnimator[2] = ObjectAnimator.ofFloat(view1, "alpha", new float[] { 1.0F, 0.2F });
			arrayOfAnimator[3] = ObjectAnimator.ofFloat(view2, "x", new float[] { j, i });
		} else {
			arrayOfAnimator[0] = ObjectAnimator.ofFloat(view2, "scaleX", new float[] { 0.5F, 1.0F });
			arrayOfAnimator[1] = ObjectAnimator.ofFloat(view2, "scaleY", new float[] { 0.5F, 1.0F });
			arrayOfAnimator[2] = ObjectAnimator.ofFloat(view2, "alpha", new float[] { 0.2F, 1.0F });
			arrayOfAnimator[3] = ObjectAnimator.ofFloat(view1, "x", new float[] { i, j });
		}
		mAnimSet.playTogether(arrayOfAnimator);

		mAnimSet.addListener(new SimpleAnimtionListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				vPage.removeView(mPageContent);
				if (mCurPageid != rid2) {
					getPage(mCurPageid).removeNotify();
					getPage(rid2).addNotify();
					mCurPageid = rid2;
				}
				super.onAnimationCancel(arg0);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if (view1 != null) {
					view1.setX(i);
				}
				vPage.removeView(mPageContent);
				if (mCurPageid != rid2) {
					getPage(mCurPageid).removeNotify();
					getPage(rid2).addNotify();
					mCurPageid = rid2;
				}
				super.onAnimationEnd(arg0);
			}

		});
		mAnimSet.setDuration(400L);
		mAnimSet.start();
	}

	private void vView2vCommon(int rid) {
		switch (rid) {
		// case R.id.view_page_language:
		// PageCommon.vPageCommon.setVisibility(View.VISIBLE);
		// PageCommon.vPageLanguage.setVisibility(View.GONE);
		// PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_start));
		// PageCommon.vPageLanguage.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_end));
		// PageCommon.isLanguage = false;
		// break;
		// case R.id.view_page_wifi:
		// PageCommon.vPageCommon.setVisibility(View.VISIBLE);
		// PageCommon.vPageWifi.setVisibility(View.GONE);
		// PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_start));
		// PageCommon.vPageWifi.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_end));
		// PageCommon.isWifi = false;
		// break;
		// case R.id.view_page_info:
		// PageCommon.vPageCommon.setVisibility(View.VISIBLE);
		// PageCommon.vPageInfo.setVisibility(View.GONE);
		// PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_start));
		// PageCommon.vPageInfo.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_end));
		// PageCommon.isVersion = false;
		// break;
		// case R.id.view_page_factory:
		// PageCommon.vPageCommon.setVisibility(View.VISIBLE);
		// PageCommon.vPageFactory.setVisibility(View.GONE);
		// PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_start));
		// PageCommon.vPageFactory.setAnimation(AnimationUtils.loadAnimation(mContext,
		// R.anim.app_end));
		// PageCommon.isFactory = false;
		// break;
		default:
			break;
		}

	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.page_carstatus:
			break;
		case R.id.page_energy:
			break;
		case R.id.page_carhelp:
			break;
		default:
			break;
		}
		if (arg0.getId() != mCurPageid) {
			refreshModUI(arg0.getId(), mRootView);
			pageAnim(arg0.getId());
		}

	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
