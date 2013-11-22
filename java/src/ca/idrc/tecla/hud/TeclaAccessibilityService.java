package ca.idrc.tecla.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;
import ca.idrc.tecla.lib.TeclaUtils;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static String CLASS_TAG = "TeclaAccessibilityService";

	private ArrayList<AccessibilityNodeInfo> mActiveLeafs;
	private ArrayList<Rect> mKeyBoundsList;
	private AccessibilityNodeInfo mCurrentLeaf;
	private int mLeafIndex, mKeyIndex;
	private boolean isKeyboardVisible;

	private Handler mHandler;
	private boolean isShuttingDown = false;
	
	private AccessibilityNodeInfo mLastNode;
	
	private TeclaAccessibilityOverlay mHighlighter;

	//private final static String MAP_VIEW = "android.view.View";
	// For later use for custom actions 
	//private final static String WEB_VIEW = "Web View";
	
	@Override
	public void onCreate() {
		TeclaDebug.logD(CLASS_TAG, "Service created");

		init();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		TeclaDebug.logD(CLASS_TAG, "Service connected");

	}

	private void init() {

		//Debug.waitForDebugger();
		mDebugScanHandler = new Handler();
		mHandler = new Handler();
		mLeafIndex = 0;
		mKeyIndex = 0;
		isKeyboardVisible = false;

		mActiveLeafs = new ArrayList<AccessibilityNodeInfo>();
		mKeyBoundsList = new ArrayList<Rect>();
		mHighlighter = new TeclaAccessibilityOverlay(this);

		updateActiveLeafs(null);
		mDebugScanHandler.post(mDebugScanRunnable);
		registerReceiver(mReceiver, TeclaMessaging.mKeyboardDrawnFilter);
		registerReceiver(mReceiver, TeclaMessaging.mIMEHiddenFilter);
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
			CharSequence desc = event.getPackageName();
			if (desc != null) {
				TeclaDebug.logW(CLASS_TAG, "Event Type: " + AccessibilityEvent.eventTypeToString(event.getEventType()) + " received from package: " + desc.toString());
			}				
			switch (event_type) {
			case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
			case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
			case AccessibilityEvent.TYPE_VIEW_FOCUSED:
			case AccessibilityEvent.TYPE_VIEW_SELECTED:
			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				updateActiveLeafs(node);
			}
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(TeclaMessaging.EVENT_IME_HIDING)) {
				TeclaDebug.logD(CLASS_TAG, "IME Window hiding!");
				isKeyboardVisible = false;
			}
			if (intent.hasExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST)) {
				TeclaDebug.logD(CLASS_TAG, "Keyboard drawn!");
				mKeyBoundsList = (ArrayList<Rect>) intent.getSerializableExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST);
				Collections.sort(mKeyBoundsList, TeclaUtils.mRectComparator);
				isKeyboardVisible = true; //Assume IME is showing
				mKeyIndex = 0;
				mDebugScanHandler.post(mDebugScanRunnable);
			}
		}
		
	};
	
	private Runnable mUpdateActiveLeafsRunnable = new Runnable() {

		@Override
		public void run() {
			AccessibilityNodeInfo thisnode = mLastNode;
			ArrayList<AccessibilityNodeInfo> these_leafs = new ArrayList<AccessibilityNodeInfo>();
			Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
			q.add(thisnode);
			while (!q.isEmpty()) {
				thisnode = q.poll();
				if (isActive(thisnode)) {
					these_leafs.add(thisnode);
				}
				for (int i=0; i<thisnode.getChildCount(); ++i) {
					AccessibilityNodeInfo n = thisnode.getChild(i);
					if (n != null) q.add(n); // Don't add if null!
				}
			};
			Collections.sort(these_leafs, TeclaUtils.mNodeComparator);
			boolean is_same = false;
			if ((mActiveLeafs.size() == these_leafs.size()) && (mActiveLeafs.size() > 0)) {
				Rect cBounds = new Rect();
				Rect nBounds = new Rect();
				int i = 0;
				is_same = true;
				do {
					mActiveLeafs.get(i).getBoundsInScreen(cBounds);
					these_leafs.get(i).getBoundsInScreen(nBounds);
					if (!TeclaUtils.isSameBounds(cBounds,nBounds)) is_same = false;
					i++;
				} while (i < mActiveLeafs.size() && is_same);
			} else {
				mDebugScanHandler.removeCallbacks(mDebugScanRunnable);
				hideHighlighter();
				mLeafIndex = 0;
			}
			if (!is_same) {
				mDebugScanHandler.removeCallbacks(mDebugScanRunnable);
				mActiveLeafs = these_leafs;
				mDebugScanHandler.post(mDebugScanRunnable);
			}
			TeclaDebug.logD(CLASS_TAG, these_leafs.size() + " leafs in the node!");
		}
		
	};
	
	private void updateActiveLeafs(AccessibilityNodeInfo node) {
		if (node == null) {
			TeclaDebug.logW(CLASS_TAG, "Node is null, nothing to do!");
		} else {
			mHandler.removeCallbacks(mUpdateActiveLeafsRunnable);
			AccessibilityNodeInfo parent = node.getParent();
			while (parent != null) {
				node = parent;
				parent = node.getParent();
			}
			mLastNode = node;
			mHandler.post(mUpdateActiveLeafsRunnable);				
		}
	}
	
	private void showHighlighter() {
		if (mHighlighter != null) {
			if (!mHighlighter.isVisible()) {
				mHighlighter.show();
			}
		}
	}
	
	private void hideHighlighter() {
		if (mHighlighter != null) {
			if (mHighlighter.isVisible()) {
				mHighlighter.hide();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutDown();
	}

	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	public void shutDown() {	
		TeclaDebug.logD(CLASS_TAG, "Shutting down...");
		isShuttingDown = true;
		unregisterReceiver(mReceiver);
	}

	private boolean isActive(AccessibilityNodeInfo node) {
		boolean is_active = false;
		//AccessibilityNodeInfo parent = node.getParent();
		if (node.isVisibleToUser()
				&& node.isClickable()
				&& (isA11yFocusable(node) || isInputFocusable(node))
				//&& !(!isInputFocusable(node) && !(node.isScrollable() || parent.isScrollable()))
				&& node.isEnabled())
			is_active = true;
		return is_active;
	}
	
	private boolean isInputFocusable(AccessibilityNodeInfo node) {
		return (node.getActions() & AccessibilityNodeInfo.ACTION_FOCUS) == AccessibilityNodeInfo.ACTION_FOCUS;
	}
	
	private boolean isA11yFocusable(AccessibilityNodeInfo node) {
		return (node.getActions() & AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS;
	}
	
	/** DEBUGGING CODE **/
	private final static int DEBUG_SCAN_DELAY = 600;
	private Handler mDebugScanHandler;
	private Runnable mDebugScanRunnable = new Runnable() {

		@Override
		public void run() {
			mDebugScanHandler.removeCallbacks(mDebugScanRunnable);

			if (isKeyboardVisible) {
				mHighlighter.setBounds(mKeyBoundsList.get(mKeyIndex));
				showHighlighter();
				mKeyIndex++;
				if (mKeyIndex >= mKeyBoundsList.size()) {
					mKeyIndex = 0;
				}
				TeclaDebug.logD(CLASS_TAG, "Set bounds " + mKeyBoundsList.get(mKeyIndex).toShortString() + " for key #" + mKeyIndex);
			} else {
				if (mActiveLeafs.size() > 0) {
					mCurrentLeaf = mActiveLeafs.get(mLeafIndex);
					mHighlighter.setNode(mCurrentLeaf);
					showHighlighter();
					mLeafIndex++;
					if (mLeafIndex >= mActiveLeafs.size()) {
						mLeafIndex = 0;
					}
					//logProperties(mCurrentLeaf);
				}
			}
			if (!isShuttingDown) {
				mDebugScanHandler.postDelayed(mDebugScanRunnable, DEBUG_SCAN_DELAY);
			} else {
				hideHighlighter();
				mHighlighter = null;
			}
		}
		
	};
	
	private void logProperties(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo parent = node.getParent();
		TeclaDebug.logW(CLASS_TAG, "Node properties");
		TeclaDebug.logW(CLASS_TAG, "isA11yFocusable? " + Boolean.toString(isA11yFocusable(node)));
		TeclaDebug.logW(CLASS_TAG, "isInputFocusable? " + Boolean.toString(isInputFocusable(node)));
		//TeclaStatic.logD(CLASS_TAG, "isVisible? " + Boolean.toString(node.isVisibleToUser()));
		//TeclaStatic.logD(CLASS_TAG, "isClickable? " + Boolean.toString(node.isClickable()));
		//TeclaStatic.logD(CLASS_TAG, "isEnabled? " + Boolean.toString(node.isEnabled()));
		//TeclaStatic.logD(CLASS_TAG, "isScrollable? " + Boolean.toString(node.isScrollable()));
		//TeclaStatic.logD(CLASS_TAG, "isSelected? " + Boolean.toString(node.isSelected()));
		//TeclaStatic.logW(CLASS_TAG, "Parent properties");
		//TeclaStatic.logW(CLASS_TAG, "isVisible? " + Boolean.toString(parent.isVisibleToUser()));
		//TeclaStatic.logW(CLASS_TAG, "isClickable? " + Boolean.toString(parent.isClickable()));
		//TeclaStatic.logW(CLASS_TAG, "isEnabled? " + Boolean.toString(parent.isEnabled()));
		//TeclaStatic.logW(CLASS_TAG, "isA11yFocusable? " + Boolean.toString((parent.getActions() & AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS));
		//TeclaStatic.logW(CLASS_TAG, "isInputFocusable? " + Boolean.toString((parent.getActions() & AccessibilityNodeInfo.ACTION_FOCUS) == AccessibilityNodeInfo.ACTION_FOCUS));
		//TeclaStatic.logW(CLASS_TAG, "isScrollable? " + Boolean.toString(parent.isScrollable()));
		//TeclaStatic.logW(CLASS_TAG, "isSelected? " + Boolean.toString(parent.isSelected()));
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}
    
}
