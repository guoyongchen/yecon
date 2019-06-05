package com.carocean.vsettings.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @ClassName: ItemInfo
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2018.04.24
 **/
@SuppressLint("ViewHolder")
public class WallpaperGridViewAdapter extends BaseAdapter {
	public static Context mContext;
	ViewHolder mViewHolder = null;
	private LayoutInflater mInflater;
	List<ItemInfo> mList = new ArrayList<ItemInfo>();
	private int mSelectItem;

	public WallpaperGridViewAdapter(Context context, List<ItemInfo> list) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mList = list;

	}

	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	public ItemInfo getItem(int paramInt) {
		return (ItemInfo) mList.get(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	public void setSelectItem(int paramInt) {
		mSelectItem = paramInt;
		notifyDataSetChanged();
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int paramInt, View view, ViewGroup viewGrop) {
		// TODO Auto-generated method stub
		if (view == null) {
			mViewHolder = new ViewHolder();
		}
		view = mInflater.inflate(R.layout.setting_layout_wallpaper_item, null);
		view.setTag(mViewHolder);
		mViewHolder.imageView = ((ImageView) view.findViewById(R.id.wallpaper_image));
		mViewHolder.imageView.setBackgroundResource(R.drawable.setting_ic_wallpaperbg_n);
		mViewHolder.imageView.setImageDrawable(getItem(paramInt).getDrawable());
		mViewHolder.imageView.setPadding(3, 2, 2, 2);

		if (paramInt == mSelectItem) {
			mViewHolder.imageView.setBackgroundResource(R.drawable.setting_ic_wallpaperbg_p);
		}
		return view;
	}

	private static class ViewHolder {
		ImageView imageView;
	}
}
