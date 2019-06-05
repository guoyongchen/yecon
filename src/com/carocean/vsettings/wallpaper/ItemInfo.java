package com.carocean.vsettings.wallpaper;

import android.graphics.drawable.Drawable;

public class ItemInfo {
	private Drawable drawable;
	private int id;

	public ItemInfo(Drawable paramDrawable, int paramInt) {
		id = paramInt;
		drawable = paramDrawable;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public int getId() {
		return id;
	}

	public void setDrawable(Drawable paramDrawable) {
		drawable = paramDrawable;
	}

	public void setId(int paramInt) {
		id = paramInt;
	}
}
