package com.android.tecla;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;

public class TeclaKeyboardView extends com.android.inputmethod.keyboard.KeyboardView {

	/**
	 * Tag used for logging
	 */
	public static final String CLASS_TAG = "TeclaKeyboardView";
	
	private TeclaKeyboardView sInstance = this;
	
	private Rect mParentBounds;
	
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
		mLocalBroadcastManager.registerReceiver(mReceiver, TeclaMessaging.mKeySelectedFilter);
		mKeyboardDrawnIntent.setAction(TeclaMessaging.EVENT_KEYBOARD_DRAWN);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(TeclaMessaging.EXTRA_KEY_BOUNDS)) {
				TeclaDebug.logD(CLASS_TAG, "Key selected event received");
				Rect bounds = (Rect) intent.getExtras().get(TeclaMessaging.EXTRA_KEY_BOUNDS);
				bounds.offset(-mParentBounds.left, -mParentBounds.top);
				MotionEvent down = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN,
						bounds.centerX(), bounds.centerY(),
						1, 1, 0, 1, 1, 0, 0);
				sInstance.dispatchTouchEvent(down);
				down.recycle();
				MotionEvent up = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP,
						bounds.centerX(), bounds.centerY(),
						1, 1, 0, 1, 1, 0, 0);
				sInstance.dispatchTouchEvent(up);
				up.recycle();
			}
		}
		
	};
	
	/* (non-Javadoc)
	 * @see com.android.inputmethod.keyboard.KeyboardView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (TeclaMessaging.isAccessibilityEnabled(getContext())) {
			mHandler.post(mBroadcastKeyboardDrawnRunnable);
		}
	}

	private Handler mHandler = new Handler();
	private Runnable mBroadcastKeyboardDrawnRunnable = new Runnable() {

		@Override
		public void run() {
			TeclaDebug.logD(CLASS_TAG, "Keyboard drawn!");
			Keyboard keyboard = getKeyboard();
			mParentBounds = new Rect();
			createAccessibilityNodeInfo().getBoundsInScreen(mParentBounds);
			mKeyBoundsList.clear();
			for (Key key : keyboard.mKeys) {
				Rect bounds = new Rect(
						key.mHitBox.left,
						key.mHitBox.top,
						key.mHitBox.right,
						key.mHitBox.bottom
						);
				bounds.offset(mParentBounds.left, mParentBounds.top);
				bounds.set(bounds.left,
						bounds.top,
						bounds.right,
						bounds.bottom - keyboard.mVerticalGap);
				mKeyBoundsList.add(bounds);
			}
			mKeyboardDrawnIntent.removeExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST);
			mKeyboardDrawnIntent.putExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST, mKeyBoundsList);
			mLocalBroadcastManager.sendBroadcast(mKeyboardDrawnIntent);
		}

	};
		
}
