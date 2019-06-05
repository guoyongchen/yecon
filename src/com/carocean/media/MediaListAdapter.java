package com.carocean.media;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.carocean.R;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.scan.MediaFileParse;
import com.carocean.media.scan.MediaObject;
import com.carocean.media.service.MediaActivityProxy;

import java.util.ArrayList;
import java.util.List;

public class MediaListAdapter extends BaseAdapter {
	private final String TAG = getClass().getName();
	private List<MediaObject> mDataList = new ArrayList<MediaObject>();
	private Activity mActivity = null;
	private int mMediaType = MediaType.MEDIA_AUDIO;
	private boolean mPlayStatus = false;

	public MediaListAdapter(Activity activity, List<MediaObject> list, int mediaType) {
		mMediaType = mediaType;
		mActivity = activity;
		mDataList = list;
	}

	public void setPlayStatus(boolean paramBoolean, boolean bFresh) {
		// bFresh:强制刷新
		if (bFresh || (mPlayStatus != paramBoolean && mMediaType != MediaType.MEDIA_IMAGE && mMediaType != MediaType.MEDIA_DIR)) {
			mPlayStatus = paramBoolean;
			Log.i(TAG, "media---setPlayStatus=" + mPlayStatus);
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		if (mDataList != null) {
			return mDataList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mDataList != null) {
			return mDataList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void setDatas(List<MediaObject> datas, boolean bNotifyChanged) {
		mDataList = new ArrayList<MediaObject>(datas);
		if (bNotifyChanged) {
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Log.i(TAG, "getView position=" + position);
		ViewHolder viewHolder = null;
		try {
			if (view == null) {
				viewHolder = new ViewHolder();
				view = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.media_file_item, null);
				viewHolder.mItem = (LinearLayout) view.findViewById(R.id.file_list_item);
				viewHolder.mIcon = (ImageView) view.findViewById(R.id.file_list_title);
				viewHolder.mFolderIcon = (ImageView) view.findViewById(R.id.file_list_folder_bg);
				viewHolder.mTextView = (HorizontalScrollTextView) view.findViewById(R.id.file_list_music_info);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			final Object obj = getItem(position);
			if (obj == null) {
				return view;
			} else {
				if (obj instanceof MediaObject) {
					MediaObject data = (MediaObject) obj;
					// viewHolder.mTextView.setText((position + 1) + "." +
					// data.getFileName());
					viewHolder.mTextView.setText(data.getFileName());
					viewHolder.mTextView.init();
					if (data.getMediaType() == MediaType.MEDIA_DIR) {
						viewHolder.mFolderIcon.setBackgroundResource(R.drawable.media_folder_bg);
						viewHolder.mFolderIcon.setVisibility(View.VISIBLE);
					} else if (data.getMediaType() == MediaType.MEDIA_ARTIST) {
						viewHolder.mFolderIcon.setBackgroundResource(R.drawable.media_artist_bg);
						viewHolder.mFolderIcon.setVisibility(View.VISIBLE);
					} else if (data.getMediaType() == MediaType.MEDIA_ALBUM) {
						viewHolder.mFolderIcon.setBackgroundResource(R.drawable.media_album_bg);
						viewHolder.mFolderIcon.setVisibility(View.VISIBLE);
					} else if (mDataList.get(0).getMediaType() == MediaType.MEDIA_DIR || 
							mDataList.get(0).getMediaType() == MediaType.MEDIA_ARTIST ||
							mDataList.get(0).getMediaType() == MediaType.MEDIA_ALBUM) {
						// 文件夹列表中的文件，需要缩进
						viewHolder.mFolderIcon.setVisibility(View.INVISIBLE);
					} else {
						viewHolder.mFolderIcon.setVisibility(View.GONE);
					}
					String strFile = MediaActivityProxy.getService().getFileName(mMediaType);
					boolean bSelect = false;
					if (strFile != null && !strFile.equals("")) {
						if (data.getMediaType() == MediaType.MEDIA_DIR
								&& data.getFilePath().equals(MediaFileParse.getParentDir(strFile))) {
							bSelect = true;
							viewHolder.mIcon.setVisibility(View.INVISIBLE);
							viewHolder.mItem.setSelected(true);
							viewHolder.mTextView.startScroll();
						} else if (data.getFilePath().equals(strFile)) {
							bSelect = true;
							viewHolder.mIcon.setVisibility(View.VISIBLE);
							viewHolder.mItem.setSelected(true);
							viewHolder.mTextView.startScroll();
							AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.mIcon.getDrawable();
							if (mMediaType == MediaType.MEDIA_AUDIO) {
								// 图片列表不需要动态显示
								if ((animationDrawable != null)) {
									Log.i(TAG, "media---mPlayStatus=" + mPlayStatus);
									if (mPlayStatus) {
										animationDrawable.start();
									} else {
										animationDrawable.stop();
									}
								}
							} else {
								if ((animationDrawable != null)) {
									animationDrawable.stop();
								}
							}
						}
					}
					if (!bSelect) {
						AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.mIcon.getDrawable();
						if (animationDrawable != null) {
							animationDrawable.stop();
						}
						viewHolder.mIcon.setVisibility(View.GONE);
						viewHolder.mTextView.stopScroll();
						viewHolder.mItem.setSelected(false);
					}
				}
			}

		} catch (Exception e) {

		}
		return view;
	}

	public static class ViewHolder {
		LinearLayout mItem;
		ImageView mIcon;
		ImageView mFolderIcon;
		HorizontalScrollTextView mTextView;
	}
}
