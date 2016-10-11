package com.kingjoy.sdk;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import cn.paypalm.pppayment.PPInterface;
import cn.paypalm.pppayment.global.ResponseDataToMerchant;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class PPPay {
	static final String SERVER_URL = "http://api.kingjoy.com/mobile/payorder";
	static final String CALLBACK_URL = "http://api.kingjoy.com/mobile/pppaynotify";
	static final PPHandle handler = new PPHandle();
	private static final String MERCHANT_ID = "2015012215";
	public static ResponseDataToMerchant merchant = new ResponseDataToMerchant() {
		public void responseData(int arg0, String arg1) {
			switch (arg0) {
			case 1:
				String tn = "success";
				Utils.sendMessage(PPPay.handler, 3, tn);

				break;
			case -2:
				String tn1 = arg1;
				Utils.sendMessage(PPPay.handler, 3, tn1);

				break;
			case -1:
			case 0:
			}
		}
	};

	public static void pay(Context context,
			final KingjoySDK.PaymentParams params,
			KingjoySDK.PaymentCallback callback) {
		KingjoySDK.showWait();
		handler.enable(context, params, callback);

		PPInterface.startSafe(context, "2015012215");
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String tn = null;
				AndroidHttpClient httpClient = AndroidHttpClient
						.newInstance(null);
				HttpPost req = new HttpPost(
						"http://api.kingjoy.com/mobile/payorder");
				try {
					req.setEntity(new UrlEncodedFormEntity(params
							.toHttpParams(3), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try {
					HttpResponse resp = httpClient.execute(req);
					int statusCode = resp.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						tn = EntityUtils.toString(resp.getEntity());
					} else {
						httpClient.close();
						Utils.sendMessage(PPPay.handler, 1, null);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					httpClient.close();
					Utils.sendMessage(PPPay.handler, 1, null);
					return;
				} finally {
					httpClient.close();
				}
				httpClient.close();

				Utils.sendMessage(PPPay.handler, 2, tn);
			}
		});
		thread.start();
	}

	static void pay(Context context, String order, String userid,
			String amount, String productname) {
		String tel = "";
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService("phone");
			tel = tm.getLine1Number();
		} catch (Exception localException) {
		}
		PPInterface.startPPPayment(context, order, tel, "2015012215", userid,
				amount, "100001", null,
				"http://api.kingjoy.com/mobile/pppaynotify", productname, null,
				null, merchant);
	}

	static class PPHandle extends Handler {
		Context context;
		KingjoySDK.PaymentParams params;
		KingjoySDK.PaymentCallback callback;
		boolean enabled = false;

		public void enable(Context context, KingjoySDK.PaymentParams params,
				KingjoySDK.PaymentCallback callback) {
			this.context = context;
			this.params = params;
			this.callback = callback;
			this.enabled = true;
		}

		public void disable() {
			this.enabled = false;
		}

		public void handleMessage(Message msg) {
			KingjoySDK.hideWait();
			if (!this.enabled) {
				return;
			}
			switch (msg.arg1) {
			case 1:
				String err = "获取订单号失败!";
				Utils.error(err);
				disable();
				this.callback.onError(err);
				break;
			case 2:
				try {
					JSONObject obj = new JSONObject((String) msg.obj);
					if (!"0000".equalsIgnoreCase(obj.getString("code"))) {
						disable();
						Utils.error(obj.getString("message"));
						this.callback.onError(obj.getString("message"));
					} else {
						PPPay.pay(
								this.context,
								obj.getString("order"),
								KingjoySDK.getLoginInfo().getAccountID(),
								String.valueOf(Integer.parseInt(new DecimalFormat(
										"0").format(this.params.getMoney() * 100.0D))),
								this.params.getName());
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Utils.sendMessage(PPPay.handler, 1, null);
				}

			case 3:
				String str = (String) msg.obj;
				if (str.equalsIgnoreCase("success"))
					this.callback.onComplete();
				else if (str.equalsIgnoreCase("fail"))
					this.callback.onError("支付失败");
				else if (str.equalsIgnoreCase("cancel")) {
					this.callback.onCancel();
				}
				break;
			}

			super.handleMessage(msg);
		}
	}
}
