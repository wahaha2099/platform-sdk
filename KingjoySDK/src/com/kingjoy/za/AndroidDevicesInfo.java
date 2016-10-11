package com.kingjoy.za;

import android.content.Context;
import android.telephony.TelephonyManager;

//注：根据Android的安全机制，在使用TelephonyManager时，
//必须在AndroidManifest.xml中添加<uses-permission android:name="READ_PHONE_STATE" /> 否则无法获得系统的许可。
/**
 * @Title: AndroidDevicesInfo.java
 * @Package com.kingjoy.uplus
 * @Description: Copyright (C) 2014 kingjoy
 * @author WarZhan
 * @date 2014-12-18 上午11:27:29
 * @version V1.0
 */
public class AndroidDevicesInfo {

	private static AndroidDevicesInfo _instance = null;

	public static AndroidDevicesInfo instance() {
		if (null == _instance) {
			_instance = new AndroidDevicesInfo();
		}
		return _instance;
	}

	private Context mContext;

	public void init(Context context) {
		mContext = context;
	}

	public String getDevicesInfo() {
		// 解释：
		// IMSI是国际移动用户识别码的简称(International Mobile Subscriber Identity)
		// IMSI共有15位，其结构如下：
		// MCC+MNC+MIN
		// MCC：Mobile Country Code，移动国家码，共3位，中国为460;
		// MNC:Mobile NetworkCode，移动网络码，共2位
		// 在中国，移动的代码为电00和02，联通的代码为01，电信的代码为03
		// 合起来就是（也是Android手机中APN配置文件中的代码）：
		// 中国移动：46000 46002
		// 中国联通：46001
		// 中国电信：46003
		// 举例，一个典型的IMSI号码为460030912121001

		// IMEI是International Mobile Equipment Identity （国际移动设备标识）的简称
		// IMEI由15位数字组成的”电子串号”，它与每台手机一一对应，而且该码是全世界唯一的
		// 其组成为：
		// 1. 前6位数(TAC)是”型号核准号码”，一般代表机型
		// 2. 接着的2位数(FAC)是”最后装配号”，一般代表产地
		// 3. 之后的6位数(SNR)是”串号”，一般代表生产顺序号
		// 4. 最后1位数(SP)通常是”0″，为检验码，目前暂备用

		String info = "";
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mtyb = android.os.Build.BRAND; // 手机品牌
		String mtype = android.os.Build.MODEL; // 手机型号
		String deviceID = tm.getDeviceId();
		String imsi = tm.getSubscriberId();
		// String numer = tm.getLine1Number(); // 手机号码
		String serviceName = tm.getSimOperatorName(); // 运营商
		info = "品牌: " + mtyb + " 型号: " + mtype + " 版本: Android "
				+ android.os.Build.VERSION.RELEASE + " 设备ID: " + deviceID
				+ " IMSI: " + imsi + " 运营商: " + serviceName;
		// android.os.Build.MODEL
		// android.os.Build.VERSION.RELEASE
		return info;
	}

}
