package com.carocean.vsettings.view;

import com.carocean.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @ClassName: vHeaderLayout
 * @Description: TODO
 * @author: LZY
 * @date: 2018.04.24
 **/
public class vHeaderLayout extends LinearLayout {
    private final static String TAG="vHeaderLayout";
	private LayoutInflater mInflater;
	private Context mContext;
	private View mHeader;
	private View mView;
	private PolicyHandler mHandler;
	private LinearLayout mLayoutRightContainer;
	private ImageView mIcon;
	private TextView mSubTitle1, mSubTitle2, mHintTitle;
	private String item_style, item_subTitle1, item_subTitle2, item_hintTitle, item_two_button_left,
			item_two_button_right;
	private Drawable item_Drawable;

	private boolean mEnable = true;

	// only right text
	private TextView mRightTextTitle;

	// two button left/right
	private Button mLeftButton, mRightButton;
	private TextView mMiddleText;
	private onTwoButtonListener mTwoButtonListener;

	// only check 2
	private Button mOneCheckBox;
	private onOneCheckBoxListener mOneCheckListener;

	// only right button 3
	private onOneButtonListener mOneButtonListener;
	private Button mOnlyOneButton;
	private TextView mPrompt_title, mPrompt_title2;

	// two button 4
	private RadioGroup item_two_radioButton;
	private RadioButton mRadioButton1, mRadioButton2;
	private onTwoRadioButtonListener mTwoRadioListener;

	// progress
	private int item_seekbarMax = 100, item_seekbarPos = 50;
	private TextView mSeekbarTitle;
	private SeekBar mSeekBar;

	private final String STYLE_ONLY_TITLE = "title";
	private final String STYLE_LEFT_RIGHT_BUTTON = "twoButton";
	private final String STYLE_ONLY_CHECKBOX = "checkbox";
	private final String STYLE_ONLY_ONE_BUTTON = "button";
	private final String STYLE_TWO_RADIOBUTTON = "twoRadioButton";
	private final String STYLE_SEEKBAR = "seekbar";

	//add by xuhh for long click
	private final static int DELAY_LEFT=1;
    private final static int DELAY_RIGHT=2;
	private int mDelayType;
	private Handler mDelayBtnHandler = new Handler();
	private Runnable mDelayBtnRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDelayType == DELAY_LEFT) {
                sendLeftBtnMsg();
            }else if (mDelayType == DELAY_RIGHT) {
                sendRightBtnMsg();
            }

            mDelayBtnHandler.postDelayed(mDelayBtnRunnable, 100);
        }
    };
	//add end

	vHeaderLayout getInstance(Context context) {
		vHeaderLayout mHeaderLayout = new vHeaderLayout(context);
		return mHeaderLayout;
	}

	public vHeaderLayout(Context context) {
		super(context);
		init(context);
	}

	public vHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.vHeaderLayout);

		item_style = ta.getString(R.styleable.vHeaderLayout_vitem_style);
		item_Drawable = ta.getDrawable(R.styleable.vHeaderLayout_vitem_icon);
		item_subTitle1 = ta.getString(R.styleable.vHeaderLayout_vitem_subTitle);
		item_subTitle2 = ta.getString(R.styleable.vHeaderLayout_vitem_subTitle2);
		item_hintTitle = ta.getString(R.styleable.vHeaderLayout_vitem_hintTitle);

		if (item_style.equalsIgnoreCase("twoRadioButton")) {
			item_two_button_left = ta.getString(R.styleable.vHeaderLayout_vitem_two_button_left);
			item_two_button_right = ta.getString(R.styleable.vHeaderLayout_vitem_two_button_right);
		} else if (item_style.equalsIgnoreCase("seekbar")) {
			item_seekbarMax = ta.getInt(R.styleable.vHeaderLayout_vitem_seekbarMax, 100);
			item_seekbarPos = ta.getInt(R.styleable.vHeaderLayout_vitem_seekbarPos, 50);
		}

		ta.recycle();
		init(context);
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		mEnable = enabled;
		mHeader.setEnabled(mEnable);
		mSubTitle1.setEnabled(mEnable);
		mSubTitle2.setEnabled(mEnable);
		mHintTitle.setEnabled(mEnable);
		if (mOnlyOneButton != null)
			mOnlyOneButton.setEnabled(mEnable);
		super.setEnabled(enabled);
	}

	public void init(Context context) {
		mHandler = new PolicyHandler();
		mInflater = LayoutInflater.from(context);
		mHeader = mInflater.inflate(R.layout.setting_layout_head_item_parent, null);
		addView(mHeader);
		mView = (View) mHeader.getParent();
		initViews();
		init(item_style);
	}

	public void initViews() {
		mLayoutRightContainer = (LinearLayout) findViewByHeaderId(R.id.item_parent_right);
		mIcon = (ImageView) findViewByHeaderId(R.id.item_icon);
		mSubTitle1 = (TextView) findViewByHeaderId(R.id.item_sub_title1);
		mSubTitle2 = (TextView) findViewByHeaderId(R.id.item_sub_title2);
		mHintTitle = (TextView) findViewByHeaderId(R.id.item_hint_title);

		setIcon(item_Drawable);
		setSubTitle(item_subTitle1, item_subTitle2);
		setHintTitle(item_hintTitle);

		if (item_style.equalsIgnoreCase(STYLE_ONLY_ONE_BUTTON)) {
			mHeader.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (mOneButtonListener != null) {
						mOneButtonListener.onOneButtonClick(mView);
					}
				}
			});
		}
	}

	public View findViewByHeaderId(int id) {
		return mHeader.findViewById(id);
	}

	public void setSelected() {
		mHeader.setBackgroundResource(R.drawable.setting_listbk_down);
	}

	public void init(String hStyle) {
		defaultTitle();
		if (hStyle.equalsIgnoreCase(STYLE_ONLY_TITLE)) {
			initOnlyRightText();
		} else if (hStyle.equalsIgnoreCase(STYLE_LEFT_RIGHT_BUTTON)) {
			initLeftRightButton();
		} else if (hStyle.equalsIgnoreCase(STYLE_ONLY_CHECKBOX)) {
			initOnlyCheckBox();
		} else if (hStyle.equalsIgnoreCase(STYLE_ONLY_ONE_BUTTON)) {
			initOnlyOneButton();
		} else if (hStyle.equalsIgnoreCase(STYLE_TWO_RADIOBUTTON)) {
			initTwoRadioButton();
		} else if (hStyle.equalsIgnoreCase(STYLE_SEEKBAR)) {
			initSeekBarLayout();
		}
	}

	private void defaultTitle() {
		mLayoutRightContainer.removeAllViews();
	}

	public void setIcon(Drawable icon) {
		if (icon != null) {
			mIcon.setVisibility(View.VISIBLE);
			mIcon.setBackground(icon);
		} else {
			mIcon.setVisibility(View.GONE);
		}
	}

	public void setRightTitle(CharSequence title) {
		if (mRightTextTitle == null)
			return;
		if (!TextUtils.isEmpty(title)) {
			mRightTextTitle.setText(title);
		}
	}

	public void setSubTitle(CharSequence title1, CharSequence title2) {
		if (!TextUtils.isEmpty(title1)) {
			mSubTitle1.setVisibility(View.VISIBLE);
			mSubTitle1.setText(title1);
		} else {
			mSubTitle1.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(title2)) {
			mSubTitle2.setVisibility(View.VISIBLE);
			mSubTitle2.setText(title2);
		} else {
			mSubTitle2.setVisibility(View.GONE);
		}
	}

	public void setSubTitle2(CharSequence title2) {
		if (!TextUtils.isEmpty(title2)) {
			mSubTitle2.setVisibility(View.VISIBLE);
			mSubTitle2.setText(title2);
		} else {
			mSubTitle2.setVisibility(View.GONE);
		}
	}

	public void setHintTitle(CharSequence title) {
		if (title != null && !item_style.equalsIgnoreCase(STYLE_ONLY_CHECKBOX)) {
			mHintTitle.setVisibility(View.VISIBLE);
			mHintTitle.setText(title);
		} else {
			mHintTitle.setVisibility(View.GONE);
		}
	}

	public TextView getHintTitle() {
		return mHintTitle;
	}

	public void initOnlyRightText() {
		View view = mInflater.inflate(R.layout.setting_layout_head_item_only_right_text, null);
		mLayoutRightContainer.addView(view);
		mRightTextTitle = (TextView) view.findViewById(R.id.one_right_text);
	}

	//add by xuhh for long click
    void startDelayTimer() {
	    if (mDelayBtnHandler != null && mDelayBtnRunnable != null) {
            Log.i(TAG, "vHeaderLayout---startDelayTimer");
            mDelayBtnHandler.removeCallbacks(mDelayBtnRunnable);
            mDelayBtnHandler.postDelayed(mDelayBtnRunnable, 100);
        }
    }

    void stopDelayTimer() {
        if (mDelayBtnHandler != null && mDelayBtnRunnable != null) {
            Log.i(TAG, "vHeaderLayout---stopDelayTimer");
            mDelayBtnHandler.removeCallbacks(mDelayBtnRunnable);
        }
    }

	void sendLeftBtnMsg() {
	    if (mHandler != null && mLeftButton != null) {
            Log.i(TAG, "vHeaderLayout---sendLeftBtnMsg");
            if (mHandler.hasMessages(mLeftButton.getId())) {
                mHandler.removeMessages(mLeftButton.getId());
            }
            mHandler.sendEmptyMessage(mLeftButton.getId());
        }
    }

    void sendRightBtnMsg() {
        if (mHandler != null && mRightButton != null) {
            Log.i(TAG, "vHeaderLayout---sendRightBtnMsg");
            if (mHandler.hasMessages(mRightButton.getId())) {
                mHandler.removeMessages(mRightButton.getId());
            }
            mHandler.sendEmptyMessage(mRightButton.getId());
        }
    }
	//add by xuhh for long click end

	void removeLeftBtnMsg() {
        if (mHandler != null) {
            if (mLeftButton != null) {
                if (mHandler.hasMessages(mLeftButton.getId())) {
                    Log.i(TAG, "vHeaderLayout---removeLeftBtnMsg");
                    mHandler.removeMessages(mLeftButton.getId());
                }
            }
        }
    }

    void removeRightBtnMsg() {
        if (mHandler != null) {
            if (mRightButton != null) {
                if (mHandler.hasMessages(mRightButton.getId())) {
                    Log.i(TAG, "vHeaderLayout---removeRightBtnMsg");
                    mHandler.removeMessages(mRightButton.getId());
                }
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        Log.i(TAG, "vHeaderLayout---action="+action);

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            removeLeftBtnMsg();
            removeRightBtnMsg();
            stopDelayTimer();
        }

        return super.onInterceptTouchEvent(ev);
    }

    public void initLeftRightButton() {
		View view = mInflater.inflate(R.layout.setting_layout_head_item_two_button, null);
		mLayoutRightContainer.addView(view);
		mLeftButton = (Button) view.findViewById(R.id.item_icon_left);
		mRightButton = (Button) view.findViewById(R.id.item_icon_right);
		mMiddleText = (TextView) view.findViewById(R.id.item_text_middle);
		mLeftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mTwoButtonListener != null) {
					mTwoButtonListener.onLeftButtonClick(mView);
				}
			}
		});

        mLeftButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i(TAG, "vHeaderLayout---left btn onLongClick");
                mDelayType = DELAY_LEFT;
                sendLeftBtnMsg();
                startDelayTimer();
                return false;
            }
        });

		mRightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mTwoButtonListener != null) {
					mTwoButtonListener.onRightButtonClick(mView);
				}
			}
		});

		mRightButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i(TAG, "vHeaderLayout---right btn onLongClick");
                mDelayType = DELAY_RIGHT;
                sendRightBtnMsg();
                startDelayTimer();
                return false;
            }
        });
	}

	private class PolicyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.item_icon_left:
				mLeftButton.performClick();
				if (mHandler.hasMessages(msg.what))
					mHandler.removeMessages(msg.what);
				break;
			case R.id.item_icon_right:
				mRightButton.performClick();
				if (mHandler.hasMessages(msg.what))
					mHandler.removeMessages(msg.what);
				break;
			}

		}
	}

	public TextView getMiddleTitle() {
		return mMiddleText;
	}

	public void setMiddleTitle(CharSequence title) {
		if (title != null) {
			mMiddleText.setVisibility(View.VISIBLE);
			mMiddleText.setText(title);
		} else {
			mMiddleText.setVisibility(View.INVISIBLE);
		}
	}

	public void setTwoButtonListener(onTwoButtonListener listener) {
		mTwoButtonListener = listener;
	}

	public interface onTwoButtonListener {
		void onLeftButtonClick(View view);

		void onRightButtonClick(View view);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	boolean mbSwitch = false;

	private void freshSwitchUI(Context context, boolean bSwitch) {
		Drawable drawableOn = context.getResources().getDrawable(R.drawable.setting_head_item_switch_on);
		drawableOn.setBounds(0, 0, drawableOn.getMinimumWidth(), drawableOn.getMinimumHeight());
		Drawable drawableOff = context.getResources().getDrawable(R.drawable.setting_head_item_switch_off);
		drawableOff.setBounds(0, 0, drawableOff.getMinimumWidth(), drawableOff.getMinimumHeight());
		if(mOneCheckBox!=null) {
		mOneCheckBox.setBackground(bSwitch ? drawableOn : drawableOff);}
	}

	public void initOnlyCheckBox() {
		View view = mInflater.inflate(R.layout.setting_layout_head_item_one_checkbox, null);
		mLayoutRightContainer.addView(view);
		mOneCheckBox = (Button) view.findViewById(R.id.one_checkbox);
		mOneCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				mbSwitch = !mbSwitch;
				freshSwitchUI(mContext, mbSwitch);
				if (mOneCheckListener != null) {
					mOneCheckListener.onCheckout(mView, mbSwitch);
				}
			}
		});
	}

	public void setOneCheckBoxListener(final onOneCheckBoxListener listener) {
		if (mOneCheckBox != null) {
			setOneCheckListener(listener);
		}
	}

	public void setOneCheckListener(onOneCheckBoxListener listener) {
		mOneCheckListener = listener;
	}

	public interface onOneCheckBoxListener {
		void onCheckout(View view, boolean value);
	}

	public void setChecked(boolean value) {
		mbSwitch = value;
		freshSwitchUI(mContext, value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setPromptTitle(CharSequence title) {
		if (mPrompt_title == null)
			return;
		if (title != null) {
			mPrompt_title.setVisibility(View.VISIBLE);
			mPrompt_title.setText(title);
		} else {
			mPrompt_title.setVisibility(View.GONE);
		}
	}

	public void setPromptTitle2(CharSequence title) {
		if (mPrompt_title2 == null)
			return;
		if (title != null) {
			mPrompt_title2.setVisibility(View.VISIBLE);
			mPrompt_title2.setText(title);
		} else {
			mPrompt_title2.setVisibility(View.GONE);
		}

	}

	public void initOnlyOneButton() {
		View view = mInflater.inflate(R.layout.setting_layout_head_item_only_right_button, null);
		mLayoutRightContainer.addView(view);
		mOnlyOneButton = (Button) view.findViewById(R.id.one_right_button);
		mPrompt_title = (TextView) view.findViewById(R.id.item_prompt_title);
		mPrompt_title2 = (TextView) view.findViewById(R.id.item_prompt_title2);
		mOnlyOneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mOneButtonListener != null) {
					mOneButtonListener.onOneButtonClick(mView);
				}
			}
		});
	}

	public void setOneButtonListener(onOneButtonListener listener) {
		mOneButtonListener = listener;
	}

	public interface onOneButtonListener {
		void onOneButtonClick(View view);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void initTwoRadioButton() {

		View view = mInflater.inflate(R.layout.setting_layout_head_item_two_radiobutton, null);
		mLayoutRightContainer.addView(view);
		item_two_radioButton = (RadioGroup) view.findViewById(R.id.item_two_radioButton);
		mRadioButton1 = (RadioButton) view.findViewById(R.id.item_button_1);
		mRadioButton2 = (RadioButton) view.findViewById(R.id.item_button_2);

		if (item_two_button_left != null) {
			mRadioButton1.setText(item_two_button_left);
		}
		if (item_two_button_right != null) {
			mRadioButton2.setText(item_two_button_right);
		}

		item_two_radioButton.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				item_two_radioButton.playSoundEffect(android.view.SoundEffectConstants.CLICK);
				if (checkedId == R.id.item_button_1) {
					if (mTwoRadioListener != null) {
						mTwoRadioListener.onLeftRadioButtonClick(mView);
					}
				} else {
					if (mTwoRadioListener != null) {
						mTwoRadioListener.onRightRadioButtonClick(mView);
					}
				}
			}
		});
	}

	public void setRadioButtonTitle(int StrId1, int StrId2) {
		mRadioButton1.setText(StrId1);
		mRadioButton2.setText(StrId2);
	}

	public void setRadioCheck(int index) {
		if (index == 1) {
			item_two_radioButton.check(mRadioButton1.getId());
		} else {
			item_two_radioButton.check(mRadioButton2.getId());
		}
	}

	public void setTwoRadioButtonListener(vHeaderLayout headlayout, final onTwoRadioButtonListener listener) {
		if (item_two_radioButton != null) {
			setTwoRadioButtonListener(listener);
		}
	}

	public void setTwoRadioButtonListener(onTwoRadioButtonListener listener) {
		mTwoRadioListener = listener;
	}

	public interface onTwoRadioButtonListener {
		void onLeftRadioButtonClick(View view);

		void onRightRadioButtonClick(View view);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void initSeekBarLayout() {
		View view = mInflater.inflate(R.layout.setting_layout_head_item_seekbar, null);
		mLayoutRightContainer.addView(view);
		mSeekbarTitle = (TextView) view.findViewById(R.id.item_seekbar_title);
		mSeekBar = (SeekBar) view.findViewById(R.id.item_seekbar);
		mSeekBar.setMax(item_seekbarMax);
		setSeekbarPos(item_seekbarPos);
		setSeekbarText(item_seekbarPos);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				// mSeekbarTitle.setText(progress + "");
				if (mOnProgressChange != null)
					mOnProgressChange.onProgressChanged(mView, seekbar, progress);
				if (mOnProgressChangeEx != null)
					mOnProgressChangeEx.onProgressChanged(mView, seekbar, progress, arg2);
			}
		});
	}

	public void setSeekbarMax(int max) {
		if (mSeekBar != null) {
			item_seekbarMax = max;
			mSeekBar.setMax(item_seekbarMax);
		}
	}

	public int getSeekbarMax() {
		if (mSeekBar != null)
			return mSeekBar.getMax();
		return item_seekbarMax;
	}

	public void setSeekbarText(int pos) {
		if (mSeekbarTitle != null)
			mSeekbarTitle.setText(pos + "");
	}

	public void setSeekbarPos(int pos) {
		if (mSeekBar != null)
			mSeekBar.setProgress(pos);
	}

	private onProgressChanged mOnProgressChange;

	public interface onProgressChanged {
		public void onProgressChanged(View view, SeekBar mSeekbar, int progress);
	}

	public void setOnProgressChanged(onProgressChanged mP) {
		mOnProgressChange = mP;
	}

	private onProgressChangedEx mOnProgressChangeEx;

	public interface onProgressChangedEx {
		public void onProgressChanged(View view, SeekBar mSeekbar, int progress, boolean user);
	}

	public void setOnProgressChangedEx(onProgressChangedEx mP) {
		mOnProgressChangeEx = mP;
	}

    @Override
    protected void onDetachedFromWindow() {
	    Log.i(TAG, "vHeaderLayout---onDetachedFromWindow");
        removeLeftBtnMsg();
	    removeRightBtnMsg();
	    stopDelayTimer();
        super.onDetachedFromWindow();
    }
}
