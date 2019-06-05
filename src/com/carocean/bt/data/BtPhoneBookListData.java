package com.carocean.bt.data;

import java.util.ArrayList;

public class BtPhoneBookListData {

	private static BtPhoneBookListData mInstance = null;
	private ArrayList<BtPhoneBookData> mPhoneBookData = new ArrayList<BtPhoneBookData>();

	private BtPhoneBookListData() {
	}

	public static BtPhoneBookListData getInstance() {
		if (mInstance == null) {
			mInstance = new BtPhoneBookListData();
		}
		return mInstance;
	}

	public void addData(BtPhoneBookData data) {
		if (null != mPhoneBookData && null != data) {
			mPhoneBookData.add(data);
		}
	}

	public ArrayList<BtPhoneBookData> getData() {
		return mPhoneBookData;
	}

	public boolean isInstance() {
		return mInstance == null ? false : true;
	}

	public boolean isDataEmpty() {
		return mPhoneBookData.isEmpty();
	}

	public void dataClear() {
		if (null != mPhoneBookData && !mPhoneBookData.isEmpty()) {
			mPhoneBookData.clear();
		}
	}
}
