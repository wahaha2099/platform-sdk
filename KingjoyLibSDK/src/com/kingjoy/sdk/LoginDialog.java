package com.kingjoy.sdk;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

final class LoginDialog extends WebDialog {
	private KingjoySDK.Settings params;

	public LoginDialog(Context context, KingjoySDK.Settings params) {
		super(context, "login.html", new LoginJSObject(context));
		this.params = params;
		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 4) {
					LoginDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setCallback(KingjoySDK.LoginCallback callback) {
		((LoginJSObject) this.jsobject).setCallback(callback);
	}

	public void show() {
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
		call("resetControl");
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
					Log.i("onMessage method", method + ":" + str + ":"
							+ new JSONObject(str));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			switch (method) {
			case 1:
				KingjoySDK.hideLogin();
				if (Utils.getAccountCount(this.context) <= 0)
					this.callback.onCancel();
				else {
					KingjoySDK.showSelect(this.callback);
				}
				break;
			case 2:
				KingjoySDK.hideLogin();
				KingjoySDK.showJoin(this.callback);
				break;
			case 3:
				Utils.info("一键注册");
				try {
					// {"code":"0000","message":"berhasil","returnObj":{"bbsData":null,"newestServerNo":null,"userName":"xx12345g"}}

					Log.i("onMessage method", method + ":" + str + ":"
							+ new JSONObject(str));
					
					JSONObject objRs = new JSONObject(str);
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						String err = objRs.getString("message");
						Utils.error(err);
						Toast.makeText(this.context, "一键注册失败!", Toast.LENGTH_LONG).show();
						
						this.callback.onError(err);
					} else {
						JSONObject obj = (JSONObject) objRs.get("returnObj");
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

						Toast.makeText(this.context, "一键注册成功", Toast.LENGTH_LONG).show();
						Utils.saveAccoutList(this.context, clone);
						KingjoySDK.hideLogin();
						KingjoySDK.showSelect(this.callback);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;
			case 4:
				try {
					JSONObject objRs = new JSONObject(str);
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
					KingjoySDK.hideLogin();
					KingjoySDK.setLoginInfo(args);
					this.callback.onComplete(args);
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
