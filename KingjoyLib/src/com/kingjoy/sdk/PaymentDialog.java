package com.kingjoy.sdk;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

class PaymentDialog extends WebDialog {
	KingjoySDK.Settings settings;
	KingjoySDK.PaymentParams params;

	public PaymentDialog(Context context, KingjoySDK.Settings settings) {
		super(context, "payment.html", new PaymentJSObject(context));
		this.settings = settings;

		setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == 4) {
					PaymentDialog.this.getJSObject().sendMessage(1);
					return true;
				}
				return false;
			}
		});
	}

	public void setParams(KingjoySDK.PaymentParams params,
			KingjoySDK.PaymentCallback callback) {
		PaymentJSObject obj = (PaymentJSObject) this.jsobject;
		obj.params = params;
		obj.callback = callback;
		obj.settings = this.settings;
		this.params= params;
	}

	public void show() {
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("money", params.getMoney());
		hash.put("name", params.getName());
		JSONObject obj = new JSONObject(hash);
		call("setContent", obj);
		super.show();
	}

	public void hide() {
		call("resetControl");
		super.hide();
	}

	static class PaymentJSObject extends WebDialog.JSObject {
		KingjoySDK.PaymentParams params;
		KingjoySDK.PaymentCallback callback;
		KingjoySDK.Settings settings;

		public PaymentJSObject(Context context) {
			super(context);
		}

		protected void onMessage(int method, String str) {
			switch (method) {
			case 1:
				KingjoySDK.hidePayment();
				this.callback.onCancel();
				break;
			case 2:
				KingjoySDK.hidePayment();
				int type = Integer.parseInt(str);
				switch (type) {
				case 1:
					Alipay.pay(this.context, this.params, this.callback);
					break;
				case 2:
					Uppay.pay(this.context, this.params, this.callback);
					break;
				case 3:
					WeixinPay.pay(this.context, this.params, this.callback);
					break;
				}

				break;
			}

			super.onMessage(method, str);
		}
	}
}
