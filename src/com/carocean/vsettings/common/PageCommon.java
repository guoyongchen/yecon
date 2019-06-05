package com.carocean.vsettings.common;

import java.util.Locale;

import com.android.internal.app.LocalePicker;
import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.settings.sound.SoundArray;
import com.carocean.settings.sound.SoundMethodManager;
import com.carocean.settings.sound.SoundUtils;
import com.carocean.settings.sound.SoundUtils.AUDParaT;
import com.carocean.settings.utils.FileCopy;
import com.carocean.settings.utils.PersistUtils;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.settings.utils.SettingUtils;
import com.carocean.settings.utils.tzUtils;
import com.carocean.settings.utils.verUtils;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.view.vHeaderLayout;
import com.carocean.vsettings.view.vHeaderLayout.onOneButtonListener;
import com.carocean.vsettings.view.vHeaderLayout.onOneCheckBoxListener;
import com.carocean.vsettings.view.vHeaderLayout.onTwoButtonListener;
import com.carocean.vsettings.wifi.PageWifi;
import com.yecon.settings.YeconSettings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @ClassName: PageCommon
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageCommon implements IPage, onOneButtonListener, OnClickListener, OnCheckedChangeListener,
		onOneCheckBoxListener, onTwoButtonListener {
	Context mContext;
	private ViewGroup mRootView;
	private ScrollView mScrollView;
	PageWifi mPageWifi;
	public View mPopupView;
	private PopupWindow mPopupWindow_SysInfo;
	public static PopupWindow mPopupWindow_NewSysInfo;
	public static PopupWindow mPopupWindow_Restore;
	char CharSet[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	int ID_BUTTON_NUM[] = { R.id.factory_password_num0, R.id.factory_password_num1, R.id.factory_password_num2,
			R.id.factory_password_num3, R.id.factory_password_num4, R.id.factory_password_num5,
			R.id.factory_password_num6, R.id.factory_password_num7, R.id.factory_password_num8,
			R.id.factory_password_num9, R.id.factory_password_confirm, R.id.factory_password_del, };
	Button mButtonNum[] = new Button[ID_BUTTON_NUM.length];

	int ID_TEXTVIEW[] = { R.id.factory_password_currpassword, R.id.storage_total, R.id.storage_free,
			R.id.setting_language_cn, R.id.setting_language_us };
	TextView mTextView[] = new TextView[ID_TEXTVIEW.length];

	int ID_BUTTON[] = { R.id.language_confirm, R.id.language_cancel, };
	Button mButton[] = new Button[ID_BUTTON.length];
	int ID_ONEBUTTON[] = { R.id.view_common_language, R.id.view_common_wifi, R.id.view_common_btset,
			R.id.view_common_storage, R.id.view_common_version, R.id.view_common_factory, R.id.view_error_log_code,
			R.id.view_carapp_vision, R.id.view_system_info, R.id.view_common_restore, R.id.view_common_theme_settings,
			R.id.view_common_wallpaper_settings, R.id.view_common_set_volume, R.id.view_common_guide };
	vHeaderLayout mLayout_OneButton[] = new vHeaderLayout[ID_ONEBUTTON.length];

	int ID_ONECHECKBOX[] = { R.id.view_sound_effsound, R.id.view_sound_loud, R.id.view_ligth_control,
			R.id.view_common_handlebrake, R.id.view_common_autonavi, R.id.view_common_collision,
			R.id.view_common_orignal_sound_switch, R.id.view_common_gps_mix, R.id.view_common_starttip,
			R.id.view_common_reversing_mute, R.id.view_common_ldw, R.id.view_common_settings_intelligent_charging_pile,
			R.id.view_common_backcar_voice };
	vHeaderLayout mLayout_OneCheckBox[] = new vHeaderLayout[ID_ONECHECKBOX.length];

	int ID_TWOBUTTON[] = { R.id.view_common_mixsound, R.id.view_common_reboot_sound };
	vHeaderLayout mLayout_TwoButton[] = new vHeaderLayout[ID_TWOBUTTON.length];

	RadioGroup mRadioGroud;
	int ID_RADIOBUTTON[] = { R.id.radiobutton_cn, R.id.radiobutton_tw, R.id.radiobutton_us, };
	SettingConstants.LANGUAGE_T mLanguageType = SettingConstants.LANGUAGE_T.CN;

	private final String PERSIST_SYS_ANIMATION = "persist.sys.animation";
	public final String path = "/mnt/sdcard";
	private ProgressBar storageProgressBar;

	public static Boolean isLanguage = false;
	public static Boolean isSetVolume = false;
	public static Boolean isStorage = false;
	public static Boolean isVersion = false;
	public static Boolean isFactory = false;
	public static Boolean isWifi = false;
	public static Boolean isTheme = false;
	public static Boolean isWallpaper = false;
	public static Boolean isRestore = false;
	public static View vPageCommon;
	public static View vPageStorage;
	public static View vPageInfo;
	public static View vPageFactory;
	public static View vPageLanguage;
	public static View vPageWifi;

	String strDlgTitle;
	String[] arrayBrightnessMode;
	String[] specialLocaleCodes, specialLocaleNames;
	StringBuffer mStrBuf = new StringBuffer();
	DataShared mDataShared;

	private final static int COUNTS = 8;// 点击次数
	private final static long DURATION = 3 * 1000;// 规定有效时间
	private long[] mHits = new long[COUNTS];

	void init(Context context) {

		mDataShared = DataShared.getInstance(context);
		strDlgTitle = context.getResources().getString(R.string.setting_brightness_mode_set);
		arrayBrightnessMode = context.getResources().getStringArray(R.array.brightness_mode_values);

		specialLocaleCodes = context.getResources().getStringArray(R.array.special_locale_codes);
		specialLocaleNames = context.getResources().getStringArray(R.array.special_locale_names);
		String curCountry = context.getResources().getConfiguration().locale.getCountry();
		if (curCountry.equals("CN")) {
			SettingConstants.eLanguage = SettingConstants.LANGUAGE_T.CN;
		} else if (curCountry.equals("TW")) {
			SettingConstants.eLanguage = SettingConstants.LANGUAGE_T.TW;
		} else if (curCountry.equals("US")) {
			SettingConstants.eLanguage = SettingConstants.LANGUAGE_T.US;
		}
		mLanguageType = SettingConstants.eLanguage;

		mPopupView = MediaActivity.mActivity.getWindow().getDecorView();
	}

	void initView(ViewGroup root) {

		for (int i = 0; i < ID_TEXTVIEW.length; i++) {
			mTextView[i] = (TextView) root.findViewById(ID_TEXTVIEW[i]);
			mTextView[i].setOnClickListener(this);
		}

		for (int i = 0; i < ID_BUTTON.length; i++) {
			Button button = (Button) root.findViewById(ID_BUTTON[i]);
			button.setOnClickListener(this);
		}

		for (int i = 0; i < ID_BUTTON_NUM.length; i++) {
			Button button = (Button) root.findViewById(ID_BUTTON_NUM[i]);
			button.setOnClickListener(this);
		}

		for (int i = 0; i < ID_ONEBUTTON.length; i++) {
			vHeaderLayout layout = (vHeaderLayout) root.findViewById(ID_ONEBUTTON[i]);
			layout.setOneButtonListener(this);
		}

		for (int i = 0; i < ID_ONECHECKBOX.length; i++) {
			vHeaderLayout layout = (vHeaderLayout) root.findViewById(ID_ONECHECKBOX[i]);
			layout.setOneCheckBoxListener(this);
		}

		for (int i = 0; i < ID_TWOBUTTON.length; i++) {
			vHeaderLayout layout = (vHeaderLayout) root.findViewById(ID_TWOBUTTON[i]);
			layout.setTwoButtonListener(this);
		}

		mScrollView = (ScrollView) root.findViewById(R.id.layout_scrollview);
		vPageCommon = root.findViewById(R.id.view_page_common);
		vPageLanguage = root.findViewById(R.id.view_page_language);
		vPageStorage = root.findViewById(R.id.view_page_storage);
		vPageInfo = root.findViewById(R.id.view_page_info);
		vPageFactory = root.findViewById(R.id.view_page_factory);
		vPageWifi = root.findViewById(R.id.view_page_wifi);
		// PageWifi mPageWifi = new PageWifi(mContext, root);

		mRadioGroud = (RadioGroup) root.findViewById(R.id.radiogroup_language);
		mRadioGroud.check(ID_RADIOBUTTON[SettingConstants.eLanguage.ordinal()]);
		String str = specialLocaleNames[SettingConstants.eLanguage.ordinal()];
		((vHeaderLayout) root.findViewById(R.id.view_common_language)).setHintTitle(str);
		mRadioGroud.setOnCheckedChangeListener(this);

		switch (mLanguageType) {
		case CN:
			mTextView[3].setSelected(true);
			mTextView[4].setSelected(false);
			break;
		case TW:

			break;
		case US:
			mTextView[3].setSelected(false);
			mTextView[4].setSelected(true);
			break;

		default:
			break;
		}

		boolean checked = tzUtils.getBuzzerEnable(mContext);
		((vHeaderLayout) root.findViewById(R.id.view_common_starttip))
				.setChecked(SystemProperties.getInt(PERSIST_SYS_ANIMATION, 1) != 0);
		((vHeaderLayout) root.findViewById(R.id.view_sound_effsound)).setChecked(checked);
		((vHeaderLayout) root.findViewById(R.id.view_sound_loud)).setChecked(SoundUtils.bLoundness);
		((vHeaderLayout) root.findViewById(R.id.view_common_orignal_sound_switch))
				.setChecked(SettingConstants.def_volume_switch);
		((vHeaderLayout) root.findViewById(R.id.view_common_gps_mix)).setChecked(SettingConstants.remix_enable);
		SettingConstants.remix_ratio = SystemProperties.getInt(PersistUtils.PERSYS_REMIX_RATIO,
				SettingConstants.remix_ratio);
		((vHeaderLayout) root.findViewById(R.id.view_common_mixsound))
				.setMiddleTitle(String.valueOf(SettingConstants.remix_ratio));
		((vHeaderLayout) root.findViewById(R.id.view_common_reboot_sound))
				.setMiddleTitle(String.valueOf(SettingConstants.defaultVolume));
		((vHeaderLayout) root.findViewById(R.id.view_common_reboot_sound)).setVisibility(
				SystemProperties.getBoolean(PersistUtils.PERSYS_PWRON_VOLUME_ENABLE, true) ? View.VISIBLE : View.GONE);

		((vHeaderLayout) root.findViewById(R.id.view_common_reversing_mute)).setChecked(SettingConstants.backcar_mute);
		((vHeaderLayout) root.findViewById(R.id.view_common_backcar_voice))
				.setChecked(SettingConstants.backcar_reduce_volume);

		((vHeaderLayout) root.findViewById(R.id.view_common_autonavi)).setChecked(SettingConstants.autonavi);
		((vHeaderLayout) root.findViewById(R.id.view_common_settings_intelligent_charging_pile))
				.setChecked(SettingConstants.intelligent_charging_pile);

		((vHeaderLayout) root.findViewById(R.id.view_common_handlebrake)).setChecked(SettingConstants.handlebrake);
		((vHeaderLayout) root.findViewById(R.id.view_common_brakeassist)).setChecked(SettingConstants.brakeassist);
		((vHeaderLayout) root.findViewById(R.id.view_common_collision)).setChecked(SettingConstants.collision);
		((vHeaderLayout) root.findViewById(R.id.view_ligth_control)).setChecked(SettingConstants.ligth_control);
		// Lane Departure Warning
		((vHeaderLayout) root.findViewById(R.id.view_common_ldw)).setChecked(SettingConstants.ldw);
		/**************** version info **********************/

		String title = verUtils.getFirmwareVersion();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_firmware_version)).setSubTitle2(title);
		title = verUtils.getCpuType();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_cpu_info)).setSubTitle2(title);
		title = verUtils.getmcuVersion();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_mcu_version)).setSubTitle2(title);
		title = verUtils.getBtVersion();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_bt_version)).setSubTitle2(title);
		title = verUtils.getCANVersion();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_canbus_version)).setSubTitle2(title);
		title = verUtils.getSerialNumber();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_serial_number)).setSubTitle2(title);
		title = verUtils.getWifiMacAddr();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_wifi_mac_address)).setSubTitle2(title);
		title = verUtils.getUUID();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_quick_response_code)).setSubTitle2(title);
		title = "APP 1280x720 " + verUtils.getAppBuildTime();
		if (!TextUtils.isEmpty(title))
			((vHeaderLayout) root.findViewById(R.id.view_system_info)).setSubTitle2(title);

		storageProgressBar = (ProgressBar) root.findViewById(R.id.storageProgressBar);
		// 设置存储空间数据
		Utils.CardInfo info = Utils.getCardInfo(path);
		if (info != null) {
			storageProgressBar.setMax((int) (info.total / (1024 * 1024)));
			storageProgressBar.setProgress((int) ((info.total - info.free) / (1024 * 1024)));

			String storage_total = Utils.convertStorage(info.total);
			String storage_free = Utils.convertStorage(info.free);
			mTextView[1]
					.setText(mContext.getResources().getString(R.string.common_settings_memory_total, storage_total));
			mTextView[2].setText(mContext.getResources().getString(R.string.common_settings_memory_free, storage_free));
		}
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_common, null));
			init(context);
			initView(mRootView);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(launcherUtils.OPEN_WIFI_SETTING_ACTION);
		context.registerReceiver(mBroadcastReceiver, filter);
		if ("iswifi".equals(MediaActivity.mActivity.switchType)) {
			Intent intent = new Intent(launcherUtils.OPEN_WIFI_SETTING_ACTION);
			mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
		}
		return mRootView;
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
	public void onOneButtonClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {

		case R.id.view_common_language:
			isLanguage = true;
			vPageCommon.setVisibility(View.GONE);
			vPageLanguage.setVisibility(View.VISIBLE);
			vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			vPageLanguage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			break;
		case R.id.view_common_wifi:
			isWifi = true;
			if (mPageWifi == null)
				mPageWifi = new PageWifi(mContext, mRootView);
			vPageCommon.setVisibility(View.GONE);
			vPageWifi.setVisibility(View.VISIBLE);
			vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			vPageWifi.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			break;

		case R.id.view_common_set_volume:
			isSetVolume = true;
			launcherUtils.startSetVolume();
			break;

		case R.id.view_common_storage:
			isStorage = true;
			vPageCommon.setVisibility(View.GONE);
			vPageStorage.setVisibility(View.VISIBLE);
			vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			vPageStorage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			break;

		case R.id.view_common_version:
			// isVersion = true;
			// vPageCommon.setVisibility(View.GONE);
			// vPageInfo.setVisibility(View.VISIBLE);
			// vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext,
			// R.anim.app_end));
			// vPageInfo.setAnimation(AnimationUtils.loadAnimation(mContext,
			// R.anim.app_start));
			// mScrollView.scrollTo(0, 0);
			if (mPopupWindow_NewSysInfo == null)
				mPopupWindow_NewSysInfo = createPopWindow_NewSystemInfo();
			mPopupWindow_NewSysInfo.showAtLocation(mPopupView, Gravity.CENTER, 89 , 0);
			break;
		case R.id.view_common_factory:
			isFactory = true;
			vPageCommon.setVisibility(View.GONE);
			vPageFactory.setVisibility(View.VISIBLE);
			vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
			vPageFactory.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
			break;
		case R.id.view_error_log_code:
			FileCopy.getInstance().copyCrashData(MediaActivity.mActivity);
			break;

		case R.id.view_system_info:
			// if (mPopupWindow_SysInfo == null)
			// mPopupWindow_SysInfo = createPopWindow_SystemInfo();
			// mPopupWindow_SysInfo.showAtLocation(mPopupView, Gravity.CENTER,
			// 0, 50);

			if (mPopupWindow_NewSysInfo == null)
				mPopupWindow_NewSysInfo = createPopWindow_NewSystemInfo();
			mPopupWindow_NewSysInfo.showAtLocation(mPopupView, Gravity.CENTER, 0, 50);

			break;
		case R.id.view_common_theme_settings:
			isTheme = true;
			launcherUtils.startTheme();
			break;
		case R.id.view_common_wallpaper_settings:
			isWallpaper = true;
			launcherUtils.startWallpaper();
			break;
		case R.id.view_common_btset:
			SettingUtils.startBTSet();
			break;
		case R.id.view_carapp_vision:

			break;
		case R.id.view_common_restore:
			isRestore = true;
			if (mPopupWindow_Restore == null)
				mPopupWindow_Restore = createPopWindow_restore();
			mPopupWindow_Restore.showAtLocation(mPopupView, Gravity.CENTER, 60, 0);
			break;
		case R.id.view_common_guide:
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ComponentName cn = new ComponentName("com.yecon.OperateIntro", "com.yecon.OperateIntro.mainActivity");
			intent.setComponent(cn);
			mContext.startActivity(intent);
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.language_confirm:
			if (SettingConstants.eLanguage != mLanguageType) {
				SettingConstants.eLanguage = mLanguageType;
				sendMessage(R.id.view_common_language, SettingConstants.eLanguage.ordinal());
			}
			break;
		case R.id.language_cancel:
			mLanguageType = SettingConstants.eLanguage;
			mRadioGroud.check(ID_RADIOBUTTON[SettingConstants.eLanguage.ordinal()]);
			break;
		case R.id.factory_password_num0:
		case R.id.factory_password_num1:
		case R.id.factory_password_num2:
		case R.id.factory_password_num3:
		case R.id.factory_password_num4:
		case R.id.factory_password_num5:
		case R.id.factory_password_num6:
		case R.id.factory_password_num7:
		case R.id.factory_password_num8:
		case R.id.factory_password_num9:
			for (int i = 0; i < ID_BUTTON_NUM.length; i++) {
				if (view.getId() == ID_BUTTON_NUM[i] && i < CharSet.length) {
					mStrBuf.setLength(0);
					mStrBuf.append(mTextView[0].getText().toString()).append(String.valueOf(CharSet[i]));
					mTextView[0].setText(mStrBuf);
				}
			}
			break;
		case R.id.factory_password_del:
			int len = mTextView[0].getText().length();
			if (len > 0)
				mStrBuf.setLength(len - 1);
			mTextView[0].setText(mStrBuf);
			break;
		case R.id.factory_password_confirm:
			if (mTextView[0].getText().toString().equalsIgnoreCase(SettingConstants.factory_password)) {
				Utils.RunApp("com.yecon.carsetting", "com.yecon.carsetting.FactorySettingActivity");
			} else if (mTextView[0].getText().toString().equalsIgnoreCase("169")) {
				SettingUtils.onSet_filterAPP(MediaActivity.mActivity.getFragmentManager());
			} else {
				tzUtils.showToast(R.string.factory_settings_password_wrong);
				mStrBuf.setLength(0);
				mTextView[0].setText(mStrBuf);
			}
			mTextView[0].setText("");
			break;

		case R.id.setting_language_cn:
			mTextView[3].setSelected(true);
			mTextView[4].setSelected(false);
			mLanguageType = SettingConstants.LANGUAGE_T.CN;
			if (SettingConstants.eLanguage != mLanguageType) {
				SettingConstants.eLanguage = mLanguageType;
				sendMessage(R.id.view_common_language, SettingConstants.eLanguage.ordinal());
			}
			break;
		case R.id.setting_language_us:
			mTextView[3].setSelected(false);
			mTextView[4].setSelected(true);
			mLanguageType = SettingConstants.LANGUAGE_T.US;
			if (SettingConstants.eLanguage != mLanguageType) {
				SettingConstants.eLanguage = mLanguageType;
				sendMessage(R.id.view_common_language, SettingConstants.eLanguage.ordinal());
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.playSoundEffect(android.view.SoundEffectConstants.CLICK);
		switch (arg1) {
		case R.id.radiobutton_cn:
			mLanguageType = SettingConstants.LANGUAGE_T.CN;
			break;
		case R.id.radiobutton_tw:
			mLanguageType = SettingConstants.LANGUAGE_T.TW;
			break;
		case R.id.radiobutton_us:
			mLanguageType = SettingConstants.LANGUAGE_T.US;
			break;
		}
	}

	private void sendMessage(int id, int arg1) {
		Message msg = new Message();
		msg.what = id;
		msg.arg1 = arg1;
		mHandler.sendMessageDelayed(msg, 100);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (R.id.view_common_language == msg.what) {
				Utils.showToast(mContext.getResources().getString(R.string.setting_language_car_success));
				int index = msg.arg1;
				String str = specialLocaleNames[index];
				((vHeaderLayout) mRootView.findViewById(R.id.view_common_language)).setHintTitle(str);
				str = specialLocaleCodes[index];
				String language = str.substring(0, 2);
				String country = str.substring(3, 5);
				final Locale l = new Locale(language, country);
				LocalePicker.updateLocale(l);

				// �������Ժ���Ҫ�رո�Activity��Ȼ�и��ʲ�ˢ�����Ի�ˢ��һ��
				if (null != mContext) {
					mContext.sendBroadcast(new Intent("UPDATA_LANGUAGE"));
				}

			}
		}
	};

	@Override
	public void onCheckout(View view, boolean value) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.view_common_starttip:
			SystemProperties.set(PERSIST_SYS_ANIMATION, "" + (value ? 1 : 0));
			break;
		case R.id.view_sound_effsound:
			tzUtils.setBuzzerEnable(mContext, value);
			break;
		case R.id.view_sound_loud:
			SoundUtils.bLoundness = value;
			int nLoudNessType = SoundUtils.bLoundness ? SoundUtils.audio[AUDParaT.LOUNDNESS.ordinal()] : 0;
			SoundMethodManager.getInstance(mContext).mMtksetting.SetLoudNess(nLoudNessType,
					SoundArray.LoudNess_gLoudNessGain[nLoudNessType]);
			break;
		case R.id.view_ligth_control:
			SettingConstants.ligth_control = value;
			mDataShared.putBoolean(SettingConstants.key_ligth_control, value);
			mDataShared.commit();
			break;
		case R.id.view_common_handlebrake:
			SettingConstants.handlebrake = value;
			mDataShared.putBoolean(SettingConstants.key_handlebrake, value);
			mDataShared.commit();
			break;
		case R.id.view_common_autonavi:
			SettingConstants.autonavi = value;
			mDataShared.putBoolean(SettingConstants.key_autonavi, value);
			mDataShared.commit();
			break;
		case R.id.view_common_settings_intelligent_charging_pile:
			SettingConstants.intelligent_charging_pile = value;
			mDataShared.putBoolean(SettingConstants.key_intelligent_charging_pile, value);
			mDataShared.commit();
			break;
		case R.id.view_common_collision:
			SettingConstants.collision = value;
			mDataShared.putBoolean(SettingConstants.key_collision, value);
			mDataShared.commit();
			break;
		case R.id.view_common_orignal_sound_switch:
			SettingConstants.def_volume_switch = value;
			SystemProperties.set(PersistUtils.PERSYS_PWRON_VOLUME_ENABLE, value ? "true" : "false");
			((vHeaderLayout) mRootView.findViewById(R.id.view_common_reboot_sound))
					.setVisibility(value ? View.VISIBLE : View.GONE);
			break;
		case R.id.view_common_gps_mix:
			SettingConstants.remix_enable = value;
			SystemProperties.set(PersistUtils.PERSYS_NAVI_REMIX, value ? "true" : "false");
			break;

		case R.id.view_common_reversing_mute:
			SettingConstants.backcar_mute = value;
			SystemProperties.set(PersistUtils.PERSYS_BACKCAR_MUTE, value ? "true" : "false");
			break;
		case R.id.view_common_ldw:
			SettingConstants.ldw = value;
			mDataShared.putBoolean(SettingConstants.key_ldw, value);
			break;
		case R.id.view_common_backcar_voice:
			SettingConstants.backcar_reduce_volume = value;
			SystemProperties.set(PersistUtils.PERSYS_BACKCAR_REDUCE_VOLUME, value ? "true" : "false");
			break;

		}
	}

	@Override
	public void onLeftButtonClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.view_common_mixsound:
			if (SettingConstants.remix_ratio > 0)
				SettingConstants.remix_ratio--;
			String strValue = String.valueOf(SettingConstants.remix_ratio);
			SystemProperties.set(PersistUtils.PERSYS_REMIX_RATIO, strValue);
			((vHeaderLayout) mRootView.findViewById(R.id.view_common_mixsound)).setMiddleTitle(strValue);
			break;
		case R.id.view_common_reboot_sound:
			if (SettingConstants.defaultVolume > 5)
				SettingConstants.defaultVolume--;
			SystemProperties.set(PersistUtils.PERSYS_PWRON_VOLUME, String.valueOf(SettingConstants.defaultVolume));
			((vHeaderLayout) mRootView.findViewById(R.id.view_common_reboot_sound))
					.setMiddleTitle(String.valueOf(SettingConstants.defaultVolume));
			//YeconSettings.setDefaultVolume(mContext, SettingConstants.defaultVolume);
			break;
		default:
			break;
		}
	}

	@Override
	public void onRightButtonClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.view_common_mixsound:
			if (SettingConstants.remix_ratio < 100)
				SettingConstants.remix_ratio++;
			String strValue = String.valueOf(SettingConstants.remix_ratio);
			SystemProperties.set(PersistUtils.PERSYS_REMIX_RATIO, strValue);
			((vHeaderLayout) mRootView.findViewById(R.id.view_common_mixsound)).setMiddleTitle(strValue);
			break;
		case R.id.view_common_reboot_sound:
			if (SettingConstants.defaultVolume < 25)
				SettingConstants.defaultVolume++;
			SystemProperties.set(PersistUtils.PERSYS_PWRON_VOLUME, String.valueOf(SettingConstants.defaultVolume));
			((vHeaderLayout) mRootView.findViewById(R.id.view_common_reboot_sound))
					.setMiddleTitle(String.valueOf(SettingConstants.defaultVolume));
			//YeconSettings.setDefaultVolume(mContext, SettingConstants.defaultVolume);
			break;
		default:
			break;
		}
	}

	private PopupWindow createPopWindow(View contentView, int width, int height) {
		PopupWindow window = new PopupWindow(contentView, width, height);
		window.setFocusable(true);
		window.setTouchable(true);
		window.setOutsideTouchable(true);
		window.setAnimationStyle(R.style.popAnimationFade);
		window.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.setting_dlg_shape_bg));
		return window;
	}

	private PopupWindow createNewPopWindow(View contentView, int width, int height) {
		PopupWindow window = new PopupWindow(contentView, width, height);
		window.setFocusable(true);
		window.setTouchable(true);
		window.setOutsideTouchable(true);
		window.setAnimationStyle(R.style.popAnimationFade);
		window.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pop_bg));
		return window;
	}

	// 版本信息
	private PopupWindow createPopWindow_NewSystemInfo() {
		View localView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.new_system_info, null);
		final PopupWindow popupWindow = createNewPopWindow(localView, 320, 240);
		TextView tv_general_about_SwVerion = ((TextView) localView.findViewById(R.id.tv_general_about_SwVerion));
		TextView tv_general_about_HwVerion = ((TextView) localView.findViewById(R.id.tv_general_about_HwVerion));
		tv_general_about_SwVerion.setText(
				mContext.getResources().getString(R.string.general_about_SwVerion, verUtils.getnewSystemBuildTime()));
		tv_general_about_HwVerion.setText(
				mContext.getResources().getString(R.string.general_about_HwVerion, verUtils.getHardwareVersion()));

		tv_general_about_SwVerion.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				// TODO Auto-generated method stub
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
					if (mPopupWindow_NewSysInfo != null) {
						mPopupWindow_NewSysInfo.dismiss();
					}
					isVersion = true;
					vPageCommon.setVisibility(View.GONE);
					vPageInfo.setVisibility(View.VISIBLE);
					vPageCommon.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_end));
					vPageInfo.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.app_start));
					mScrollView.scrollTo(0, 0);
				}
			}
		});

		return popupWindow;
	}

	private PopupWindow createPopWindow_SystemInfo() {
		View localView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.system_info, null);
		final PopupWindow popupWindow = createPopWindow(localView, 700, 400);
		TextView mTvInfo = ((TextView) localView.findViewById(R.id.tv_system_info));
		StringBuffer strBuf = new StringBuffer();
		strBuf.setLength(0);
		strBuf.append(mContext.getResources().getString(R.string.general_about_kernelVersion,
				verUtils.getFormattedKernelVersion()));
		strBuf.append("\n");
		strBuf.append(mContext.getResources().getString(R.string.general_about_Version, verUtils.getSystemVersion()));
		strBuf.append("\n");
		strBuf.append(mContext.getResources().getString(R.string.general_about_system_buildtime,
				verUtils.getSystemBuildTime()));
		strBuf.append("\n");
		strBuf.append(
				mContext.getResources().getString(R.string.general_about_app_buildtime, verUtils.getAppBuildTime()));
		strBuf.append("\n");
		strBuf.append(mContext.getResources().getString(R.string.general_about_screen_resolutions, "1280x720"));
		strBuf.append("\n");
		strBuf.append(mContext.getResources().getString(R.string.general_about_touch_resolutions, "1280x720"));
		strBuf.append("\n");
		strBuf.append(
				mContext.getResources().getString(R.string.general_about_SwVerion, verUtils.getSoftwareVersionName()));
		strBuf.append("\n");
		strBuf.append(
				mContext.getResources().getString(R.string.general_about_HwVerion, verUtils.getHardwareVersion()));
		strBuf.append("\n");
		mTvInfo.setText(strBuf);
		return popupWindow;
	}

	private PopupWindow createPopWindow_restore() {
		View localView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.setting_dlg_prompt_layout, null);
		final PopupWindow popupWindow = createPopWindow(localView, 400, 240);
		TextView textView = (TextView) localView.findViewById(R.id.dlg_content);
		StringBuffer strBuf = new StringBuffer();
		strBuf.setLength(0);
		strBuf.append(mContext.getResources().getString(R.string.setting_recovery_prompt_alarm));
		strBuf.append("\n");
		textView.setText(strBuf);
		TextView tvConfirm = (TextView) localView.findViewById(R.id.dlg_ok);
		tvConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				tzUtils.recoverySystem(mContext, false);
				// SettingMethodManager.getInstance(mContext).recoveryFactoryPara();
				// tzUtils.rebootSystem(mContext);
				popupWindow.dismiss();
				isRestore = false;
			}
		});
		TextView tvCancel = (TextView) localView.findViewById(R.id.dlg_cancle);
		tvCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popupWindow.dismiss();
				isRestore = false;
			}
		});
		return popupWindow;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(launcherUtils.OPEN_WIFI_SETTING_ACTION)) {
				if (mRootView != null) {
					onOneButtonClick(mRootView.findViewById(R.id.view_common_wifi));
				}
			}
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
