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

final class JoinDialog extends WebDialog {
	private KingjoySDK.Settings params;

	public JoinDialog(Context context, KingjoySDK.Settings params) {
		super(context, "join.html", new JoinJSObject(context));
		this.params = params;
		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 4) {
					JoinDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setCallback(KingjoySDK.LoginCallback callback) {
		((JoinJSObject) this.jsobject).setCallback(callback);
	}

	public void show() {
		super.show();

		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("appid", this.params.getAppID());
		hash.put("appkey", this.params.getAppkey());
		hash.put("channel", this.params.getChannel());
		hash.put("serAddress", Utils.platformip());
		JSONObject obj = new JSONObject(hash);
		call("setContent", obj);
	}

	public void hide() {
		call("resetControl");
		super.hide();
	}

	static class JoinJSObject extends WebDialog.JSObject {
		private KingjoySDK.LoginCallback callback;

		protected void onMessage(int method, String str) {
			super.onMessage(method, str);

			Utils.info("join callback " + str);

			switch (method) {
			case 1:
				KingjoySDK.hideJoin();
				if (Utils.getAccountCount(this.context) > 0) {
					KingjoySDK.showSelect(this.callback);
				} else
					KingjoySDK.showLogin(this.callback);
				break;
			case 2:
				try {
					// {"code":"0000","message":"berhasil","returnObj":{"bbsData":null,"newestServerNo":null,"userName":"xx12345g"}}

					JSONObject objRs = new JSONObject(str);
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						String err = objRs.getString("message");
						Utils.error(err);
						Toast.makeText(this.context, "注册失败!", Toast.LENGTH_LONG).show();
						
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

						Toast.makeText(this.context, "注册成功", Toast.LENGTH_LONG).show();
						Utils.saveAccoutList(this.context, clone);
						KingjoySDK.hideJoin();
						KingjoySDK.showSelect(this.callback);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		public JoinJSObject(Context context) {
			super(context);
		}

		public void setCallback(KingjoySDK.LoginCallback callback) {
			this.callback = callback;
		}
	}
}
