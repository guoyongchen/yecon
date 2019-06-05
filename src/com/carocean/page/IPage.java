package com.carocean.page;

import android.content.Context;
import android.view.View;

/**
 * @ClassName: IPage
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public abstract interface IPage {
	public abstract void addNotify();

	public abstract View getContentView(Context context, boolean isCurPage);

	public abstract boolean hasInnerAnim();

	public abstract void removeNotify();

	public abstract void onResume();
}
