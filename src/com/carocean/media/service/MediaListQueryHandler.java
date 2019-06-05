package com.carocean.media.service;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

interface IMediaListQueryHandler {
	void onQueryComplete(int token, Object cookie, Cursor cursor);
}

public class MediaListQueryHandler {

	private IMediaListQueryHandler mHandlerInterface;
	
	private ContentResolver mCR;
	
	private AsyncQuery mAsyncQuery;

	public MediaListQueryHandler(ContentResolver cr, IMediaListQueryHandler HandlerInterface) {
		mCR = cr;
		mHandlerInterface = HandlerInterface;
		mAsyncQuery = new AsyncQuery(cr);
		
	}
	
	public void startQuery(Boolean bAsync, int token, Object cookie, Uri uri,
            String[] projection, String selection, String[] selectionArgs,
            String orderBy) {
		if (bAsync) {
			mAsyncQuery.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);	
		} else {
			try {
				Cursor cursor = mCR.query(uri, projection, selection, selectionArgs, orderBy);
				mHandlerInterface.onQueryComplete(token, cookie, cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Cursor startQuery(Uri uri,
            String[] columns, String selection, String[] selectionArgs,
            String orderBy) {
		Cursor c = null;
		try {
			c = mCR.query(uri, columns, selection, selectionArgs, orderBy);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}
	
	@SuppressLint("HandlerLeak")
	private class AsyncQuery extends AsyncQueryHandler {
		
		public AsyncQuery(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			try {
				mHandlerInterface.onQueryComplete(token, cookie, cursor);
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
