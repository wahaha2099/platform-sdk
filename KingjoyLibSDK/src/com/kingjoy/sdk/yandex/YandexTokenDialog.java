package com.kingjoy.sdk.yandex;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.kingjoy.sdk.KingjoySDK;
import com.kingjoy.sdk.Utils;
import com.kingjoy.sdk.WebDialog;

/**
 * 获取yandex token
 * @author martin
 */
public class YandexTokenDialog  extends WebDialog {
	private KingjoySDK.Settings params;

	public static YandexTokenDialog dialog;
	
	public YandexTokenDialog(Context context, KingjoySDK.Settings params) {
/*
 * https://oauth.yandex.com/authorize?response_type=code & client_id=<app ID> [& state=<any string>] [& device_id=<device ID>] [& device_name=<device name>]
 */
		super(context, "https://oauth.yandex.com/authorize?response_type=code&client_id=3da8a0750ec44d5f8d44440b0fe17ba2", new LoginJSObject(context));
		//super(context, "http://113.107.150.196:6005/ps/yandex/token.do?code=3388872&state=123", new LoginJSObject(context));
		//http://113.107.150.196:6005/ps/yandex/token.do?code=3388872
		this.params = params;
	}

	public void setCallback(KingjoySDK.LoginCallback callback) {
		((LoginJSObject) this.jsobject).setCallback(callback);
	}

	public void show() {
		YandexTokenDialog.dialog = this;
		
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("appid", this.params.getAppID());
		hash.put("appkey", this.params.getAppkey());
		hash.put("channel", this.params.getChannel());
		hash.put("serAddress", Utils.platformip());
		JSONObject obj = new JSONObject(hash);
		call("setContent", obj);
		super.show();
	}

	public void hide() {
		//call("resetControl");
		super.hide();
	}

	private static class LoginJSObject extends WebDialog.JSObject {
		private KingjoySDK.LoginCallback callback;

		public LoginJSObject(Context context) {
			super(context);
		}

		protected void onMessage(int method, String str) {
			super.onMessage(method, str);

			Log.i("onMessage method", method + "");
			if (method == 4) {
				try {
					Log.i("onMessage method", method + ":" + str + ":" + new JSONObject(str));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			switch (method) {
			case 4:
				try {
					JSONObject objRs = new JSONObject(str);
					String code = objRs.getString("code");
					String state = objRs.getString("state");
					Utils.info("code is " + code);
					Utils.info("state is " + state);
					
					/*
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						String err = objRs.getString("message");
						Utils.error(err);
						Toast.makeText(this.context, "登录失败,用户名或者密码错误!", Toast.LENGTH_SHORT).show();
						
						this.callback.onError(err);
						break;
					}
					JSONObject obj = (JSONObject) objRs.get("returnObj");// "returnObj":{"userId":13545034,"userName":"xx123456","token":"fe16107d0d157c63b03c3dc1954f7575"}
					JSONArray list = Utils.getAccountList(this.context);
					JSONArray clone = new JSONArray();
					clone.put(obj);
					for (int i = 0; i < list.length(); i++) {
						if (i >= 2)
							break;
						JSONObject child = list.getJSONObject(i);
						if (!child.get("userId").equals(obj.get("userId"))) {
							clone.put(child);
						}
					}
					Toast.makeText(this.context, "登录成功", Toast.LENGTH_SHORT).show();
					KingjoySDK.AccountInfo args = new KingjoySDK.AccountInfo(obj);
					Utils.saveAccoutList(this.context, clone);
					//KingjoySDK.hideLogin();
					//KingjoySDK.setLoginInfo(args);
					*/
					
					YandexTokenDialog.dialog.hide();
					
					//this.callback.onComplete(args);
				} catch (JSONException e) {
					Utils.warn("JSON转换失败: " + str);
				}

				Utils.info("登录完毕");
			}
		}

		public void setCallback(KingjoySDK.LoginCallback callback) {
			this.callback = callback;
		}
	}

	public static void main(String[] args) throws JSONException {

	}
}
