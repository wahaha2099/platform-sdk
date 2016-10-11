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

final class UpdateDetailDialog extends WebDialog {
	private KingjoySDK.Settings params;
	private KingjoySDK.AccountInfo accountInfo;

	public UpdateDetailDialog(Context context, KingjoySDK.Settings params) {
		super(context, "update_detail.html", new DetailJSObject(context));
		this.params = params;
		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 2) {
					UpdateDetailDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setCallback(KingjoySDK.DetailCallback callback) {
		((DetailJSObject) this.jsobject).setCallback(callback);
	}
	public void setAccountInfo(KingjoySDK.AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}
	public void show() {
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("appid", this.params.getAppID());
		hash.put("appkey", this.params.getAppkey());
		hash.put("channel", this.params.getChannel());
		hash.put("accountID", accountInfo.getAccountID());
		hash.put("username", accountInfo.getUsername());
		hash.put("serAddress", Utils.platformip());
		JSONObject obj = new JSONObject(hash);
		call("setContent", obj);
		super.show();
	}

	public void hide() {
		call("resetControl");
		super.hide();
	}

	private static class DetailJSObject extends WebDialog.JSObject {
		private KingjoySDK.DetailCallback callback;

		public DetailJSObject(Context context) {
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
				KingjoySDK.hideDetail();
				break;
			case 2:
				Utils.info("设置详细信息");
				try {
					Log.i("onMessage method", method + ":" + str);
					JSONObject objRs = new JSONObject(str);
					if ("0000".equalsIgnoreCase(objRs.getString("code"))) {
						//设置详细信息成功后的逻辑
						String err = objRs.getString("message");
						Utils.error(err);
						Toast.makeText(this.context, err, Toast.LENGTH_SHORT).show();					
						this.callback.onError(err);
						KingjoySDK.hideDetail();
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

		public void setCallback(KingjoySDK.DetailCallback callback) {
			this.callback = callback;
		}
	}

	public static void main(String[] args) throws JSONException {

	}
}
