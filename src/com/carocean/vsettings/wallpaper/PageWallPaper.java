package com.carocean.vsettings.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;
import com.carocean.page.IPage;
import com.carocean.settings.utils.SettingConstants;
import com.carocean.utils.DataShared;
import com.carocean.vsettings.settings.Utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PageWallPaper implements IPage {
	private Context mContext;
	private ViewGroup mContentView;
	private List<Drawable> drawables = new ArrayList<Drawable>();
	private GridView gridView;
	private WallpaperGridViewAdapter gridViewAdapter;
	private ItemInfo info;
	private List<ItemInfo> itemInfo = new ArrayList<ItemInfo>();
	public final static int[] ids = { R.drawable.setting_ic_wallpaper_0, R.drawable.setting_ic_wallpaper_1,
			R.drawable.setting_ic_wallpaper_2, R.drawable.setting_ic_wallpaper_3 };
	final int[] ids_small = { R.drawable.setting_ic_wallpaper_small_0, R.drawable.setting_ic_wallpaper_small_1,
			R.drawable.setting_ic_wallpaper_small_2, R.drawable.setting_ic_wallpaper_small_3 };
	private static final int HANDLE_WALLPAPER_SET = 100;
	private static final int HANDLE_WALLPAPER_SET_FINISH = 1000;
	private boolean flag = false;

	void init() {
		drawables.clear();
		itemInfo.clear();
		for (int i = 0; i < ids_small.length; i++) {
			drawables.add(mContext.getResources().getDrawable(ids_small[i]));
			info = new ItemInfo((Drawable) drawables.get(i), ids_small[i]);
			itemInfo.add(info);
		}
	}

	void initView() {
		gridView = (GridView) mContentView.findViewById(R.id.gridview_wallpaper);
		gridViewAdapter = new WallpaperGridViewAdapter(mContext, itemInfo);
		gridViewAdapter.setSelectItem(0);
		gridView.setAdapter(gridViewAdapter);
		gridView.setSelector(new ColorDrawable(0));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				gridViewAdapter.setSelectItem(arg2);
				if (mHandler.hasMessages(HANDLE_WALLPAPER_SET))
					mHandler.removeMessages(HANDLE_WALLPAPER_SET);
				Message message = new Message();
				message.what = HANDLE_WALLPAPER_SET;
				message.arg1 = arg2;
				mHandler.sendMessage(message);

			}
		});

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (HANDLE_WALLPAPER_SET == msg.what) {
				if (!flag) {
					gridView.setEnabled(false);
					launcherUtils.mWallpaper = msg.arg1;
					Utils.showToast(mContext.getResources().getString(R.string.vsetting_set_wallpaper_going));
					new Thread(runnable).start();
				}
			} else if (HANDLE_WALLPAPER_SET_FINISH == msg.what) {
				gridView.setEnabled(true);
			}
		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// 这是新的线程，可在这儿处理耗时的操作，更新不了UI界面的操作的
			flag = true;
			Utils.setWallPaper(ids[launcherUtils.mWallpaper]);
			DataShared.getInstance(mContext).putInt(SettingConstants.key_ui_wallpaper, launcherUtils.mWallpaper);
			DataShared.getInstance(mContext).commit();
			Utils.showToast(mContext.getResources().getString(R.string.vsetting_set_wallpaper_success));
			Message message = new Message();
			message.what = HANDLE_WALLPAPER_SET_FINISH;
			mHandler.sendMessage(message);
			flag = false;
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
		mContentView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.setting_layout_wallpaper, null));
		init();
		initView();
		return mContentView;
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
