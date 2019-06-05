package com.carocean.launcher.appinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carocean.R;
import com.carocean.settings.fragment.DialogFragmentBase;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class Fragment_appinfo extends DialogFragmentBase {

	private ListView listview = null;

	private PackageManager pm;
	private int mFilter = appinfoUtil.FILTER_ALL_APP;
	private List<AppInfo> mlistAppInfo;
	private BrowseApplicationInfoAdapter browseAppAdapter = null;
	private Button btfinish; // 所有应用程序

	View mRootView;
	private Context mContext;

	public Fragment_appinfo(int filter) {
		// TODO Auto-generated constructor stub
		mFilter = filter;
	}

	private void initView(View rootView) {
		listview = (ListView) rootView.findViewById(R.id.listviewApp);
		mlistAppInfo = queryFilterAppInfo(mFilter); // 查询所有应用程序信息
		// 构建适配器，并且注册到listView
		browseAppAdapter = new BrowseApplicationInfoAdapter(getActivity(), mlistAppInfo);
		listview.setAdapter(browseAppAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AppInfo appInfo = mlistAppInfo.get(arg2);
				try {
					Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appInfo.getPkgName());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);	
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

		});
		btfinish = (Button) rootView.findViewById(R.id.btreturn);
		btfinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.launcher_appinfo_browse_app_list, container, false);
		initView(mRootView);
		return mRootView;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, 0);
		mContext = getActivity();
	}

	// 根据查询条件，查询特定的ApplicationInfo
	private List<AppInfo> queryFilterAppInfo(int filter) {
		pm = mContext.getPackageManager();
		// 查询所有已经安装的应用程序
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));// 排序
		List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo
		// 根据条件来过滤
		switch (filter) {
		case appinfoUtil.FILTER_ALL_APP: // 所有应用程序
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				appInfos.add(getAppInfo(app));
			}
			return appInfos;
		case appinfoUtil.FILTER_SYSTEM_APP: // 系统程序
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		case appinfoUtil.FILTER_THIRD_APP: // 第三方应用程序
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			break;
		case appinfoUtil.FILTER_SDCARD_APP: // 安装在SDCard的应用程序
			appInfos.clear();
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
					appInfos.add(getAppInfo(app));
				}
			}
			return appInfos;
		default:
			return null;
		}
		return appInfos;
	}

	// 构造一个AppInfo对象 ，并赋值
	private AppInfo getAppInfo(ApplicationInfo app) {
		AppInfo appInfo = new AppInfo();
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);
		return appInfo;
	}
}