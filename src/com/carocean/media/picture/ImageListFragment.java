package com.carocean.media.picture;

import java.util.ArrayList;
import java.util.List;

import com.carocean.media.MediaListFragment;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.scan.MediaObject;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;

public class ImageListFragment extends MediaListFragment {
	private List<MediaObject> mListImage = new ArrayList<MediaObject>();
	private final int MSG_START_IMAGE_ACTIVITY = 255;
	public ImageListFragment() {
		mMediaType = MediaType.MEDIA_IMAGE;
		TAG = ImageListFragment.class.getSimpleName();
	}
	
	protected void init() {
		super.init();
		if (mView != null) {
			mListView.setOnItemClickListener(new MusicListOnItemClickListener());
		}
	}

	private class MusicListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				if (MediaActivityProxy.isBindService()) {
					if (mDataList != null && mDataList.get(position) != null
							&& mDataList.get(position).getMediaType() == MediaType.MEDIA_DIR) {
						MediaActivityProxy.getService().requestDirList(true,
								mDataList.get(position).getDirId(), mMediaType);
					} else {
						mHandler.removeMessages(MSG_START_IMAGE_ACTIVITY);
						Message message = mHandler.obtainMessage();
						message.what = MSG_START_IMAGE_ACTIVITY;
						message.arg1 = MediaActivityProxy.getService().getListType(mMediaType);
						message.arg2 = position;
						message.obj = mDataList.get(position).getFileName();
						mHandler.sendMessage(message);
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void updateList(int iListType) {
		try {
			if (iListType == ListType.ALL_IMAGE_FILE || iListType == ListType.IMAGE_MIX) {
				mDataList = MediaActivityProxy.getService().getFileList(iListType);
			}
			mListImage = MediaActivityProxy.getService().getImageList();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void startImage(Context context, int position, int iAllfilePos, boolean isFolder) {
		Intent intent = new Intent(context, ImagePlayActivity.class);
		if (isFolder) {
			intent.putExtra(ImagePlayActivity.EXTRA_IMAGE_URLS, "folder");
		} else {
			intent.putExtra(ImagePlayActivity.EXTRA_IMAGE_URLS, "file");
		}
		intent.putExtra(ImagePlayActivity.EXTRA_IMAGE_INDEX, position);
		intent.putExtra(ImagePlayActivity.EXTRA_IMAGE_ALL_FILE_INDEX, iAllfilePos);
		context.startActivity(intent);
	}

	@Override
	public boolean handleMessage(Message msg) {
		try {
			switch (msg.what) {
			case MSG_START_IMAGE_ACTIVITY:
				int position = msg.arg2;
				if (msg.arg1 == ListType.IMAGE_MIX && mListImage != null) {
					for (int i = 0; i < mListImage.size(); i++) {
						if (mListImage.get(i).getFileName().equals((String)msg.obj)) {
							position = i;
							break;
						}
					}
				}
				PageMedia.setView(ViewType.ViewPicture);
				MediaActivityProxy.getService().requestPlayImage(position);
				startImage(mActivity, position, position, false);
				break;
			default:
				break;
			}
		} catch (RemoteException e) {

		}
		return super.handleMessage(msg);
	}
}
