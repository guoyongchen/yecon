package com.carocean.bt.data;

public class BtPhoneBookData {

	private int mType;
	private String mName;
	private String mNumber;

	public BtPhoneBookData(int type, String name, String number) {
		mType = type;
		mName = name;
		mNumber = number;
	}

	public String getNumber() {
		return mNumber;
	}

	public String getName() {
		return mName;
	}

	public int getType() {
		return mType;
	}
}
