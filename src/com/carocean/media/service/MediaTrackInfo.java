package com.carocean.media.service;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaTrackInfo implements Parcelable {
	
	// 文件路径
	private String mstrPath;
	// 文件名
	private String mstrName;
	// 标题
	private String mstrTitle;
	// 专辑
	private String mstrAlbum;
	// 艺术家
	private String mstrArtist;
	// 数据库中的ID
	private int mFileID;
	// 时长
	private int mDuration;
	// 播放进度
	private int mCurTime;
	// track id
	private int mTrackID;
	// track title
	private int mTrackTotal;
	// Apic 
	private Bitmap mBmpApic;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mstrPath);
		dest.writeString(mstrName);
		dest.writeString(mstrTitle);
		dest.writeString(mstrAlbum);
		dest.writeString(mstrArtist);
		dest.writeInt(mFileID);
		dest.writeInt(mDuration);
		dest.writeInt(mCurTime);
		dest.writeInt(mTrackID);
		dest.writeInt(mTrackTotal);
		dest.writeValue(mBmpApic);
	}
	
	public String getPath() {
		return mstrPath;
	}

	public void setPath(String mstrPath) {
		this.mstrPath = mstrPath;
	}

	public String getName() {
		return mstrName;
	}

	public void setName(String strName) {
		this.mstrName = strName;
	}

	public String getTitle() {
		return mstrTitle;
	}

	public void setTitle(String strTitle) {
		this.mstrTitle = strTitle;
	}

	public String getAlbum() {
		return mstrAlbum;
	}

	public void setAlbum(String strAlbum) {
		this.mstrAlbum = strAlbum;
	}

	public String getArtist() {
		return mstrArtist;
	}

	public void setArtist(String strArtist) {
		this.mstrArtist = strArtist;
	}

	public int getFilePosInList() {
		return mFileID;
	}

	public void setFileID(int fileID) {
		this.mFileID = fileID;
	}

	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
		this.mDuration = duration;
	}

	public int getCurTime() {
		return mCurTime;
	}

	public void setCurTime(int time) {
		this.mCurTime = time;
	}
	
	public int getTrackID() {
		return mTrackID;
	}


	public void setTrackID(int TrackID) {
		this.mTrackID = TrackID;
	}

	public int getTrackTotal() {
		return mTrackTotal;
	}

	public void setTrackTotal(int TrackTotal) {
		this.mTrackTotal = TrackTotal;
	}

	public Bitmap getApicBmp() {
		synchronized (this) {
			return mBmpApic;
		}
	}

	public void setApicBmp(Bitmap mBmpApic) {
		this.mBmpApic = mBmpApic;
	}
	
	public void recycle() {
//		synchronized (this) {
//			if (this.mBmpApic != null) {
//				this.mBmpApic.recycle();
//				this.mBmpApic = null;
//			}
//		}
	}

	public static final Parcelable.Creator<MediaTrackInfo> CREATOR = new Creator<MediaTrackInfo>() {
		
		@Override
		public MediaTrackInfo[] newArray(int size) {
			return new MediaTrackInfo[size];
		}
		
		@Override
		public MediaTrackInfo createFromParcel(Parcel source) {
			MediaTrackInfo obj = new MediaTrackInfo();
			obj.setPath(source.readString());
			obj.setName(source.readString());
			obj.setTitle(source.readString());
			obj.setAlbum(source.readString());
			obj.setArtist(source.readString());
			obj.setFileID(source.readInt());
			obj.setDuration(source.readInt());
			obj.setCurTime(source.readInt());
			obj.setTrackID(source.readInt());
			obj.setTrackTotal(source.readInt());
			obj.setApicBmp((Bitmap) source.readValue(Bitmap.class.getClassLoader()));
			return obj;
		}
	};
}
