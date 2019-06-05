package com.carocean.floatwindow;

import static android.constant.YeconConstants.BLUETOOTH_PACKAGE_NAME;
import static android.constant.YeconConstants.BLUETOOTH_START_ACTIVITY;
import static android.constant.YeconConstants.FMRADIO_PACKAGE_NAME;
import static android.constant.YeconConstants.FMRADIO_START_ACTIVITY;
import static android.constant.YeconConstants.MEDIA_PACKAGE_NAME;
import static android.constant.YeconConstants.MEDIA_START_ACTIVITY;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.floatwindow.fUtils.FLOAT_WINDOW_TYPE;
import com.carocean.utils.sLog;
import com.yecon.settings.YeconSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
/**
 * @ClassName: FloatWindowManager
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class FloatWindowManager {

	private static final String TAG = FloatWindowManager.class.getSimpleName();
	private static FloatWindowManager windowManagerInstance = null;
	WindowManager mWindowManager;
	WindowManager.LayoutParams mLParams = null;
	FloatWindow mFloatWindow;


	public FloatWindowManager() {
		// TODO Auto-generated constructor stub
		Bitmap mBitmapBG = BitmapFactory.decodeResource(ApplicationManage.getInstance().getResources(),
				R.drawable.f_media_bk);
		if (mLParams == null) {
			mLParams = new WindowManager.LayoutParams();
			mLParams.type = LayoutParams.TYPE_PHONE; // TYPE_SYSTEM_ERROR;
			mLParams.width = mBitmapBG.getWidth();

			mLParams.height = mBitmapBG.getHeight();
			mLParams.format = PixelFormat.RGBA_8888;
			mLParams.flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM | LayoutParams.FLAG_NOT_FOCUSABLE
			// | LayoutParams.FLAG_LAYOUT_IN_SCREEN
			// |LayoutParams.FLAG_LAYOUT_NO_LIMITS
					| LayoutParams.FLAG_LAYOUT_INSET_DECOR;
			mLParams.gravity = Gravity.TOP;
		}
	}

	public static FloatWindowManager getInstance() {
		if (windowManagerInstance == null) {
			windowManagerInstance = new FloatWindowManager();
		}
		return windowManagerInstance;
	}

	private WindowManager getWindowManager() {
		if (mWindowManager == null) {
			mWindowManager = ((WindowManager) ApplicationManage.getContext().getSystemService(Context.WINDOW_SERVICE));
		}
		return mWindowManager;
	}

	public void createFloatWindow(FLOAT_WINDOW_TYPE eType) {
		fUtils.floatwindowType = eType;
		if (mFloatWindow == null) {
			Context context = ApplicationManage.getContext();
			mFloatWindow = new FloatWindow(context);
			getWindowManager().addView(mFloatWindow, mLParams);
		} else {
			mFloatWindow.showWindow(eType);
		}
	}

	public void removeFloatWindow() {
		if (mFloatWindow != null) {
			getWindowManager().removeView(mFloatWindow);
			mFloatWindow = null;
			fUtils.floatwindowType = null;
		}
	}

	public boolean isExistFloatWindow() {
		return mFloatWindow != null ? true : false;
	}

	public void showFloatWindow(boolean bShow) {
		if (!bShow) {
			removeFloatWindow();
		}
	}

	public boolean moveTaskToBack(Activity activity, Intent intent, FLOAT_WINDOW_TYPE eType) {
		boolean bMoveTaskToBack = intent.getBooleanExtra("moveTaskToBack", false);
		if (bMoveTaskToBack) {
			if (mFloatWindow != null && fUtils.floatwindowType == eType) {
				switch (eType) {
				case FLOAT_MEDIA:
					YeconSettings.RunApp(activity, false, MEDIA_PACKAGE_NAME, MEDIA_START_ACTIVITY);
					break;
				case FLOAT_RADIO:
					YeconSettings.RunApp(activity, false, FMRADIO_PACKAGE_NAME, FMRADIO_START_ACTIVITY);
					break;
				case FLOAT_BT:
					YeconSettings.RunApp(activity, false, BLUETOOTH_PACKAGE_NAME, BLUETOOTH_START_ACTIVITY);
					break;
				default:
					break;
				}
				
			} else {
				createFloatWindow(eType);
				YeconSettings.setAutoNaviScreenMode(activity, YeconSettings.SCREEN_MODE_SPLIT_BOTTOM);
				activity.moveTaskToBack(true);
			}
		} else {
			removeFloatWindow();
		}
		sLog.d(TAG, "....moveTaskToBack......................................" + bMoveTaskToBack);
		return bMoveTaskToBack;
	}

}
