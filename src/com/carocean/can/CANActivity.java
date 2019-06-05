package com.carocean.can;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.carocean.R;
import com.carocean.can.AlertDialogEx.OnDialogItemSelectChangeListener;
import com.carocean.can.CanActionBar.OnSelectedChangeListener;
import com.carocean.can.CanPageSimpleItem.OnStatusChangeListener;
import com.carocean.can.CanServiceClient.OnAdasStatusChangeListener;
import com.carocean.can.CanServiceClient.OnAirClnrStatusChangeListener;
import com.carocean.can.CanServiceClient.OnBCMStatusChangeListener;
import com.carocean.can.CanServiceClient.OnFICMStatusChangeListener;
import com.carocean.can.CanServiceClient.OnOverSpeedStatusChangeListener;
import com.carocean.can.CanServiceClient.OnPDCStatusChangeListener;
import com.carocean.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class CANActivity extends Activity
		implements OnClickListener, OnSelectedChangeListener, OnStatusChangeListener,
		OnDialogItemSelectChangeListener,OnPageChangeListener,
		OnOverSpeedStatusChangeListener,
		OnAdasStatusChangeListener,
		OnPDCStatusChangeListener,
		OnFICMStatusChangeListener,
		OnBCMStatusChangeListener,
		OnAirClnrStatusChangeListener{

	private CanActionBar mCanActionBar;

	private int mItemsSimpleString[][] = { { R.string.can_over_speed_alarming, 0, 0, R.drawable.selector_btn_can_item}, // speed_alarming
			{ R.string.can_alarming_when_exceed_this_speed, 0, R.string.can_over_speed_default, R.drawable.selector_btn_can_item },
			{ R.string.can_vehicle_starting_open, R.string.can_vehicle_starting_tips, 0, R.drawable.selector_btn_can_item},
			{ R.string.can_turn_on_the_light_at_low_speed_open, R.string.can_turn_on_the_light_at_low_speed_tips, 0,
			  R.drawable.selector_btn_can_item },
			{ R.string.can_forward_collision_support_system, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_front_collision_alarm_mode, 0, R.string.can_front_collision_alarm_mode_fcw_on, 0 },
			{ R.string.can_front_collision_alarming_sensitivity, 0, R.string.can_over_speed_default, 0 },
			{ R.string.can_lane_keeping_system, R.string.can_lane_keeping_title, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_lane_departure_warning_mode, 0, R.string.can_lane_departure_warning_mode_fcw_on, 0 },
			{ R.string.can_front_collision_alarming_sensitivity, 0, R.string.can_over_speed_default, 0 },
			{ R.string.can_speed_limit_identification, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_before_parking_pdc, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_blind_spot_detection_and_alarm, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_fatigue_remind, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_follow_me_home, 0, 0, 0 },
			{ R.string.can_keep_the_lights_on, 0, R.string.can_keep_the_lights_on_time, 0 },
			{ R.string.can_find_car_feedback, 0, R.string.can_find_car_feedback_mode_1, R.drawable.selector_btn_can_item },
			{ R.string.can_car_automatically_locked, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_car_extinguish_automatic_lock, 0, 0, 0 },
			{ R.string.can_car_lock_feedback, 0, R.string.can_car_lock_feedback_mode_1, 0 },
			{ R.string.can_car_the_keyto_unlock, 0, R.string.can_car_the_keyto_unlock_mode_1, 0 },
			{ R.string.can_car_nokey_tounlock_automatically, 0, R.string.can_over_speed_default, 0 },
			{ R.string.can_car_pm_relevance_aircleane, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_car_rearview_mirror_auto, 0, 0, R.drawable.selector_btn_can_item },
			{ R.string.can_car_power_liftgate, 0, R.string.can_car_power_liftgate_mode_all, R.drawable.selector_btn_can_item },
			{ R.string.can_car_restore_all, R.string.can_car_restore_tips, 0, R.drawable.selector_btn_can_item },};

	private int mPageItemIds[] = { R.id.can_eba_item0, R.id.can_eba_item1, R.id.can_eba_item2, R.id.can_eba_item3,
			R.id.can_eba_item4, R.id.can_eba_item5, R.id.can_eba_item6, R.id.can_eba_item7, R.id.can_eba_item8,
			R.id.can_eba_item9, R.id.can_eba_item10, R.id.can_eba_item11, R.id.can_eba_item12, R.id.can_eba_item13, 
			R.id.can_eba_item14, R.id.can_eba_item15, R.id.can_eba_item16, R.id.can_eba_item17, R.id.can_eba_item18,
			R.id.can_eba_item19, R.id.can_eba_item20, R.id.can_eba_item21, R.id.can_eba_item22, R.id.can_eba_item23,
			R.id.can_eba_item24,R.id.can_eba_item25};

	private int mPageLayouts[] = { R.layout.can_eba_page_1, R.layout.can_eba_page_2,R.layout.can_eba_page_3,R.layout.can_eba_page_4,
			                       R.layout.can_comfort_and_convenience_page_1,R.layout.can_comfort_and_convenience_page_2,
			                      };

	private int mCollisionAlarmDialogItems[] = { R.string.can_front_collision_alarm_off,
			R.string.can_front_collision_alarm_fcm_on, R.string.can_front_collision_alarm_fcm_aeb_on };
	private int mAlarmSentisivityDialogItems[] = { R.string.can_front_collision_alarming_sensitivity_low,
			R.string.can_front_collision_alarming_sensitivity_standard,R.string.can_front_collision_alarming_sensitivity_high};
	private int mLaneDepartureModeDialogItems[] = { R.string.can_front_collision_alarm_off,
			R.string.can_lane_departure_warning_mode_ldw_on, R.string.can_lane_departure_warning_mode_ldwlka_on };
	private int mPowerLiftgateDialogItems[] = {R.string.can_car_power_liftgate_mode_all,
			                                   R.string.can_car_power_liftgate_mode_cheku};
	private int mLightsOnDialogItems[] = {R.string.can_set_keep_the_lights_on_30s,
			                              R.string.can_set_keep_the_lights_on_60s,
			                              R.string.can_set_keep_the_lights_on_90s};
	private int mFindCarFeekbackDialogItem[] = {R.string.can_find_car_feedback_mode_2,R.string.can_find_car_feedback_mode_1
			                                    };
	private int mLockCarFeekbackDialogItem[] = {R.string.can_car_lock_feedback_mode_1,
			                                    R.string.can_car_lock_feedback_mode_2,
			                                    R.string.can_car_lock_feedback_mode_3};
	private int mUnlockDialogItem[] = {R.string.can_car_the_keyto_unlock_mode_1,
			                           R.string.can_car_the_keyto_unlock_mode_2};
	private int mLeftButtons[] = {R.id.can_over_speed_alarming_btn,R.id.can_forward_collision_support_system_btn,
			                           R.id.can_lane_keeping_assist_system_btn,R.id.can_before_parking_pdc_btn,
			                           R.id.can_fatigue_remind_btn};
	private int mItemIndex = 0;
	private ViewPager mContentViewPager;
	private List<CanPageSimpleItem> mItemList = new ArrayList<CanPageSimpleItem>();
	private List<View> mPagerlist;
	private CanTpmsPage mCanTpmsPage;
	private int mDialogSelectedItem[];
	PageView mPagerView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		showSystemUI(false);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_can);
		initViews();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		showSystemUI(false);
		super.onResume();
		mPagerView.post(new Runnable(){
			         @Override
			   	    public void run() {
			        	 //mPagerView.setCurrentItem(2);
			         }
			     });

		
	}
	protected void onPause() {
		showSystemUI(true);
		super.onPause();
	}
	@Override
	public void onClick(View view) {
		if(R.id.can_close_btn == view.getId()) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void OnSelectedChange(int index) {
		int page = 0;
		if (null != mContentViewPager) {
			switch (index) {
			case 0:
				page = 0;
				break;
			case 1:{
				int current = mContentViewPager.getCurrentItem();
				if(1 == current || 2 == current) {
					page = current;
				}else {
					page = 1;
				}
			}
				break;
			case 2:
				page = 3;
				break;
			default:
				break;
			}
			
			if (page >=0 && page < mPagerlist.size()) {
				mContentViewPager.setCurrentItem(page, false);
			}
		}

	}

	private int mCurrentIndex = 0;

	public void onClick(int index) {
		mCurrentIndex = index;
		int dialogStyle = 0;
		String title = null;
		int items[] = null;
		switch (index) {
		case 1:
			title = getResources().getString(R.string.can_set_alarming_speed) 
			       + getResources().getString(R.string.can_speed_unit);
			dialogStyle = 1;
			break;
		case 5:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_front_collision_alarm_title);
			items = mCollisionAlarmDialogItems;
			break;
		case 6:
		case 9:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_front_collision_alarming_sensitivity_title);
			items = mAlarmSentisivityDialogItems;
			break;
		case 8:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_lane_departure_warning_mode_title);
			items = mLaneDepartureModeDialogItems;
			break;
		/*case 9:
			title = getResources().getString(R.string.can_set_alarming_speed) 
		       + getResources().getString(R.string.can_speed_unit);
			dialogStyle = 1;
			break;*/
		case 15:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_set_keep_the_lights_on_time);
			items = mLightsOnDialogItems;
			break;
		case 16:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_set_keep_the_lights_on_time);
			items = mFindCarFeekbackDialogItem;
			break;
		case 19:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_set_car_lock_feedback);
			items = mLockCarFeekbackDialogItem;
			break;
		case 20:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_set_car_the_keyto_unlock);
			items = mUnlockDialogItem;
			break;
		case 21:
			dialogStyle = 1;
			title = getResources().getString(R.string.can_set_car_nokey_tounlock_automatically);
			//items = mUnlockDialogItem;
			break;
		case 24:
			dialogStyle = 0;
			title = getResources().getString(R.string.can_set_car_power_liftgate_mode);
			items = mPowerLiftgateDialogItems;
			break;
		}

		if(null != title) {
			AlertDialogEx dialog = new AlertDialogEx(this, R.layout.can_selected_dialog, items, title);
			dialog.setStyle(dialogStyle, mDialogSelectedItem[index]);
			dialog.setOnDialogItemSelectChangeListener(this);
			dialog.show();	
		}

	}

	public void onCheck(boolean checked, int index) {
		switch(index) {
		case 0:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:{
			byte fcm = 0;
			if(checked) {
				fcm = 1;
			}
			mCanServiceClient.sendCollisionWarningParam(fcm, (byte)2, (byte)2, (byte)1);
		}
			break;
		}
	}

	public int allocateIndex() {
		return mItemIndex++;
	}

	public void OnDialogItemSelectChange(int index, String data) {
		if (null != data) {
			mItemList.get(mCurrentIndex).setItemData(data);
		}
	}
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	public void onPageSelected(int arg0) {
		turnActionBar(arg0);
	}

	public void onPageScrollStateChanged(int arg0) {
		 if (arg0 != ViewPager.SCROLL_STATE_IDLE) {
			 return;
		 }
		 
		 //turnActionBar(arg0);
	}
	private void turnActionBar(int arg0) {
		if(0 == arg0) {
			mCanActionBar.setCurrentItem(0);
		}else if(arg0 >= 1 && arg0 <= 2) {
			mCanActionBar.setCurrentItem(1);
		}else {
			mCanActionBar.setCurrentItem(2);
		}
	}
	
	private void showSystemUI(boolean show) {
		try {
			Utils.showSystemUI(this, show);
		}catch(Exception e) {
			Log.v("", e.toString());
		}
	}
	
	private void initViews() {
		mProcessHandler = new ProcessHandler(this);
		View view = findViewById(R.id.can_close_btn);
		
		if(null != view) {
			view.setOnClickListener(this);
		}
		
		for(int i = 0; i < mLeftButtons.length; i ++) {
			View v = findViewById(mLeftButtons[i]);
			
			if(null != v) {
				v.setOnClickListener(this);
			}
		}
		mCanActionBar = (CanActionBar) findViewById(R.id.can_action_bar);
		mContentViewPager = (ViewPager) findViewById(R.id.can_viewpaper);
		mContentViewPager.setOnPageChangeListener(this);
		mPagerlist = new ArrayList<View>();
		int index = 0;
		
		view = getLayoutInflater().inflate(R.layout.can_eba_page, null);
		mPagerView = (PageView)view.findViewById(R.id.can_eba_pageview);
		for (int j = 0; j < mPageLayouts.length; j++) {
			View parent = getLayoutInflater().inflate(mPageLayouts[j], null);

			for (; index < mPageItemIds.length;) {
				CanPageSimpleItem item = (CanPageSimpleItem) parent.findViewById(mPageItemIds[index]);

				if (null != item) {
					item.setOnStatusChangeListener(this);
					item.setProperty(mItemsSimpleString[index++]);
					mItemList.add(item);
				} else {
					break;
				}
			}
			
			if(j < 4) {
				mPagerView.addPage(parent);
				
				if(3 == j) {
					mPagerlist.add(view);
				}
			}
			
			if(j > 3) {
				mPagerlist.add(parent);
			}
		}
		
		mCanTpmsPage = (CanTpmsPage)getLayoutInflater().inflate(R.layout.can_tpms_page, null);
		mPagerlist.add(mCanTpmsPage);
		CanViewPaperAdapter adapter = new CanViewPaperAdapter(mPagerlist);
		mContentViewPager.setAdapter(adapter);

		mCanActionBar.setOnSelectedChangeListener(this);
		
		for(int i = 0; i < 4; i ++) {
			int max = 400;
			int min = 250;
			Random random = new Random();
			int value = random.nextInt(max) % (max - min + 1) + min;
			mCanTpmsPage.OnPressureChange(i, value);
			
			max = 110;
			min = 10;
			
			value = random.nextInt(max) % (max - min + 1) + min;
			mCanTpmsPage.OnTemperatureChange(i, value);
		}
		mCanServiceClient = CanServiceClient.getInstance(this);
		
		if(null != mCanServiceClient) {
			mCanServiceClient.setOnOverSpeedStatusChangeListener(this);
			mCanServiceClient.setOnAdasStatusChangeListener(this);
			mCanServiceClient.setOnPDCStatusChangeListener(this);
			mCanServiceClient.setOnFICMStatusChangeListener(this);
			mCanServiceClient.setOnBCMStatusChangeListener(this);
			mCanServiceClient.setOnAirClnrStatusChangeListener(this);
		}
		
		mDialogSelectedItem = new int [mItemList.size()];
	}
	private CanServiceClient mCanServiceClient;
	private int mIPKOverSpdFnSts = 0;
	private int mPKOverSpdThreshholdVal = 120;
	private void processOverSpeedStatusChange() {
		boolean checked = false;
		
		if(0 != mIPKOverSpdFnSts) {
			checked = true;
		}
		CanPageSimpleItem item = mItemList.get(0);
		item.setItemChecked(checked);
		
		item = mItemList.get(1);
		mDialogSelectedItem[1] = mPKOverSpdThreshholdVal;
		String text = String.valueOf(mPKOverSpdThreshholdVal) + getString(R.string.can_speed_unit_pure);
		item.setItemData(text);
	}
	public void onOverSpeedStatusChange(int status, int speed) {
		mIPKOverSpdFnSts = status;
		mPKOverSpdThreshholdVal = speed;
		mProcessHandler.sendEmptyMessage(0);
	}
	private int mDASTSRMainSwitchFeedback = 0;
	private int mDASFCMMainSwitchFeedback = 0;//0x1:off 0x2:fcm 0x3:fcm+aeb
	private int mDASFCMMainSensitivityFeedback = 0;//0x01:low,0x02:standard,0x03:high
	private int mDASLKAMainSwitchFeedback = 0;
	private int mDASLKAMainSensitivityFeedback = 0;
	private int mDASLKASwitchAvailableFeedback = 0;
	public void onAdasStatusChange(int stsr, int fcws, int fcwss, int lkaEnable, int lka) {
		mDASTSRMainSwitchFeedback = stsr;
		mDASFCMMainSwitchFeedback = fcws;
		mDASFCMMainSensitivityFeedback = fcwss;
		mDASLKAMainSwitchFeedback = lka;
		mDASLKASwitchAvailableFeedback = lkaEnable;
		mProcessHandler.sendEmptyMessage(1);
	}
	private int mFrontPDCEnableStatus = 0;//0: OFF,1: ON
	public void onPDCStatusChange(int status) {
		mFrontPDCEnableStatus = status;
		mProcessHandler.sendEmptyMessage(2);
	}
	private int mFICMDDDFunctionStatus = 0;//0 not support,1 off 2 --on
	private int mBSDSwitchStatus = 0;//0 not support,1 off 2 --on
	public void OnFICMStatusChange(int dddStatus, int bsdStatus) {
		mFICMDDDFunctionStatus = dddStatus;
		mBSDSwitchStatus = bsdStatus;
		mProcessHandler.sendEmptyMessage(3);
	}
	private int mAutoFollowMeHomeOption = 0;//0:Disabled,1:Enabled,2:Reserved,3:not Available
	private int mFollowMeHomeDuration = 0;//0:0s,1:30s,2:60,3:90
	private int mMirrorAutoFoldOption = 0;//0:Disabled,1:Enabled,2:Reserved,3:all mirror auto unfold related feature not Available
	private int mFindMyCarFeedbackOptions = 0;//0:Lights Only,1:Horn And Lights On
	private int mAutoUnlockingOption = 0;//0:disabled,1:enabled,2:Reserved,3:not Available
	private int mAutomaticLockOption = 0;//0:disabled,1:enabled,2:Reserved,3:not Available
	public void onBCMFMHStatusChange(int home, int homeTime) {
		mAutoFollowMeHomeOption = home;
		mFollowMeHomeDuration = homeTime;
		mProcessHandler.sendEmptyMessage(4);
	}
	public void onBCMFCarStatusChange(int fdCar) {
		mFindMyCarFeedbackOptions = fdCar;
		mProcessHandler.sendEmptyMessage(4);
	}

	public void onBCMMirrorAutoFoldOptionChange(int option) {
		mMirrorAutoFoldOption = option;
		mProcessHandler.sendEmptyMessage(4);
	}
	private void processBCMFCarStatusChange() {
		boolean checked = (0 != mAutoFollowMeHomeOption)?true:false;
		CanPageSimpleItem item = mItemList.get(14);
	    
	    item.setItemChecked(checked);
	    
	    int duration = mFollowMeHomeDuration*30;
	    
	    item = mItemList.get(15);
	    String data = String.valueOf(duration)+getString(R.string.can_second_unit);
	    mDialogSelectedItem[15] = mFollowMeHomeDuration - 1;
	    item.setItemData(data);
	    
	    int index = mFindMyCarFeedbackOptions;;
	    
	    if(index >= 0 && index < mFindCarFeekbackDialogItem.length) {
	    	item = mItemList.get(16);
		    data = getString(mFindCarFeekbackDialogItem[index]);
		    mDialogSelectedItem[16] = index;
		    item.setItemData(data);
	    }
	    
	    checked = (mMirrorAutoFoldOption==1)?true:false;
	    item = mItemList.get(23);
	    item.setItemChecked(checked);
	}
	private void processFICMStatusChange() {
		boolean checked = false;
		
		if(0 != mFICMDDDFunctionStatus) {
			checked = true;
		}
		CanPageSimpleItem item = mItemList.get(13);
	    
	    item.setItemChecked(checked);
		checked = false;
		
		if(0 != mBSDSwitchStatus) {
			checked = true;
		}
		item = mItemList.get(12);
	    
	    item.setItemChecked(checked);
	}
	private void processPDCStatusChange() {
		boolean checked = false;
		
		if(0 != mFrontPDCEnableStatus) {
			checked = true;
		}
		
		CanPageSimpleItem item = mItemList.get(11);
	    
	    item.setItemChecked(checked);
	}
	private void processAdasStatusChange() {
		boolean checked = false;
		if(0 != mDASFCMMainSwitchFeedback) {
			checked = true;
		}
		CanPageSimpleItem item = mItemList.get(4);
		item.setItemChecked(checked);
		String text = "";
		int index = mDASFCMMainSwitchFeedback - 1;
		index = index >= 0?index:0;
		if(index < mCollisionAlarmDialogItems.length) {
			text = getString(mCollisionAlarmDialogItems[index]);
			item = mItemList.get(5);
			item.setItemData(text);
			mDialogSelectedItem[5] = index;
		}
		index = mDASFCMMainSensitivityFeedback - 1;
		index = index >= 0?index:0;
		
		if(index < mAlarmSentisivityDialogItems.length) {
			text = getString(mAlarmSentisivityDialogItems[index]);
			item = mItemList.get(6);
			mDialogSelectedItem[6] = index;
			item.setItemData(text);
		}
		checked = false;
		if(0 != mDASLKASwitchAvailableFeedback) {
			checked = true;
		}
		
		item = mItemList.get(7);
		item.setItemChecked(checked);
		
		index = mDASLKAMainSwitchFeedback - 1;
		index = index >= 0?index:0;
		
		if(index < mLaneDepartureModeDialogItems.length) {
			text = getString(mLaneDepartureModeDialogItems[index]);
			item = mItemList.get(8);
			mDialogSelectedItem[8] = index;
			item.setItemData(text);
		}
		
		index = mDASLKAMainSensitivityFeedback - 1;
		index = index >= 0?index:0;
		
		if(index < mAlarmSentisivityDialogItems.length) {
			text = getString(mAlarmSentisivityDialogItems[index]);
			item = mItemList.get(9);
			mDialogSelectedItem[9] = index;
			item.setItemData(text);
		}
	    checked = false;
	    
	    if(0 != mDASTSRMainSwitchFeedback) {
	    	checked = true;
	    }
	    
	    item = mItemList.get(10);
	    
	    item.setItemChecked(checked);
	}
	private int mAirClnrUserCustAutoWDC = 0;//3: Function disable,2: Reserved,1: on,0: off
	public void OnAirClnrStatusChange(int power, int pm, int userCustAutoWDC) {
		mAirClnrUserCustAutoWDC = userCustAutoWDC;
		mProcessHandler.sendEmptyMessage(5);
	}
	private void processClnrStatusChange() {
		boolean checked = false;
		if(1 == mAirClnrUserCustAutoWDC) {
			checked = true;
		}
		CanPageSimpleItem item = mItemList.get(22);
	    
	    item.setItemChecked(checked);
	}
	private ProcessHandler mProcessHandler;
	static class ProcessHandler extends Handler{
		private CANActivity mCanActivity;
		ProcessHandler(CANActivity parent){
			mCanActivity = parent;
		}
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				if (null != mCanActivity) {
					mCanActivity.processOverSpeedStatusChange();
				}
			}
			break;
			case 1:
				if (null != mCanActivity) {
					mCanActivity.processAdasStatusChange();
				}
				break;
			case 2:
				if(null != mCanActivity) {
					mCanActivity.processPDCStatusChange();
				}
				break;
			case 3:
				if(null != mCanActivity) {
					mCanActivity.processFICMStatusChange();
				}
				break;
			case 4:
				if(null != mCanActivity) {
					mCanActivity.processBCMFCarStatusChange();
				}
				break;
			case 5:
				if(null != mCanActivity) {
					mCanActivity.processClnrStatusChange();
				}
				break;
				default:
					break;
			}
		}
	}	
}
