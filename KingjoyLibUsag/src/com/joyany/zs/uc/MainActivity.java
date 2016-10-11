package com.joyany.zs.uc;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.kingjoy.sdk.KingjoySDK;
import com.kingjoy.sdk.KingjoySDK.AccountInfo;
import com.kingjoy.sdk.service.FloatWindowService;

public class MainActivity extends Activity {

	public static final String MSG_LOGIN_SUCCESS = "loginSuccess";

	public static final String MSG_LOGIN_FAIL = "loginFail";
	public static final String GAME_NAME = "众神风暴";
	// debugMode值为true时，为调试环境模式，当�?为false时，是生产环境模式，验收及对外发布时，要求必须使用生产环境模�?
	public static boolean debugMode = false;
	private String _cgDelegateGO = "Engine";
	private String _cgDelegateFun = "OnMessage";
	private String sid;
	private Context context = null;

	private String account;
	private String token;

	// 默认SDK设置，运行过程中会被覆盖
	KingjoySDK.Settings settings = new KingjoySDK.Settings("1000", // appid
			"VC2amTo1GQiyGL9g", // appkey
			"1001"); // 渠道

	/**
	 * 初始化appid和渠道
	 */
	public void initSDKSetting(String appid, String appkey, String channelID) {
		KingjoySDK.Settings settings = new KingjoySDK.Settings(appid, // appid
				appkey, // appkey
				channelID);
	
		Log.i("KingjoySDK", "init sdk " + appid + " " + channelID);
		KingjoySDK.init(context, settings);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = this;
		KingjoySDK.init(context, settings);
		
		doLogin("1000", // appid
				"VC2amTo1GQiyGL9g", "1001");
		//启动service
		Intent intent = new Intent(MainActivity.this, FloatWindowService.class);
		startService(intent);
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String callBackInfo = "{\"serverNo\":9,\"platForm\":\"all\",\"playerId\":5000059,\"productId\":100008,\"from_client\":1,\"gameOrderId\":5000090,\"price\":25}";
				String productName = "钻石";
				String callbackUrl = "http://113.107.150.196:6005/ps/uccallback/550673.do";
				double price = 0.02;
				try {
					doCharge(callBackInfo, productName, callbackUrl, price);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 发�?ActivityResult
		KingjoySDK.sendActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * SDK登录功能<br>
	 */
	public void doLogin(final String appid, final String appkey,
			final String channelID) {

		Log.e("UCSDKLOGIN", "进入login方法");
		try {
			this.runOnUiThread(new Runnable() {
				public void run() {

					KingjoySDK.Settings settings = new KingjoySDK.Settings(appid, // appid
							appkey, // appkey
							channelID);

					Log.i("KingjoySDK", "init sdk " + appid + " " + channelID);

					KingjoySDK.init(context, settings);

					KingjoySDK.login(new KingjoySDK.LoginCallback() {
						@Override
						public void onComplete(AccountInfo info) {

							// Toast.makeText(context, "登录成功:" +
							// info.toString(), Toast.LENGTH_LONG).show();
							token = info.getCode();
							account = info.getAccountID();
							Log.i("SDK", "token=" + token);
							Log.i("SDK", "account=" + account);
							
							// http://api.kingjoy.com/mobile/auth/
						}

						@Override
						public void onCancel() {
							Toast.makeText(context, "取消登录", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onError(String err) {
							Toast.makeText(context, err, Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			});
		} catch (Exception e) {
			Log.e("doLogin", "进入doLogin方法发生异常情况", e);
		}
	}

	/**
	 * 充�?功能
	 * 
	 * @param callBackInfo
	 * @throws JSONException
	 */
	public void doCharge(String callBackInfo, String productName,
			String callbackUrl, double price) throws JSONException {
		Log.e("SDK", "进入doCharge()" + callBackInfo);

		JSONObject json = new JSONObject(callBackInfo);

		// 支付参数
		final KingjoySDK.PaymentParams params = new KingjoySDK.PaymentParams(
				productName, // 名称
				productName, // 内容
				price, // 金额
				callbackUrl, // 回调地址
				callBackInfo);

		try {
			this.runOnUiThread(new Runnable() {
				public void run() {

					KingjoySDK.pay(params, new KingjoySDK.PaymentCallback() {

						@Override
						public void onError(String err) {
							Toast.makeText(context, err, Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onComplete() {
							Toast.makeText(context, "支付成功", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onCancel() {
							Toast.makeText(context, "支付失败", Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			});
		} catch (Exception e) {
			Log.e("doLogin", "进入doLogin方法发生异常情况", e);
		}
	}

	public void cgInitDelegate(String go, String fun) {
		_cgDelegateGO = go;
		_cgDelegateFun = fun;
	}

	public String cgGetUserInfo() {
		Log.e("cgGetUserInfo", "进入cgGetUserInfotoken");
		if (token != null) {
			return "type=" + "" + "&" + "id=" + account + "&" + "userName="
					+ "" + "&" + "token=" + token + "&" + "channelId=" + "";
		}
		return null;
	}

}
