package com.carocean.media.video;

import com.carocean.media.MediaListFragment;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;

import android.content.Intent;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;

public class VideoListFragment extends MediaListFragment {
	
	public VideoListFragment() {
		mMediaType = MediaType.MEDIA_VIDEO;
		TAG = "VideoListFragment";
	}

	protected void init() {
		super.init();
		if (null != mView) {
			mListView.setOnItemClickListener(new VideoListOnItemClickListener());
		}
	}

	private class VideoListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (MediaActivityProxy.isBindService()) {
				try {
					if (mDataList != null && mDataList.get(position) != null  && mDataList.get(position).getMediaType() == MediaType.MEDIA_DIR) {
						MediaActivityProxy.getService().requestDirList(true, mDataList.get(position).getDirId(), mMediaType);
					} else {
						MediaActivityProxy.getService().requestPlayList(mMediaType,
								0,
								position, mDataList.get(position).getFilePath());
						mActivity.startActivity(new Intent(mActivity, VideoActivity.class));
						PageMedia.setView(ViewType.ViewVideo);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void updateList(int iListType) {
		try {
			if (iListType == ListType.ALL_VIDEO_FILE || iListType == ListType.VIDEO_MIX) {
				mDataList = MediaActivityProxy.getService().getFileList(iListType);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
