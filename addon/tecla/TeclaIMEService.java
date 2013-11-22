package com.android.tecla;

import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;
import ca.idrc.tecla.lib.TeclaUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.support.v4.content.LocalBroadcastManager;

public class TeclaIMEService extends InputMethodService {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "Input Method Service";

	private LocalBroadcastManager mLocalBroadcastManager;
	private Intent mIMEHiddingIntent = new Intent();
	
	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onCreate()
	 */
	@Override
	public void onCreate() {
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mLocalBroadcastManager.registerReceiver(mReceiver, TeclaMessaging.mKeyboardDrawnFilter);
		mIMEHiddingIntent.setAction(TeclaMessaging.EVENT_IME_HIDING);
		super.onCreate();
	}

	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		mLocalBroadcastManager.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TeclaDebug.logW(CLASS_TAG, "Forwarding keyboard drawn event");
			sendBroadcast(intent);
		}
		
	};
	
	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onWindowShown()
	 */
	@Override
	public void onWindowShown() {
		super.onWindowShown();
		TeclaDebug.logW(CLASS_TAG, "Window shown");
	}

	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onWindowHidden()
	 */
	@Override
	public void onWindowHidden() {
		TeclaDebug.logW(CLASS_TAG, "Window hiding");
		if (TeclaUtils.isAccessibilityEnabled(getApplicationContext())) {
			sendBroadcast(mIMEHiddingIntent);
		}
		super.onWindowHidden();
	}
	
}
