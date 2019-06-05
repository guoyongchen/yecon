package com.carocean.settings.screensaver;

import com.carocean.ApplicationManage;
import com.carocean.R;

public class scUtils {

	public static final int ScreenSaverTimes[] = { 10 * 1000, 30 * 1000, 60 * 1000, 5 * 60 * 1000, 0 };
	public static int timesIndex = 4;
	public static int imageSelected = 0;
	public static int[] imageIds = { R.drawable.screensaver_00, R.drawable.screensaver_01, R.drawable.screensaver_02, R.drawable.screensaver_03,
			R.drawable.screensaver_04, R.drawable.screensaver_05, R.drawable.screensaver_06, R.drawable.screensaver_07, };
	public static String[] styles = ApplicationManage.getContext().getResources().getStringArray(R.array.screensaver_styles_values);
}
