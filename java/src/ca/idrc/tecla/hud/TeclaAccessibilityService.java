package ca.idrc.tecla.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import ca.idrc.tecla.hud.utils.TeclaUtils;
import ca.idrc.tecla.lib.TeclaMessaging;
import ca.idrc.tecla.lib.TeclaDebug;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService implements OnSharedPreferenceChangeListener {

	private final static String CLASS_TAG = "TeclaAccessibilityService";

	private TeclaUtils mTeclaUtils;
	
	private ArrayList<AccessibilityNodeInfo> mActiveLeafs;
	private ArrayList<Rect> mLeafBoundsList, mKeyBoundsList;
	private int[] mLeafRowIndexes, mKeyRowIndexes;
	private boolean[] mLeafSkipFlags;
	private AccessibilityNodeInfo mCurrentLeaf;
	private int mLeafIndex, mKeyIndex;
	private boolean isKeyboardVisible;

	public SharedPreferences shared_prefs;

	private Handler mHandler;
	private boolean isShuttingDown = false;
	
	private AccessibilityNodeInfo mLastNode;
	
	private HUDOverlay mHighlighter;

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

		mTeclaUtils = new TeclaUtils(this);

		//Debug.waitForDebugger();
		mBetaScanHandler = new Handler();
		mHandler = new Handler();
		
		mLeafIndex = 0;
		mKeyIndex = 0;
		isKeyboardVisible = false;

		mActiveLeafs = new ArrayList<AccessibilityNodeInfo>();
		mLeafBoundsList = new ArrayList<Rect>();
		mKeyBoundsList = new ArrayList<Rect>();
		mHighlighter = new HUDOverlay(this);

		updateActiveLeafs(null);
		mBetaScanHandler.post(mBetaScanRunnable);
		registerReceiver(mReceiver, TeclaMessaging.mKeyboardDrawnFilter);
		registerReceiver(mReceiver, TeclaMessaging.mIMEHiddenFilter);
		
		//Register for changes in preferences
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		shared_prefs.registerOnSharedPreferenceChangeListener(this);

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
				isKeyboardVisible = false;
				TeclaDebug.logD(CLASS_TAG, "Now Scanning UI!");
			}
			if (intent.hasExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST)) {
				mBetaScanHandler.removeCallbacks(mBetaScanRunnable);
				hideHighlighter();
				mKeyBoundsList = (ArrayList<Rect>) intent.getSerializableExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST);
				Collections.sort(mKeyBoundsList, TeclaUtils.mRectComparator);
				mKeyRowIndexes = mTeclaUtils.getRowIndexes(mKeyBoundsList);
				isKeyboardVisible = true; //Assume IME is showing
				mKeyIndex = 0;
				mBetaScanHandler.post(mBetaScanRunnable);
				TeclaDebug.logD(CLASS_TAG, "Now Scanning Keyboard!");
			}
		}
		
	};
	
	private Runnable mUpdateActiveLeafsRunnable = new Runnable() {

		@Override
		public void run() {
			int i;
			AccessibilityNodeInfo thisnode = mLastNode;
			ArrayList<AccessibilityNodeInfo> theseleafs = new ArrayList<AccessibilityNodeInfo>();
			Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
			q.add(thisnode);
			while (!q.isEmpty()) {
				thisnode = q.poll();
				if (isActive(thisnode)) {
					theseleafs.add(thisnode);
				}
				for (i=0; i<thisnode.getChildCount(); ++i) {
					AccessibilityNodeInfo n = thisnode.getChild(i);
					if (n != null) q.add(n); // Don't add if null!
				}
			};
			TeclaDebug.logD(CLASS_TAG, theseleafs.size() + " leafs in this node!");
			if (theseleafs.size() > 0) {
				Collections.sort(theseleafs, TeclaUtils.mNodeComparator);
				boolean is_same = true; //Assume these leafs are no different from the previous ones
				if ((mActiveLeafs.size() == theseleafs.size())) {
					// These leafs are the same size, so we need to make sure the bounds aren't different!
					Rect cBounds = new Rect();
					Rect nBounds = new Rect();
					i = 0;
					do {
						mActiveLeafs.get(i).getBoundsInScreen(cBounds);
						theseleafs.get(i).getBoundsInScreen(nBounds);
						if (!TeclaUtils.isSameBounds(cBounds,nBounds)) is_same = false;
						i++;
					} while (i < mActiveLeafs.size() && is_same);
				} else {
					is_same = false;
				}
				mActiveLeafs = theseleafs;
				if (!is_same) {
					//Already know these leafs are different, so we need to reset scanning
					mBetaScanHandler.removeCallbacks(mBetaScanRunnable);
					hideHighlighter();
					mLeafBoundsList = TeclaUtils.getBoundsList(mActiveLeafs);
					mLeafRowIndexes = mTeclaUtils.getRowIndexes(mLeafBoundsList);
					mLeafIndex = 0;
					mBetaScanHandler.post(mBetaScanRunnable);
				}
			}
		}
		
	};
	
	private void updateActiveLeafs(AccessibilityNodeInfo node) {
		if (node == null) {
			TeclaDebug.logW(CLASS_TAG, "Node is null, nothing to do!");
		} else {
			AccessibilityNodeInfo parent = node.getParent();
			while (parent != null) {
				node = parent;
				parent = node.getParent();
			}
			mLastNode = node;
			mHandler.removeCallbacks(mUpdateActiveLeafsRunnable);
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		TeclaDebug.logD(CLASS_TAG, "A preference changed!");
		
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
	
	/** BETA CODE **/
	private final static int SCAN_DEPTH_ROW = 0x55;
	private final static int SCAN_DEPTH_COLUMN = 0xAA;
	private final static int BETA_SCAN_DELAY = 600;
	private static int mScanDepth = SCAN_DEPTH_ROW;
	private Handler mBetaScanHandler;
	private Runnable mBetaScanRunnable = new Runnable() {

		@Override
		public void run() {

			if (isKeyboardVisible) {
				if (mKeyBoundsList.size() > 0) {
					if (mScanDepth == SCAN_DEPTH_ROW) {
						int thisrowindex = mKeyRowIndexes[mKeyIndex];
						mHighlighter.setBounds(
								TeclaUtils.getRowBounds(mKeyBoundsList, mKeyRowIndexes, thisrowindex));
						showHighlighter();
						while (mKeyRowIndexes[mKeyIndex] == thisrowindex) {
							incrementKeyIndex();
						}
					} else {
						mHighlighter.setBounds(mKeyBoundsList.get(mKeyIndex));
						showHighlighter();
						incrementKeyIndex();
					}
				}
			} else {
				if (mLeafBoundsList.size() > 0) {
					if (mScanDepth == SCAN_DEPTH_ROW) {
						int thisrowindex = mLeafRowIndexes[mLeafIndex];
						mHighlighter.setBounds(
								TeclaUtils.getRowBounds(mLeafBoundsList, mLeafRowIndexes, thisrowindex));
						showHighlighter();
						while (mLeafRowIndexes[mLeafIndex] == thisrowindex) {
							incrementLeafIndex();
						}
					} else {
						mHighlighter.setBounds(mLeafBoundsList.get(mLeafIndex));
						showHighlighter();
						incrementLeafIndex();
						//logProperties(mCurrentLeaf);
					}
				}
			}
			if (!isShuttingDown) {
				mBetaScanHandler.postDelayed(mBetaScanRunnable, BETA_SCAN_DELAY);
			} else {
				hideHighlighter();
				mHighlighter = null;
			}
		}
		
	};
	
	private void incrementLeafIndex() {
		mLeafIndex++;
		if (mLeafIndex >= mActiveLeafs.size()) {
			mLeafIndex = 0;
		}
	}
	
	private void incrementKeyIndex() {
		mKeyIndex++;
		if (mKeyIndex >= mKeyBoundsList.size()) {
			mKeyIndex = 0;
		}
	}
	
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
