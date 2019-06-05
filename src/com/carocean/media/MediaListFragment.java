package com.carocean.media;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;
import com.carocean.media.MediaListAdapter;
import com.carocean.media.constants.MediaPlayerContants;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.constants.MediaPlayerContants.ListType;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.PlayStatus;
import com.carocean.media.constants.MediaPlayerContants.ServiceStatus;
import com.carocean.media.scan.MediaObject;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaStorage;
import com.carocean.vmedia.MediaActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class MediaListFragment extends MediaFragment implements OnClickListener, Callback{

	protected String TAG = "";
	protected View mView;
	protected MediaListAdapter mAdapter;
	protected ListView mListView;
	protected TextView mTextView;
	protected LinearLayout mLinearLayout;
	int[] miSourceIds = { R.id.media_btn_local, R.id.media_btn_usb1, R.id.media_btn_usb2 };
	protected TextView[] mSourceTvs = new TextView[miSourceIds.length];
	protected TextView mBtnAllFile, mBtnFolder, mBtnArtist, mBtnAlbum;
	protected LinearLayout mLayoutCtrol, mLayoutSource, mLayoutFileType, mLayoutSel;
	protected TextView mBtnFresh, mBtnSource, mBtnFileType;
	protected List<MediaObject> mDataList = new ArrayList<MediaObject>();
	protected MediaActivity mActivity = null;
	protected int mMediaType = MediaType.MEDIA_AUDIO;

	protected Handler mHandler = new Handler(this);
	
	public void onAttach(Activity activity) {
		Log.i(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = (MediaActivity) activity;
		MediaActivityProxy.getInstance().RegisterHandler(mHandler);
	}

	protected void recoverDevice(Activity activity) {
		Log.i(TAG, "recoverDevice");
		try {
			Intent intent = activity.getIntent();
			String device = null;
			if (intent != null) {
				device = intent.getStringExtra("plugindevice");
				if (device == null) {
					if (MediaActivityProxy.isBindService()
							&& MediaActivityProxy.getService().getPlayingStorage() == null) {
						device = MediaPlayerContants.LAST_MEMORY_DEVICE;
					} else {
						device = MediaPlayerContants.LAST_MEMORY_DEVICE;
					}
				}
			}

			if (device != null && device.length() > 0) {
				Log.i(TAG, "recoverDevice -> loading " + device);
				MediaActivityProxy.getInstance().AttachStorage(device, mMediaType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		if (null == mView) {
			mView = inflater.inflate(R.layout.activity_media_file, null);
			init();
			initData();
		}
		return mView;
	}

	protected void init() {
		if (null != mView) {
			mBtnFresh = (TextView) mView.findViewById(R.id.media_btn_fresh);
			mBtnFresh.setOnClickListener(this);
			mListView = (ListView) mView.findViewById(R.id.file_list_info);
			mTextView = (TextView) mView.findViewById(R.id.usb_music_text_prompt);
			mLinearLayout = (LinearLayout) mView.findViewById(R.id.usb_music_prompt);
			for (int i = 0; i < mSourceTvs.length; i++) {
				mSourceTvs[i] = (TextView) mView.findViewById(miSourceIds[i]);
				mSourceTvs[i].setOnClickListener(this);
			}
			mBtnSource = (TextView) mView.findViewById(R.id.media_btn_source);
			mBtnFileType = (TextView) mView.findViewById(R.id.media_btn_file_type);
			mBtnAllFile = (TextView) mView.findViewById(R.id.media_btn_all_file);
			mBtnFolder = (TextView) mView.findViewById(R.id.media_btn_folder);
			mBtnArtist = (TextView) mView.findViewById(R.id.media_btn_artist);
			mBtnAlbum = (TextView) mView.findViewById(R.id.media_btn_album);
			
			mBtnSource.setOnClickListener(this);
			mBtnFileType.setOnClickListener(this);
			mBtnAllFile.setOnClickListener(this);
			mBtnFolder.setOnClickListener(this);
			mBtnArtist.setOnClickListener(this);
			mBtnAlbum.setOnClickListener(this);
			mLayoutCtrol = (LinearLayout) mView.findViewById(R.id.media_list_ctrol_layout);
			mLayoutSource = (LinearLayout) mView.findViewById(R.id.media_source_layout);
			mLayoutFileType = (LinearLayout) mView.findViewById(R.id.media_file_type_layout);
			mAdapter = new MediaListAdapter(mActivity, mDataList, mMediaType);
			mListView.setAdapter(mAdapter);
			if (mMediaType == MediaType.MEDIA_AUDIO) {
				mBtnAllFile.setText(getstring(R.string.media_str_all_music));
				mBtnAllFile.setBackgroundResource(R.drawable.media_btn_all_music_bg);
			} else if (mMediaType == MediaType.MEDIA_VIDEO) {
				mLayoutFileType.setBackgroundResource(R.drawable.media_file_group_bg1);
				mBtnAllFile.setText(getstring(R.string.media_str_all_video));
				mBtnAllFile.setBackgroundResource(R.drawable.media_btn_all_video_bg);
				mBtnArtist.setVisibility(View.GONE);
				mBtnAlbum.setVisibility(View.GONE);
				
				mLayoutSource.setBackgroundResource(R.drawable.media_file_group_bg1);
				mSourceTvs[0].setVisibility(View.GONE);
			} else if (mMediaType == MediaType.MEDIA_IMAGE) {
				mLayoutFileType.setBackgroundResource(R.drawable.media_file_group_bg1);
				mBtnAllFile.setText(getstring(R.string.media_str_all_image));
				mBtnAllFile.setBackgroundResource(R.drawable.media_btn_all_image_bg);
				mBtnArtist.setVisibility(View.GONE);
				mBtnAlbum.setVisibility(View.GONE);
				
				mLayoutSource.setBackgroundResource(R.drawable.media_file_group_bg1);
				mSourceTvs[0].setVisibility(View.GONE);
			}
			
		}
	}

	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		if (mActivity.isMediaPage()) {
			initData();
		}
	}
	
	public void initData() {
		if (!MediaActivityProxy.isBindService()) {
			showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
		} else {
			try {
				if (MediaActivityProxy.getService().getStorageList().size() > 0) {
					boolean bRecoverUsb = false;
					if (mMediaType == MediaType.MEDIA_VIDEO || mMediaType == MediaType.MEDIA_IMAGE) {
						// 如果是视频和图片，存在USB设备，并且当前的设备是本地磁盘，切换到USB
						if (MediaActivityProxy.getService().getStorageList().size() > 1 && 
								MediaActivityProxy.getService().getPlayingStorage() != null && 
								MediaActivityProxy.getService().getPlayingStorage().getPath().equals(MediaScanConstans.EXTERNAL_PATH)) {
							
							for (MediaStorage mediaStorage : MediaActivityProxy.getService().getStorageList()) {
								if (mediaStorage.getPath() != null && !mediaStorage.getPath().equals(MediaScanConstans.EXTERNAL_PATH)) {
									bRecoverUsb = true;
									showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
									MediaActivityProxy.getService()
									.requestAttachStorage(mediaStorage.getPath(), mMediaType);
									break;
								}
							}
						}
					}
					if (!bRecoverUsb) {
						MediaActivityProxy.getService().requestRecover(mMediaType);
						if (MediaActivityProxy.getInstance().isScaningAttachedDevice()) {
							recoverDevice(mActivity);
							showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
						} else {
							getMediaData(MediaActivityProxy.getService().getListType(mMediaType), true);
						}
					}
				} else {
					showPromptType(PROMPT_TYPE.PROMPT_TYPE_TEXT);
				}
			} catch (Exception e) {

			}
			updatePlayState(true);
			updateDevice();
		}
	}
	
	public void updateListType(int iListType) {
		if (iListType == ListType.MUSIC_MIX || iListType == ListType.VIDEO_MIX || iListType == ListType.IMAGE_MIX) {
			mBtnFileType.setText(getstring(R.string.media_str_folder));
		} else if (iListType == ListType.MUSIC_MIX || iListType == ListType.VIDEO_MIX || iListType == ListType.IMAGE_MIX) {
			
		} else {
			if (mMediaType == MediaType.MEDIA_AUDIO) {
				if (iListType == ListType.ALBUM_MIX || iListType == ListType.ALL_ALBUM) {
					mBtnFileType.setText(getstring(R.string.media_str_album));
				} else if (iListType == ListType.ARTIST_MIX || iListType == ListType.ALL_ARTIST) {
					mBtnFileType.setText(getstring(R.string.media_str_artist));
				} else {
					mBtnFileType.setText(getstring(R.string.media_str_all_music));
				}
			} else if (mMediaType == MediaType.MEDIA_VIDEO) {
				mBtnFileType.setText(getstring(R.string.media_str_all_video));
			} else if (mMediaType == MediaType.MEDIA_IMAGE) {
				mBtnFileType.setText(getstring(R.string.media_str_all_image));
			}
		}
	}

	@Override
	public void onPause() {
		showLayoutSource(false);
		showLayoutFileType(false);
		super.onPause();
	}
	
	public void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		if (mHandler != null) {
			MediaActivityProxy.getInstance().UnRegisterHandler(mHandler);
			mHandler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}

	abstract protected void updateList(int iListType);
	protected void getMediaData(int iListType, boolean bFreshPositon) {
		updateListType(iListType);
		updateList(iListType);
		if (iListType != 0) {
			if (mDataList == null || mDataList.isEmpty()) {
				showPromptType(PROMPT_TYPE.PROMPT_TYPE_TEXT);
			} else {
				mAdapter.setDatas(mDataList, true);
				if (bFreshPositon) {
					LocalListPosition(iListType);
				}
				showPromptType(PROMPT_TYPE.PROMPT_TYPE_NULL);
			}
		}
	}

	protected void showPromptType(PROMPT_TYPE type) {
		if (null != mLinearLayout && null != mTextView && null != mListView) {
			mLinearLayout.setVisibility(type == PROMPT_TYPE.PROMPT_TYPE_SEARCH ? View.VISIBLE : View.GONE);
			if (type == PROMPT_TYPE.PROMPT_TYPE_SEARCH) {
				mBtnFresh.setVisibility(View.GONE);
			}
			if (mMediaType == MediaType.MEDIA_VIDEO) {
				mTextView.setText(mActivity.getResources().getString(R.string.media_emptyplaylist_video));
			} else if (mMediaType == MediaType.MEDIA_AUDIO) {
				mTextView.setText(mActivity.getResources().getString(R.string.media_emptyplaylist_music));
			} else if (mMediaType == MediaType.MEDIA_IMAGE) {
				mTextView.setText(mActivity.getResources().getString(R.string.media_emptyplaylist_picture));
			}
			mTextView.setVisibility(type == PROMPT_TYPE.PROMPT_TYPE_TEXT ? View.VISIBLE : View.GONE);
			mListView.setVisibility(type == PROMPT_TYPE.PROMPT_TYPE_NULL ? View.VISIBLE : View.GONE);
			mLayoutCtrol.setVisibility(type != PROMPT_TYPE.PROMPT_TYPE_SEARCH ? View.VISIBLE : View.GONE);
		}
	}

	protected enum PROMPT_TYPE {
		PROMPT_TYPE_TEXT, PROMPT_TYPE_SEARCH, PROMPT_TYPE_NULL,
	}

	public String getstring(int id){
		if (MediaActivity.mActivity != null) {
			return MediaActivity.mActivity.getString(id);
		}else{
			return "";
		}
	}
	
	protected void updateDevice() {
		try {
			for (TextView imageView : mSourceTvs) {
				imageView.setEnabled(false);
				imageView.setSelected(false);
			}
			mBtnSource.setText(getstring(R.string.media_usb));
			mBtnFresh.setVisibility(View.GONE);
			List<MediaStorage> list = MediaActivityProxy.getService().getStorageList();
			if (MediaActivityProxy.getService().getPlayingStorage() != null) {
				String path = MediaActivityProxy.getService().getPlayingStorage().getPath();
				if (path != null) {
					if (path.equals(MediaScanConstans.UDISK1_PATH)) {
						mBtnSource.setText(getstring(R.string.media_usb1));
						mBtnFolder.setEnabled(true);
					} else if (path.equals(MediaScanConstans.UDISK2_PATH)) {
						mBtnSource.setText(getstring(R.string.media_usb2));
						mBtnFolder.setEnabled(true);
					} else if (path.equals(MediaScanConstans.EXTERNAL_PATH)) {
						if (mMediaType == MediaType.MEDIA_VIDEO || mMediaType == MediaType.MEDIA_IMAGE) {
							mBtnSource.setText(getstring(R.string.media_usb));
						} else if (mMediaType == MediaType.MEDIA_AUDIO) {
							mBtnSource.setText(getstring(R.string.media_local));
							mBtnFolder.setEnabled(false);
							mBtnFolder.setSelected(false);
							if (mLinearLayout.getVisibility() != View.VISIBLE) {
								mBtnFresh.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			}

			if (list != null && list.size() > 0) {
				for (MediaStorage mediaStorage : list) {
					if (mediaStorage.getPath().equals(MediaScanConstans.EXTERNAL_PATH) && mMediaType == MediaType.MEDIA_AUDIO) {
						mSourceTvs[0].setEnabled(true);
					} else if (mediaStorage.getPath().equals(MediaScanConstans.UDISK1_PATH)) {
						mSourceTvs[1].setEnabled(true);
					} else if (mediaStorage.getPath().equals(MediaScanConstans.UDISK2_PATH)) {
						mSourceTvs[2].setEnabled(true);
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void LocalListPosition(int iListType) {
		if (mListView != null) {
			int iPosition = getSelectedPosition(iListType);
			try {
				if (iPosition != MediaPlayerContants.ID_INVALID && iListType == MediaActivityProxy.getService().getPlayType(mMediaType)) {
					Log.i(TAG, "LocalListPosition iPosition:" + iPosition);
					mListView.setAdapter(mAdapter);
					mAdapter.setDatas(mDataList, true);
					mListView.setSelection(iPosition);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getSelectedPosition(int iListType) {
		int iPosition = 0;
		try {
			if (MediaActivityProxy.isBindService()) {
				iPosition = (int) MediaActivityProxy.getService().getSearchPos(iListType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (iPosition < 0 || iPosition >= mDataList.size()) {
			iPosition = 0;
		}
		return iPosition;
	}
	
	@Override
	public void onClick(View arg0) {
		try {
			switch (arg0.getId()) {
			case R.id.media_btn_fresh:
				Uri uri = Uri.parse("file://" + MediaScanConstans.getExternalPath());
				mActivity.sendBroadcast(new Intent(MediaScanConstans.ACTION_MEDIA_QUERY, uri));
				break;
			case R.id.media_btn_source:
				showLayoutSource(mLayoutSource.getVisibility() == View.VISIBLE ? false : true);
				break;
			case R.id.media_btn_file_type:
				showLayoutFileType(mLayoutFileType.getVisibility() == View.VISIBLE ? false : true);
				break;
			case R.id.media_btn_all_file:
				if (MediaActivityProxy.getService().getStorageList().size() > 0) {
					if (mMediaType == MediaType.MEDIA_AUDIO) {
						MediaActivityProxy.getService().requestFileList(ListType.ALL_MUSIC_FILE);
					} else if (mMediaType == MediaType.MEDIA_VIDEO) {
						MediaActivityProxy.getService().requestFileList(ListType.ALL_VIDEO_FILE);
					} else if (mMediaType == MediaType.MEDIA_IMAGE) {
						MediaActivityProxy.getService().requestFileList(ListType.ALL_IMAGE_FILE);
					}
				}
				break;
			case R.id.media_btn_folder:
				if (MediaActivityProxy.getService().getStorageList().size() > 0) {
					if (mMediaType == MediaType.MEDIA_AUDIO) {
						MediaActivityProxy.getService().requestFileList(ListType.MUSIC_MIX);
					} else if (mMediaType == MediaType.MEDIA_VIDEO) {
						MediaActivityProxy.getService().requestFileList(ListType.VIDEO_MIX);
					} else if (mMediaType == MediaType.MEDIA_IMAGE) {
						MediaActivityProxy.getService().requestFileList(ListType.IMAGE_MIX);
					}
				}
				break;
			case R.id.media_btn_album:
				if (MediaActivityProxy.getService().getStorageList().size() > 0) {
					MediaActivityProxy.getService().requestFileList(ListType.ALBUM_MIX);
				}
				break;
			case R.id.media_btn_artist:
				if (MediaActivityProxy.getService().getStorageList().size() > 0) {
					MediaActivityProxy.getService().requestFileList(ListType.ARTIST_MIX);
				}
				break;
			case R.id.media_btn_local:
				if (MediaActivityProxy.getService().getPlayingStorage() != null && !MediaActivityProxy.getService()
						.getPlayingStorage().getPath().equals(MediaScanConstans.EXTERNAL_PATH)) {
					showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
					MediaActivityProxy.getService().requestAttachStorage(MediaScanConstans.EXTERNAL_PATH, mMediaType);
				}
				break;
			case R.id.media_btn_usb1:
				if (MediaActivityProxy.getService().getPlayingStorage() != null
						&& !MediaActivityProxy.getService().getPlayingStorage().getPath()
								.equals(MediaScanConstans.UDISK1_PATH)) {
					showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
					MediaActivityProxy.getService()
							.requestAttachStorage(MediaScanConstans.UDISK1_PATH, mMediaType);
				}
				break;
			case R.id.media_btn_usb2:
				if (MediaActivityProxy.getService().getPlayingStorage() != null
						&& !MediaActivityProxy.getService().getPlayingStorage().getPath()
								.equals(MediaScanConstans.UDISK2_PATH)) {
					showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
					MediaActivityProxy.getService()
							.requestAttachStorage(MediaScanConstans.UDISK2_PATH, mMediaType);
				}
				break;
			default:
				break;
			}
			if (arg0.getId() != R.id.media_btn_source) {
				showLayoutSource(false);
			}
			if (arg0.getId() != R.id.media_btn_file_type) {
				showLayoutFileType(false);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	};
	
	protected void showLayoutSource(boolean bShow) {
		mBtnSource.setSelected(bShow ? true : false);
		mLayoutSource.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
	}
	
	protected void showLayoutFileType(boolean bShow) {
		mBtnFileType.setSelected(bShow ? true : false);
		mLayoutFileType.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
	}
	
	protected void updatePlayState(boolean bFresh) {
		try {
			if (MediaActivityProxy.isBindService()) {
				int iPlayStatus = MediaActivityProxy.getService().getPlayStatus();
				boolean bPlaying = iPlayStatus == PlayStatus.PAUSED || iPlayStatus == PlayStatus.IDLE ? false : true;
				if (mAdapter != null) {
					mAdapter.setPlayStatus(bPlaying, bFresh);
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void addNotify() {
		if (mView != null) {
			initData();
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		try {
			Log.i(TAG, "handleMessage : " + msg.what + " ,val = " + msg.arg1);
			switch (msg.what) {
			case MediaPlayerMessage.UPDATE_BIND_SUCCESS:
				initData();
				break;
			case MediaPlayerMessage.UPDATE_LIST_DATA:
				getMediaData(msg.arg1, false);
				break;
			case MediaPlayerMessage.UPDATE_PLAY_INDEX:
				mAdapter.notifyDataSetChanged();
				break;
			case MediaPlayerMessage.UPDATE_PLAY_STATE:
				updatePlayState(false);
				break;
			case MediaPlayerMessage.UPDATE_SERVICE_STATE:
				if (msg.arg1 == ServiceStatus.SCANED) {
					getMediaData(MediaActivityProxy.getService().getListType(mMediaType), true);
				} else if (msg.arg1 == ServiceStatus.SCANING) {
					Log.i(TAG, "SCANING");
					if (mActivity.isMediaPage() && isResumed()) {
						Bundle data = msg.getData();
						String strPath = data.getString(MediaPlayerContants.PATH);
						MediaStorage storage = MediaActivityProxy.getService().getPlayingStorage();
						if (strPath != null ) {
							if ((mMediaType == MediaType.MEDIA_VIDEO || mMediaType == MediaType.MEDIA_IMAGE) && 
									!strPath.equals(MediaScanConstans.EXTERNAL_PATH)) {
								// 视频和图片时，本地磁盘不显示
								if (storage == null || storage.getPath().equals(MediaScanConstans.EXTERNAL_PATH) || strPath.equals(storage.getPath())) {
									showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
									MediaActivityProxy.getInstance().AttachStorage(strPath, mMediaType);
								}
							} else if (mMediaType == MediaType.MEDIA_AUDIO && (storage == null || strPath.equals(storage.getPath()))) {
								// 当前是音乐时，如果当前没有磁盘播放或者当前设备和扫描的设备是同一个时
								MediaActivityProxy.getService().RegisterSource();
								showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
								MediaActivityProxy.getInstance().AttachStorage(strPath, mMediaType);
							}
						}
					}
				} else if (msg.arg1 == ServiceStatus.LOST_CUR_STORAGE) {
					if (mActivity.isMediaPage() && isResumed()) {
						if (mMediaType == MediaType.MEDIA_VIDEO || mMediaType == MediaType.MEDIA_IMAGE) {
							if (MediaActivityProxy.getService().getStorageList().size() <= 1) {
								// 视频或者图片 丢失设备后size<=1 说明只有本地存在，直接显示无内容
								showPromptType(PROMPT_TYPE.PROMPT_TYPE_TEXT);
							} else {
								for (MediaStorage mediaStorage : MediaActivityProxy.getService().getStorageList()) {
									if (mediaStorage.getPath() != null && !mediaStorage.getPath().equals(MediaScanConstans.EXTERNAL_PATH)) {
										showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
										MediaActivityProxy.getService().requestAttachStorage(mediaStorage.getPath(), mMediaType);
										break;
									}
								}
							}
						} else if (mMediaType == MediaType.MEDIA_AUDIO) {
							// 音乐时，有其他设备就切换到其他设备
							if (MediaActivityProxy.getService().getStorageList().size() == 0) {
								showPromptType(PROMPT_TYPE.PROMPT_TYPE_TEXT);
							} else if (MediaActivityProxy.getService().getStorageList().size() == 1) {
								showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
								MediaActivityProxy.getInstance().AttachStorage(
										MediaActivityProxy.getService().getStorageList().get(0).getPath(), MediaType.MEDIA_AUDIO);
							} else {
								for (MediaStorage mediaStorage : MediaActivityProxy.getService().getStorageList()) {
									if (mediaStorage.getPath() != null && !mediaStorage.getPath().equals(MediaScanConstans.EXTERNAL_PATH)) {
										showPromptType(PROMPT_TYPE.PROMPT_TYPE_SEARCH);
										MediaActivityProxy.getService().requestAttachStorage(mediaStorage.getPath(), mMediaType);
										break;
									}
								}
							}
						}
					}
				}
				updateDevice();
				break;
			default:
				break;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
}
