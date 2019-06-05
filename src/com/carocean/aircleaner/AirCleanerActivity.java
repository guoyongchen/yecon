package com.carocean.aircleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.carocean.R;
import com.carocean.can.CanServiceClient;
import com.carocean.can.CanServiceClient.OnAirClnrStatusChangeListener;
import com.carocean.can.CanServiceClient.OnAirTemChangeListener;
import com.carocean.can.PMGraphaView;
import com.carocean.utils.Utils;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AirCleanerActivity extends FragmentActivity implements View.OnClickListener, OnAirClnrStatusChangeListener,OnAirTemChangeListener{
	private final String TAG = "AirConditionerFragment";
	private PMGraphaView mBezierView;
	
	private ProcessDataHandler mHandler;
	private List<Point> mPointList = new ArrayList<Point>();
	private List<Integer> mPMValueList = new ArrayList<Integer>();
	private int mPmBackgroundIds[] = {R.drawable.can_air_purifier_pm_40,R.drawable.can_air_purifier_pm_70,
			                          R.drawable.can_air_purifier_pm_120,R.drawable.can_air_purifier_pm_170,
			                          R.drawable.can_air_purifier_pm_200,R.drawable.can_air_purifier_pm_220};
	private int mPmQualityBg[]     = {R.drawable.air_quality_inside_grade_bg_0,R.drawable.air_quality_inside_grade_bg_1,
			                          R.drawable.air_quality_inside_grade_bg_2,R.drawable.air_quality_inside_grade_bg_3,
			                          R.drawable.air_quality_inside_grade_bg_4,R.drawable.air_quality_inside_grade_bg_5};
	private int mPmQualityText[]   = {R.string.air_conditioner_air_excellent,R.string.air_conditioner_air_well,
			                          R.string.air_conditioner_air_mildcontamination,R.string.air_conditioner_air_middlelevel_pollution,
			                          R.string.air_conditioner_air_heavy_pollution,R.string.air_conditioner_air_very_pollution};
	private ImageView   mAirHintBg;
	private TextView    mCurrentPmInside;
	private TextView    mCurrentPmOutside;
	private TextView    mPmQualityInside;
	private View      mCloseBtn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aircleaner);
		initViews();
		CanServiceClient client = CanServiceClient.getInstance(this);
		
		if(null != client) {
			client.setOnAirClnrStatusChangeListener(this);
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Utils.showSystemUI(this,false);
		super.onResume();
	}
	protected void onPause() {
		Utils.showSystemUI(this,true);
		super.onPause();
	}
	private void initViews() {
		mHandler = new ProcessDataHandler(this);
		mBezierView = (PMGraphaView)findViewById(R.id.air_conditioner_pm_grapha);
		mAirHintBg = (ImageView)findViewById(R.id.air_pm_hint_bg);
		mCurrentPmInside = (TextView)findViewById(R.id.can_current_pm_in_car);
		mCurrentPmOutside = (TextView)findViewById(R.id.can_current_pm_outside);
		mPmQualityInside = (TextView)findViewById(R.id.air_pm_inside_quality);
		mCloseBtn = findViewById(R.id.can_close_btn);
		mCloseBtn.setOnClickListener(this);
		processPMData();
		handler.postDelayed(runnable, 5000);

	}
	public void onClick(View v) {
		if(R.id.can_close_btn == v.getId()) {
			finish();
		}
	}
	private void onPMValueChange(int power, int value) {
		mAirClnrPowerStatus = power;
		mCurrentPMValue = value;
		mHandler.sendEmptyMessage(0);
	}
	private int mCurrentPMValue = 0;
	private int mAirClnrPowerStatus = 0;
	private int mLifeCyclePercent = 0;
	private void onFilterLifeCycleChange(int percent) {
		mLifeCyclePercent = percent;
		mHandler.sendEmptyMessage(0);
	}

	void processPMData() {
		if (0 != mAirClnrPowerStatus) {
			int value = mCurrentPMValue;
			Log.v(TAG, "processPMData." + ",value:" + value);
			vaule2Point(value);
			mBezierView.setPmValue(value);
			processPMBG();
		}
	}
	private void processTemChange() {
		
	}
	private void processPMBG() {
		int index = 0;
		if(mCurrentPMValue >= 0 && mCurrentPMValue <= 50) {
			index = 0;
		}else if(mCurrentPMValue > 50 && mCurrentPMValue <= 100) {
			index = 1;
		}else if(mCurrentPMValue > 100 && mCurrentPMValue <= 150) {
			index = 2;
		}else if(mCurrentPMValue > 150 && mCurrentPMValue <= 200) {
			index = 3;
		}else if(mCurrentPMValue > 200 && mCurrentPMValue <= 300) {
			index = 4;
		}else if(mCurrentPMValue > 300){
			index = 5;
		}
		
		if(null != mAirHintBg) {
			mAirHintBg.setBackgroundResource(mPmBackgroundIds[index]);
		}
		
		if(null != mCurrentPmInside) {
			mCurrentPmInside.setText(String.valueOf(mCurrentPMValue));
		}
		
		if(null != mCurrentPmOutside) {
			String tips = getResources().getString(R.string.air_pm_tips);
			tips += String.valueOf(mCurrentPMValue);
			mCurrentPmOutside.setText(tips);
		}
		
		if(null != mPmQualityInside) {
			mPmQualityInside.setText(mPmQualityText[index]);
			mPmQualityInside.setBackgroundResource(mPmQualityBg[index]);
		}
	}

	private void vaule2Point(int value) {
		int size = mPointList.size();
		int maxPoints = mBezierView.getPoints();
		if (size >= maxPoints) {
			mPointList.remove(0);
			mPMValueList.remove(0);
			size = mPointList.size();
		}
		int pointX = 1024/maxPoints;
		int maginLeft = 0;
		mPMValueList.add(value);
		int y = 200 - value / 2;

		y = y > 20 ? y : 20;
		mPointList.add(new Point(size * pointX + maginLeft, y));
		int last = mPMValueList.get(0);
		size = mPointList.size();

		if (maxPoints == size) {
			for (int i = 0; i < maxPoints; i++) {
				mPointList.get(i).x = i * pointX + maginLeft;
			}
		}

		mBezierView.setPointList(mPointList);
		mBezierView.invalidate();
	}

	static class ProcessDataHandler extends Handler {
		private AirCleanerActivity mActivity;

		ProcessDataHandler(AirCleanerActivity context) {
			mActivity = (AirCleanerActivity) context;
		}

		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				mActivity.processPMData();
				break;
			case 1:
				break;
			}
			
		}
	}
	public void OnAirClnrStatusChange(int power, int pm, int userCustAutoWDC) {
		onPMValueChange(power,pm);
	}
	private int mIstdAirTem = 0;
	private int mOtsdAirTem = 0;
	public void OnAirTemChange(int istdTem, int otsdTem) {
		mIstdAirTem = istdTem;
		mOtsdAirTem = otsdTem;
	}
	final Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case 1:

				int max = 400;
				int min = 10;
				Random random = new Random();
				int value = random.nextInt(max) % (max - min + 1) + min;
				onPMValueChange(1,value);
				postDelayed(runnable, 20000);

				break;

			}

			super.handleMessage(msg);

		}

	};

	private Runnable runnable = new Runnable() {

		public void run() {

			Message message = handler.obtainMessage();

			message.what = 1;

			handler.sendMessage(message);

		}

	};
}
