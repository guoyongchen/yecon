package com.carocean.bt;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ExDevp.ExDevPort;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import static com.carocean.bt.BTService.*;

public class Profile implements OnReceiveListener{
	public String TAG = "Profile";

	public Context mContext;
	public ExDevPort mSerialPort;
	public byte[]buffer=new byte[4];
	public Profile(Context context){
		mContext = context;
		startbtrc();
	}
	public void getbtstate(){
	}
	
	public void senddata(String data){
		if (mSerialPort == null) {
			return;
		}
		try {
			Log.e(TAG, "senddata:" + data);
			data = "AT-" + data + "\r\n";
			mSerialPort.getOutputStream().write(data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendcmd(String cmd){
		if (mSerialPort == null) {
			return;
		}
		try {
			Log.e(TAG, "sendcmd:" + cmd);
			cmd = "AT-" + cmd + "\r\n";
			mSerialPort.getOutputStream().write(cmd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static final int buffermax = 1024;
	public static byte[] remaindata = new byte[buffermax*2];
	public static int remaindatalen = 0;
	public ReceiveThread receiveThread;
	public void startbtrc(){
		if (mSerialPort == null) {
			try {
				mSerialPort = new ExDevPort(new File("/dev/ttyMT1"),921600);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mSerialPort != null) {
				Log.e(TAG, "openport success");
			}else{
				Log.e(TAG, "openport fail");
			}
			if (mSerialPort != null) {
				receiveThread = new ReceiveThread("", mSerialPort.getInputStream(), buffermax);
				receiveThread.setReceiveListener(this);
				receiveThread.start();
			}    
		}
	}

	@Override
	public int onReceive(int len, byte[] data) { 
		// TODO Auto-generated method stub
		String intdata = "";
		for (int i = 0; i < len; i++) {
			intdata += data[i];
			intdata += " ";
		}
		Log.e(TAG, "portdata :" + intdata + "|");

		for (int i = 0; i < len; i++) {
			remaindata[remaindatalen + i] = data[i];
		}
		remaindatalen = remaindatalen + len;
		
		if (remaindatalen < 3) {
			return 0;
		}

		while(true){
			int bodyendindex = 0;
			boolean bfoundend = false;
			for (int i = 0; i < remaindatalen; i++) {
				if (remaindata[i] == '\r' && i < remaindatalen - 1 && remaindata[i + 1] == '\n') {
					bfoundend = true;
					bodyendindex = i - 1;
					break;
				}else if (remaindata[i] == '\n') {// gukai error
					remaindatalen = resortdata(remaindata, i + 1, remaindatalen);
				}
			}
			if (!bfoundend) {
				return 0;
			}
			if (bodyendindex < 0) {
				remaindatalen = resortdata(remaindata, bodyendindex + 3, remaindatalen);
				continue;
			}
			handlecmd(remaindata, bodyendindex);
			if (bodyendindex + 2 < remaindatalen - 1) {
				remaindatalen = resortdata(remaindata, bodyendindex + 3, remaindatalen);
			}else{
				remaindatalen = 0;
				break;
			}
		}
		return 0;
	}

	public int resortdata(byte[] data, int startpos, int totallen){
		int remainlen = 0;
		for (int i = 0, j = startpos; j < totallen; i++, j++) {
			data[i] = data[j];
			remainlen = i + 1;
		}
		return remainlen;
	}
	synchronized public boolean handlecmd(byte[] data, int bodyendindex){
		String body = getdata(data, 0, bodyendindex + 1);
		Log.e(TAG, "recevie:" + body + "|len=" + (bodyendindex + 1));
		if (bodyendindex < 1) {
			return true;
		}
		String cmd = getdata(data, 0, 2);
		String value = getdata(data, 2, bodyendindex + 1 - 2);
		if (cmd.equals("MM")) {
			mContext.sendBroadcast(new Intent(ACTION_DEVICE_NAME).putExtra(EXTRA_NAME, value));
		}else if (cmd.equals("SF")) {
			String mac = getdata(data, 3, 12);
			String name = getdata(data, 15, bodyendindex + 1 - 15);
			mContext.sendBroadcast(new Intent(ACTION_DEVICE_FOUND).putExtra(EXTRA_NAME, name).putExtra(EXTRA_MAC, mac));
		}else if (cmd.equals("QS")) {
			mContext.sendBroadcast(new Intent(ACTION_DISCOVERY_START));
		}else if (cmd.equals("SH")) {
			mContext.sendBroadcast(new Intent(ACTION_DISCOVERY_END));
		}else if (cmd.equals("MX")) {
			String index = getdata(data, 2, 1);
			String mac = getdata(data, 3, 12);
			String name = getdata(data, 15, bodyendindex + 1 - 15);
			if (index.equals("0")) {
				ContactDB.getinstance().open(mac.toUpperCase());
				ContactDB.getinstance().loadTable();
				mContext.sendBroadcast(new Intent(ACTION_CONNECTED).putExtra(EXTRA_NAME, name).putExtra(EXTRA_MAC, mac));
			}else{
				mContext.sendBroadcast(new Intent(ACTION_PAIREDLIST).putExtra(EXTRA_NAME, name).putExtra(EXTRA_MAC, mac).putExtra(EXTRA_INDEX, mac));
			}
		}else if (cmd.equals("IA")) {
			mContext.sendBroadcast(new Intent(ACTION_DISCONNECTED));
		}else if (cmd.equals("IV")) {
			mContext.sendBroadcast(new Intent(ACTION_CONNECTING));
		}else if (cmd.equals("PB")) {
			int name_len = Integer.parseInt(getdata(data, 2, 2));
		//	int num_len = Integer.parseInt(getdata(data, 4, 2));
			String name = getdata(data, 6, name_len);
			String num = getdata(data, 6 + name_len, bodyendindex + 1 - 6 - name_len);
			mContext.sendBroadcast(new Intent(ACTION_CONTACT).putExtra(EXTRA_NAME, name).putExtra(EXTRA_NUM, num));
		}else if (cmd.equals("PA")) {
			mContext.sendBroadcast(new Intent(ACTION_DOWNLOAD_STATE).putExtra(EXTRA_STATE, Integer.parseInt(value.substring(0, 1))));
		}else if (cmd.equals("PC") || cmd.equals("PE")) {
			mContext.sendBroadcast(new Intent(ACTION_DOWNLOAD_STATE).putExtra(EXTRA_STATE, DOWNLOAD_STATE_END));
		}else if (cmd.equals("PD")) {
			String type = getdata(data, 2, 1);
			int name_len = Integer.parseInt(getdata(data, 3, 2));
			int num_len = Integer.parseInt(getdata(data, 5, 2));
		//	int time_len = Integer.parseInt(getdata(data, 7, 2));
			String name = getdata(data, 9, name_len);
			String num = getdata(data, 9 + name_len, num_len);
			String time = getdata(data, 9 + name_len + num_len, bodyendindex + 1 - 9 - name_len - num_len);
			Log.e(TAG, "loadrecord name=" + name + " num=" + num + " time=" + time + " type=" + type);
			mContext.sendBroadcast(new Intent(ACTION_RECORD).putExtra(EXTRA_NAME, name).putExtra(EXTRA_NUM, num).putExtra(EXTRA_TYPE, type)
					.putExtra(EXTRA_TIME, time));
		}else if (cmd.equals("IC")) {
			String num = getdata(data, 4, bodyendindex + 1 - 4);
			mContext.sendBroadcast(new Intent(ACTION_CALL_STATE).putExtra(EXTRA_STATE, Callinfo.STATUS_OUTGOING).putExtra(EXTRA_NUM, num));
		}else if (cmd.equals("ID")) {
			String num = getdata(data, 4, bodyendindex + 1 - 4);
			mContext.sendBroadcast(new Intent(ACTION_CALL_STATE).putExtra(EXTRA_STATE, Callinfo.STATUS_INCOMING).putExtra(EXTRA_NUM, num));
		}else if (cmd.equals("IG")) {
			mContext.sendBroadcast(new Intent(ACTION_CALL_STATE).putExtra(EXTRA_STATE, Callinfo.STATUS_SPEAKING));
		}else if (cmd.equals("IF")) {
			mContext.sendBroadcast(new Intent(ACTION_CALL_STATE).putExtra(EXTRA_STATE, Callinfo.STATUS_TERMINATE));
		}else if (cmd.equals("T0")) {
			mContext.sendBroadcast(new Intent(ACTION_AUDIO).putExtra(EXTRA_PATH, "car"));
		}else if (cmd.equals("T1")) {
			mContext.sendBroadcast(new Intent(ACTION_AUDIO).putExtra(EXTRA_PATH, "phone"));
		}else if (cmd.equals("IO")) {
			mContext.sendBroadcast(new Intent(ACTION_MIC).putExtra(EXTRA_STATE, true));
		}else if (cmd.equals("MA")) {
			mContext.sendBroadcast(new Intent(ACTION_MUSIC_PLAYING).putExtra(EXTRA_STATE, false));
		}else if (cmd.equals("MB")) {
			mContext.sendBroadcast(new Intent(ACTION_MUSIC_PLAYING).putExtra(EXTRA_STATE, true));
		}else if (cmd.equals("MI")) {
			String title = "";
			String singer = "";
			int len = 0;
			int count = 0, preindex = 0;;
			for (int i = 2; i <= bodyendindex; i++) {
				if (data[i] == -1) {
					count++;

					switch (count) {
					case 1:
						title = getdata(data, 2, i - 2);
						break;

					case 2:
						singer = getdata(data, preindex, i - preindex);
						break;
						
					case 3:
						String number = getdata(data, preindex, i - preindex);
						Log.e(TAG, "number=" + number);
						if(number.length() > 0 && isnum(number)) {
							
							len = Integer.parseInt(number);
					
							mContext.sendBroadcast(new Intent(ACTION_MUSIC_ID3).putExtra(EXTRA_NAME, title).putExtra(EXTRA_SINGER, singer)
								.putExtra(EXTRA_TIME, len));
						}
						return true;
					default:
						break;
					}
					
					preindex = i + 1;
				}
				
			}
		}else if (cmd.equals("PS")) {//电量，信号量
			String power = getdata(data, 4, 2);
			mContext.sendBroadcast(new Intent(ACTION_PHONE_POWER_STATUS_CHANGE).putExtra(EXTRA_PATH, power));
		}else if(cmd.equals("M7")) {//播放位置
			int current = 0;
			String len = getdata(data, 2, bodyendindex + 1 - 2);
			current = Integer.valueOf(len);
			mContext.sendBroadcast(new Intent(ACTION_MUSIC_PLAY_POS).putExtra(EXTRA_TIME, current));
		}else if (cmd.equals("MW")) {
			version = getdata(data, 2, bodyendindex - 2);
		}else if (cmd.equals("ST")) {
			String state = getdata(data, 2, 1);
			if (state.equals("0")) {
				mContext.sendBroadcast(new Intent(ACTION_DISCONNECTED));
				mContext.sendBroadcast(new Intent(ACTION_BTSTATE).putExtra(EXTRA_STATE, false));
			}else{
				mContext.sendBroadcast(new Intent(ACTION_BTSTATE).putExtra(EXTRA_STATE, true));
			}
		}else if (cmd.equals("IS")) {
			mContext.sendBroadcast(new Intent(ACTION_BTSTATE).putExtra(EXTRA_STATE, true));
		}else if (bodyendindex == 2 && cmd.substring(0, 1).equals("S")) {
			if (getdata(data, 1, 1).equals("1")) {
				mContext.sendBroadcast(new Intent(ACTION_DISCONNECTED).putExtra(EXTRA_PATH, "hfp"));
			}else{
				mContext.sendBroadcast(new Intent(ACTION_CONNECTED).putExtra(EXTRA_PATH, "hfp"));
			}
			if (getdata(data, 2, 1).equals("1")) {
				mContext.sendBroadcast(new Intent(ACTION_DISCONNECTED).putExtra(EXTRA_PATH, "a2dp"));
			}else{
				mContext.sendBroadcast(new Intent(ACTION_CONNECTED).putExtra(EXTRA_PATH, "a2dp"));
			}
		}
		return true;
	}
	
	public String getdata(byte[] data, int offset, int byteCount){
		if (byteCount > 0) {
			return new String(data, offset, byteCount);
		}else{
			return "";
		}
	}
	
	public static String version = "";

    public boolean isnum(String num){
		Pattern p = Pattern.compile("[0-9]*"); 
		Matcher m = p.matcher(num);
		Log.e(TAG, "isnum=" + m.matches());
		return m.matches();
    }
}
