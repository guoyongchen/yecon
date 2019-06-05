package com.carocean.media.picture;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.carocean.R;
import com.carocean.media.constants.MediaPlayerContants.MediaPlayerMessage;
import com.carocean.media.constants.MediaPlayerContants.MediaType;
import com.carocean.media.constants.MediaPlayerContants.ServiceStatus;
import com.carocean.media.constants.MediaScanConstans;
import com.carocean.media.picture.ImagePlayFragment.ParseImageListener;
import com.carocean.media.service.MediaActivityProxy;
import com.carocean.media.service.MediaPlayerService;
import com.carocean.utils.PopDialog;
import com.carocean.vmedia.media.PageMedia;
import com.carocean.vmedia.media.PageMedia.ViewType;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class ImagePlayActivity extends FragmentActivity implements OnClickListener, ParseImageListener {
	private final String TAG = ImagePlayActivity.class.getSimpleName();
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	public static final String EXTRA_IMAGE_ALL_FILE_INDEX = "image_all_file_index";
	public static final int IMAGE_MAX_PIXS = 1024*4*1024*4;

	private HackyViewPager mPager;
	private int pagerPosition;
	private FrameLayout mBarTop;
	private LinearLayout mBarBottom;
	private TextView mBtnList, mBtnPrev, mBtnPlay, mBtnPause, mBtnNext, mBtnRotate;
	private TextView mTVFileName;
	private ImagePagerAdapter mAdapter;
	private final static int MSG_AUTO_PLAY = 255;
	private final static int MSG_FULL_SCREEN = 254;
	private final static int MSG_UPDATE_PARSE_STATE = 253;
	private final static int DURATION_AUTO_PLAY = 5000;
	private final static int TIME_AUTO_FULLSREEN = 8 * 1000;
	private int miRotate = 0;
	private int miItem = -1;
	private boolean mbAutoPlay = false;
	private String mCurUrl = "";
	ArrayList<ImageParseState> mImageParseStates = new ArrayList<ImageParseState>();
	private PopDialog mDialog = null;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MediaPlayerMessage.UPDATE_SERVICE_STATE:
				if (msg.arg1 == ServiceStatus.SCANING || msg.arg1 == ServiceStatus.LOST_CUR_STORAGE || 
					msg.arg1 == ServiceStatus.LOST_AUDIO_FOCUS || msg.arg1 == ServiceStatus.SWITCH_STORAGE ||
					msg.arg1 == ServiceStatus.SWITCH_MEDIA_LIST || msg.arg1 == ServiceStatus.RELEASE_ACTIVITY) {
					if (!isFinishing()) {
						backToList();
					}
				}
				break;
			case MediaPlayerMessage.UPDATE_MEDIA_TYPE:
				if (msg.arg1 != MediaType.MEDIA_IMAGE) {
					if (!isFinishing()) {
						backToList();
					}
				}
				break;
			case MSG_AUTO_PLAY:
				if (!mPager.isTouched()) {
					execNext();
				}
				execPPlay(true);
				break;
			case MSG_FULL_SCREEN:
				showBar(false);
				break;
			case MSG_UPDATE_PARSE_STATE:
				handleParseState((String)msg.obj, msg.arg1);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onResume() {
		PageMedia.setView(ViewType.ViewPicture);
	    if (mbAutoPlay) {
			execPPlay(true);
		}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
	    if (mbAutoPlay) {
			mHandler.removeMessages(MSG_AUTO_PLAY);
		}
		if (mDialog.isAdded()) {
			mDialog.dismiss();
		}
		super.onPause();
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "oncreate()");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		setContentView(R.layout.image_player_activity);
		MediaActivityProxy.getInstance().RegisterHandler(mHandler);
		mPager = (HackyViewPager) findViewById(R.id.pager);
		pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), MediaPlayerService.mImageUrlList);
		mPager.setAdapter(mAdapter);
		mTVFileName = (TextView) findViewById(R.id.tv_image_title);
		mBarTop = (FrameLayout) findViewById(R.id.layout_image_top);
		mBarBottom = (LinearLayout) findViewById(R.id.layout_image_op);
        mBarBottom.setOnClickListener(this);
        mBarTop.setOnClickListener(this);
        findViewById(R.id.image_btn_delete).setOnClickListener(this);
		
		mBtnList = (TextView) findViewById(R.id.media_image_list_btn);
		mBtnPrev = (TextView) findViewById(R.id.media_image_prev_btn);
		mBtnPlay = (TextView) findViewById(R.id.media_image_play_btn);
		mBtnPause = (TextView) findViewById(R.id.media_image_pause_btn);
		mBtnNext = (TextView) findViewById(R.id.media_image_next_btn);
		mBtnRotate = (TextView) findViewById(R.id.media_image_rotate_btn);
		
		mBtnList.setOnClickListener(this);
		mBtnPrev.setOnClickListener(this);
		mBtnPlay.setOnClickListener(this);
		mBtnPause.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
		mBtnRotate.setOnClickListener(this);
		
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				switch (arg0) {
				case ViewPager.SCROLL_STATE_DRAGGING:
					mHandler.removeMessages(MSG_AUTO_PLAY);
					miItem = mPager.getCurrentItem();
					break;
				case ViewPager.SCROLL_STATE_IDLE:
					if (miItem != -1) {
						execPPlay(mbAutoPlay);
					}
					miItem = -1;
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				updatePageInfo(arg0);
				miRotate = 0;
				SetCurItemRotate(miRotate);
			}
		});
		
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}
		if (mDialog == null) {
			mDialog = new PopDialog(getResources().getString(R.string.media_delete_picture_title));
		}
		mPager.setCurrentItem(pagerPosition);
		updatePageInfo(pagerPosition);
		PrepareFullScreen();
        mbAutoPlay = true;
	}
	
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
		MediaActivityProxy.getInstance().UnRegisterHandler(mHandler);
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	public void FullScreen() {
		boolean bFull = mBarBottom.getVisibility() == View.VISIBLE ? true : false;
		showBar(!bFull);
		updatePageInfo(pagerPosition);
		PrepareFullScreen();
	}
	
	public void showBar(boolean bShow) {
		Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (bShow) {
        	params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		mBarBottom.setVisibility(View.VISIBLE);
    		mBarTop.setVisibility(View.VISIBLE);
		} else {
			params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			mBarBottom.setVisibility(View.INVISIBLE);
			mBarTop.setVisibility(View.INVISIBLE);
		}
        window.setAttributes(params);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	@SuppressLint("UseSparseArrays")
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public List<String> fileList = new ArrayList<String>();
		public HashMap<String, WeakReference<ImagePlayFragment>> mMapFragment = new HashMap<String, WeakReference<ImagePlayFragment>>();

		public ImagePagerAdapter(FragmentManager fm, List<String> fileList) {
			super(fm);
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = fileList.get(position);
			ImagePlayFragment item = null;
			if (mMapFragment.containsKey(url)) {
				item = mMapFragment.get(url).get();
			}
			if (item == null) {
				item = ImagePlayFragment.newInstance(url);
				mMapFragment.put(url, new WeakReference<ImagePlayFragment>(item));
			} else {
				item.setRotate(0);
				item.setScale(1.0f);
			}
			item.setParseListener(new WeakReference<ImagePlayFragment.ParseImageListener>(ImagePlayActivity.this));
			return item;
		}
	}
	private long mClickTime = SystemClock.uptimeMillis();
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (SystemClock.uptimeMillis() - mClickTime < 300) {
			return;
		}
		mClickTime = SystemClock.uptimeMillis();
		boolean bStop = false;
		boolean bPreFullHideBar = true;
        switch (v.getId()) {
		case R.id.media_image_list_btn:
			backToList();
			break;
		case R.id.media_image_prev_btn:
			handleNext(false);
			break;
		case R.id.media_image_play_btn:
            bStop = false;
			execPPlay(true);
			break;
		case R.id.media_image_pause_btn:
            bStop = true;
            break;
		case R.id.media_image_next_btn:
			handleNext(true);
			break;
		case R.id.media_image_rotate_btn:
            bStop = true;
			miRotate -= 90;
			SetCurItemRotate(miRotate);
			break;
		case R.id.image_btn_delete:
			mHandler.removeMessages(MSG_AUTO_PLAY);
			mDialog.show(getFragmentManager(), this.getClass().getName());
			mDialog.setListener(this);
			break;
		case R.id.pop_btn_yes:
			try {
				int iTempPos = pagerPosition;
				MediaActivityProxy.getService().deleteFile(iTempPos, MediaType.MEDIA_IMAGE);
				if (mDialog != null) {
					mDialog.dismiss();
				}
				if (MediaActivityProxy.getService().getImageList().size() > 0) {
					if (pagerPosition > MediaActivityProxy.getService().getImageList().size() - 1) {
						// 如果删除的是最后一张图片，那么就播放第一张，否则更新列表后，依然使用相同的索引播放
						pagerPosition = 0;
					}
					if (pagerPosition >= 0) {
						mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), MediaPlayerService.mImageUrlList);
						mPager.setAdapter(mAdapter);
						mPager.setCurrentItem(pagerPosition);
						updatePageInfo(pagerPosition);
					}
					if (mbAutoPlay) {
						execPPlay(mbAutoPlay);
					}
				} else {
					mHandler.removeCallbacksAndMessages(null);
					backToList();
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.pop_btn_no:
			if (mDialog != null) {
				mDialog.dismiss();
			}
			if (mbAutoPlay) {
				execPPlay(mbAutoPlay);
			}
			break;
		default:
			bPreFullHideBar = false;
			bStop = false;
			break;
		}
		
		if (bStop) {
			execPPlay(false);
		}
		if (bPreFullHideBar) {
			PrepareFullScreen();
		} else {
			FullScreen();
		}
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		PrepareFullScreen();
		return super.onTouchEvent(event);
	}
	
	public void PrepareFullScreen() {
		mHandler.removeMessages(MSG_FULL_SCREEN);
		mHandler.sendEmptyMessageDelayed(MSG_FULL_SCREEN, TIME_AUTO_FULLSREEN);
	}

	public void SetCurItemRotate(int iRotate) {
		try {
			ImagePlayFragment item = (ImagePlayFragment) mAdapter
					.getItem(pagerPosition);
			if (item != null) {
				item.setRotate(iRotate);
			}
			mPager.invalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updatePageInfo(int arg0) {
		try {
			MediaActivityProxy.getService().requestPlayImage(arg0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		pagerPosition = arg0;
		if (mAdapter.fileList != null) {
			if (pagerPosition < mAdapter.fileList.size()) {
				mCurUrl = mAdapter.fileList.get(pagerPosition);
			}
		}
		try {
			mTVFileName.setText("");
			if (mAdapter.fileList != null) {
				if (arg0 < mAdapter.fileList.size()) {
					String url = getFileName(mAdapter.fileList.get(arg0));
					int lastId = url.lastIndexOf('/');
					mTVFileName.setText(url.substring(lastId+1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mbAutoPlay) {
			execPPlay(mbAutoPlay);
		}
	}
	
	public void execPPlay(boolean bPlay) {
		synchronized (this) {
			mHandler.removeMessages(MSG_AUTO_PLAY);
			if (bPlay) {
				mbAutoPlay = true;
				if (mCurUrl != null && mImageParseStates != null) {
					for (ImageParseState imageParseState : mImageParseStates) {
						if (imageParseState.mUrl != null && imageParseState.mUrl.equals(mCurUrl)) {
							if (isResumed()) {
//								if (imageParseState.mParseState == ParseState.FAILED) {
//									showToast(getString(R.string.media_unsupport_file));
//								}
								if ((imageParseState.mParseState == ParseState.END || imageParseState.mParseState == ParseState.FAILED)) {
									mHandler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, DURATION_AUTO_PLAY);
									break;
								}
							}
						}
					}
				}
				mBtnPause.setVisibility(View.VISIBLE);
				mBtnPlay.setVisibility(View.GONE);
			} else {
				mbAutoPlay = false;
				mBtnPause.setVisibility(View.GONE);
				mBtnPlay.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void execNext() {
		if (MediaScanConstans.isPowerOff()) {
			return;
		}
		
		int iSize = 0;
		try {
			iSize = mAdapter.fileList.size();
			if (iSize > 0) {
				pagerPosition = ++pagerPosition % iSize;
				MediaActivityProxy.getService().requestPlayImage(pagerPosition);
				if (pagerPosition == 0) {
					mPager.setCurrentItem(pagerPosition, false);
				} else {
					mPager.setCurrentItem(pagerPosition);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void execPrev() {
		int iSize = 0;
		try {
			iSize = mAdapter.fileList.size();
			if (iSize > 0) {
				pagerPosition = (--pagerPosition + iSize) % iSize;
				MediaActivityProxy.getService().requestPlayImage(pagerPosition);
				if (pagerPosition == (iSize - 1)) {
					mPager.setCurrentItem(pagerPosition, false);
				} else {
					mPager.setCurrentItem(pagerPosition);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
//			onClick(mBtnPrev);
//		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
//			onClick(mBtnNext);
//		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
//			execPPlay(!mbAutoPlay);
//		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//			execPPlay(false);
//		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
//			execPPlay(true);
//		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToList();
			PageMedia.setView(ViewType.ViewPictureFile);
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void backToList() {
		if (PageMedia.getView() == ViewType.ViewPicture) {
			PageMedia.setView(ViewType.ViewPictureFile);
		}
		finish();
	}
	
	public boolean isPlaying() {
		return mbAutoPlay;
	}
	
	public static String getFileName(String url) {
		try {
			return url.substring(url.indexOf(MediaScanConstans.URL_HEAD) + MediaScanConstans.URL_HEAD.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onParseState(String url, int state) {
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_UPDATE_PARSE_STATE;
		msg.arg1 = state;
		msg.obj = url;
		mHandler.sendMessage(msg);
	}
	
	private void handleParseState(String url, int state) {
		synchronized (this) {
			if (mImageParseStates != null) {
				boolean bFind = false;
				for (ImageParseState imageParseState : mImageParseStates) {
					if (imageParseState.mUrl != null && imageParseState.mUrl.equals(url)) {
						bFind = true;
						if (state == ParseState.DESTROY) {
							mImageParseStates.remove(imageParseState);
						} else {
							imageParseState.mParseState = state;
						}
						break;
					}
				}
				if (!bFind && state != ParseState.DESTROY) {
					ImageParseState imageParseState = new ImageParseState(url, state);
					mImageParseStates.add(imageParseState);
				}
			}
			if (url != null && url.equals(mCurUrl) && mbAutoPlay && 
				(state == ParseState.END || state == ParseState.FAILED)) {
				execPPlay(mbAutoPlay);
			}
		}
	}
	
	private void handleNext(boolean next) {
		synchronized (this) {
			if (mCurUrl != null && mImageParseStates != null) {
				for (ImageParseState imageParseState : mImageParseStates) {
					if (imageParseState.mUrl != null && imageParseState.mUrl.equals(mCurUrl)) {
						if (imageParseState.mParseState == ParseState.END || imageParseState.mParseState == ParseState.FAILED) {
							mHandler.removeMessages(MSG_AUTO_PLAY);
//							mbAutoPlay = true;
							if (mbAutoPlay) {
								mHandler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, DURATION_AUTO_PLAY);
								mBtnPause.setVisibility(View.VISIBLE);
								mBtnPlay.setVisibility(View.GONE);
							}
							if (next) {
								execNext();
							} else {
								execPrev();
							}
				            break;
						}
					}
				}
			}	
		}
		
	}
	
	private class ImageParseState {
		public String mUrl;
		public int mParseState;
		
		public ImageParseState(String url, int state) {
			mUrl = url;
			mParseState = state;
		}
	}
	
	public static class ParseState{
		public static final int CREATE = 1;
		public static final int START = 2;
		public static final int END = 3;
		public static final int FAILED = 4;
		public static final int DESTROY = 5;
	}
}