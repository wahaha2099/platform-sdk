package com.kingjoy.sdk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({ "HandlerLeak" })
public class WebDialog extends Dialog {
	private Boolean loaded = Boolean.valueOf(false);
	private String callstr;
	protected WebView webview;
	protected JSObject jsobject;

	@SuppressLint({ "SetJavaScriptEnabled" })
	public WebDialog(Context context, String name, JSObject jsobject) {
		super(context);

		setCancelable(false);
		requestWindowFeature(1);

		Window window = getWindow();
		window.setBackgroundDrawable(new ColorDrawable(0));

		this.webview = new WebView(context);
		WebSettings settings = this.webview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("utf-8");
		settings.setAppCacheEnabled(false);
		settings.setAppCacheMaxSize(1L);
		settings.setCacheMode(2);
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

		this.webview.setBackgroundColor(0);
		this.webview.setVerticalScrollBarEnabled(false);
		this.webview.setHorizontalScrollBarEnabled(false);
		if (jsobject != null) {
			this.webview.addJavascriptInterface(jsobject, "android");
			this.jsobject = jsobject;
		}
		this.webview.setWebChromeClient(new WebChromeClient());
		this.webview.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				if (!WebDialog.this.loaded.booleanValue()) {
					WebDialog.this.loaded = Boolean.valueOf(true);
					if (WebDialog.this.callstr != null) {
						WebDialog.this.webview.loadUrl(WebDialog.this.callstr);
						WebDialog.this.callstr = null;
					}
				}
				super.onPageFinished(view, url);
			}
		});
		if(name.startsWith("http://") || name.startsWith("https://")){
			this.webview.loadUrl(name);
		}else{
			this.webview.loadUrl(Utils.localURL(name));
		}
		
		setContentView(this.webview);

		WindowManager.LayoutParams params = window.getAttributes();
		params.dimAmount = 0.2F;
		window.setAttributes(params);
	}

	public JSObject getJSObject() {
		return this.jsobject;
	}

	public void call(String method, JSONObject obj) {
		call(method, obj.toString());
	}

	public void call(String method, String arg) {
		if (!this.loaded.booleanValue()) {
			this.callstr = ("javascript:" + method + "('" + arg + "')");
		} else {
			this.webview.loadUrl("javascript:" + method + "('" + arg + "')");
		}
	}

	public void call(String method) {
		call(method, "");
	}

	public static class JSObject {
		public static final int METHOD_INFO = 1001;
		public static final int METHOD_DEBUG = 1002;
		public static final int METHOD_ERROR = 1003;
		public static final int METHOD_WARN = 1004;
		public static final int METHOD_TOAST = 1005;
		public static final int METHOD_SHOWWAIT = 1006;
		public static final int METHOD_HIDEWAIT = 1007;
		protected Context context;
		protected Handler handler;

		public JSObject(Context context) {
			this.context = context;
			this.handler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					WebDialog.JSObject.this.onMessage(msg.arg1,
							(String) msg.obj);
				}
			};
		}

		@SuppressLint({ "JavascriptInterface" })
		public void info(String msg) {
			sendMessage(1001, msg);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void debug(String msg) {
			sendMessage(1002, msg);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void error(String msg) {
			sendMessage(1003, msg);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void warn(String msg) {
			sendMessage(1003, msg);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void toast(String msg) {
			sendMessage(1005, msg);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void showWait() {
			sendMessage(1006);
		}

		@SuppressLint({ "JavascriptInterface" })
		public void hideWait() {
			sendMessage(1007);
		}

		public void sendMessage(int type, Object obj) {
			Message msg = new Message();
			msg.arg1 = type;
			msg.obj = obj;
			this.handler.sendMessage(msg);
		}

		public void sendMessage(int type) {
			sendMessage(type, null);
		}

		protected void onMessage(int method, String str) {
			switch (method) {
			case 1001:
				Utils.info(str);
				break;
			case 1002:
				Utils.debug(str);
				break;
			case 1003:
				Utils.error(str);
				break;
			case 1004:
				Utils.warn(str);
				break;
			case 1005:
				Utils.toast(this.context, str);
				break;
			case 1006:
				KingjoySDK.showWait();
				break;
			case 1007:
				KingjoySDK.hideWait();
			}
		}

		@SuppressLint({ "JavascriptInterface" })
		public void call(String str) {
			try {
				JSONObject obj = new JSONObject(str);
				String args = "";
				if (obj.has("args")) {
					args = obj.getString("args");
				}
				sendMessage(obj.getInt("method"), args);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
