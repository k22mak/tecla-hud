package com.android.tecla;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;

import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.Key;
import com.android.tecla.utils.TeclaDebug;

public class TeclaKeyboardView extends KeyboardView {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "Keyboard View";

	public TeclaKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TeclaKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private ArrayList<Rect> key_bounds;

	private void init(Context context) {
		//Debug.waitForDebugger();
		key_bounds = new ArrayList<Rect>();
	}

	/* (non-Javadoc)
	 * @see com.android.inputmethod.keyboard.KeyboardView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mHandler.post(mLogBoundsRunnable);
	}

	private Handler mHandler = new Handler();
	private Runnable mLogBoundsRunnable = new Runnable() {

		@Override
		public void run() {
			TeclaDebug.logW(CLASS_TAG, "Keyboard drawn!");
			Keyboard thiskeyboard = getKeyboard();
			key_bounds.clear();
			for (Key key : thiskeyboard.mKeys) {
				TeclaDebug.logI(CLASS_TAG, key.toString() + " at " + key.mHitBox.toString());
				key_bounds.add(key.mHitBox);
			}
		}

	};

}
