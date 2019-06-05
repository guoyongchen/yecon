package com.carocean.vsettings.theme;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.DataShared;
import com.carocean.utils.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * @ClassName: PageTheme
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.04.24
 **/
public class PageTheme implements IPage {
	private Context mContext;
	private ViewGroup mRootView;
	private List<Drawable> drawables = new ArrayList<Drawable>();
	private List<String> strTheme = new ArrayList<String>();
	private GridView gridView;
	private ThemeGridViewAdapter gridViewAdapter;
	private ItemInfo info;
	private List<ItemInfo> itemInfo = new ArrayList<ItemInfo>();
	final int[] ids = { R.drawable.setting_ic_theme_0, R.drawable.setting_ic_theme_1, R.drawable.setting_ic_theme_2, };
	final int[] ids_str = { R.string.theme_type_0, R.string.theme_type_1, R.string.theme_type_2, };

	private static final int HANDLE_THEME_SET = 100;

	void init() {
		drawables.clear();
		strTheme.clear();
		itemInfo.clear();

		for (int i = 0; i < ids.length; i++) {
			drawables.add(mContext.getResources().getDrawable(ids[i]));
			strTheme.add(mContext.getResources().getString(ids_str[i]));
			info = new ItemInfo(drawables.get(i), strTheme.get(i));
			itemInfo.add(info);
		}
	}

	void initView() {
		gridView = (GridView) mRootView.findViewById(R.id.gridview_theme);
		gridViewAdapter = new ThemeGridViewAdapter(mContext, itemInfo);
		gridViewAdapter.setSelectItem(launcherUtils.mTheme);
		gridView.setAdapter(gridViewAdapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				gridViewAdapter.setSelectItem(arg2);
				if (mHandler.hasMessages(HANDLE_THEME_SET))
					mHandler.removeMessages(HANDLE_THEME_SET);
				Message message = new Message();
				message.what = HANDLE_THEME_SET;
				message.arg1 = arg2;
				mHandler.sendMessage(message);
			}
		});
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (HANDLE_THEME_SET == msg.what) {
				Utils.showToast(mContext.getResources().getString(R.string.vsetting_set_theme_success));
				launcherUtils.mTheme = msg.arg1;
				if (Utils.mThemeChangeListener != null)
					Utils.mThemeChangeListener.onItemClick(launcherUtils.mTheme);
				DataShared.getInstance(mContext).putInt(SettingConstants.key_ui_theme, launcherUtils.mTheme);
				DataShared.getInstance(mContext).commit();
				Utils.TransKey(KeyEvent.KEYCODE_HOME);
			}
		}
	};

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		mContext = context;
		mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_theme, null));
		init();
		initView();
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
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
