package com.kingjoy.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.alipay.android.app.sdk.AliPay;
import com.kingjoy.sdk.alipay.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

class Alipay {
	private static final TaskHandler taskHandler = new TaskHandler();

	// 创建支付宝需要的订单信息
//	private static String getOrderInfo(String orderId,
//			KingjoySDK.PaymentParams params) {
//		StringBuilder sb = new StringBuilder();
//		try {
//			sb.append("partner=\"");
//			sb.append("2088911385045325");// 合作身份者id
//			sb.append("\"&out_trade_no=\"");
//			sb.append(orderId);// 这个是订单编号
//			sb.append("\"&subject=\"");
//			sb.append(params.getName());// 这个应该是商品名称
//			sb.append("\"&body=\"");
//			sb.append(params.getDesc());// 这个应该是商品的描述，具体你可以参考demo
//			sb.append("\"&total_fee=\"");
//			sb.append(String.valueOf(params.getMoney()).replace("一口价:", ""));// 这个是要付款的金额
//			sb.append("\"&notify_url=\"");
//
//			sb.append(URLEncoder.encode(Utils.platformip()
//					+ "/pay/payCallBack/AssertNotify/A0", "utf-8"));// 服务器异步通知页面,完成交易后通知商家服务器的页面，以post的形式将商品订单信息发送到指定页面，手机客户端不需要可以先放在这不管
//			sb.append("\"&service=\"mobile.securitypay.pay");
//			sb.append("\"&_input_charset=\"UTF-8");
//			sb.append("\"&return_url=\"");
//			sb.append(URLEncoder.encode(Utils.payip() + "/uccallback/550673",
//					"utf-8"));
//			sb.append("\"&payment_type=\"1");
//			sb.append("\"&seller_id=\"");
//			sb.append("2013kingjoy@gmail.com");// 卖家id
//			sb.append("\"&it_b_pay=\"1m");
//			sb.append("\"");
//		} catch (Exception e) {
//			e.printStackTrace();//
//		}
//		return new String(sb);
//	}

	// 创建支付宝需要的订单信息
	private static String getOrderInfo(JSONObject obj,
			KingjoySDK.PaymentParams params) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("partner=\"");
			sb.append(obj.getString("partner"));// 合作身份者id
			sb.append("\"&out_trade_no=\"");
			sb.append(obj.getString("orderId"));// 这个是订单编号
			sb.append("\"&subject=\"");
			sb.append(params.getName());// 这个应该是商品名称
			sb.append("\"&body=\"");
			sb.append(params.getDesc());// 这个应该是商品的描述，具体你可以参考demo
			sb.append("\"&total_fee=\"");
			sb.append(String.valueOf(params.getMoney()).replace("一口价:", ""));// 这个是要付款的金额
			sb.append("\"&notify_url=\"");

			sb.append(URLEncoder.encode(obj.getString("returnUrl"), "utf-8"));// 服务器异步通知页面,完成交易后通知商家服务器的页面，以post的形式将商品订单信息发送到指定页面，手机客户端不需要可以先放在这不管
			sb.append("\"&service=\"mobile.securitypay.pay");
			sb.append("\"&_input_charset=\"UTF-8");
			sb.append("\"&return_url=\"");
			sb.append(URLEncoder.encode(obj.getString("returnUrl"), "utf-8"));
			sb.append("\"&payment_type=\"1");
			sb.append("\"&seller_id=\"");
			sb.append(obj.getString("seller"));// 卖家id
			sb.append("\"&it_b_pay=\"1m");
			sb.append("\"");
		} catch (Exception e) {
			e.printStackTrace();//
		}
		return new String(sb);
	}

	private static String getSignType() {
		return "sign_type=\"RSA\"";
	}

	public static void pay(final Context context,
			final KingjoySDK.PaymentParams params,
			final KingjoySDK.PaymentCallback callback) {

		KingjoySDK.showWait();
		taskHandler.enable(context, params, callback);

		// 1.调用平台创建订单
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String msg = null;
				AndroidHttpClient httpClient = AndroidHttpClient.newInstance(null);
				HttpPost req = new HttpPost(
				// "http://192.168.1.137:8080/platform-web/pay/createAlipayOrder?_c=A&_s=0&_f=&user=xx123456&u=&_g=1&_z=1&_pname=11&p=33&player=V5Inter&_n=A20150421153324Z00002&amount=10");
				// "http://192.168.1.137:8080/platform-web/pay/generateOrderNumber?channel=A");
						Utils.platformip() + "/pay/createOrder");

				Utils.info("pay request " + req.getURI());
				try {
					req.setEntity(new UrlEncodedFormEntity(params
							.toHttpParams(1), "UTF-8"));

					String reqStr = EntityUtils.toString(req.getEntity());
					Utils.info("pay request " + reqStr + " " + req.getURI());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					HttpResponse resp = httpClient.execute(req);
					int statusCode = resp.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						msg = EntityUtils.toString(resp.getEntity());
					} else {
						httpClient.close();
						Utils.sendMessage(Alipay.taskHandler, 3, null);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					httpClient.close();
					Utils.sendMessage(Alipay.taskHandler, 3, null);
					return;
				} finally {
					httpClient.close();
				}
				httpClient.close();
				try {
					Utils.info("pay callback " + msg);
					JSONObject objRs = new JSONObject(msg);
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						Alipay.taskHandler.disable();
						Utils.error(objRs.getString("message"));
						callback.onError(objRs.getString("msg"));
					} else {
						Utils.sendMessage(Alipay.taskHandler, 4, null);
						JSONObject obj = (JSONObject) objRs.get("returnObj");

						// 2.调用支付宝进行支付请求
						String info = Alipay.getOrderInfo(obj, params);
						String sign = Alipay.Rsa.sign(info,
								obj.getString("privateKey"));

						sign = URLEncoder.encode(sign, "UTF-8");
						info = info + "&sign=\"" + sign + "\"&" + getSignType();

						AliPay alipay = new AliPay((Activity) context,
								Alipay.taskHandler);
						String result = alipay.pay(info);

						Utils.sendMessage(Alipay.taskHandler, 1, result);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Utils.sendMessage(Alipay.taskHandler, 3, null);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	static final class Keys {
		// 合作身份者id，以2088开头的16位纯数字，这个你申请支付宝签约成功后就会看见
		public static final String DEFAULT_PARTNER = "2088611103111106";
		// 这里填写收款支付宝账号，即你付款后到账的支付宝账号
		public static final String DEFAULT_SELLER = "joy@kingjoy.com";
		// 商户私钥，自助生成，即rsa_private_key.pem中去掉首行，最后一行，空格和换行最后拼成一行的字符串，
		// rsa_private_key.pem这个文件等你申请支付宝签约成功后，按照文档说明你会生成的.........................
		// 如果android版本太高，这里要用PKCS8格式用户私钥，不然调用不会成功的，那个格式你到时候会生成的，表急。
		public static final String PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANB6U4AFrB/ov8Yk/Z/qUYJJtS6nEDCzp/QbXcbXega5ABYUVLkVM1lue40u/RuWjgt6cc9Yd1J81jObFvguV7Q3s/VVktZl/KZNeVdD1b/RjBVPN/6iMjySmuTKZx0zXFyt9m3tFHmqYs/sTMCxDjqxBfxVN2lwKhrIGImxcYebAgMBAAECgYEA0Anx/2QpsqH5lYpeSdFzSzewhQCTyHXPWWpht0ZZcBH3AxGAxj1gw5Lb2jSUwY4I621h2w6l6/vFaTQPxNPCEbbbDJ5B7befJExG4fhX4QfAUFIxG0/5q4Zyay2SWDEHic1hfmDPh5ZCQ3hrbK+xyeDOfglYDOxoqj4bSwJQugkCQQD/DHq01N6ix4pvqjdoipozeuJZsMHb8fGU6qHpLFU++nPfOsZUKDtMrNv/qdgZiGp/Vwe0QAmYHvnNuQJ0/D5/AkEA0UFhg/8JnDVhF2/Ill1pBIGtFGBRcTrPjoPSeRWGQNkZwwMWELx0ZNXqTM16RFzVsQe8S/vO8wr9QHuDGKJg5QJAFUiYUNDWx2S5c7oTp6CxX4xoqS1r8AvPqiquWkP91SSXWNCRzmIRGMPwE/S7eN3+bSP5G5ygcFZdaEKBJvwJxwJAIoz+EfJZXt59coY1g4ZW4bSbpGKm7+A7BxgC95UzPp/aHhXzXb4QJdogPqAq0QDXE8zoLU+dgwnMeQorwjSFdQJAIqzN/RJ8TVKixV2QLHk/xyBfXYQVlFDMilpXRTnKZzo6biGb6rXAdJd4T3wPH6jmQDKzXtr9LO/kBnNZjH8fZA==";
		// 支付宝（RSA）公钥 ,demo自带不用改，或者用签约支付宝账号登录ms.alipay.com后，在密钥管理页面获取；或者文档上也有。
		public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
		public static final String SERVER_URL = "http://api.kingjoy.com/mobile/payorder";
		public static final String CALLBACK_URL = "http://api.kingjoy.com/mobile/paynotify";
	}

	static class Result {
		private static final Map<String, String> sResultStatus = new HashMap();
		private String mResult;
		String resultStatus = null;
		String memo = null;
		String result = null;
		String statusCode = null;
		boolean isSignOk = false;

		static {
			sResultStatus.put("9000", "支付成功");
			sResultStatus.put("4000", "系统异常");
			sResultStatus.put("4001", "订单参数错误");
			sResultStatus.put("6001", "用户取消支付");
			sResultStatus.put("6002", "网络连接异常");
		}

		public Result(String result) {
			this.mResult = result;
		}

		public boolean getIsSignOk() {
			return this.isSignOk;
		}

		public String getStatusCode() {
			return this.statusCode;
		}

		public String getResult() {
			String src = this.mResult.replace("{", "");
			src = src.replace("}", "");
			return getContent(src, "memo=", ";result");
		}

		public void parseResult() {
			try {
				String src = this.mResult.replace("{", "");
				src = src.replace("}", "");
				String rs = getContent(src, "resultStatus=", ";memo");
				if (sResultStatus.containsKey(rs)) {
					this.resultStatus = ((String) sResultStatus.get(rs));
					this.statusCode = rs;
				} else {
					this.resultStatus = "其他错误";
				}
				this.resultStatus = (this.resultStatus + "(" + rs + ")");

				this.memo = getContent(src, "memo=", ";result");
				this.result = getContent(src, "result=", null);
				this.isSignOk = checkSign(this.result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private boolean checkSign(String result) {
			boolean retVal = false;
			try {
				JSONObject json = string2JSON(result, "&");

				int pos = result.indexOf("&sign_type=");
				String signContent = result.substring(0, pos);

				String signType = json.getString("sign_type");
				signType = signType.replace("\"", "");

				String sign = json.getString("sign");
				sign = sign.replace("\"", "");

				if (signType.equalsIgnoreCase("RSA"))
					retVal = Alipay.Rsa
							.doCheck(
									signContent,
									sign,
									"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB");
			} catch (Exception e) {
				e.printStackTrace();
				Log.i("Result", "Exception =" + e);
			}
			Log.i("Result", "checkSign =" + retVal);
			return retVal;
		}

		public JSONObject string2JSON(String src, String split) {
			JSONObject json = new JSONObject();
			try {
				String[] arr = src.split(split);
				for (int i = 0; i < arr.length; i++) {
					String[] arrKey = arr[i].split("=");
					json.put(arrKey[0],
							arr[i].substring(arrKey[0].length() + 1));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return json;
		}

		private String getContent(String src, String startTag, String endTag) {
			String content = src;
			int start = src.indexOf(startTag);
			start += startTag.length();
			try {
				if (endTag != null) {
					int end = src.indexOf(endTag);
					content = src.substring(start, end);
				} else {
					content = src.substring(start);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return content;
		}
	}

	static class Rsa {
		private static final String ALGORITHM = "RSA";
		public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

		private static PublicKey getPublicKeyFromX509(String algorithm,
				String bysKey) throws NoSuchAlgorithmException, Exception {
			byte[] decodedKey = Base64.decode(bysKey);
			X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);

			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			return keyFactory.generatePublic(x509);
		}

		@SuppressLint({ "TrulyRandom" })
		public static String encrypt(String content, String key) {
			try {
				PublicKey pubkey = getPublicKeyFromX509("RSA", key);

				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(1, pubkey);

				byte[] plaintext = content.getBytes("UTF-8");
				byte[] output = cipher.doFinal(plaintext);

				return new String(Base64.encode(output));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public static String sign(String content, String privateKey) {
			String charset = "UTF-8";
			try {
				PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
						Base64.decode(privateKey));
				KeyFactory keyf = KeyFactory.getInstance("RSA");
				PrivateKey priKey = keyf.generatePrivate(priPKCS8);

				Signature signature = Signature.getInstance("SHA1WithRSA");

				signature.initSign(priKey);
				signature.update(content.getBytes(charset));

				byte[] signed = signature.sign();

				return Base64.encode(signed);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static String getMD5(String content) {
			String s = null;
			char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
					'9', 'a', 'b', 'c', 'd', 'e', 'f' };
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(content.getBytes());
				byte[] tmp = md.digest();
				char[] str = new char[32];
				int k = 0;
				for (int i = 0; i < 16; i++) {
					byte byte0 = tmp[i];
					str[(k++)] = hexDigits[(byte0 >>> 4 & 0xF)];
					str[(k++)] = hexDigits[(byte0 & 0xF)];
				}
				s = new String(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return s;
		}

		public static boolean doCheck(String content, String sign,
				String publicKey) {
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				byte[] encodedKey = Base64.decode(publicKey);
				PublicKey pubKey = keyFactory
						.generatePublic(new X509EncodedKeySpec(encodedKey));

				Signature signature = Signature.getInstance("SHA1WithRSA");

				signature.initVerify(pubKey);
				signature.update(content.getBytes("utf-8"));
				Log.i("Result", "content :   " + content);
				Log.i("Result", "sign:   " + sign);
				boolean bverify = signature.verify(Base64.decode(sign));
				Log.i("Result", "bverify = " + bverify);
				return bverify;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return false;
		}
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
			if (!this.enabled) {
				return;
			}
			switch (msg.arg1) {
			case 1:
			case 2:
				disable();
				Alipay.Result result = new Alipay.Result((String) msg.obj);
				result.parseResult();
				if (!result.getIsSignOk()) {
					if (result.getStatusCode().equals("6001")) {
						Utils.info("取消操作");
						this.callback.onCancel();
					} else {
						Utils.sendMessage(this, 3, result.getResult());
					}
					return;
				}

				Utils.info("支付成功: 支付宝");
				this.callback.onComplete();
				break;
			case 3:
				disable();
				String err = "获取订单号失败!";
				Utils.error(err);
				this.callback.onError(err);
				break;
			case 4:
				disable();
				KingjoySDK.hidePayment();
			}

			super.handleMessage(msg);
		}
	}
}
