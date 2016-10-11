package com.kingjoy.sdk;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Message;

import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

class Uppay {
	static final String SERVER_URL = "http://api.kingjoy.com/mobile/payorder";
	static final String MODE = "01";//"00" - 启动银联正式环境 "01" - 连接银联测试环境
	static final TaskHandler handler = new TaskHandler();

	public static void pay(Context context,
			final KingjoySDK.PaymentParams params,
			KingjoySDK.PaymentCallback callback) {
		KingjoySDK.showWait();
		handler.enable(context, params, callback);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				String tn = null;
				AndroidHttpClient httpClient = AndroidHttpClient.newInstance(null);
				HttpPost req = new HttpPost(Utils.platformip() + "/pay/createOrder");
				try {
					req.setEntity(new UrlEncodedFormEntity(params.toHttpParams(2), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try {
					HttpResponse resp = httpClient.execute(req);
					int statusCode = resp.getStatusLine().getStatusCode();
					Utils.info("statusCode======================>:"+statusCode);
					if (statusCode == 200) {
					 String	msg = EntityUtils.toString(resp.getEntity());
					 Utils.info("pay callback " + msg);
						JSONObject objRs = new JSONObject(msg);
						if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
							Utils.error(objRs.getString("message"));
							Utils.sendMessage(Uppay.handler, 3, null);
						} else {
							JSONObject obj = (JSONObject) objRs.get("returnObj");
							tn = obj.getString("orderId");
							Utils.info("tn======================>"+tn);
						}
					} else {
						httpClient.close();
						Utils.sendMessage(Uppay.handler, 1, null);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					httpClient.close();
					Utils.sendMessage(Uppay.handler, 1, null);
					return;
				} finally {
					httpClient.close();
				}
				httpClient.close();

				Utils.sendMessage(Uppay.handler, 2, tn);
			}
		});
		thread.start();
	}

	static void sendMessage(Message msg) {
		handler.sendMessage(msg);
	}

	static void pay(Activity activity, String tn, String mode) {
		Utils.info("===============>>>>>> tn:"+tn+ "  mode:"+mode);
		UPPayAssistEx.startPayByJAR(activity, PayActivity.class, null, null,tn, mode);
	}

	static class TaskHandler extends Handler {
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
			Utils.info("handleMessage============>"+this.enabled + "   msg.arg1:"+msg.arg1 + " msg:"+msg.obj);
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
//				try {
//					JSONObject obj = new JSONObject((String) msg.obj);
//					if (!"0000".equalsIgnoreCase(obj.getString("code"))) {
//						disable();
//						Utils.error(obj.getString("message"));
//						this.callback.onError(obj.getString("message"));
//					} else {
//						
//						Uppay.pay((Activity) this.context, (String)msg.obj,MODE);
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//					Utils.sendMessage(Uppay.handler, 1, null);
//				}
				Utils.info("--------------------------->startPayByJAR tn:"+msg.obj);
				Uppay.pay((Activity) this.context, ""+msg.obj,MODE);
				break;

			case 3:
				String str = (String) msg.obj;
				if (str.equalsIgnoreCase("success")) {
					Utils.info("支付成功: 支付宝");
					this.callback.onComplete();
				} else if (str.equalsIgnoreCase("fail")) {
					Utils.error("支付失败");
					this.callback.onError("支付失败");
				} else if (str.equalsIgnoreCase("cancel")) {
					Utils.info("取消操作");
					this.callback.onCancel();
				}
				break;
			}

			super.handleMessage(msg);
		}
	}
}
