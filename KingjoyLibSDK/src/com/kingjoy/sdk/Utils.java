package com.kingjoy.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public final class Utils {

	private static XMLConfiguration config;

	public static final String LOGTAG = "kingjoy.sdk";

	public static void info(String msg) {
		Log.i("kingjoy.sdk", msg);
	}

	public static void debug(String msg) {
		Log.d("kingjoy.sdk", msg);
	}

	public static void error(String msg) {
		Log.e("kingjoy.sdk", msg);
	}

	public static void warn(String msg) {
		Log.w("kingjoy.sdk", msg);
	}

	public static void toast(Context context, String msg) {
		Toast.makeText(context, msg, 1).show();
	}

	public static String localURL(String name) {
		return "file:///android_asset/kingjoy/sdk/" + name;
	}

	public static File getLocalDataFile(Context context) {
		String path = context.getFilesDir().getPath();
		path = path + "/kingjoy.json";
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static String readFileContent(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}

		try {
			InputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			return sb.substring(0, sb.length() - 1).toString();
		} catch (Exception e) {
			warn("读取文件失败: " + file.getPath());
		}

		return null;
	}

	public static String getLocalData(Context context) {
		File file = getLocalDataFile(context);
		String content = readFileContent(file.getPath());
		if ((content == null) || (content.length() == 0)) {
			content = "{'accounts':[]}";
		}
		return content;
	}

	public static JSONArray getAccountList(Context context) {
		String data = getLocalData(context);
		try {
			JSONObject obj = new JSONObject(data);
			return obj.getJSONArray("accounts");
		} catch (JSONException e) {
			warn("JSON数据转换失败: " + data);
		}

		return null;
	}

	public static int getAccountCount(Context context) {
		String data = getLocalData(context);
		try {
			JSONObject obj = new JSONObject(data);
			JSONArray arr = obj.getJSONArray("accounts");
			return arr.length();
		} catch (JSONException e) {
			warn("JSON数据转换失败: " + data);
		}

		return 0;
	}

	public static void saveAccoutList(Context context, JSONArray list) {
		String data = getLocalData(context);
		try {
			JSONObject root = new JSONObject(data);
			root.put("accounts", list);

			File file = getLocalDataFile(context);
			file.deleteOnExit();

			FileOutputStream output = new FileOutputStream(file);
			output.write(root.toString().getBytes("UTF-8"));
			output.flush();
			output.close();
		} catch (Exception e) {
			warn("JSON数据转换失败: " + data);
		}
	}

	public static void sendMessage(Handler handler, int type, Object obj) {
		Message msg = new Message();
		msg.arg1 = type;
		msg.obj = obj;
		handler.sendMessage(msg);
	}

	public static String platformip() {
		//return "http://113.107.150.196:6010/platform-web";
		return "http://192.168.1.34:8080/platform-web";
		//initConfiguration();
		//return config.getString("platform.ip");
	}
	
	public static String payip() {
		return "http://192.168.1.137:8080/ps";
		//initConfiguration();
		//return config.getString("pay.ip");
	}

	private static void initConfiguration() {
		if (config == null) {
			try {
				config = new XMLConfiguration("server.xml");
			} catch (ConfigurationException e) {
				warn("找不到配置文件: " + "server.xml");
			}

		}
	}
}
