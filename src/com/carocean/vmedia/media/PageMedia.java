package com.carocean.vmedia.media;

import com.carocean.R;
import com.carocean.media.MediaFragment;
import com.carocean.media.bt.BtMusicFragment;
import com.carocean.media.music.MusicListFragment;
import com.carocean.media.music.MusicPlayerFragment;
import com.carocean.media.picture.ImageListFragment;
import com.carocean.media.picture.ImagePlayActivity;
import com.carocean.media.video.VideoActivity;
import com.carocean.media.video.VideoListFragment;
import com.carocean.page.IPage;
import com.carocean.vmedia.MediaActivity;
import com.carocean.vsettings.view.CustomSeekbar;
import com.carocean.vsettings.view.CustomSeekbar.OnProgressChangedListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * @ClassName: PageMedia
 * @Description: TODO
 * @author: LIUZHIYUAN
 * @date: 2019.01.23
 **/
public class PageMedia implements IPage, OnClickListener, OnTouchListener, OnProgressChangedListener {
	public static Context mContext;
	private ViewGroup mRootView;
	final private String ACTION_UPDATA_LANGUAGE = "UPDATA_LANGUAGE";
	final static int MESSAGE_ID_SET = 300;
	final static int MSG_SHOW_FRAGMENT = 301;
	private RadioGroup mRadioGroup = null;

	private MediaFragment mCurFragment = null;
	private MusicPlayerFragment mMusicPlayerFragment = new MusicPlayerFragment();
	private MusicListFragment mMusicListFragment = new MusicListFragment();
	private BtMusicFragment mBtPlayerFragment = new BtMusicFragment();
	private VideoListFragment mVideoListFragment = new VideoListFragment();
	private ImageListFragment mPictureListFragment = new ImageListFragment();

	private static ViewType mViewType = ViewType.ViewUsbMusicFile;

	public enum ViewType {
		ViewUsbMusic, ViewUsbMusicFile, ViewBtMusic, ViewVideo, ViewVideoFile, ViewPicture, ViewPictureFile
	}

	private static PageMedia mPageMedia = null;

	public static PageMedia getInstance() {
		return mPageMedia;
	}

	void init(Context context) {
		mPageMedia = this;
	}

	void initView(ViewGroup root) {
		mRadioGroup = (RadioGroup) root.findViewById(R.id.page_type_radiogroup);
		if (null != mRadioGroup) {
			mRadioGroup.setOnCheckedChangeListener(new RadioGroupCheck());
		}

		showView(mViewType);
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub
		if (mCurFragment != null) {
			mCurFragment.addNotify();
		}
	}

	@Override
	public View getContentView(Context context, boolean isCurPage) {
		// TODO Auto-generated method stub
		if (mRootView == null) {
			mContext = context;
			mRootView = ((ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_media, null));
			init(context);
			initView(mRootView);
		} else if (isCurPage) {
			showView(mViewType);
		}
		return mRootView;
	}
	
	public void onResume(){
		if (MediaActivity.mActivity.isMediaPage()) {
			showView(mViewType);
		}
	}

	@Override
	public boolean hasInnerAnim() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeNotify() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onChanged(CustomSeekbar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		default:
			break;
		}
	}

	void changeProgress(CustomSeekbar seekbar, int offset) {
		int pos = seekbar.getProgress() + offset;
		seekbar.setProgress(pos);
		onChanged(seekbar);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			mHandler.sendEmptyMessageDelayed(view.getId(), 100);
			break;
		case MotionEvent.ACTION_UP:
			if (mHandler.hasMessages(view.getId()))
				mHandler.removeMessages(view.getId());
			break;
		default:
			break;
		}
		return false;
	}

	public void SetSeekBarProgress(int arg0) {
		Message message = Message.obtain();
		message.what = MESSAGE_ID_SET;
		message.obj = arg0;
		mHandler.sendMessage(message);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_ID_SET:
				break;
			case MSG_SHOW_FRAGMENT:
				showFragment((Fragment) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPDATA_LANGUAGE);
		if (null != LanguageBroadcastReceiver && null != filter) {
			mContext.registerReceiver(LanguageBroadcastReceiver, filter);
		}
	}

	private BroadcastReceiver LanguageBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_UPDATA_LANGUAGE)) {

			}
		}
	};

	private class RadioGroupCheck implements OnCheckedChangeListener {

		public void onCheckedChanged(RadioGroup arg0, int id) {
			switch (id) {
			case R.id.media_usb_btn:
				mViewType = ViewType.ViewUsbMusicFile;
				swithFragment(mMusicListFragment);
				break;
			case R.id.media_bt_music_btn:
				mViewType = ViewType.ViewBtMusic;
				swithFragment(mBtPlayerFragment);
				break;
			case R.id.media_video_btn:
				mViewType = ViewType.ViewVideoFile;
				swithFragment(mVideoListFragment);
				break;
			case R.id.media_picture_btn:
				mViewType = ViewType.ViewPictureFile;
				swithFragment(mPictureListFragment);
				break;
			default:
				break;
			}
		}
	}
	
	private void swithFragment(Fragment frament) {
		mHandler.removeMessages(MSG_SHOW_FRAGMENT);
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_SHOW_FRAGMENT;
		msg.obj = frament;
		mHandler.sendMessage(msg);
	}

	private void showFragment(Fragment frament) {
		if (frament != null && frament != mCurFragment) {

			FragmentManager ObjFragmentManager = ((Activity) mContext).getFragmentManager();
			FragmentTransaction ObjTransaction = ObjFragmentManager.beginTransaction();
			if (null != ObjFragmentManager && null != ObjTransaction) {
				ObjTransaction.replace(R.id.media_fragment, frament);
				ObjTransaction.commit();

				if (null != mCurFragment) {
					ObjTransaction.remove(mCurFragment);
				}
				mCurFragment = (MediaFragment) frament;
			}
		}
	}

	public void showView(ViewType viewType) {

		mViewType = viewType;

		switch (viewType) {
		case ViewUsbMusic:
		case ViewUsbMusicFile: {
			RadioButton btn = (RadioButton) mRadioGroup.findViewById(R.id.media_usb_btn);
			if (btn.isChecked()) {
				if (mViewType == ViewType.ViewUsbMusic) {
					showFragment(mMusicPlayerFragment);
				} else {
					showFragment(mMusicListFragment);
				}
			} else {
				btn.setChecked(true);
			}
		}
			break;
		case ViewBtMusic:
			((RadioButton) mRadioGroup.findViewById(R.id.media_bt_music_btn)).setChecked(true);
			break;
		case ViewVideoFile:
		case ViewVideo: {
			RadioButton btn = (RadioButton) mRadioGroup.findViewById(R.id.media_video_btn);
			if (btn.isChecked()) {
				if (mViewType == ViewType.ViewVideo ) {
					if (MediaActivity.mActivity != null && MediaActivity.mActivity.isMediaPage()) {
						mContext.startActivity(new Intent(mContext, VideoActivity.class));
					}
				} else {
					showFragment(mVideoListFragment);
				}
			} else {
				btn.setChecked(true);
			}
		}
			break;
		case ViewPicture:
		case ViewPictureFile:{
			RadioButton btn = (RadioButton) mRadioGroup.findViewById(R.id.media_picture_btn);
			if (btn.isChecked()) {
				if (mViewType == ViewType.ViewPicture) {
					if (MediaActivity.mActivity != null && MediaActivity.mActivity.isMediaPage()) {
						mContext.startActivity(new Intent(mContext, ImagePlayActivity.class));
					}
				} else {
					showFragment(mPictureListFragment);
				}
			} else {
				btn.setChecked(true);
			}
		}
			break;
		default:
			break;
		}
	}

	public static ViewType getView() {
		return mViewType;
	}

	public static void setView(ViewType viewType) {
		mViewType = viewType;
	}

}
