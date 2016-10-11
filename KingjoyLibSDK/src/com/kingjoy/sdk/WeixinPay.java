package com.kingjoy.sdk;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sourceforge.simcpux.Constants;
import net.sourceforge.simcpux.MD5;
import net.sourceforge.simcpux.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

class WeixinPay {

	private static PayReq req;
	private static IWXAPI msgApi;
	private static Context context;
	private static Map<String, String> resultunifiedorder;

	// private static StringBuffer sb;

	public static void pay(final Context context,
			final KingjoySDK.PaymentParams params,
			final KingjoySDK.PaymentCallback callback) {

		KingjoySDK.showWait();

		WeixinPay.context = context;
		req = new PayReq();
		msgApi = WXAPIFactory.createWXAPI(context, null);
		msgApi.registerApp(Constants.APP_ID);

		// 1.调用平台创建订单
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String msg = null;
				AndroidHttpClient httpClient = AndroidHttpClient
						.newInstance(null);
				HttpPost req = new HttpPost(
				// "http://192.168.1.137:8080/platform-web/pay/createAlipayOrder?_c=A&_s=0&_f=&user=xx123456&u=&_g=1&_z=1&_pname=11&p=33&player=V5Inter&_n=A20150421153324Z00002&amount=10");
				// "http://192.168.1.137:8080/platform-web/pay/generateOrderNumber?channel=A");
						Utils.platformip() + "/pay/createOrder");

				Utils.info("pay request " + req.getURI());
				try {
					req.setEntity(new UrlEncodedFormEntity(params
							.toHttpParams(3), "UTF-8"));

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
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					httpClient.close();
					return;
				} finally {
					httpClient.close();
				}
				httpClient.close();
				try {
					Utils.info("pay callback " + msg);
					JSONObject objRs = new JSONObject(msg);
					if (!"0000".equalsIgnoreCase(objRs.getString("code"))) {
						Utils.error(objRs.getString("message"));
						callback.onError(objRs.getString("msg"));
					} else {
						JSONObject obj = (JSONObject) objRs.get("returnObj");
						/* 2.获得服务端的参数*/
						WeixinPay.req.appId = Constants.APP_ID;
						WeixinPay.req.partnerId = Constants.MCH_ID;
						WeixinPay.req.prepayId = obj.getString("prepay_id");
						WeixinPay.req.packageValue = "Sign=WXPay";
						WeixinPay.req.nonceStr = obj.getString("nonce_str");
						WeixinPay.req.timeStamp = obj.getString("timeStamp");				
						WeixinPay.req.sign = obj.getString("sign");
						
						WeixinPay.msgApi.registerApp(Constants.APP_ID);
						WeixinPay.msgApi.sendReq(WeixinPay.req);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

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

	/**
	 * 生成签名
	 */

	private static String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", packageSign);
		return packageSign;
	}

	private static String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		// WeixinPay.sb.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", appSign);
		return appSign;
	}

	private static String toXml(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<" + params.get(i).getName() + ">");

			sb.append(params.get(i).getValue());
			sb.append("</" + params.get(i).getName() + ">");
		}
		sb.append("</xml>");

		Log.e("orion", sb.toString());
		return sb.toString();
	}

	public static Map<String, String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName = parser.getName();
				switch (event) {
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:

					if ("xml".equals(nodeName) == false) {
						// 实例化student对象
						xml.put(nodeName, parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion", e.toString());
		}
		return null;

	}

	private static String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private static long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	private static String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	//
	private static String genProductArgs() {
		String nonceStr = genNonceStr();

		List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
		packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
		packageParams.add(new BasicNameValuePair("body", "weixin"));
		packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
		packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
		packageParams.add(new BasicNameValuePair("notify_url",
				"http://localhost"));
		packageParams
				.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
		packageParams.add(new BasicNameValuePair("spbill_create_ip",
				"127.0.0.1"));
		packageParams.add(new BasicNameValuePair("total_fee", "1"));
		packageParams.add(new BasicNameValuePair("trade_type", "APP"));

		String sign = genPackageSign(packageParams);
		packageParams.add(new BasicNameValuePair("sign", sign));

		String xmlstring = toXml(packageParams);

		return xmlstring;
	}

}
