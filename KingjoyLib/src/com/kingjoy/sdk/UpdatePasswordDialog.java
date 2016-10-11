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

final class UpdatePasswordDialog extends WebDialog {
	private KingjoySDK.Settings params;

	public UpdatePasswordDialog(Context context, KingjoySDK.Settings params) {
		super(context, "update_password.html", new PasswordJSObject(context));
		this.params = params;
		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 2) {
					UpdatePasswordDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setCallback(KingjoySDK.PasswordCallback callback) {
		((PasswordJSObject) this.jsobject).setCallback(callback);
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

	private static class PasswordJSObject extends WebDialog.JSObject {
		private KingjoySDK.PasswordCallback callback;

		public PasswordJSObject(Context context) {
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
				KingjoySDK.hidePassword();
				break;
			case 2:
				Utils.info("重新设置密码成功");
				try {
					Log.i("onMessage method", method + ":" + str);		
					JSONObject objRs = new JSONObject(str);
					if ("0000".equalsIgnoreCase(objRs.getString("code"))) {
						//处理修改密码成功后的逻辑
						String err = objRs.getString("message");
						Toast.makeText(this.context, err, Toast.LENGTH_SHORT).show();					
						this.callback.onError(err);
						KingjoySDK.hidePassword();
					} else {
						String err = objRs.getString("message");
						Utils.error(err);
						Toast.makeText(this.context, err, Toast.LENGTH_SHORT).show();					
						this.callback.onError(err);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;	
			}
		}

		public void setCallback(KingjoySDK.PasswordCallback callback) {
			this.callback = callback;
		}
	}

	public static void main(String[] args) throws JSONException {

	}
}
