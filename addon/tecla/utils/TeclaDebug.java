package com.android.tecla.utils;

//import android.app.ActivityManager;
//import android.app.ActivityManager.RunningServiceInfo;
//import android.content.Context;
//import android.provider.Settings;
import android.util.Log;

public class TeclaDebug {
	/**
	 * Main debug switch, turns on/off debugging for the whole framework
	 */
	public static final boolean DEBUG = true;
	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String TAG = "TeclaNextFramework";
	
/*	private static final String IME_ID = "com.android.inputmethod.latin/.LatinIME";
	private static final String IME_SERVICE = "ca.idrc.tecla.ime.TeclaIME";
	public static final String A11Y_SERVICE = "com.android.tecla.ServiceAccessibility";

	public static Boolean isDefaultIMESupported(Context context) {
		String ime_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		logD(CLASS_TAG, "IME ID: " + ime_id);
		return IME_ID.equals(ime_id);
	}
*/
	public static final void logV(String class_tag, String msg) {
		if (DEBUG) Log.v(TAG, class_tag + ": " + msg);
	}

	public static final void logI(String class_tag, String msg) {
		if (DEBUG) Log.i(TAG, class_tag + ": " + msg);
	}

	public static final void logD(String class_tag, String msg) {
		if (DEBUG) Log.d(TAG, class_tag + ": " + msg);
	}

	public static final void logW(String class_tag, String msg) {
		if (DEBUG) Log.w(TAG, class_tag + ": " + msg);
	}

	public static final void logE(String class_tag, String msg) {
		if (DEBUG) Log.e(TAG, class_tag + ": " + msg);
	}

}
