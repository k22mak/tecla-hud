package com.android.tecla;

import com.android.inputmethod.accessibility.AccessibleKeyboardViewProxy;
import com.android.tecla.utils.TeclaDebug;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class TeclaIMEService extends InputMethodService {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "Input Method Service";

	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onWindowShown()
	 */
	@Override
	public void onWindowShown() {
		TeclaDebug.logW(CLASS_TAG, "Window shown");
		AccessibleKeyboardViewProxy mAccessibleKeyboardViewProxy = AccessibleKeyboardViewProxy.getInstance();
		super.onWindowShown();
	}

	/* (non-Javadoc)
	 * @see android.inputmethodservice.InputMethodService#onWindowHidden()
	 */
	@Override
	public void onWindowHidden() {
		TeclaDebug.logW(CLASS_TAG, "Window hidden");
		super.onWindowHidden();
	}

}
