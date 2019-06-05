package com.carocean.vmedia.bt;

import com.carocean.R;
import com.carocean.bt.BTUtils;
import com.carocean.bt.BTService;
import com.carocean.bt.ui.BtCallHistoryFragment;
import com.carocean.bt.ui.BtDialPadFragment;
import com.carocean.bt.ui.BtPhoneCallFragment;
import com.carocean.bt.ui.BtPhonebookFragment;
import com.carocean.bt.ui.BtSettingFragment;
import com.carocean.page.IPage;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.constant.YeconConstants;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * @ClassName: PageBT
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageBT implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {

	private static final String TAG = "PageBT";
	Context mContext;
	private ViewGroup mRootView;
	private RadioGroup mRadioGroup;
	private BtSettingFragment mBtSettingFragment = new BtSettingFragment();
	private BtCallHistoryFragment mBtCallHistoryFragment = new BtCallHistoryFragment();
	private BtPhonebookFragment mBtPhonebookFragment = new BtPhonebookFragment();
	private BtDialPadFragment mBtDialPadFragment = new BtDialPadFragment();
	private BtPhoneCallFragment mBtPhoneCallFragment = new BtPhoneCallFragment();
	
	private RadioButton rb_callbook, rb_contact, rb_record, rb_setting;
	private Fragment curpage, prepage;

	private static PageBT mPageBT = null;
	public static PageBT getInstance() {
		return mPageBT;
	}
	void init(Context context) {
		Log.e(TAG, "init");
		mContext = context;
		addOnSoftKeyBoardVisibleListener(MediaActivity.mActivity, mBtSettingFragment);
	}

	void initView(ViewGroup root) {
		Log.e(TAG, "initView");
		mRadioGroup = (RadioGroup) root.findViewById(R.id.bt_page_type_radiogroup);
		rb_callbook = ((RadioButton) mRadioGroup.findViewById(R.id.bt_call_btn));
		rb_contact = ((RadioButton) mRadioGroup.findViewById(R.id.bt_contact_btn));
		rb_record = ((RadioButton) mRadioGroup.findViewById(R.id.bt_record__btn));
		rb_setting = ((RadioButton) mRadioGroup.findViewById(R.id.bt_set_btn));

		rb_callbook.setOnClickListener(this);
		rb_contact.setOnClickListener(this);
		rb_record.setOnClickListener(this);
		rb_setting.setOnClickListener(this);
	}

	@Override
	public void addNotify() {
		Log.e(TAG, "addNotify");
		BTService.registerNotifyHandler(uiHandler);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onResume");
		checkbtcallpage();
	}

	public void checkbtcallpage(){
		Log.e(TAG, "checkbtcallpage curpage=" + curpage + " prepage=" + prepage + " BTService.bkeyphoneon=" + BTUtils.bkeyphoneon
				+ " firstpagetype=" + firstpagetype.toString());
		if (BTUtils.mBluetooth.iscallidle()) {
			setEnabled(true);
			if (curpage == mBtPhoneCallFragment) {
				if (prepage != null) {
					showFragment(prepage);
				}else{
					showFragment(mBtSettingFragment);
				}
			}else if (BTUtils.bkeyphoneon) {
				showFragment(mBtDialPadFragment);
			}else if (curpage == null){
				switch (firstpagetype) {
				case TAB_INDEX_BT_CALL_BOOK:
					showFragment(mBtDialPadFragment);
					break;
				case TAB_INDEX_BT_CONTACT:
					showFragment(mBtPhonebookFragment);
					break;
				case TAB_INDEX_BT_RECORD:
					showFragment(mBtCallHistoryFragment);
					break;
				case TAB_INDEX_BT_SETTINGS:
					showFragment(mBtSettingFragment);
					break;
				case TAB_INDEX_BT_CALL:
					showFragment(mBtPhoneCallFragment);
					break;

				default:
					showFragment(mBtSettingFragment);
					break;
				}
			}else{
				curpage.onResume();
			}
			
		}else{
			setEnabled(false);
			if (curpage != mBtPhoneCallFragment) {
				prepage = curpage;
				showFragment(mBtPhoneCallFragment);
			}
		}
		BTUtils.bkeyphoneon = false;
		firstpagetype = PageType.NONE;
	}
	
	@Override
	public View getContentView(Context context, boolean isCurPage) {
		Log.e(TAG, "getContentView mRootView=" + mRootView);
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_bt, null));
			init(context);
			initView(mRootView);
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
		Log.e(TAG, "removeNotify");
		BTService.unregisterNotifyHandler(uiHandler);
	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_call_btn:
			showFragment(mBtDialPadFragment);
			break;
		case R.id.bt_contact_btn:
			showFragment(mBtPhonebookFragment);
			break;
		case R.id.bt_record__btn:
			showFragment(mBtCallHistoryFragment);
			break;
		case R.id.bt_set_btn:
			showFragment(mBtSettingFragment);
			break;
		default:
			break;
	}
	}
	void changeProgress(CustomSeekbar seekbar, int offset) {
		int pos = seekbar.getProgress() + offset;
		seekbar.setProgress(pos);
		onChanged(seekbar);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (uiHandler.hasMessages(view.getId()))
				uiHandler.removeMessages(view.getId());
			uiHandler.sendEmptyMessageDelayed(view.getId(), 100);
			break;
		case MotionEvent.ACTION_UP:
			if (uiHandler.hasMessages(view.getId()))
				uiHandler.removeMessages(view.getId());
			break;
		default:
			break;
		}
		return false;
	}

	public static int MSG_BT_SHOWFRAGMENT = 1001;
	Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_BT_SHOWFRAGMENT) {
				myshowFragment((Fragment)msg.obj);
				return;
			}

			if (msg.what == BTService.MSG_BT_STATUS_NOTIFY && MediaActivity.bforeground) {
				Intent intent = (Intent) msg.obj;
				String action = intent.getAction();

				if (action.equals(BTService.ACTION_CONNECTED) || action.equals(BTService.ACTION_DISCONNECTED)) {
					checkbtcallpage();
				} else if (action.equals(BTService.ACTION_CALL_STATE) && MediaActivity.bforeground) {
					checkbtcallpage();
				} else if (action.equals(YeconConstants.ACTION_YECON_KEY_UP)) {
					Log.e(TAG, "ACTION_YECON_KEY_UP");
		        	int keyCode = intent.getIntExtra("key_code", 0);
		        	if (keyCode == KeyEvent.KEYCODE_YECON_PHONE_ON) {
						checkbtcallpage();
		        	}
				}
			}
		}
	};

	private void setEnabled(boolean enabled) {
		rb_callbook.setEnabled(enabled);
		rb_contact.setEnabled(enabled);
		rb_record.setEnabled(enabled);
		rb_setting.setEnabled(enabled);
	}
	
	
	
	private void showFragment(Fragment fragment) {
		if (uiHandler.hasMessages(MSG_BT_SHOWFRAGMENT)) {
			uiHandler.removeMessages(MSG_BT_SHOWFRAGMENT);
		}
		Message msg = new Message();
		msg.what = MSG_BT_SHOWFRAGMENT;
		msg.obj = fragment;
		uiHandler.sendMessageDelayed(msg, 50);
	}
	private void myshowFragment(Fragment fragment) {
		Log.e(TAG, "showFragment curpage=" + curpage + " prepage=" + prepage + " fragment=" + fragment.toString());
		if (fragment == mBtDialPadFragment) {
			rb_callbook.setChecked(true);
		}else if (fragment == mBtPhonebookFragment) {
			rb_contact.setChecked(true);
		}else if (fragment == mBtCallHistoryFragment) {
			rb_record.setChecked(true);
		}else if (fragment == mBtSettingFragment) {
			rb_setting.setChecked(true);
		}else if (fragment == mBtPhoneCallFragment) {
			rb_callbook.setChecked(true);
		}
		
		if (fragment != null && fragment != curpage) {
			FragmentManager ObjFragmentManager = ((Activity) mContext).getFragmentManager();
			FragmentTransaction ObjTransaction = ObjFragmentManager.beginTransaction();
			if (null != ObjFragmentManager && null != ObjTransaction) {
				ObjTransaction.replace(R.id.bt_fragment, fragment);
				if (null != curpage) {
					ObjTransaction.remove(curpage);
				}
				ObjTransaction.commitAllowingStateLoss();
				curpage = fragment;
			}
		}else if (fragment != null && fragment == curpage) {
			curpage.onResume();
		}
	}

    public interface IKeyBoardVisibleListener{  
        void onSoftKeyBoardVisible(boolean visible , int windowBottom);  
    }
    
    static boolean lastvisible = false;
    
    public void addOnSoftKeyBoardVisibleListener(Activity activity, final IKeyBoardVisibleListener listener) {
    	final View decorView = activity.getWindow().getDecorView();
    	decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
    		@Override
    		public void onGlobalLayout() {
    			Rect rect = new Rect();
    			decorView.getWindowVisibleDisplayFrame(rect);
    			int displayHight = rect.bottom - rect.top;
    			int hight = decorView.getHeight();
    			boolean visible = (double) displayHight / hight < 0.8;
    			Log.e(TAG, "onGlobalLayout visible=" + visible);
    			if (lastvisible != visible) {
    				listener.onSoftKeyBoardVisible(visible, 0);
    				MediaActivity.mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    			}
    			lastvisible = visible;
    		}
    	});
    	
    	Log.e(TAG, "addOnSoftKeyBoardVisibleListener");
    }
    
    public void setupkeybordlistener(Activity ac){
    	addOnSoftKeyBoardVisibleListener(ac, mBtSettingFragment);
    }
    
	private static PageType firstpagetype = PageType.NONE;

	public enum PageType {
		TAB_INDEX_BT_CALL, // 来/去/通电
		TAB_INDEX_BT_CALL_BOOK, // 拨号
		TAB_INDEX_BT_CONTACT, // 联系人
		TAB_INDEX_BT_RECORD, // 通话记录
		TAB_INDEX_BT_SETTINGS, // 设置
		NONE
	}

	public static void setPageType(PageType type) {
		firstpagetype = type;
	}
}
