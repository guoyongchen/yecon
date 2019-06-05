
package com.carocean.vmedia;

import static android.constant.YeconConstants.ACTION_YECON_KEY_UP;

import java.util.Locale;

import com.carocean.R;
import com.carocean.bt.BTUtils;
import com.carocean.bt.BTService;
import com.carocean.bt.Callinfo;
import com.carocean.launcher.popupwindow.SettingPopupWindow;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.page.SimpleAnimtionListener;
import com.carocean.utils.Constants;
import com.carocean.vmedia.allapp.PageAllApp;
import com.carocean.vmedia.bt.PageBT;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;
import com.carocean.vmedia.radio.PageRadio;
import com.carocean.vmedia.setting.PageSetting;
import com.carocean.vmedia.t19can.PageCarHelp;
import com.carocean.vmedia.t19can.PageCarInfo;
import com.carocean.vsettings.common.PageCommon;
import com.carocean.vsettings.wifi.PageWifi;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mcu.McuExternalConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * @ClassName: MainActivity
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class MediaActivity extends Activity implements OnClickListener, OnTouchListener {
	String TAG = "MediaActivity";
	public static MediaActivity  mActivity;
	public static ViewGroup vPage;
	// 增加一个控件，在动画切换时 覆盖在页面上，从而禁止掉底下的按钮切换
	private View vPageGround;
	public String switchType="is_carInfo";
	private Context mContext;
	private final IPage PAGE_MEDIA = new PageMedia();
	private final IPage PAGE_RADIO = new PageRadio();
	private final IPage PAGE_BT = new PageBT();
	private final IPage PAGE_ALLAPP = new PageAllApp();
	private final IPage PAGE_SETTING = new PageSetting();
	private final IPage Page_CARINFO = new PageCarInfo();

	public static boolean bforeground = false; 
	AnimatorSet mAnimSet;
	private View mPageContent;
	private View mNextPageContent;
	private int mCurPageid;
	private int mDestPageid;

	View mViewPageGroup;

	final int ID_BUTTON[] = { R.id.page_navi, R.id.page_media, R.id.page_radio, R.id.page_bt, R.id.page_allapp,
			R.id.page_setting, R.id.page_carinfo };
	Button mButton[] = new Button[ID_BUTTON.length];

	final int ID_DRAWABLE_P[] = { R.drawable.launcher_ic_navi, R.drawable.launcher_ic_media,
			R.drawable.launcher_ic_radio, R.drawable.launcher_ic_bt, R.drawable.launcher_ic_app,
			R.drawable.launcher_ic_setting, R.drawable.launcher_ic_can };

	private float mY = 0;
	private FrameLayout bottom_fr;
	private SettingPopupWindow mSettingPopupWindow;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(launcherUtils.START_SETTINGS_ACTION)) {
				showpage(R.id.page_setting);
			} else if(Constants.ACTION_REQUEST_SRC.equals(action)) {
				int srcId = Constants.SOURCE_MAX;
				if(isBtPage()){
					srcId = Constants.TYPE_BT;
				} else if(isRadioPage()) {
					srcId = Constants.TYPE_RADIO;
				} else if (isMediaPage()) {
					switch (PageMedia.getView()) {
					case ViewUsbMusic:
					case ViewUsbMusicFile:
						srcId = Constants.TYPE_AUDIO;
						break;
					case ViewBtMusic:
						srcId = Constants.TYPE_BT_MUSIC;
						break;
					case ViewVideo:
					case ViewVideoFile:
						srcId = Constants.TYPE_VIDEO;
						break;
					case ViewPicture:
					case ViewPictureFile:
						srcId = Constants.TYPE_PHOTO;
						break;
					default:
						break;
					}
				}
				Intent replyIntent = new Intent(Constants.ACTION_REPLY_SRC);
				replyIntent.putExtra(Constants.SRC_ID, srcId);
				sendBroadcast(replyIntent);
			}
		}
	};


	Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();
				if (action.equals(ACTION_YECON_KEY_UP)) {
					Log.e(TAG, "ACTION_YECON_KEY_UP");
		        	int keyCode = intent.getIntExtra("key_code", 0);
		            if (keyCode == KeyEvent.KEYCODE_YECON_PHONE_ON) {
		            	checkbtcallpage();
		            }
				} else if (action.equals(BTService.ACTION_CALL_STATE)) {
					Log.e(TAG, "bforeground=" + bforeground);
					int status = intent.getIntExtra(BTService.EXTRA_STATE, 0);
					if (status == Callinfo.STATUS_TERMINATE) {
						Log.e(TAG, "BTService.bback=" + BTService.bback);
						if (BTService.bback) {
							moveTaskToBack(true);
							BTService.bback = false;
						}
					}
					checkbtcallpage();
				} 
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate page_navi=" + R.id.page_navi + " page_media=" + R.id.page_media + " page_radio=" + R.id.page_radio
				 + " page_bt=" + R.id.page_bt + " page_allapp=" + R.id.page_allapp);
		setContentView(R.layout.vlauncher_layout_main);
		initData();
		initView();
		if (mSettingPopupWindow == null) {
			mSettingPopupWindow = new SettingPopupWindow(mContext);
		}
		Animation localAnimation = AnimationUtils.loadAnimation(this, R.anim.app_start);
		vPage.startAnimation(localAnimation);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		BTService.registerNotifyHandler(uiHandler);
	}
	@Override
	protected void onPause() {
		bforeground = false;
		super.onPause();
	}
	
	public boolean isdestpagebt(){
		Log.e(TAG, "isdestpagebt mAnimSet=" + mAnimSet + " isBtPage=" + isBtPage() + " mAnimSet.isRunning=" + (mAnimSet == null ? false : mAnimSet.isRunning())
				+ " anim_dest.getId()=" + anim_dest);
		if (mAnimSet != null && mAnimSet.isRunning()) {
			return anim_dest == R.id.page_bt;
		}else{
			return isBtPage();
		}
	}

	@Override
	protected void onResume() {
		Log.e(TAG, "onResume mCurPageid=" + mCurPageid);
		bforeground = true;
		checkbtcallpage();
		if (BTUtils.mBluetooth.iscallidle()) {
			BTUtils.isMediaActivityMaxPreCall = true;
		}
		super.onResume();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e(TAG, "onNewIntent");
		refreshPageUI(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		getPage(mCurPageid).removeNotify();
		unregisterReceiver(mBroadcastReceiver);
		BTService.unregisterNotifyHandler(uiHandler);
		BTUtils.isMediaActivityMaxPreCall = false;
		super.onDestroy();
	}

	void initData() {
		mActivity = this;
		mContext = this;
		IntentFilter filter = new IntentFilter();
		filter.addAction(launcherUtils.START_SETTINGS_ACTION);
		filter.addAction(Constants.ACTION_REQUEST_SRC);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	void initView() {
		boolean bChangeTextSize = false;
		Locale curLocale = getResources().getConfiguration().locale;
		if (curLocale.equals(Locale.SIMPLIFIED_CHINESE) || curLocale.equals(Locale.TAIWAN)) {
			bChangeTextSize = true;
		}
		for (int i = 0; i < ID_BUTTON.length; i++) {
			Button button = (Button) findViewById(ID_BUTTON[i]);
			button.setOnClickListener(this);
			if (bChangeTextSize) {
				button.setTextSize(20.0f);
				button.setPadding(60, 0, 0, 0);
			}
		}

		mViewPageGroup = (View) findViewById(R.id.view_page_group);
		bottom_fr = (FrameLayout) findViewById(R.id.bottom_fr);
		bottom_fr.setOnTouchListener(this);

		vPage = (ViewGroup) findViewById(R.id.page_view);
		vPageGround = findViewById(R.id.page_view_frogroud);
		vPageGround.setOnClickListener(this);
		vPageGround.setVisibility(View.INVISIBLE);
		Log.i(TAG, "doAnim  initView INVISIBLE");
		Intent intent = getIntent();
		refreshPageUI(intent);

	}

	public void showpage(int iPageid){
		Log.e(TAG, "showpage mCurPageid=" + mCurPageid + " iPageid=" + iPageid);
		mDestPageid = iPageid;
		if (!BTUtils.mBluetooth.iscallidle() && iPageid != R.id.page_bt) {
			return;
		}
		
		if (mCurPageid != 0 && iPageid == mCurPageid) {
			getPage(mCurPageid).onResume();
			return;
		}
		if (mCurPageid == 0) {
			fixPage(iPageid);
			mCurPageid = iPageid;
			if (mCurPageid == R.id.page_bt) {
				((PageBT)getPage(mCurPageid)).onResume();
			}
			refreshModUI(mCurPageid);
		}else{
			refreshModUI(iPageid);
			pageAnim(iPageid);
		}

		// 设置进入设置时默认显示通用样式
		if (PageCommon.isLanguage || PageCommon.isVersion || PageCommon.isFactory || PageCommon.isTheme
				|| PageCommon.isWallpaper) {
			launcherUtils.backPageCommon();
		} else if (PageCommon.isWifi) {
			vView2vCommon(R.id.view_page_wifi);
			if (null != PageWifi.mPopupWindow_connect) {
				PageWifi.mPopupWindow_connect.dismiss();
			}
			if (null != PageWifi.fragment) {
				PageWifi.fragment.dismiss();
			}
		} else if (PageCommon.isRestore) {
			if (null != PageCommon.mPopupWindow_Restore) {
				PageCommon.mPopupWindow_Restore.dismiss();
			}
		}
	}
	
	public void refreshPageUI(Intent intent) {
		String strType = intent.getStringExtra("mediaType");
		switchType = intent.getStringExtra("switchType");
		int iPageid = -1;
		if ("radio".equals(strType)) {
			PageRadio.isSRadio = true;
			iPageid = R.id.page_radio;
		} else if ("bt".equals(strType)) {
			iPageid = R.id.page_bt;
		} else if ("media".equals(strType)) {
			iPageid = R.id.page_media;
		} else if ("allapp".equals(strType)) {
			iPageid = R.id.page_allapp;
		} else if ("setting".equals(strType)) {
			iPageid = R.id.page_setting;
		} else if ("carinfo".equals(strType)) {
			iPageid = R.id.page_carinfo;
		} else {
			iPageid = R.id.page_radio;
		}
		showpage(iPageid);
	}

	void refreshModUI(int rid) {
		for (int i = 0; i < mButton.length; i++) {
			if (rid == ID_BUTTON[i]) {
				mViewPageGroup.setBackgroundResource(ID_DRAWABLE_P[i]);
				break;
			}
		}
	}

	public void checkbtcallpage(){
		Log.e(TAG, "checkbtcallpage mCurPageid=" + mCurPageid + " mPreCallPageid=" + mPreCallPageid + " BTUtils.bkeyphoneon=" + BTUtils.bkeyphoneon);
		if (BTUtils.mBluetooth.iscallidle()) {
			if (mCurPageid == R.id.page_bt && mPreCallPageid != 0) {
				showpage(mPreCallPageid);
				mPreCallPageid = 0;
			}else if (BTUtils.bkeyphoneon) {
				showpage(R.id.page_bt);
				mPreCallPageid = 0;
			}else{
				getPage(mCurPageid).onResume();
			}
		}else{
			if (mCurPageid != R.id.page_bt) {
				mPreCallPageid = mCurPageid;
				showpage(R.id.page_bt);
			}
		}
	}
	
	private int mPreCallPageid = 0;
	@Override
	public void onClick(View arg0) {
		Log.e(TAG, "onClick id=" + arg0.getId() + " mCurPageid=" + mCurPageid + " BTUtils.mBluetooth.iscallidle()=" + BTUtils.mBluetooth.iscallidle());
		 //TODO Auto-generated method stub
		if (!BTUtils.mBluetooth.iscallidle() && arg0.getId() != R.id.page_bt) {
			return;
		}
		if (arg0.getId() == mCurPageid || mAnimSet != null && mAnimSet.isRunning()) {
			return;
		}
		switch (arg0.getId()) {
		case R.id.page_navi:
			launcherUtils.startNavi();
			return;
		case R.id.page_media:
			break;
		case R.id.page_radio:
			break;
		case R.id.page_bt:
			break;
		case R.id.page_allapp:
			break;
		case R.id.page_setting:
			break;
		case R.id.page_carinfo:
			break;
		case R.id.page_view_frogroud:
			return;

		default:
			break;
		}
		showpage(arg0.getId());
	}

	private void fixPage(int rid) {
		Log.e(TAG, "fixPage rid=" + rid + " mCurPageid=" + mCurPageid);
		resetPageContent();
		vPage.removeAllViews();
		mPageContent = getPage(rid).getContentView(this, true);
		vPage.addView(mPageContent);
		if (mCurPageid != rid) {
			getPage(mCurPageid).removeNotify();
			getPage(rid).addNotify();
			mCurPageid = rid;
		}
	}

	private IPage getPage(int paramInt) {
		switch (paramInt) {
		case R.id.page_media:
			return PAGE_MEDIA;
		case R.id.page_radio:
			return PAGE_RADIO;
		case R.id.page_bt:
			return PAGE_BT;
		case R.id.page_allapp:
			return PAGE_ALLAPP;
		case R.id.page_setting:
			return PAGE_SETTING;
		case R.id.page_carinfo:
			return Page_CARINFO;
		default:
			return PAGE_MEDIA;
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
		Log.e(TAG, "pageAnim rid=" + rid + " mCurPageid=" + mCurPageid);
		mPageContent = getPage(mCurPageid).getContentView(this, false);
		mNextPageContent = getPage(rid).getContentView(this, true);

		vPage.removeAllViews();
		vPage.addView(mPageContent);
		vPage.addView(mNextPageContent);
		doAnim(mPageContent, mNextPageContent, mCurPageid, rid);
	}

	public void setenable(View v, boolean bable){
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View v_child = vg.getChildAt(i);
				setenable(v_child, bable);
			}
		}else{
			v.setEnabled(bable);
		}
	}
	boolean cancelevent = false;
	int anim_dest = 0;
	private void doAnim(final View view1,final View view2, int rid1, final int rid2) {
		Log.e(TAG, "doAnim rid1=" + rid1 + " rid2=" + rid2);
		if ((mAnimSet != null) && (mAnimSet.isRunning())) {
			Log.e(TAG, "doAnim anim_dest=" + anim_dest + " rid2=" + rid2);
			if (rid2 != anim_dest) {
				mAnimSet.cancel();
			}else{
				return;
			}
		}
		if (vPageGround != null) {
			vPageGround.setVisibility(View.VISIBLE);
			Log.i(TAG, "doAnim VISIBLE");
		}
		anim_dest = rid2;
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
				Log.e(TAG, "onAnimationCancel rid2=" + rid2 + " mCurPageid=" + mCurPageid);
				// TODO Auto-generated method stub
				vPage.removeView(mPageContent);
		/*		if (mCurPageid != rid2) {
					getPage(mCurPageid).removeNotify();
					getPage(rid2).addNotify();
					mCurPageid = rid2;
				}*/
				cancelevent = true;
				super.onAnimationCancel(arg0);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				Log.e(TAG, "onAnimationEnd rid2=" + rid2 + " mCurPageid=" + mCurPageid);
				// TODO Auto-generated method stub
				if (view1 != null) {
					view1.setX(i);
				}
				vPage.removeView(mPageContent);
				if (mCurPageid != rid2 && !cancelevent) {
					getPage(mCurPageid).removeNotify();
					getPage(rid2).addNotify();
					mCurPageid = rid2;
				}
				if (mCurPageid == R.id.page_bt) {
					((PageBT)getPage(mCurPageid)).onResume();
				}
				cancelevent = false;
				if (vPageGround != null) {
					vPageGround.setVisibility(View.INVISIBLE);
					Log.i(TAG, "doAnim  end INVISIBLE");
				}
				super.onAnimationEnd(arg0);
			}

		});
		mAnimSet.setDuration(400L);
		mAnimSet.start();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if (PageCommon.isLanguage) {
			vView2vCommon(R.id.view_page_language);
		} else if (PageCommon.isWifi) {
			vView2vCommon(R.id.view_page_wifi);
		} else if (PageCommon.isVersion) {
			vView2vCommon(R.id.view_page_info);
		} else if (PageCommon.isFactory) {
			vView2vCommon(R.id.view_page_factory);
		} else if (PageCommon.isTheme) {
			PageCommon.isTheme = false;
			launcherUtils.backPageCommon();
		} else if (PageCommon.isWallpaper) {
			PageCommon.isWallpaper = false;
			launcherUtils.backPageCommon();
		} else if (PageCommon.isStorage) {
			vView2vCommon(R.id.view_page_storage);
		}else if(PageCommon.isSetVolume){
			PageCommon.isSetVolume=false;
			launcherUtils.backPageCommon();
		} else if (PageAllApp.isSettings) {
			PageAllApp.isSettings = false;
			showpage(R.id.page_allapp);
		} else if (PageCarHelp.isSetTime) {
			PageCarHelp.isSetTime = false;
			launcherUtils.startCarHelpSetTime(false);
		} else if (PageRadio.isSRadio) {
			moveTaskToBack(false);
		} else {
			super.onBackPressed();
		}
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
		case R.id.view_page_storage:
			PageCommon.vPageCommon.setVisibility(View.VISIBLE);
			PageCommon.vPageStorage.setVisibility(View.GONE);
			PageCommon.vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			PageCommon.vPageStorage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			PageCommon.isStorage = false;
			break;
		default:
			break;
		}
	}
	
	public boolean isMediaPage() {
		return mDestPageid == R.id.page_media;
	}
	
	public boolean isRadioPage() {
		return mDestPageid == R.id.page_radio;
	}
	
	public boolean isBtPage() {
		return mDestPageid == R.id.page_bt;
	}
	
	public boolean isSettingPage() {
		return mDestPageid == R.id.page_setting;
	}
	
	public boolean isCarInfoPage() {
		return mDestPageid == R.id.page_carinfo;
	}
	
	public void colsePage(int appid) {
		if (isResumed()) {
			if (isMediaPage()) {
				if (appid == Constants.MEDIA_ID 
					|| (appid == Constants.IMAGE_ID && (PageMedia.getView() == ViewType.ViewPicture || PageMedia.getView() == ViewType.ViewPictureFile))
					|| (appid == Constants.MUSIC_ID && (PageMedia.getView() == ViewType.ViewUsbMusic || PageMedia.getView() == ViewType.ViewUsbMusicFile))
					|| (appid == Constants.VIDEO_ID && (PageMedia.getView() == ViewType.ViewVideo || PageMedia.getView() == ViewType.ViewVideoFile))) {
					finish();
				} 
			} else if ((isBtPage() && appid == Constants.BT_ID) 
					|| (isSettingPage() && appid == Constants.SETTING_ID)
					|| (isCarInfoPage() && appid == Constants.CAR_ID)) {
				finish();
			}
		}
	}

	@Override
	public boolean onTouch(View paramView, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mY = event.getY();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float y_move = event.getY();
			if (!"true".equals(SystemProperties.get(McuExternalConstant.PROPERTY_KEY_BTPHONE_STARTUP))) {
				if (mY - y_move > 20) {
					if (mSettingPopupWindow != null) {
						mSettingPopupWindow.setBacklight();
						mSettingPopupWindow.showAtLocation(bottom_fr, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
					}
				}
			}
		}
		return true;
	}
}
