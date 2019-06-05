package com.carocean.media.scan;

import com.carocean.media.constants.MediaPlayerContants;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaObject implements Parcelable {
	private final String DEFAULT_VALUE = "";
	// media type
	private int miMediaType = 0;
	// mime type
	private int miMimeType = 0;
	// id
	private int miID = MediaPlayerContants.ID_INVALID;
	// file name
	private String mstrFileName = DEFAULT_VALUE;
	// file path
	private String mstrFilePath = DEFAULT_VALUE;
	// file parent
	private String mstrFileParent = DEFAULT_VALUE;
	// Damage
	private int mDamage = 0;
	// Audio Amount
	private int miAudio = 0;
	// Vedio Amount
	private int miVideo = 0;
	// Image Amount
	private int miImage = 0;
	// Dir id
	private int miDirId = MediaPlayerContants.ID_INVALID;
	// title
	private String mTitle = DEFAULT_VALUE;
	// artist
	private String mArtist = DEFAULT_VALUE;
	// album
	private String mAlbum = DEFAULT_VALUE;
	// iIndex 用于混合列表中的寻址
	private int miIndex = MediaPlayerContants.ID_INVALID;

	
	public MediaObject() {
		
	}
	
	public MediaObject(String path, String fileName, String parent, int mediaType, int mimeType) {
		mstrFilePath = path;
		mstrFileName = fileName;
		mstrFileParent = parent;
		miMediaType = mediaType;
		miMimeType = mimeType;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(miMediaType);
		dest.writeInt(miMimeType);
		dest.writeInt(miID);
		dest.writeString(mstrFileName);
		dest.writeString(mstrFilePath);
		dest.writeString(mstrFileParent);
		dest.writeInt(mDamage);
		dest.writeInt(miAudio);
		dest.writeInt(miVideo);
		dest.writeInt(miImage);
		dest.writeInt(miDirId);
	}
	
	public int getMediaType() {
		return miMediaType;
	}

	public void setMediaType(int iMediaType) {
		this.miMediaType = iMediaType;
	}
	
	public int getMimeType() {
		return miMimeType;
	}

	public void setMimeType(int iMimeType) {
		this.miMimeType = iMimeType;
	}
	
	public int getID() {
		return miID;
	}
	
	public void setID(int iID) {
		miID = iID;
	}

	public String getFileName() {
		return mstrFileName;
	}

	public void setFileName(String strName) {
		this.mstrFileName = strName;
	}
	
	public String getFilePath() {
		return mstrFilePath;
	}
	
	public void setFilePath(String strPath) {
		this.mstrFilePath = strPath;
	}
	
	public String getFileParent() {
		return mstrFileParent;
	}
	
	public void setFileParent(String strParent) {
		this.mstrFileParent = strParent;
	}
	
	public void setDamage(int iDamage) {
		this.mDamage = iDamage;
	}
	
	public boolean getDamage() {
		return (this.mDamage == 1);
	}

	public void setAudioCount(int iAudio) {
		this.miAudio = iAudio;
	}
	
	public int getAudioCount() {
		return miAudio;
	}
	
	public void setVideoCount(int iVideo) {
		this.miVideo = iVideo;
	}
	
	public int getVideoCount() {
		return miVideo;
	}
	
	public void setImageCount(int iImage) {
		this.miImage = iImage;
	}
	
	public int getImageCount() {
		return miImage;
	}

	public void setDirId(int iDirId) {
		this.miDirId = iDirId;
	}
	
	public int getDirId() {
		return miDirId;
	}
	
	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setArtist(String artist) {
		this.mArtist = artist;
	}
	
	public String getArtist() {
		return mArtist;
	}
	
	public void setAlbum(String album) {
		this.mAlbum = album;
	}
	
	public String getAlbum() {
		return mAlbum;
	}
	
	public void setIndex(int iIndex) {
		miIndex = iIndex;
	}
	
	public int getIndex() {
		return miIndex;
	}
	
	public static final Parcelable.Creator<MediaObject> CREATOR = new Parcelable.Creator<MediaObject>() {

		@Override
		public MediaObject createFromParcel(Parcel source) {
			MediaObject obj = new MediaObject();
			obj.miMediaType = source.readInt();
			obj.miMimeType = source.readInt();
			obj.miID = source.readInt();
			obj.mstrFileName = source.readString();
			obj.mstrFilePath = source.readString();
			obj.mstrFileParent = source.readString();
			obj.mDamage = source.readInt();
			obj.miAudio = source.readInt();
			obj.miVideo = source.readInt();
			obj.miImage = source.readInt();
			obj.miDirId = source.readInt();
			return obj;
		}

		@Override
		public MediaObject[] newArray(int size) {
			return new MediaObject[size];
		}
	};
}
