package com.kingjoy.sdk;

import android.content.Context;

final class WaitDialog extends WebDialog {
	public WaitDialog(Context context) {
		super(context, "wait.html", null);
	}

	public void show() {
		call("startAction");
		super.show();
	}

	public void hide() {
		call("stopAction");
		super.hide();
	}
}
