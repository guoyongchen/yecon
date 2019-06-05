package com.carocean.vmedia.setting;

import java.util.Locale;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.page.SimpleAnimtionListener;
import com.carocean.utils.Constants;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vsettings.balance.PageBalance;
import com.carocean.vsettings.common.PageCommon;
import com.carocean.vsettings.display.PageDisplay;
import com.carocean.vsettings.sound.PageSound;
import com.carocean.vsettings.theme.PageTheme;
import com.carocean.vsettings.time.PageTime;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;
import com.carocean.vsettings.volume.PageSetVolume;
import com.carocean.vsettings.wallpaper.PageWallPaper;

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
import android.widget.ImageView;

public class PageSetting implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {

	public static ViewGroup vPage;
	public static Context mContext;
	private ViewGroup mRootView;
	private static PageSetting mPageSetting = null;

	private final IPage PAGE_COMMON = new PageCommon();
	private final IPage PAGE_TIME = new PageTime();
	private final IPage PAGE_DISPLAY = new PageDisplay();
	private final IPage PAGE_WALLPAPER = new PageWallPaper();
	private final IPage PAGE_THEME = new PageTheme();
	private final IPage PAGE_SOUND = new PageSound();
	private final IPage PAGE_BALANCE = new PageBalance();
	private final IPage PAGE_SETVOLUME = new PageSetVolume();

	AnimatorSet mAnimSet;
	private View mPageContent;
	private View mNextPageContent;
	private static int mCurPageid = R.id.page_common;

	private static int mAtePage = -1;

	final int ID_BUTTON[] = { R.id.page_common, R.id.page_time, R.id.page_display, R.id.page_wallpaper, R.id.page_theme,
			R.id.page_sound, R.id.page_balance, R.id.page_set_volume };
	Button mButton[] = new Button[ID_BUTTON.length];
	final int ID_DRAWABLE_N[] = { R.drawable.setting_ic_common_n, R.drawable.setting_ic_time_n,
			R.drawable.setting_ic_display_n, R.drawable.setting_ic_wallpaper_n, R.drawable.setting_ic_theme_n,
			R.drawable.setting_ic_sound_n, R.drawable.setting_ic_balance_n, R.drawable.setting_ic_time_n };
	final int ID_DRAWABLE_P[] = { R.drawable.setting_ic_common_p, R.drawable.setting_ic_time_p,
			R.drawable.setting_ic_display_p, R.drawable.setting_ic_wallpaper_p, R.drawable.setting_ic_theme_p,
			R.drawable.setting_ic_sound_p, R.drawable.setting_ic_balance_p, R.drawable.setting_ic_time_p };

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(launcherUtils.START_THEME_ACTION)) {
				onClick(mRootView.findViewById(R.id.page_theme));
			} else if (action.equals(launcherUtils.START_WALLPAPER_ACTION)) {
				onClick(mRootView.findViewById(R.id.page_wallpaper));
			} else if (action.equals(launcherUtils.START_SET_VOLUME_ACTION)) {
				onClick(mRootView.findViewById(R.id.page_set_volume));
			} else if (action.equals(launcherUtils.BACK_PAGECOMMOM_ACTION)) {
				onClick(mRootView.findViewById(R.id.page_common));
			}
		}
	};

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_main, null));
			init(context);
			initView(mRootView);

			IntentFilter filter = new IntentFilter();
			filter.addAction(launcherUtils.START_THEME_ACTION);
			filter.addAction(launcherUtils.START_WALLPAPER_ACTION);
			filter.addAction(launcherUtils.BACK_PAGECOMMOM_ACTION);
			filter.addAction(launcherUtils.START_SET_VOLUME_ACTION);
			mContext.registerReceiver(mBroadcastReceiver, filter);
			Animation localAnimation = AnimationUtils.loadAnimation(mContext, R.anim.app_start);
			vPage.startAnimation(localAnimation);
		} else {
			if (mCurPageid != 0 && mAtePage != -1 &&  mAtePage != mCurPageid) {
				refreshModUI(mAtePage, mRootView);
				pageAnim(mAtePage);
			} else {
				fixPage(mCurPageid);
				mCurPageid = mAtePage;
				refreshModUI(mCurPageid, mRootView);
			}
		}
		return mRootView;
	}

	void init(Context context) {
		mPageSetting = this;
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
		mButton[0].setBackgroundResource(ID_DRAWABLE_P[0]);
		if(mAtePage != -1) {
			mCurPageid = mAtePage;
		}
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
		case R.id.page_common:
			return PAGE_COMMON;
		case R.id.page_time:
			return PAGE_TIME;
		case R.id.page_display:
			return PAGE_DISPLAY;
		case R.id.page_wallpaper:
			return PAGE_WALLPAPER;
		case R.id.page_theme:
			return PAGE_THEME;
		case R.id.page_sound:
			return PAGE_SOUND;
		case R.id.page_balance:
			return PAGE_BALANCE;
		case R.id.page_set_volume:
			return PAGE_SETVOLUME;
		default:
			return PAGE_COMMON;
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
		case R.id.view_page_language:
			PageCommon.vPageCommon.setVisibility(View.VISIBLE);
			PageCommon.vPageLanguage.setVisibility(View.GONE);
			PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			PageCommon.vPageLanguage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			PageCommon.isLanguage = false;
			break;
		case R.id.view_page_wifi:
			PageCommon.vPageCommon.setVisibility(View.VISIBLE);
			PageCommon.vPageWifi.setVisibility(View.GONE);
			PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			PageCommon.vPageWifi.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			PageCommon.isWifi = false;
			break;
		case R.id.view_page_info:
			PageCommon.vPageCommon.setVisibility(View.VISIBLE);
			PageCommon.vPageInfo.setVisibility(View.GONE);
			PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			PageCommon.vPageInfo.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			PageCommon.isVersion = false;
			break;
		case R.id.view_page_factory:
			PageCommon.vPageCommon.setVisibility(View.VISIBLE);
			PageCommon.vPageFactory.setVisibility(View.GONE);
			PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			PageCommon.vPageFactory.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			PageCommon.isFactory = false;
			break;
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
		case R.id.page_common:
			mButton[0].setBackgroundResource(ID_DRAWABLE_P[0]);
			mButton[2].setBackgroundResource(ID_DRAWABLE_N[2]);
			mButton[5].setBackgroundResource(ID_DRAWABLE_N[5]);
			mButton[6].setBackgroundResource(ID_DRAWABLE_N[6]);
			if (PageCommon.isLanguage) {
				vView2vCommon(R.id.view_page_language);
			} else if (PageCommon.isWifi) {
				vView2vCommon(R.id.view_page_wifi);
			} else if (PageCommon.isVersion) {
				vView2vCommon(R.id.view_page_info);
			} else if (PageCommon.isFactory) {
				vView2vCommon(R.id.view_page_factory);
			}
			break;
		case R.id.page_time:
			break;
		case R.id.page_display:
			mButton[0].setBackgroundResource(ID_DRAWABLE_N[0]);
			mButton[2].setBackgroundResource(ID_DRAWABLE_P[2]);
			mButton[5].setBackgroundResource(ID_DRAWABLE_N[5]);
			mButton[6].setBackgroundResource(ID_DRAWABLE_N[6]);
			break;
		case R.id.page_wallpaper:
			break;
		case R.id.page_theme:
			break;
		case R.id.page_sound:
			mButton[0].setBackgroundResource(ID_DRAWABLE_N[0]);
			mButton[2].setBackgroundResource(ID_DRAWABLE_N[2]);
			mButton[5].setBackgroundResource(ID_DRAWABLE_P[5]);
			mButton[6].setBackgroundResource(ID_DRAWABLE_N[6]);
			break;
		case R.id.page_balance:
			mButton[0].setBackgroundResource(ID_DRAWABLE_N[0]);
			mButton[2].setBackgroundResource(ID_DRAWABLE_N[2]);
			mButton[5].setBackgroundResource(ID_DRAWABLE_N[5]);
			mButton[6].setBackgroundResource(ID_DRAWABLE_P[6]);
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
	
	public static void setPageId(int page) {
		if(page == 10001) {
			mAtePage = R.id.page_sound;
		} else if(page == 10002) {
			mAtePage = R.id.page_balance;
		} else if(page == 10003) {
			mAtePage = R.id.page_common;
		}
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}
}
