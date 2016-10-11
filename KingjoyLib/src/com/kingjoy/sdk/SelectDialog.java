package com.kingjoy.sdk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class SelectDialog extends WebDialog {
	private KingjoySDK.Settings params;
	private Context context;

	public SelectDialog(Context context, KingjoySDK.Settings params) {
		super(context, "select.html", new SelectJSObject(context));
		this.params = params;
		this.context = context;
		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 4) {
					SelectDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setCallback(KingjoySDK.LoginCallback callback) {
		((SelectJSObject) this.jsobject).setCallback(callback);
	}

	public void show() {
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("appid", this.params.getAppID());
		hash.put("appkey", this.params.getAppkey());
		hash.put("channel", this.params.getChannel());
		hash.put("serAddress", Utils.platformip());

		JSONObject obj = new JSONObject(hash);
		try {
			obj.put("accounts", Utils.getAccountList(this.context));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		call("setContent", obj);
		super.show();
	}

	public void hide() {
		call("resetControl");
		super.hide();
	}

	private static class SelectJSObject extends WebDialog.JSObject {
		private KingjoySDK.LoginCallback callback;

		public SelectJSObject(Context context) {
			super(context);
		}

		protected void onMessage(int method, String str) {
			super.onMessage(method, str);
			switch (method) {
			case 1:
				KingjoySDK.hideSelect();
				this.callback.onCancel();
				break;
			case 2:
				KingjoySDK.hideSelect();
				KingjoySDK.showJoin(this.callback);
				break;
			case 3:
				KingjoySDK.hideSelect();
				KingjoySDK.showLogin(this.callback);
				break;
			case 4:
				JSONArray list = Utils.getAccountList(this.context);
				JSONArray clone = new JSONArray();

				for (int i = 0; i < list.length(); i++) {
					if (i >= 2)
						break;
					try {
						JSONObject obj = list.getJSONObject(i);
						if (!obj.getString("account").equals(str))
							clone.put(obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				Utils.saveAccoutList(this.context, clone);

				if (clone.length() <= 0) {
					KingjoySDK.hideSelect();
					KingjoySDK.showLogin(this.callback);
				}

				break;
			case 5:
				try {
//					JSONObject obj = new JSONObject(str);
//					if (!"0000".equalsIgnoreCase(obj.getString("code"))) {
//						String err = obj.getString("message");
//						Utils.error(err);
//						this.callback.onError(err);
//						break;
//					}
					JSONObject objRs = new JSONObject(str);
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						Toast.makeText(this.context, "登录失败,用户名或者密码错误!", Toast.LENGTH_SHORT).show();
						
						String err = objRs.getString("message");
						Utils.error(err);
						this.callback.onError(err);
						break;
					}
					
					Toast.makeText(this.context, "登录成功", Toast.LENGTH_SHORT).show();
					JSONObject obj = (JSONObject) objRs.get("returnObj");
					KingjoySDK.hideSelect();
					KingjoySDK.AccountInfo args = new KingjoySDK.AccountInfo(obj);
					KingjoySDK.setLoginInfo(args);
					this.callback.onComplete(args);
					this.callback.onComplete(new KingjoySDK.AccountInfo(obj));
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
}
