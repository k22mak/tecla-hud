package ca.idrc.tecla.lib;

import android.content.IntentFilter;

public class TeclaMessaging {

	public static final String EVENT_KEYBOARD_DRAWN = "ca.idrc.tecla.lib.EVENT_KEYBOARD_DRAWN";
	public static final String EVENT_IME_SHOWN = "ca.idrc.tecla.lib.EVENT_IME_SHOWN";
	public static final String EVENT_IME_HIDING = "ca.idrc.tecla.lib.EVENT_IME_HIDDEN";
	public static final String EXTRA_KEY_BOUNDS_LIST = "ca.idrc.tecla.lib.EXTRA_KEY_BOUNDS_LIST";

	public static final IntentFilter mKeyboardDrawnFilter = new IntentFilter(EVENT_KEYBOARD_DRAWN);
	public static final IntentFilter mIMEShownFilter = new IntentFilter(EVENT_IME_SHOWN);
	public static final IntentFilter mIMEHiddenFilter = new IntentFilter(EVENT_IME_HIDING);
	
}
