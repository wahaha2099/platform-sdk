package com.kingjoy.za;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kingjoy.sdk.KingjoySDK;
import com.kingjoy.sdk.KingjoySDK.AccountInfo;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity  extends UnityPlayerActivity {

	public static final String MSG_LOGIN_SUCCESS = "loginSuccess";

	public static final String MSG_LOGIN_FAIL = "loginFail";
	public static final String GAME_NAME = "众神风暴";
	// debugMode值为true时，为调试环境模式，当�?为false时，是生产环境模式，验收及对外发布时，要求必须使用生产环境模�?
	public static boolean debugMode = false;

	private String _cgDelegateGO = "Engine";
	private String _cgDelegateFun = "OnMessage";
	private static final String TAG = "kingjoy";
	private static final String PRFIX = "kingjoy---";

	private String sid;
	private Context context = null;

	private String account;
	private String userName;
	private String uid;
	private String token;

	// 默认SDK设置，运行过程中会被覆盖
	KingjoySDK.Settings settings = new KingjoySDK.Settings("1000", // appid
			"VC2amTo1GQiyGL9g", // appkey
			"1001"); // 渠道�?

	public String cgGetUserInfo() {
		Log.e(TAG, PRFIX + "cgGetUserInfo " + token);
		if (token != null) {
			return "type=" + "" + "&" + "id=" + uid + "&" + "userName="
					+ userName + "&" + "token=" + token + "&" + "channelId="
					+ "";
		}
		return null;
	}

	public void cgInitDelegate(String go, String fun) {
		_cgDelegateGO = go;
		_cgDelegateFun = fun;
	}

	/**
	 * 充�?功能          javaObject.Call("doCharge", exinfo, productName, callbackUrl, price);
	 * 
	 * @param callBackInfo
	 * @throws JSONException
	 */
	public void doCharge(String callBackInfo, String productName,
			String callbackUrl, double price) throws JSONException {
		Log.e("SDK", "进入doCharge()" + callBackInfo);

		JSONObject json = new JSONObject(callBackInfo);

		// 支付参数
		final KingjoySDK.PaymentParams params = new KingjoySDK.PaymentParams(
				productName, // 名称
				productName, // 内容
				price, // 金额
				callbackUrl, // 回调地址
				callBackInfo);

		try {
			this.runOnUiThread(new Runnable() {
				public void run() {

					Log.i("KingjoySDK", "init sdk " + params.getDesc()+", "+ params.getMoney() +", " + params.getName());
					
					KingjoySDK.pay(params, new KingjoySDK.PaymentCallback() {

						@Override
						public void onCancel() {
							Toast.makeText(context, "支付失败", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onComplete() {
							Toast.makeText(context, "支付成功", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onError(String err) {
							Toast.makeText(context, err, Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			});
		} catch (Exception e) {
			Log.e("doLogin", "进入doLogin方法发生异常情况", e);
		}
	}

	/**
	 * 退出游戏
	 */
	public void doExit() {
		Log.d(TAG, PRFIX + "doExit");
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * SDK登录功能<br>
	 */
	public void doLogin(final String appid, final String appkey,
			final String channelID) {

		Log.e("UCSDKLOGIN", "进入login方法");
		try {
			this.runOnUiThread(new Runnable() {
				public void run() {

					KingjoySDK.Settings settings = new KingjoySDK.Settings(
							appid, // appid
							appkey, // appkey
							channelID);

					Log.i("KingjoySDK", "init sdk " + appid + " " + channelID);

					KingjoySDK.init(context, settings);

					KingjoySDK.login(new KingjoySDK.LoginCallback() {
						@Override
						public void onCancel() {
							Toast.makeText(context, "取消登录", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onComplete(AccountInfo info) {

							// Toast.makeText(context, "登录成功:" +
							// info.toString(), Toast.LENGTH_LONG).show();
							token = info.getCode();
							uid = info.getAccountID();

							Log.i("SDK", "token=" + token);
							Log.i("SDK", "userId=" + uid);

							Log.d(TAG, PRFIX + "login SUCCESS" + info);
							UnityPlayer.UnitySendMessage(_cgDelegateGO, _cgDelegateFun,
									MSG_LOGIN_SUCCESS);
						}

						@Override
						public void onError(String err) {
							Toast.makeText(context, err, Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			});
		} catch (Exception e) {
			Log.e("doLogin", "进入doLogin方法发生异常情况", e);
		}
	}

	/**
	 * 初始化appid和渠�?
	 */
	public void initSDKSetting(String appid, String appkey, String channelID) {
		KingjoySDK.Settings settings = new KingjoySDK.Settings(appid, // appid
				appkey, // appkey
				channelID);

		Log.i("KingjoySDK", "init sdk " + appid + " " + channelID);
		KingjoySDK.init(context, settings);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 发�?ActivityResult
		KingjoySDK.sendActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		KingjoySDK.init(context, settings);
/*
		doLogin("1000", // appid
				"VC2amTo1GQiyGL9g", "1001");*/
	}

	
	// 获取Android设备信息
	public String getDevicesInfo() {
		return AndroidDevicesInfo.instance().getDevicesInfo();
	}
}
