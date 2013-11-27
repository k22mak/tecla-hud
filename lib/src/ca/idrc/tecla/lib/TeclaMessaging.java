package ca.idrc.tecla.lib;

import android.content.Context;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityManager;

public class TeclaMessaging {

	public static final String EVENT_KEYBOARD_DRAWN = "ca.idrc.tecla.lib.EVENT_KEYBOARD_DRAWN";
	public static final String EVENT_IME_SHOWN = "ca.idrc.tecla.lib.EVENT_IME_SHOWN";
	public static final String EVENT_IME_HIDING = "ca.idrc.tecla.lib.EVENT_IME_HIDDEN";
	public static final String EXTRA_KEY_BOUNDS_LIST = "ca.idrc.tecla.lib.EXTRA_KEY_BOUNDS_LIST";
	public static final String EXTRA_KEY_BOUNDS = "ca.idrc.tecla.lib.EXTRA_KEY_BOUNDS";
	public static final String EVENT_KEY_SELECTED = "ca.idrc.tecla.lib.EVENT_KEY_SELECTED";

	public static final IntentFilter mKeyboardDrawnFilter = new IntentFilter(EVENT_KEYBOARD_DRAWN);
	public static final IntentFilter mIMEShownFilter = new IntentFilter(EVENT_IME_SHOWN);
	public static final IntentFilter mIMEHiddenFilter = new IntentFilter(EVENT_IME_HIDING);
	public static final IntentFilter mKeySelectedFilter = new IntentFilter(EVENT_KEY_SELECTED);
	
	public static boolean isAccessibilityEnabled(Context context) {
		AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
		return manager.isEnabled();
	}
	
}
