package com.carocean.launcher.customView;

import com.carocean.R;
import com.carocean.launcher.utils.launcherUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CarInfoWidget extends LinearLayout implements OnClickListener {

	View mView;
	int mTitleColor = Color.BLUE;
	String strTitleName;
	String strTitlePrompt1 = "";
	String strTitlePrompt2 = "";
	float mTitleSize = 24;
	TextView mPromptText1;
	TextView mPromptText2;

	void initView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.launcher_widget_radio_layout, this);
		mView = findViewById(R.id.radio_widget_layout);
		mView.setLayoutParams(new LayoutParams(getBackground().getMinimumWidth(), getBackground().getMinimumHeight()));
		TextView mTitle = (TextView) mView.findViewById(R.id.radio_title);
		// TextView mWidget_control = (TextView)
		// mView.findViewById(R.id.widget_control);
		// mPromptText1 = (TextView) mView.findViewById(R.id.title_prompt1);
		// mPromptText2 = (TextView) mView.findViewById(R.id.title_prompt2);
		// ((TextView)
		// mView.findViewById(R.id.widget_control)).setVisibility(GONE);

		mTitle.setText(strTitleName);
		mPromptText1.setText(strTitleName);
		mPromptText2.setText(strTitleName);
		if (null != mView) {
			((View) mView.getParent()).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					launcherUtils.startCAN();
				}
			});
			// mWidget_control.setOnClickListener(this);
		}
	}

	public CarInfoWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaWidget);
		strTitleName = ta.getString(R.styleable.MediaWidget_titleName);
		mTitleColor = ta.getColor(R.styleable.MediaWidget_titleColor, Color.BLUE);
		mTitleSize = ta.getDimension(R.styleable.MediaWidget_titleSize, 24);
		ta.recycle();

		initView(context);
	}

	public CarInfoWidget(Context context) {
		super(context);

		initView(context);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		default:
			break;
		}

	}

}
