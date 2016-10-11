package com.kingjoy.sdk.service;


import com.kingjoy.sdk.KingjoySDK;
import com.kingjoy.sdk.KingjoySDK.AccountInfo;
import com.kingjoy.sdk.R;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FloatWindowBigView extends LinearLayout {

	/**
	 * 记录大悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录大悬浮窗的高度
	 */
	public static int viewHeight;

	public FloatWindowBigView(final Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
		View view = findViewById(R.id.big_window_layout);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		ImageButton close = (ImageButton) findViewById(R.id.close);
		ImageButton back = (ImageButton) findViewById(R.id.back);
		ImageButton detail = (ImageButton) findViewById(R.id.detail);
		ImageButton password = (ImageButton) findViewById(R.id.password);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.removeSmallWindow(context);
				Intent intent = new Intent(getContext(), FloatWindowService.class);
				context.stopService(intent);
				android.os.Process.killProcess(android.os.Process.myPid()); 
			}
		});
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.createSmallWindow(context);
			}
		});
		detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.createSmallWindow(context);
				Log.e("SDK", "进入detail");
				KingjoySDK.detail(new KingjoySDK.DetailCallback() {
					@Override
					public void onComplete() {
						Log.e("SDK", "设置detail成功");
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
		password.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.createSmallWindow(context);
				Log.e("SDK", "进入password");
				KingjoySDK.password(new KingjoySDK.PasswordCallback() {
					@Override
					public void onComplete() {
						Log.e("SDK", "设置密码成功");
					}

					@Override
					public void onError(String err) {
						Toast.makeText(context, err, Toast.LENGTH_LONG)
								.show();
					}

					@Override
					public void onCancel() {
						Toast.makeText(context, "取消", Toast.LENGTH_LONG)
						.show();
						
					}
				});
			}
		});
		
	}
}
