package com.carocean.media.music;

import com.carocean.media.MediaListFragment;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.vmedia.media.PageMedia;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class MusicListFragment extends MediaListFragment {
	private static final int TIME_BACK_TO_PALYER = 14;
	private int miBackCount = TIME_BACK_TO_PALYER;

	public MusicListFragment() {
		mMediaType = MediaType.MEDIA_AUDIO;
		TAG = MusicListFragment.class.getSimpleName();
	}
	
	@Override
	public void onResume() {
		miBackCount = TIME_BACK_TO_PALYER;
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		miBackCount = TIME_BACK_TO_PALYER;
	}
	
	protected void init() {
		super.init();
		if (null != mView) {
			mListView.setOnItemClickListener(new MusicListOnItemClickListener());
			mListView.setOnTouchListener(new MusicListOnItemTouchListener());
		}
	}

	private class MusicListOnItemTouchListener implements AdapterView.OnTouchListener {
		public boolean onTouch(View arg0, MotionEvent arg1) {
			miBackCount = TIME_BACK_TO_PALYER;
			return false;
		}
	}
	
	@Override
	public void onClick(View arg0) {
		miBackCount = TIME_BACK_TO_PALYER;
		super.onClick(arg0);
	}

	private class MusicListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (MediaActivityProxy.isBindService()) {
				try {
					if (mDataList != null && mDataList.get(position) != null) {
						if (mDataList.get(position).getMediaType() == MediaType.MEDIA_DIR) {
							MediaActivityProxy.getService().requestDirList(true,
									mDataList.get(position).getDirId(), mMediaType);
						} else if (mDataList.get(position).getMediaType() == MediaType.MEDIA_ARTIST) {
							MediaActivityProxy.getService().requestArtistList(true,
									mDataList.get(position).getIndex());
						} else if (mDataList.get(position).getMediaType() == MediaType.MEDIA_ALBUM) {
							MediaActivityProxy.getService().requestAlbumList(true,
									mDataList.get(position).getIndex());
						} else {
							MediaActivityProxy.getService().requestPlayList(MediaType.MEDIA_AUDIO,
									0, position, mDataList.get(position).getFilePath());
									PageMedia.getInstance().showView(PageMedia.ViewType.ViewUsbMusic);
						}
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
			if (iListType == ListType.ALL_MUSIC_FILE || iListType == ListType.MUSIC_MIX || 
					iListType == ListType.ARTIST_MIX || iListType == ListType.ALBUM_MIX) {
				mDataList = MediaActivityProxy.getService().getFileList(iListType);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MediaPlayerMessage.UPDATE_PLAY_PROGRESS:
			Log.e(TAG, "count" + miBackCount);
			if (mActivity.isMediaPage() && isResumed()) {
				if (miBackCount-- <= 0) {
					PageMedia.getInstance().showView(PageMedia.ViewType.ViewUsbMusic);
				}
			}
			break;
		default:
			break;
		}
		return super.handleMessage(msg);
	}
}
