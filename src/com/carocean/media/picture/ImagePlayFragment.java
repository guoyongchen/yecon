package com.carocean.media.picture;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnScaleChangeListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.carocean.R;
import com.carocean.media.picture.ImagePlayActivity.ParseState;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;

public class ImagePlayFragment extends Fragment {
	private String mImageUrl;
	private ImageView mImageView;
	private TextView mFailedView;
	private ImageView progressBar;
	private Animation mAlphaAnimation;
	private PhotoViewAttacher mAttacher;
	private WeakReference<ParseImageListener> mListener = null;
	private final int MSG_CHECK_BMP = 1;
	private final int MSG_LOAD_BMP = 2;
	private parseBmpThread mBmpThread = null;
	private static final String TAG = "ImagePlayFragment";

	public static ImagePlayFragment newInstance(String imageUrl) {
		final ImagePlayFragment f = new ImagePlayFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
		if (mListener != null && mListener.get() != null) {
			mListener.get().onParseState(mImageUrl, ParseState.CREATE);
		}
		Log.i(TAG, "onCreate:" + mImageUrl);
	}

	@Override
	public void onDestroy() {
		if (mListener != null && mListener.get() != null) {
			mListener.get().onParseState(mImageUrl, ParseState.DESTROY);
		}
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
		if (mAttacher != null) {
			mAttacher.cleanup();
			mAttacher = null;
			System.gc();
		}
		Log.i(TAG, "onDestroy:" + mImageUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mFailedView = (TextView) v.findViewById(R.id.failedView);
		mAttacher = new PhotoViewAttacher(mImageView);

		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				if (arg0 != null) {
					arg0.playSoundEffect(android.view.SoundEffectConstants.CLICK);
				}
				((ImagePlayActivity) getActivity()).FullScreen();
			}
		});

		mAttacher.setOnScaleChangeListener(new OnScaleChangeListener() {
			
			@Override
			public void onScaleChange() {
				((ImagePlayActivity)getActivity()).execPPlay(false);
			}
		});
		
		progressBar = (ImageView) v.findViewById(R.id.loading);
		mAlphaAnimation = new RotateAnimation(0f, 360f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		mAlphaAnimation.setRepeatCount(Animation.INFINITE);
		mAlphaAnimation.setDuration(1000);
		mAlphaAnimation.setInterpolator(new LinearInterpolator());//设置动画匀速改变。
		if (progressBar != null) {
			progressBar.setImageResource(R.drawable.progress_1);
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setAnimation(mAlphaAnimation);
		mFailedView.setVisibility(View.GONE);
		mHandler.removeMessages(MSG_CHECK_BMP);
		mHandler.sendEmptyMessage(MSG_CHECK_BMP);
	}
	
	class parseBmpThread extends Thread {
		@Override
		public void run() {
			mHandler.removeMessages(MSG_LOAD_BMP);
			long time = SystemClock.uptimeMillis();
	        boolean toobigFlag = false;
			long size;

	        File imageFile = new File(ImagePlayActivity.getFileName(mImageUrl));
			if (imageFile != null && imageFile.exists()) {
	            size = imageFile.length();
	            if (size >= 16*1024*1024) {
	                toobigFlag = true;
	            }
	        }
	        if (!toobigFlag) {
	            BitmapFactory.Options opts = new BitmapFactory.Options();
	            opts.inJustDecodeBounds = true;
	            BitmapFactory.decodeFile(ImagePlayActivity.getFileName(mImageUrl), opts);
	            Log.i(TAG, "parseBmpThread time:" + (SystemClock.uptimeMillis() - time) + ", mImageUrl:" + mImageUrl);
	            if (opts.outHeight * opts.outWidth > ImagePlayActivity.IMAGE_MAX_PIXS) {
	                toobigFlag = true;
	            }
	        }
	        String ImageUrl = mImageUrl;
			if (toobigFlag) {
				ImageUrl = "";
	        }
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_LOAD_BMP;
			msg.obj = ImageUrl;
			mHandler.sendMessage(msg);
		}

		public void exit() {
			interrupt();
		}
	}
	
	private void loadBmp(String imageUrl) {
		ImageLoader.getInstance().displayImage(imageUrl, mImageView, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				if (mListener != null && mListener.get() != null) {
					// 这里传递URL使用实际的，防止大图片时，传递的值为""
					mListener.get().onParseState(mImageUrl, ParseState.START);
				}
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setAnimation(mAlphaAnimation);
				mFailedView.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				try {
					String message = "";
					if (isAdded()) {
						message = getString(R.string.media_unsupport_file);
					}
					if (mListener != null && mListener.get() != null) {
						// 这里传递URL使用实际的，防止大图片时，传递的值为""
						mListener.get().onParseState(mImageUrl, ParseState.FAILED);
					}
					// Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
					progressBar.setAnimation(null);
					progressBar.setVisibility(View.GONE);
					mFailedView.setText(message);
					mFailedView.setVisibility(View.VISIBLE);
					if (mAttacher != null) {
						mAttacher.update();
					}
				} catch (Exception e) {
					
				}
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				boolean toobigFlag = false;
				if (imageUri != null && imageUri.equals("")) {
					toobigFlag = true;
				}
				if (mListener != null && mListener.get() != null) {
					// 这里传递URL使用实际的，防止大图片时，传递的值为""
					mListener.get().onParseState(mImageUrl, toobigFlag ? ParseState.FAILED : ParseState.END);
				}
				progressBar.setAnimation(null);
				progressBar.setVisibility(View.GONE);
				
				if (toobigFlag) {
		            mFailedView.setText(getString(R.string.media_unsupport_file));
		            mFailedView.setVisibility(View.VISIBLE);
				} else {
					if (mAttacher != null) {
						mAttacher.update();
					}
				}
			}
		});
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_CHECK_BMP) {
				if (mBmpThread != null) {
					if (!mBmpThread.isInterrupted()) {
						mBmpThread.exit();
					}
					mBmpThread = null;
				} 
				mBmpThread = new parseBmpThread();
				mBmpThread.start();
			} else if (msg.what == MSG_LOAD_BMP) {
				loadBmp((String)msg.obj);
			}
		}
	};

	public void setRotate(int iAngle) {
		if (mAttacher != null) {
			mAttacher.setPhotoViewRotation(iAngle);
		}
	}

	public void setScale(float scale) {
		if (mAttacher != null) {
			mAttacher.setScale(scale);
		}
	}

	public void setParseListener(WeakReference<ParseImageListener> listener) {
		mListener = listener;
	}
	
	
	public interface ParseImageListener {
		public void onParseState(String url, int state);
	}
}
