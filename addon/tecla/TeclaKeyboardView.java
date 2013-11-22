package com.android.tecla;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;
import ca.idrc.tecla.lib.TeclaUtils;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;

public class TeclaKeyboardView extends com.android.inputmethod.keyboard.KeyboardView {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "Tecla Keyboard View";
	
	public TeclaKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TeclaKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private ArrayList<Rect> mKeyBoundsList;
	private LocalBroadcastManager mLocalBroadcastManager;
	private Intent mKeyboardDrawnIntent = new Intent();

	private void init(Context context) {
		//Debug.waitForDebugger();
		mKeyBoundsList = new ArrayList<Rect>();
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
		mKeyboardDrawnIntent.setAction(TeclaMessaging.EVENT_KEYBOARD_DRAWN);
	}

	/* (non-Javadoc)
	 * @see com.android.inputmethod.keyboard.KeyboardView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (TeclaUtils.isAccessibilityEnabled(getContext())) {
			mHandler.post(mBroadcastKeyboardDrawnRunnable);
		}
	}

	private Handler mHandler = new Handler();
	private Runnable mBroadcastKeyboardDrawnRunnable = new Runnable() {

		@Override
		public void run() {
			TeclaDebug.logW(CLASS_TAG, "Keyboard drawn!");
			Keyboard keyboard = getKeyboard();
			Rect parent_bounds = new Rect();
			createAccessibilityNodeInfo().getBoundsInScreen(parent_bounds);
			int dx = parent_bounds.left;
			int dy = parent_bounds.top;
			mKeyBoundsList.clear();
			for (Key key : keyboard.mKeys) {
				Rect bounds = new Rect(
						key.mHitBox.left,
						key.mHitBox.top,
						key.mHitBox.right,
						key.mHitBox.bottom
						);
				bounds.offset(dx, dy);
				bounds.set(bounds.left,
						bounds.top,
						bounds.right,
						bounds.bottom - keyboard.mVerticalGap);
				mKeyBoundsList.add(bounds);
			}
			//Gson gson = new Gson();
			mKeyboardDrawnIntent.removeExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST);
			mKeyboardDrawnIntent.putExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST, mKeyBoundsList);
			mLocalBroadcastManager.sendBroadcast(mKeyboardDrawnIntent);
		}

	};
		
}
