package com.android.tecla;

import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.support.v4.content.LocalBroadcastManager;

public class TeclaIMEService extends InputMethodService {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "TeclaIMEService";

	private LocalBroadcastManager mLocalBroadcastManager;
	private Intent mIMEHiddingIntent = new Intent();
	
	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onCreate()
	 */
	@Override
	public void onCreate() {
		init();
		super.onCreate();
	}
	
	private void init() {
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mLocalBroadcastManager.registerReceiver(mReceiver, TeclaMessaging.mKeyboardDrawnFilter);
		registerReceiver(mReceiver, TeclaMessaging.mKeySelectedFilter);
		mIMEHiddingIntent.setAction(TeclaMessaging.EVENT_IME_HIDING);
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
			if (intent.hasExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST)) {
				TeclaDebug.logD(CLASS_TAG, "Forwarding keyboard drawn event");
				sendBroadcast(intent);
			}
			if (intent.hasExtra(TeclaMessaging.EXTRA_KEY_BOUNDS)) {
				TeclaDebug.logD(CLASS_TAG, "Forwarding key selected event");
				mLocalBroadcastManager.sendBroadcast(intent);
			}
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
		if (TeclaMessaging.isAccessibilityEnabled(getApplicationContext())) {
			sendBroadcast(mIMEHiddingIntent);
		}
		super.onWindowHidden();
	}
	
}
