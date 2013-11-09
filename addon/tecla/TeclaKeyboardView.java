package com.android.tecla;

import com.android.tecla.utils.TeclaDebug;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.Key;

public class TeclaKeyboardView extends KeyboardView {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "Keyboard View";

	public TeclaKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public TeclaKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private Handler mHandler = new Handler();
	private Runnable mLogBoundsRunnable = new Runnable() {

		@Override
		public void run() {
			TeclaDebug.logW(CLASS_TAG, "Keyboard drawn!");
			Keyboard thiskeyboard = getKeyboard();
			for (int i=0;i<thiskeyboard.mKeys.length;i++) {
				Key thiskey = thiskeyboard.mKeys[i];
				TeclaDebug.logI(CLASS_TAG, thiskey.toString() + " at " + thiskey.mHitBox.toString());
			}
		}
		
	};
	
	/* (non-Javadoc)
	 * @see com.android.inputmethod.keyboard.KeyboardView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mHandler.post(mLogBoundsRunnable);
	}

}
