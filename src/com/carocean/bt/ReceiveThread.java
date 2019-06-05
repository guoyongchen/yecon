package com.carocean.bt;

import java.io.IOException;
import java.io.InputStream;

import android.os.HandlerThread;
import android.util.Log;

interface OnReceiveListener{
	int onReceive(int len, byte[] data);
};

class ReceiveThread extends HandlerThread{
	private static final String TAG = "ReceiveThread";
	private InputStream input;
	private byte[] data;
	private OnReceiveListener receiveListener;
	private boolean keepRunning = false;
	public ReceiveThread(String name, InputStream inputStream, int bufferMaxLen) {
		super(name);
		// TODO Auto-generated constructor stub
		try {
			setInputStream(inputStream, bufferMaxLen);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setInputStream (InputStream inputStream, int bufferMaxLen)throws Exception{
		if(inputStream==null || bufferMaxLen<=0){
			throw new Exception("Parameters error.");
		}
		input = inputStream;
		data = new byte[bufferMaxLen];
	}
	public void setReceiveListener(OnReceiveListener receiveListener){
		this.receiveListener =receiveListener;
	}
	public void start(){
		Log.i(TAG, "start");
		if(!isAlive()){
			keepRunning = true;
			super.start();
		}
	}
	public void exit(){
		Log.i(TAG, "exit");
		keepRunning = false;
		if(isAlive()){
			Log.i(TAG, "exit 1");
			if(input!=null){
				try {
					Log.i(TAG, "exit 2");
					//input.reset();
					input.close();		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if( this.getLooper()!=null){
				this.getLooper().quit();
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i(TAG, "run start.");
		int len = 0;
		if(input!=null){
			while(keepRunning){
				try {
					int countable = 0;
					countable = input.available();
					while(keepRunning && countable > 0){
						len = input.read(data, 0 , data.length);
					//	Log.e(TAG, "len=" + len);
						if(len<0)break;
						if(len>0){
							if(receiveListener!=null){
								receiveListener.onReceive(len, data);
							}
						}
					}
				
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
		}
		Log.i(TAG, " run exit.");
		super.run();
	}
}
