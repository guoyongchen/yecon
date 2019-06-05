package com.carocean.vsettings.theme;

import android.graphics.drawable.Drawable;

/**
 * @ClassName: ItemInfo
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.04.24
 **/
public class ItemInfo {
	private Drawable drawable;
	private String name;

	public ItemInfo(Drawable paramDrawable, String paramString) {
		drawable = paramDrawable;
		name = paramString;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public String getName() {
		return name;
	}

	public void setDrawable(Drawable paramDrawable) {
		drawable = paramDrawable;
	}

	public void setName(String paramString) {
		name = paramString;
	}
}