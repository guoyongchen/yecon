package com.carocean.t19can;

import java.util.ArrayList;

import android.content.Context;

/**
 * ClassName:DataConvert
 * @function:辅助工具类 /数据转换
 */
public class DataConvert {
	
	/**
	 * 
	 * <p>
	 * Title: byteToInt
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param buf
	 * @return
	 */
	public static int byteToInt(byte buf) {
		return (int) (buf & 0xFF);
	}

	/**
	 * 将int类型的数据转换为byte数组
	 * 
	 * @param n
	 *            int数据
	 * @return 生成的byte数组
	 */
	public static byte[] intToBytes(int n) {
		String s = String.valueOf(n);
		return s.getBytes();
	}

	/**
	 * 
	 * <p>
	 * Title: int2Byte
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param intValue
	 * @return
	 */
	public static byte[] Hi2Lo2Byte(int intValue) {

		byte[] bytes = new byte[4];
		bytes[0] = (byte) (intValue & 0xff);
		bytes[1] = (byte) ((intValue & 0xff00) >> 8);
		bytes[2] = (byte) ((intValue & 0xff0000) >> 16);
		bytes[3] = (byte) ((intValue & 0xff000000) >> 24);
		return bytes;
	}

	/**
	 * 将byte数组转换为int数据
	 * 
	 * @param b
	 *            字节数组
	 * @return 生成的int数据
	 */
	public static int bytesToInt(byte[] b) {
		String s = new String(b);
		return Integer.parseInt(s);
	}

	/**
	 * 
	 * <p>
	 * Title: intArrayToByteArray
	 * </p>
	 * <p>
	 * Description: In[]2byte[]
	 * </p>
	 * 
	 * @param iBuff
	 * @param ilen
	 * @return
	 */
	public static byte[] intArrayToByteArray(int[] iBuff, int ilen) {
		byte[] result = new byte[ilen];

		for (int i = 0; i < ilen; i++) {
			result[i] = (byte) (iBuff[i] & 0xFF);
		}

		return result;
	}

	/**
	 * 
	 * <p>
	 * Title: BytesToStr
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param target
	 * @return
	 */
	public static String BytesToStr(byte[] target) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0, j = target.length; i < j; i++) {
			buf.append((char) target[i]);
		}
		return buf.toString();
	}

	/**
	 * 
	 * <p>
	 * Title: Bytes2Str
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param target
	 * @return
	 */
	public static String Bytes2Str(byte[] target) {
		String buf = "[";
		for (int i = 0, j = target.length; i < j; i++) {
			String hex = Integer.toHexString(target[i] & 0xFF);

			if (1 == hex.length()) {
				hex = "0" + hex;
			}

			buf += hex + " ";
		}

		buf += "]";
		return buf;
	}
	
	public static String Bytes2StrEx(byte[] target, int ilen) {
		String buf = "[";
		for (int i = 0, j = ilen; i < j; i++) {
			String hex = Integer.toHexString(target[i] & 0xFF);

			if (1 == hex.length()) {
				hex = "0" + hex;
			}

			buf += hex + " ";
		}

		buf += "]";
		return buf;
	}

	/**
	 * 
	 * <p>
	 * Title: GetBit
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param iData
	 * @param iBitPos
	 * @return
	 */
	public static int GetBit(int iData, int iBitPos) {
		return ((iData >> iBitPos) & 0x01);
	}

	/**
	 * 
	 * <p>
	 * Title: byte2Short
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param b
	 * @param cursor
	 * @return
	 */
	public static short byte2Short(byte[] b, int cursor) {
		short shortValue = 0;
		int iLoopEnd = b.length - cursor;
		int bitCount = 0;

		if (iLoopEnd > 2) {
			iLoopEnd = 2;
		}

		for (int i = 0; i < iLoopEnd; i++) {
			shortValue += (b[i + cursor] & 0xFF) << bitCount;
			bitCount += 8;
		}

		return shortValue;
	}

	public static int byte2Int(byte[] data, int cursor) {
		int intValue = 0;
		int iLoopEnd = data.length - cursor;
		int bitCount = 0;

		if (iLoopEnd > 4) {
			iLoopEnd = 4;
		}
		for (int i = 0; i < iLoopEnd; i++) {
			intValue += (data[i + cursor] & 0xFF) << bitCount;
			bitCount += 8;
		}
		return intValue;
	}
	
	public static byte[] getdatas(byte[] packet) {
		byte[] bydatas = new byte[packet.length];
		
		bydatas[0] = packet[1];
		bydatas[1] = packet[0];
		return bydatas;
	}
	
	public static int MakeInt(byte[] packet, int cursor) {
		byte[] bydatas = getdatas(packet);
		
		int intValue = 0;
		int iLoopEnd = bydatas.length - cursor;
		int bitCount = 0;

		if (iLoopEnd > 4) {
			iLoopEnd = 4;
		}
		for (int i = 0; i < iLoopEnd; i++) {
			intValue += (bydatas[i + cursor] & 0xFF) << bitCount;
			bitCount += 8;
		}
		return intValue;
	}

	public static int getAdd(byte[] bydatas) {
		int iRet = 0;
		
		for (byte b : bydatas) {
			iRet += b;
		}
		
		return iRet;
	}
	
	public static ArrayList<String> getStringArray(String value) {

		int nStartPos = 0;
		int nPos = 0;

		ArrayList<String> list = new ArrayList<String>();
		while (true) {
			nPos = value.indexOf(",", nStartPos);
			if (nPos < nStartPos) {
				String strTmp = value.substring(nStartPos);
				list.add(strTmp);
				break;
			} else {
				String strTmp = value.substring(nStartPos, nPos);
				list.add(strTmp);
				nStartPos = nPos + 1;
			}
		}

		return list;
	}

//	public static void putInt(Context context, String string, int ival)
//			throws RemoteException {
//		SharedPreferences sp = context.getSharedPreferences(
//				CAN_SHAREDPREFERENCES_DATA, Context.MODE_PRIVATE);
//		sp.edit().putInt(string, ival).commit();
//	}
//
//	public static int getInt(Context context, String string)
//			throws RemoteException {
//		SharedPreferences sp = context.getSharedPreferences(
//				CAN_SHAREDPREFERENCES_DATA, Context.MODE_PRIVATE);
//		return sp.getInt(string, 0);
//	}
//
//	public static int getIntEx(Context context, String string, int idef)
//			throws RemoteException {
//		SharedPreferences sp = context.getSharedPreferences(
//				CAN_SHAREDPREFERENCES_DATA, Context.MODE_PRIVATE);
//		return sp.getInt(string, idef);
//	}
//	
//	public static void putStr(Context context, String string, String string2)
//			throws RemoteException {
//		SharedPreferences sp = context.getSharedPreferences(
//				CAN_SHAREDPREFERENCES_DATA, Context.MODE_PRIVATE);
//		sp.edit().putString(string, string2).commit();
//	}
//
//	public static String getStr(Context context, String string)
//			throws RemoteException {
//		SharedPreferences sp = context.getSharedPreferences(
//				CAN_SHAREDPREFERENCES_DATA, Context.MODE_PRIVATE);
//		return sp.getString(string, "");
//	}

	public static int getLayoutId(Context context, String string) {
		return context.getResources().getIdentifier(string, "layout",
				context.getPackageName());
	}

	public static int getStringId(Context context, String string) {
		return context.getResources().getIdentifier(string, "string",
				context.getPackageName());
	}

	public static int getDrawableId(Context context, String string) {
		return context.getResources().getIdentifier(string, "drawable",
				context.getPackageName());
	}

	public static int getStyleId(Context context, String string) {
		return context.getResources().getIdentifier(string, "style",
				context.getPackageName());
	}

	public static int getId(Context context, String string) {
		return context.getResources().getIdentifier(string, "id",
				context.getPackageName());
	}

	public static int getColorId(Context context, String string) {
		return context.getResources().getIdentifier(string, "color",
				context.getPackageName());
	}

	public static int getXmlId(Context context, String string) {
		return context.getResources().getIdentifier(string, "xml",
				context.getPackageName());
	}
	
	public static int bcd2Dec(byte bcd) {
		StringBuffer temp = new StringBuffer(2);
		temp.append((byte) ((bcd & 0xf0) >> 4));
		temp.append((byte) (bcd & 0x0f));
		String ret = temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
		return Integer.parseInt(ret);
	}

}
