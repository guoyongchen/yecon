package com.carocean.bt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.carocean.ApplicationManage;
import com.carocean.R;
import com.carocean.bt.data.DBManager;
import com.carocean.settings.utils.timeUtils;
import com.carocean.utils.ActivityMonitor;
import com.carocean.utils.CharacterParser;
import com.carocean.utils.SourceManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Bluetooth {
	public static final String TAG = "Bluetooth";
	public static Context mContext;
	public Profile profile;
	
	public String devicename = "";
	static public ArrayList<HashMap<String, String>> searchlist = new ArrayList<HashMap<String, String>>();
	static public ArrayList<HashMap<String, String>> pairedlist = new ArrayList<HashMap<String, String>>();
	static public ArrayList<HashMap<String, String>> recordlist = new ArrayList<HashMap<String, String>>();
	static public ArrayList<HashMap<String, String>> contactlist = new ArrayList<HashMap<String, String>>();
	
	static public ArrayList<Callinfo> calllist = new ArrayList<Callinfo>();
	private AudioManager mAudioManager = null;

	public static Bluetooth mInstance;
	ActivityMonitor am;
	
	public static Bluetooth getInstance() {
		if (null == mInstance) {
			Context context = ApplicationManage.getContext();
			mInstance = new Bluetooth(context);
		}
		return mInstance;
	}
	
	public Bluetooth(Context context) {
		mContext = context;
		profile = new Profile(mContext);
		am = new ActivityMonitor(mContext);
	}
	
	public void sendcmd(String cmd){
		profile.sendcmd(cmd);
	}
	
	boolean isbtopened = false;
	public boolean isbtopened(){
		return isbtopened;
	}
	
	public boolean isopeningbt(){
		return switchingbt == 1;
	}
	
	public boolean iscloseingbt(){
		return switchingbt == 2;
	}
	
	public void onbtstate(Intent intent){
		isbtopened = intent.getBooleanExtra(BTService.EXTRA_STATE, false);
		switchingbt = 0;
		if (!isbtopened) {
			mac_connected = "";
			name_connected = "";
			mac_connecting = "";
			isplaying = false;
			isa2dpconnected = false;
			ishfpconnected = false;
			pairedlist.clear();
			searchlist.clear();
			title = "";
			singer = "";
			music_time_max = 0;
			music_time_cur = 0;
		}
	}
	
	public void getbtstate(){
		sendcmd("P2");
	}
	
	public void openbt(){
		sendcmd("P1");
	}
	
	public void closebt(){
		sendcmd("P0");
	}
	
	int switchingbt = 0;
	public void switchbt(boolean bset){
		Log.e(TAG, "switchbt bset=" + bset + " isbtopened=" + isbtopened + " switchingbt=" + switchingbt);
		if (isbtopened == bset || switchingbt != 0) {
			return;
		}
		switchingbt = bset ? 1 : 2;
		if (bset) {
			openbt();
		}else{
			closebt();
		}
	}
	
	public void switchbt(){
		switchbt(!isbtopened);
	}
	
	public void resetbt() {
		
	}
	
	public void disconnect(){
		if (isconnected()) {
			sendcmd("CD");
		}
	}
	static public String mac_connecting = "";
	public void connect(String mac){
		if (!isconnected()) {
			sendcmd("CC" + mac);
			mac_connecting = mac;
		}
	}

	public boolean pair_mod = false;
	
	public void inpairmod() {
		if (!pair_mod) {
			sendcmd("CA");
			pair_mod = true;
		}
	}

	public void outpairmod() {
		if (pair_mod) {
			sendcmd("CB");
			pair_mod = false;
		}
	}
	
	public void switchpairmod(){
		if (pair_mod) {
			outpairmod();
		}else{
			inpairmod();
		}
	}

	public void sendBroadcastATE(Intent intent) {
		if (null != mContext && null != intent) {
			mContext.sendBroadcast(intent);
		}
	}
	
	public boolean loadcontact(){
		if (loading_contact || mask_loadrecord != 0) {
			return false;
		}
		loading_contact = true;
		contactlist.clear();
		sendcmd("PB");
		return true;
	}
	
	static public int mask_loadrecord = 0;
	
	public boolean loadrecord(){
		if (loading_contact || mask_loadrecord != 0) {
			return false;
		}
		mask_loadrecord = 3;
		loadoutgoing();
		return true;
	}

	public void loadoutgoing(){
		sendcmd("PH");
	}
	public void loadincoming(){
		sendcmd("PI");
	}
	public void loadmiss(){
		sendcmd("PJ");
	}
	
	public void getpairedlist(){
		sendcmd("MX");
	}
	
	public void getlocaldevicename(){
		sendcmd("MM");
	}
	
	public void setlocaldevicename(String name){
		if (name == null || name.isEmpty()) {
			return;
		}
		sendcmd("MM" + name);
	}
	
	public void delpair(int index){
		sendcmd("CV" + index);
	}
	
	public void delpair(String mac){
		for (int i = 0; i < pairedlist.size(); i++) {
			if (pairedlist.get(i).get("mac").equals(mac)) {
				sendcmd("CV" + i);
			}
		}
	}
	
	public boolean isonhold(){
		return false;
	}
	
	public void switchaudio(boolean set){
		if (set) {
			sendcmd("CP");
		}else{
			sendcmd("CN");
		}
	}
	
	public void switchaudio(){
		if (isaudioincar()) {
			sendcmd("CN");
		}else{
			sendcmd("CP");
		}
	}

	public DBManager mdbmanager = null;
	public void inittelzonedatabase(Context context) {
		if (mdbmanager == null) {
			mdbmanager = new DBManager(context);
			mdbmanager.openDatabase();
		}
	}
	public String gettelzone(String num) {
		if (mdbmanager != null) {
			return mdbmanager.GetTelZone(num);
		} else {
			return "";
		}
	}
	
	public void switchmic(){
    /*	int curmute = AtcSettings.Audio.GetMicMute(0);
		AtcSettings.Audio.SetMicMute(curmute == 0);*/
		sendcmd("CM");
	}
	
	public void sendDTMFCode(String num){
		sendcmd("CX" + num);
	}

	public String lastcallnum = "";
	long pre_dial = 0;
	public void dial(String num){
		if (num == null || num.isEmpty()) {
			return;
		}
		if (num.equals("#8377466#")) {
			Toast.makeText(mContext, Profile.version, Toast.LENGTH_LONG).show();
			return;
		}
		long curtime = SystemClock.elapsedRealtime();
		if (curtime - pre_dial < 3000) {
			return;
		}
		pre_dial = SystemClock.elapsedRealtime();
		sendcmd("CW" + num);
		lastcallnum = num;
	}
	
	public void hangup(){
		if (isincoming()) {
			sendcmd("CF");
		}else{
			sendcmd("CG");
		}
	}

	public final static String PERSYS_BT_AUTO_ANSWER = "persist.sys.bt_auto_answer";
	public boolean isautoanswer() {
		return SystemProperties.getBoolean(PERSYS_BT_AUTO_ANSWER, false);
	}

	public void setautoanswer(boolean b) {
		SystemProperties.set(PERSYS_BT_AUTO_ANSWER, b ? "true" : "false");
	}
	
	public void answer(){
		sendcmd("CE");
	}
	
	public boolean iscallidle(){
		return calllist.isEmpty();
	}
	
	public int getcalltimecount(){
		for (Callinfo call : calllist) {
			if (call.bActive && call.status == Callinfo.STATUS_SPEAKING) {
				return call.timecount;
			}
		}
		return 0;
	}

	public String getcallarea(){
		for (Callinfo call : calllist) {
			if (call.bActive) {
				return call.area;
			}
		}
		return "";
	}
	
	public String getcallname(){
		for (Callinfo call : calllist) {
			if (call.bActive) {
				return call.name;
			}
		}
		return "";
	}
	
	public String getcallnum(){
		for (Callinfo call : calllist) {
			if (call.bActive) {
				return call.num;
			}
		}
		return "";
	}
	
	public int getcallstatus(){
		for (Callinfo call : calllist) {
			if (call.bActive) {
				return call.status;
			}
		}
		return 0;
	}
	
	public boolean isspeaking(){
		for (Callinfo call : calllist) {
			if (call.bActive && call.status == Callinfo.STATUS_SPEAKING) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isincoming(){
		for (Callinfo call : calllist) {
			if (call.bActive && call.status == Callinfo.STATUS_INCOMING) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isoutgoing(){
		for (Callinfo call : calllist) {
			if (call.bActive && call.status == Callinfo.STATUS_OUTGOING) {
				return true;
			}
		}
		return false;
	}
	
	public void getbtversion(){
		sendcmd("MY");
	}
	
	public void handlebootcompleted(){
		inittelzonedatabase(mContext);
		Profile.remaindatalen = 0;
		
		getbtstate();
		getbtversion();
		getbtinfo();
		
		pair_mod = true;
		outpairmod();
	}
	
	public void getbtinfo(){
		getlocaldevicename();
		getpairedlist();
		gethfpstatus();
	}
	
	public void gethfpstatus(){
		sendcmd("CY");
	}
	
	public void stopdiscovery() {
		if (!bdiscovery) {
			return;
		}
		bdiscovery = false;
		sendcmd("ST");
	}
	
	public void discovery(){
		if (bdiscovery) {
			return;
		}
		bdiscovery = true;
		sendcmd("SD");
	}
	
	public void onlocaldevicenamechanged(Intent intent){
		devicename = intent.getStringExtra(BTService.EXTRA_NAME);
	}
	
	public void ondevicefound(Intent intent){
		String name = intent.getStringExtra(BTService.EXTRA_NAME);
		String mac = intent.getStringExtra(BTService.EXTRA_MAC);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("mac", mac);
		map.put("name", name);
		if (!pairedlist.contains(map)) {
			searchlist.add(map);
		}
	}
	
	static public boolean bdiscovery = false;
	public void ondiscoverystart(){
		searchlist.clear();
	}

	boolean focus = false;
	public void requesta2dpfocus() {
		Log.e(TAG, "18-9-5 requesta2dpfocus SourceManager.getSource()=" + SourceManager.getSource());

		if (focus) {
			return;
		}
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		int ret = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		
		if(AudioManager.AUDIOFOCUS_REQUEST_GRANTED == ret) {
			AvinManager.getInstance().initAvin();
			//AvinManager.getInstance().resumeAvin2AudioFocus();
		}
		if (isA2DPconnected() && !isA2DPPlaying()) {
			sendAvrcpCommand(BTService.CMD_PLAY);
		}
		focus = true;
	}
	
	public void releasea2dpfocus(){
		Log.e(TAG, "releasea2dpfocus isA2DPconnected=" + isA2DPconnected() + " isA2DPPlaying=" + isA2DPPlaying());
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		mAudioManager.abandonAudioFocus(mAudioFocusListener);

		if (isA2DPconnected() && isA2DPPlaying()) {
			sendAvrcpCommand(BTService.CMD_PAUSE);
		}
		focus = false;
	}
	
	public String getmediaTitle(){
		return title;
	}
	
	public void sendAvrcpCommand(int cmd){
		String sendCmd = "";
		switch(cmd) {
		case BTService.CMD_PLAY:
			sendCmd = "MS";
			break;
		case BTService.CMD_PAUSE:
			sendCmd = "MB";
			break;
		case BTService.CMD_NEXT:
			sendCmd = "MD";
			break;
		case BTService.CMD_PREV:
			sendCmd = "ME";
			break;
		case BTService.CMD_STOP:
			sendCmd = "MC";
			break;
		}
		sendcmd(sendCmd);
	}
	
	public boolean isplaying = false;
	public boolean isA2DPPlaying(){
		return isplaying;
	}

	public boolean isa2dpconnected = false;
	public boolean ishfpconnected = false;
	public boolean isA2DPconnected(){
		return isa2dpconnected;
	}
	public boolean isHFPconnected(){
		return ishfpconnected;
	}
	
	public boolean isaddrconnected(String mac){
		return mac_connected.equals(mac);
	}
	static public String name_connected = "";
	static public String mac_connected = "";
	
	public void onconnected(Intent intent){
		String connected = intent.getStringExtra(BTService.EXTRA_NAME);
		String mac = intent.getStringExtra(BTService.EXTRA_MAC);
		if (connected != null && mac != null) {
			name_connected = connected;
			mac_connected = mac;
			ContactDB.getinstance().getPbRecords(contactlist, recordlist);
			if (!contactlist.isEmpty()) {
				sortcontactdata();
			}
			if (!recordlist.isEmpty()) {
				sortrecorddata();
			}
			for (int i = 0; i < searchlist.size(); i++) {
				if (searchlist.get(i).get("mac").equals(mac_connected)) {
					searchlist.remove(i);
					break;
				}
			}
			boolean bfound = false;
			for (int i = 0; i < pairedlist.size(); i++) {
				if (pairedlist.get(i).get("mac").equals(mac_connected)) {
					bfound = true;
					break;
				}
			}
			
			if (!bfound) {
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("name", name_connected);
				item.put("mac", mac_connected);
				pairedlist.add(0, item);
			}
			mac_connecting = "";
		}

		String path = intent.getStringExtra(BTService.EXTRA_PATH);
		if (path != null) {
			if (path.equals("hfp")) {
				ishfpconnected = true;
			}else if (path.equals("a2dp")) {
				isa2dpconnected = true;
			}
		}else if (!ishfpconnected && !isa2dpconnected){
			gethfpstatus();
		}
	}

	static public boolean loading_contact = false;
	public void ondownloadstatechange(Intent intent){
		int state = intent.getIntExtra(BTService.EXTRA_STATE, 0);
		String path = intent.getStringExtra(BTService.EXTRA_PATH);
		Log.e(TAG, "ondownloadstatechange state=" + state + " path=" + path);
		switch (state) {
		case BTService.DOWNLOAD_STATE_FAIL:
			if (path.equals("contact")) {
				loading_contact = false;
			}else if (path.equals("record")) {
				mask_loadrecord = 0;
			}
			break;
			
		case BTService.DOWNLOAD_STATE_END:
			if (path.equals("contact")) {
				loading_contact = false;
				sortcontactdata();
			}else if (path.equals("record")) {
				switch (mask_loadrecord) {
				case 1:
					mask_loadrecord--;
					sortrecorddata();
					break;
				case 2:
					mask_loadrecord--;
					loadmiss();
					break;
				case 3:
					mask_loadrecord--;
					loadincoming();
					break;

				default:
					break;
				}
			}
			break;
			
		case BTService.DOWNLOAD_STATE_SUCCESS:
			if (path.equals("record") && mask_loadrecord == 3) {
				recordlist.clear();
			}else if (path.equals("contact") && loading_contact) {
		//		contactlist.clear();
			}
			break;

		default:
			break;
		}
	}
	
	public void onplayingstatechanged(Intent intent){
		isplaying = intent.getBooleanExtra(BTService.EXTRA_STATE, false);
	}

	public String title = "";
	public String singer = "";
	public int music_time_max = 0;
	public int music_time_cur = 0;
	public void onid3info(Intent intent){
		title = intent.getStringExtra(BTService.EXTRA_NAME);
		singer = intent.getStringExtra(BTService.EXTRA_SINGER);
		music_time_max = intent.getIntExtra(BTService.EXTRA_TIME, 0);
		music_time_max /= 1000;
	}
	
	public void onmusicpos(Intent intent){
		music_time_cur = intent.getIntExtra(BTService.EXTRA_TIME, 0);
		music_time_cur /= 1000;
	}

	public static String millSeconds2readableTime(int millseconds) {
		int hour = (millseconds / 60) / 60;
		int minute = (millseconds / 60) % 60;
		int second = millseconds % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	public String audiopath = "";
	public void onaudiochange(Intent intent){
		audiopath = intent.getStringExtra(BTService.EXTRA_PATH);
	}
	public boolean isaudioincar(){
		return audiopath.equals("car");
	}
	
	public boolean ismicopened = true;
	public void onmicchange(Intent intent){
		ismicopened = intent.getBooleanExtra(BTService.EXTRA_STATE, true);
	}
	

	private static String[] WHITELIST_GIS = { "navi", "mapbarmap", "sygic", "cld", "migo", "obile.mainframe", "map", "igo", "papago", "kingwaytek", "com.waze" };

	public boolean iscurnavimod() {
		for (String str : WHITELIST_GIS) {
			am.setActivityMonitorLisenter(str, null);
			if (am.isForeground()) {
				return true;
			}
		}
		return false;
	}

	public void oncall(Intent intent){
		String num = intent.getStringExtra(BTService.EXTRA_NUM);
		boolean bfound = false;
		
		for (Callinfo call : calllist) {
			if (!call.num.isEmpty() && num != null && call.num.equals(num)) {
				bfound = true;
				break;
			}
		}
		
		if (!bfound && num != null) {
			calllist.add(new Callinfo());
		}

		
		for (Callinfo call : calllist) {
			call.oncall(intent);
		}
		
		for (int i = calllist.size() - 1; i >= 0; i--) {
			Callinfo call = calllist.get(i);
			if (call.bFinish) {
				call.addonerecord();
				calllist.remove(i);
			}
		}
		return;
	}
	
	
	
	public static String time24to12(String time) {
		if (timeUtils.is24HourFormat(mContext)) {
			time = time.substring(0, 5);
			return time;
		} else {
			if (time.length() != 8) {
				return time;
			}
			String hour = time.substring(0, 2);
			int nHour = Integer.parseInt(hour);
			if (nHour > 12) {
				nHour -= 12;
				hour = String.format("%02d", nHour);
				time = hour + time.substring(2, 5);
				time = time + " " + "PM";
				// time =
				// mContext.getResources().getString(R.string.setting_time_pm) +
				// " " + time;
			} else {
				time = time.substring(0, 5);
				time = time + " " + "AM";
				// time =
				// mContext.getResources().getString(R.string.setting_time_am) +
				// " " + time;
			}

			return time;
		}
	}
	public static String formatcallhistorytime(String callhistorytime) {
		if (callhistorytime == null || callhistorytime.length() != 15) {
			return callhistorytime;
		}
		final long currentTimeMillis = System.currentTimeMillis();
		final long aDayInMillis = 1000 * 60 * 60 * 24;

		String year = callhistorytime.substring(0, 4);
		String mon = callhistorytime.substring(4, 6);
		String day = callhistorytime.substring(6, 8);
		String date = year + "/" + mon + "/" + day;
		String time = callhistorytime.substring(9, 15);
		String hour = time.substring(0, 2);
		String minute = time.substring(2, 4);
		String second = time.substring(4, 6);
		Time call = new Time();
		call.year = Integer.parseInt(year);
		call.month = Integer.parseInt(mon) - 1;
		call.monthDay = Integer.parseInt(day);
		long callMillis = call.toMillis(true);

		Time now = new Time();
		now.setToNow();
		Log.e(TAG, " now.year=" + now.year + " call.year=" + call.year + " now.month=" + now.month + " call.month=" + call.month + " now.monthDay="
				+ now.monthDay + " call.monthDay=" + call.monthDay);
		if (true) {
			return "" + year + "." + mon + "." + day + "  " + hour + ":" + minute + ":" + second;
		}
		if (now.year == call.year && now.month == call.month && now.monthDay == call.monthDay) {
			return time24to12(time);
		}
		now.hour = 0;
		now.minute = 0;
		now.second = 0;
		long nowMillis = now.toMillis(true);
		Log.e(TAG, " currentTimeMillis=" + currentTimeMillis + " callMillis=" + callMillis + " nowMillis=" + nowMillis);
		if (currentTimeMillis > callMillis && nowMillis - callMillis <= aDayInMillis) {
	//		return mContext.getResources().getString(R.string.bt_yesterday);
		}

		Time begin = new Time();
		begin.setToNow();
		begin.hour = 0;
		begin.minute = 0;
		begin.second = 0;
		long beginMillis = begin.toMillis(true);
		beginMillis -= (aDayInMillis * (begin.weekDay - 1));
		beginMillis -= 1;

		Time end = new Time();
		end.setToNow();
		end.hour = 0;
		end.minute = 0;
		end.second = 0;
		long endMillis = end.toMillis(true);
		endMillis += (aDayInMillis * (8 - end.weekDay));
		endMillis += 1;
		int stringid[] = { R.string.setting_weekday_monday, R.string.setting_weekday_tuesday, R.string.setting_weekday_wednesday,
				R.string.setting_weekday_thursday, R.string.setting_weekday_friday, R.string.setting_weekday_saturday, R.string.setting_weekday_sunday };
		Log.e(TAG, " beginMillis=" + beginMillis + " endMillis=" + endMillis + " callMillis=" + callMillis);
		if (callMillis > beginMillis && callMillis < endMillis) {
			int weekday = (int) ((callMillis - beginMillis) / aDayInMillis);
			Log.e(TAG, " weekday=" + weekday);
			if (weekday < 7) {
				return mContext.getResources().getString(stringid[weekday]);
			}
		}

		Log.e(TAG, " date=" + date);
		return date;
	}
	
	public void onrecordload(Intent intent){
		String name = intent.getStringExtra(BTService.EXTRA_NAME);
		String num = intent.getStringExtra(BTService.EXTRA_NUM);
		String type = intent.getStringExtra(BTService.EXTRA_TYPE);
		String time = intent.getStringExtra(BTService.EXTRA_TIME);
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("num", num);
		item.put("type", type);
		item.put("time", time);
		item.put("time_f", formatcallhistorytime(time));
		recordlist.add(item);
		ContactDB.getinstance().insertOneRecord(type, name, num, time);
	}
	
	public void oncontactload(Intent intent){
		String name = intent.getStringExtra(BTService.EXTRA_NAME);
		String num = intent.getStringExtra(BTService.EXTRA_NUM);
		Log.e(TAG, "oncontactload name=" + name + " num=" + num);
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("num", num);
		contactlist.add(item);
		ContactDB.getinstance().insertOneRecord(ContactDB.TYPE_CONTACT, name, num, "");
	}
	
	public void onpairedlist(Intent intent){
		String name = intent.getStringExtra(BTService.EXTRA_NAME);
		String mac = intent.getStringExtra(BTService.EXTRA_MAC);
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("mac", mac);
		pairedlist.add(item);
	}
	
	public static boolean isFamilynameFirstCounty(char c) {
		return c >= 0x4E00 && c <= 0x9FA5 // �й�
				|| c >= 0x3040 && c <= 0x30FF || c >= 0x31F0 && c <= 0x31FF // �ձ�
				|| c >= 0x1100 && c <= 0x11FF // ����
				|| c >= 0xAB00 && c <= 0xAB5F // Խ��
				|| c >= 0x1800 && c <= 0x18AF; // �ɹ�
	}

	public static boolean isFamilynameFirstCounty(String str) {
		if (str == null)
			return false;
		for (char c : str.toCharArray()) {
			if (isFamilynameFirstCounty(c))
				return true;
		}
		return false;
	}

	public boolean checkcountname(String countname) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(countname);
		if (m.find()) {
			return true;
		}
		return false;
	}

	public class PinyinComparator implements Comparator<HashMap<String, String>> {

		public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
			if ((null == o2.get("py") || o2.get("py").equals("")) && (null == o1.get("py") || o1.get("py").equals(""))) {
				return 0;
			} else if (null == o2.get("py") || o2.get("py").equals("")) {
				return -1;
			} else if (null == o1.get("py") || o1.get("py").equals("")) {
				return 1;
			} else {
				if ((o1.get("pyheadflag") != null && o1.get("pyheadflag").equals("#")) && o2.get("pyheadflag") != null && o2.get("pyheadflag").equals("#")) {
					return 0;
				} else if (o1.get("pyheadflag") != null && o1.get("pyheadflag").equals("#")) {
					return 1;
				} else if (o2.get("pyheadflag") != null && o2.get("pyheadflag").equals("#")) {
					return -1;
				}
				return o1.get("py").compareTo(o2.get("py"));
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	public boolean isnewtime(String c1, String c2) throws Exception {
		if (c1 == null) {
			return false;
		} else if (c2 == null) {
			return true;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String s1 = new String(c1);
		String s2 = new String(c2);

		s1 = s1.replace("T", "");
		s2 = s2.replace("T", "");
		
		Date d1 = sdf.parse(s1);
		Date d2 = sdf.parse(s2);
	//	Log.e(TAG, "isnewtime year=" + sdf.format(d1));
		return d1.getTime() > d2.getTime();
	}

	private static class nameParams {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		String num = "";
		nameParams(ArrayList<HashMap<String, String>> list, String num) {
			this.num = num;
			this.list.addAll(list);
		}
	}

	private static class name_ResParams {
		String name = "";
		String num = "";
		name_ResParams() {
		}
	}
	
	private static class OrderParams {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		OrderParams(ArrayList<HashMap<String, String>> list) {
			this.list.addAll(list);
		}
	}

	private static class ResParams {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		ResParams() {
		}
	}

	public void getcallname(String num){
		if (contactlist.isEmpty()) {
			return;
		}
		nametask = new NameTask();
		nametask.execute(new nameParams(contactlist, num));
	}
	
	NameTask nametask = null;
	class NameTask extends AsyncTask<nameParams, Integer, name_ResParams> {

		@Override
		protected name_ResParams doInBackground(nameParams... arg0) {
			name_ResParams res = new name_ResParams();

			for (HashMap<String, String> map : arg0[0].list) {
				if (map.get("num").equals(arg0[0].num)) {
					res.name = map.get("name");
					res.num = arg0[0].num;
					break;
				}
			}
			return res;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(name_ResParams result) {
			if (result.name.isEmpty()) {
				return;
			}
			for (Callinfo call : calllist) {
				if (call.num.equals(result.num)) {
					call.name = result.name;
					BTService.notifyUiBtStatus(new Intent(BTService.ACTION_CALL_STATE));
					break;
				}
			}
			super.onPostExecute(result);
		}
	}

	public static boolean bsortrecorddata = false;
	public static boolean bsortcontactdata = false;

	public void sortrecorddata() {
		if (bsortrecorddata) {
			return;
		}
		ContactDB.getinstance().flushTable();
		if (!recordlist.isEmpty()) {
			if (recordTask != null) {
				recordTask.cancel(true);
				recordTask = null;
			}
			recordTask = new RecordTask();
			Log.e(TAG, "recordTask.execute");
			recordTask.execute(new OrderParams(recordlist));
			return;
		}
		Log.e(TAG, "sortrecorddata");
	}

	public boolean isdownloadcontactidle() {
		Log.e(TAG, "isdownloadcontactidle bsortcontactdata=" + bsortcontactdata + " loading_contact=" + loading_contact);
		return !bsortcontactdata && !loading_contact;
	}
	
	public boolean isdownloadrecordidle(){
		Log.e(TAG, "isdownloadrecordidle bsortrecorddata=" + bsortrecorddata + " mask_loadrecord=" + mask_loadrecord);
		return !bsortrecorddata && mask_loadrecord == 0;
	}
	
	public boolean isdownloadidle(){
		return isdownloadcontactidle() && isdownloadrecordidle();
	}
	
	public void sortcontactdata() {
		if (bsortcontactdata) {
			return;
		}
		ContactDB.getinstance().flushTable();
		Log.e(TAG, " contactlist.size=" + contactlist.size());
		if (!contactlist.isEmpty()) {
			if (contactTask != null) {
				contactTask.cancel(true);
				contactTask = null;
			}
			contactTask = new ContactTask();
			Log.e(TAG, "contactTask.execute");
			contactTask.execute(new OrderParams(contactlist));
			return;
		}
		Log.e(TAG, "sortcontactdata");
	}

	

	public HashMap<String, Integer> map_headindex = new HashMap<String, Integer>() {
		{
			put("A", 0);
			put("B", 0);
			put("C", 0);
			put("D", 0);
			put("E", 0);
			put("F", 0);
			put("G", 0);
			put("H", 0);
			put("I", 0);
			put("J", 0);
			put("K", 0);
			put("L", 0);
			put("M", 0);
			put("N", 0);
			put("O", 0);
			put("P", 0);
			put("Q", 0);
			put("R", 0);
			put("S", 0);
			put("T", 0);
			put("U", 0);
			put("V", 0);
			put("W", 0);
			put("X", 0);
			put("Y", 0);
			put("Z", 0);
		}
	};
	private ContactTask contactTask = null;
	public static String b_disable = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	private PinyinComparator pinyinComparator = new PinyinComparator();
	class ContactTask extends AsyncTask<OrderParams, Integer, ResParams> {

		@Override
		protected ResParams doInBackground(OrderParams... arg0) {
			ResParams res = new ResParams();
			StringBuilder[] pinyin;
			String pyhead = "";
			b_disable = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
			
			for (HashMap<String, String> map : arg0[0].list) {
				String name = map.get("name");
				pinyin = CharacterParser.getSelling(name);
				pyhead = pinyin[1].toString();
				map.put("py", pinyin[0].toString());
				map.put("pyhead", pyhead);
				map.put("pytodialnum", pinyin[2].toString());
				map.put("pyheadtodialnum", pinyin[3].toString());

				String pyheadfirst = "#";
				if (pyhead.toCharArray().length != 0) {
					pyheadfirst = String.valueOf(pyhead.toCharArray()[0]);
					if (pyheadfirst.getBytes()[0] < 'a' || pyheadfirst.getBytes()[0] > 'z') {
						pyheadfirst = "#";
					}
				}
				String pyheadflag = pyheadfirst.toUpperCase();
				map.put("pyheadflag", pyheadflag);
				if (b_disable.contains(pyheadflag)) {
					b_disable = b_disable.replace(pyheadflag, "");
				}
				res.list.add(map);
			}

			if (!isCancelled()) {
				Collections.sort(res.list, pinyinComparator);
				int len_total = res.list.size();
				for (int i = len_total - 1; i >= 1; i--) {
					HashMap<String, String> map = res.list.get(i);
					HashMap<String, String> map_pre = res.list.get(i - 1);
					String name = map.get("name");
					String name_pre = map_pre.get("name");
					if (name.equals(name_pre)) {
						map_pre.put("num1", map.get("num"));
						for (int j = 2; j < len_total; j++) {
							String key = "num" + j;
							String key2 = "num" + (j - 1);
							if (map.get(key2) == null) {
								break;
							}
							map_pre.put(key, map.get(key2));
					//		Log.e(TAG, "key=" + key + " value" + map.get(key2));
						}
						
						res.list.remove(i);
					}
					
				}
				int k = 0;
		//		Log.e(TAG, "b_disable = " + b_disable);
				String list[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
						"Y", "Z", "#" };
				for (int i = 0; i < list.length; i++) {
					String str = list[i];
					if (b_disable.contains(str)) {
						continue;
					}
					for (; k < res.list.size(); k++) {
						if (res.list.get(k).get("pyheadflag").equals(str)) {
							map_headindex.put(str, k);
							k++;
							break;
						}
					}
				}
			}

			return res;
		}

		@Override
		protected void onPreExecute() {
			Log.e(TAG, "onPreExecute");
			bsortcontactdata = true;
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(ResParams result) {
			if (result != null) {
				Log.e(TAG, "onPostExecute");
				contactlist.clear();
				contactlist.addAll(result.list);
			}
			bsortcontactdata = false;
			BTService.notifyUiBtStatus(new Intent(BTService.ACTION_DOWNLOAD_STATE).putExtra(BTService.EXTRA_PATH, "contact")
					.putExtra(BTService.EXTRA_STATE, BTService.DOWNLOAD_STATE_END));
			super.onPostExecute(result);
		}

	}
	
	private RecordTask recordTask = null;
	class RecordTask extends AsyncTask<OrderParams, Integer, ResParams> {

		@Override
		protected ResParams doInBackground(OrderParams... arg0) {
			ResParams res = new ResParams();
			for (HashMap<String, String> map : arg0[0].list) {
				String time = map.get("time");

				int nIndex = 0;
				for (; nIndex < res.list.size(); nIndex++) {
					try {
						if (isnewtime(time, (String) (res.list.get(nIndex).get("time")))) {
							res.list.add(nIndex, map);
							nIndex = -1;
							break;
						}
					} catch (Exception e) {
						Log.e(TAG, "isnewtime error");
						e.printStackTrace();
					}
				}
				if (nIndex != -1) {
					res.list.add(map);
				}
			}

			return res;
		}

		@Override
		protected void onPreExecute() {
			Log.e(TAG, "onPreExecute");
			bsortrecorddata = true;
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(ResParams result) {
			if (result != null) {
				Log.e(TAG, "onPostExecute");
				recordlist.clear();
				recordlist.addAll(result.list);
			}
			bsortrecorddata = false;
			BTService.notifyUiBtStatus(new Intent(BTService.ACTION_DOWNLOAD_STATE).putExtra(BTService.EXTRA_PATH, "record")
					.putExtra(BTService.EXTRA_STATE, BTService.DOWNLOAD_STATE_END));
			super.onPostExecute(result);
		}

	}
	
	public void ondisconnected(Intent intent){
		String path = intent.getStringExtra(BTService.EXTRA_PATH);
		contactlist.clear();
		recordlist.clear();
		if (path == null) {
			name_connected = "";
			mac_connected = "";
			mac_connecting = "";
			ishfpconnected = isa2dpconnected = false;
			title = "";
			singer = "";
			music_time_max = 0;
			music_time_cur = 0;
			return;
		}

		if (path != null) {
			if (path.equals("hfp")) {
				ishfpconnected = false;
			}else if (path.equals("a2dp")) {
				isa2dpconnected = false;
				title = "";
				singer = "";
				music_time_max = 0;
				music_time_cur = 0;
			}
		}
	}
	
	public boolean isconnected(){
		if(null == mac_connected) {
			return false;
		}
		return !mac_connected.isEmpty();
	}
	
	public void ondiscoveryend(){
		bdiscovery = false;
	}
	public void getPhonePowerStatus(){
		if (!isconnected()) {
			sendcmd("QD");
		}
	}
	public void getA2dpStatus(){
		if (!isconnected()) {
			sendcmd("MA");
		}
	}
	
	private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			// TODO Auto-generated method stub
			Log.e(TAG, "18-9-5 onAudioFocusChange focusChange=" + focusChange);
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS:
				AvinManager.getInstance().deinitAvin();
				releasea2dpfocus();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (iscallidle()) {
					AvinManager.getInstance().pauseAvin2AudioFocus();
				}
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				AvinManager.getInstance().deinitAvin();
				AvinManager.getInstance().initAvin();
				//AvinManager.getInstance().resumeAvin2AudioFocus();
				break;
			default:
				break;
			}
		}
	};
	public void requesthfpfocus() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		if (mAudioManager != null) {
			mAudioManager.requestAudioFocus(mHFPFocusListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			AvinManager.getInstance().initAvin();
		}
	}
	public void releasehfpfocus() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		AvinManager.getInstance().deinitAvin();
		mAudioManager.abandonAudioFocus(mHFPFocusListener);
	}
	private AudioManager.OnAudioFocusChangeListener mHFPFocusListener = new AudioManager.OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int arg0) {
			// TODO Auto-generated method stub

		}

	};
}