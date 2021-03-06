package ca.idrc.tecla.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import ca.idrc.tecla.hud.utils.SwitchEvent;
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

public class TeclaAccessibilityService extends AccessibilityService implements OnSharedPreferenceChangeListener, SwitchEvent.OnSwitchEventListener {

	private final static String CLASS_TAG = "TeclaAccessibilityService";

	private TeclaUtils mTeclaUtils;

	private ArrayList<AccessibilityNodeInfo> mLatestActiveLeafs = new ArrayList<AccessibilityNodeInfo>();;
	private ArrayList<Rect> mLeafBoundsList, mKeyBoundsList;
	private int[] mLeafRowIndexes, mKeyRowIndexes;
	private boolean[] mLeafSkipFlags;
	private AccessibilityNodeInfo mCurrentLeaf;
	private int mLeafIndex, mKeyIndex;
	private boolean isKeyboardVisible;

	private SharedPreferences shared_prefs;
	private Intent mKeySelectedIntent = new Intent();

	private Handler mHandler;
	private boolean isShuttingDown = false;
	private boolean isDepthChanged = true;

	private AccessibilityNodeInfo mLatestCandidateNode;

	private HUDOverlay mHighlighter;
	private SwitchOverlay mSwitchOverlay;

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

		mLeafBoundsList = new ArrayList<Rect>();
		mKeyBoundsList = new ArrayList<Rect>();
		mHighlighter = new HUDOverlay(this);
		mSwitchOverlay = new SwitchOverlay(this);
		mSwitchOverlay.registerOnSwitchEventListener(this);

		updateActiveLeafs(null);
		resumeScan();
		registerReceiver(mReceiver, TeclaMessaging.mKeyboardDrawnFilter);
		registerReceiver(mReceiver, TeclaMessaging.mIMEHiddenFilter);

		//Register for changes in preferences
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		shared_prefs.registerOnSharedPreferenceChangeListener(this);
		
		//Init intents
		mKeySelectedIntent.setAction(TeclaMessaging.EVENT_KEY_SELECTED);

	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
			CharSequence desc = event.getPackageName();
			if (desc != null) {
				TeclaDebug.logD(CLASS_TAG, "Event Type: " + AccessibilityEvent.eventTypeToString(event.getEventType()) + " received from package: " + desc.toString());
			}				
			switch (event_type) {
			case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
			//case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
			case AccessibilityEvent.TYPE_VIEW_FOCUSED:
			case AccessibilityEvent.TYPE_VIEW_SELECTED:
			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				updateActiveLeafs(node);
			}
		} else {
			TeclaDebug.logW(CLASS_TAG, "Node is null onAccessibilityEvent, nothing to do!");
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
				pauseScan();
				hideHighlighter();
				mKeyBoundsList = (ArrayList<Rect>) intent.getSerializableExtra(TeclaMessaging.EXTRA_KEY_BOUNDS_LIST);
				Collections.sort(mKeyBoundsList, mTeclaUtils.mRectComparator);
				mKeyRowIndexes = mTeclaUtils.getRowIndexes(mKeyBoundsList);
				isKeyboardVisible = true; //Assume IME is showing
				mKeyIndex = 0;
				mScanDepth = SCAN_DEPTH_ROW;
				resumeScan();
				TeclaDebug.logD(CLASS_TAG, "Now Scanning Keyboard!");
			}
		}

	};

	private void updateActiveLeafs(AccessibilityNodeInfo node) {
		if (node != null) {
			AccessibilityNodeInfo parent = node.getParent();
			while (parent != null) {
				node = parent;
				parent = node.getParent();
			}
			mLatestCandidateNode = node;
			mHandler.removeCallbacks(mUpdateActiveLeafsRunnable);
			mHandler.post(mUpdateActiveLeafsRunnable);				
		} else {
			TeclaDebug.logW(CLASS_TAG, "Node is null on updateActiveLeafs, nothing to do!");
		}
	}

	private Runnable mUpdateActiveLeafsRunnable = new Runnable() {

		@Override
		public void run() {
			int i;
			AccessibilityNodeInfo thisnode = mLatestCandidateNode;
			ArrayList<AccessibilityNodeInfo> theseleafs = new ArrayList<AccessibilityNodeInfo>();
			Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
			q.add(thisnode);
			while (!q.isEmpty()) {
				thisnode = q.poll();
				if (TeclaUtils.isActive(thisnode)) {
					theseleafs.add(thisnode);
				}
				for (i=0; i<thisnode.getChildCount(); ++i) {
					AccessibilityNodeInfo n = thisnode.getChild(i);
					if (n != null) q.add(n); // Don't add if null!
				}
			};
			TeclaDebug.logD(CLASS_TAG, theseleafs.size() + " leafs in this node!");
			if (theseleafs.size() > 0) {
				Collections.sort(theseleafs, mTeclaUtils.mNodeComparator);
				boolean is_same = true; //Assume these leafs are no different from the previous ones
				if ((mLatestActiveLeafs.size() == theseleafs.size())) {
					// We got the same number of leafs, so we need to make sure the bounds aren't different!
					Rect cBounds = new Rect();
					Rect nBounds = new Rect();
					i = 0;
					do {
						mLatestActiveLeafs.get(i).getBoundsInScreen(cBounds);
						theseleafs.get(i).getBoundsInScreen(nBounds);
						if (!TeclaUtils.isSameBounds(cBounds,nBounds)) is_same = false;
						i++;
					} while (i < mLatestActiveLeafs.size() && is_same);
				} else {
					is_same = false;
				}
				if (!is_same) {
					//Already know these leafs are different, so we need to reset scanning
					pauseScan();
					hideHighlighter();
					mLatestActiveLeafs = theseleafs;
					mLeafBoundsList = TeclaUtils.getBoundsList(mLatestActiveLeafs);
					mLeafRowIndexes = mTeclaUtils.getRowIndexes(mLeafBoundsList);
					mLeafIndex = 0;
					resumeScan();
				}
			} else {
				TeclaDebug.logW(CLASS_TAG, "Node has no active leafs, nothing to do!");
			}
		}

	};

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

		TeclaDebug.logD(CLASS_TAG, "Preference " + key + " changed!");
		if (key.equals(getString(R.string.PREF_FULL_SCREEN_SWITCH))) {
			if (sharedPreferences.getBoolean(key, false)) {
				mSwitchOverlay.show();
				TeclaDebug.logD(CLASS_TAG, "Full screen on!");
			} else {
				mSwitchOverlay.hide();
				TeclaDebug.logD(CLASS_TAG, "Full screen off!");
			}
		}

	}

	@Override
	public void onSwitchPress(int switch_id) {
		//TODO: Do different things for different switches!
		TeclaDebug.logD(CLASS_TAG, "Switch pressed!");
		pauseScan();
		if (mScanDepth == SCAN_DEPTH_ROW) {
			if (isSingleRowItem()) {
				selectItem();
			} else {
				mScanDepth = SCAN_DEPTH_ITEM;
				isDepthChanged = true;
			}
		} else if (mScanDepth == SCAN_DEPTH_ITEM) {
			selectItem();
		}
		resumeScan();
	}
	
	private boolean isSingleRowItem() {
		if (isKeyboardVisible) {
			return isSingleRowItem(mKeyRowIndexes, mKeyIndex);
		} else {
			return isSingleRowItem(mLeafRowIndexes, mLeafIndex);
		}
	}
	
	private boolean isSingleRowItem(int[] row_indexes, int index) {
		boolean has_row_siblings = false;
		if (index > 0) { //Has previous
			//Test previous
			has_row_siblings = (row_indexes[index] == row_indexes[index-1]);
		}
		if (!has_row_siblings && (index < (row_indexes.length - 1))) {
			//Test next			
			has_row_siblings = (row_indexes[index] == row_indexes[index+1]);
		}
		return !has_row_siblings;
	}
	
	private void selectItem() {
		if (isKeyboardVisible) {
			mKeySelectedIntent.removeExtra(TeclaMessaging.EXTRA_KEY_BOUNDS);
			mKeySelectedIntent.putExtra(TeclaMessaging.EXTRA_KEY_BOUNDS, mKeyBoundsList.get(mKeyIndex));
			sendBroadcast(mKeySelectedIntent);
			mKeyIndex = 0;
			TeclaDebug.logD(CLASS_TAG, "Sent type key event for key at " + mKeyBoundsList.get(mKeyIndex).centerX() + ", " + mKeyBoundsList.get(mKeyIndex).centerY());
		} else {
			mLatestActiveLeafs.get(mLeafIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
			TeclaUtils.logProperties(mLatestActiveLeafs.get(mLeafIndex));
			mLeafIndex = 0;
		}
		mScanDepth = SCAN_DEPTH_ROW;
		isDepthChanged = true;
	}

	@Override
	public void onSwitchRelease(int switch_id) {
		TeclaDebug.logD(CLASS_TAG, "Switch release!");
	}

	@Override
	public void onDestroy() {
		shutDown();
		super.onDestroy();
	}

	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	public void shutDown() {	
		TeclaDebug.logD(CLASS_TAG, "Shutting down...");
		unregisterReceiver(mReceiver);
		shared_prefs.unregisterOnSharedPreferenceChangeListener(this);
		isShuttingDown = true;
	}

	/** BETA CODE **/
	private final static int SCAN_DEPTH_ROW = 0x55;
	private final static int SCAN_DEPTH_ITEM = 0xAA;
	private final static int BETA_SCAN_DELAY = 800;
	private static int mScanDepth = SCAN_DEPTH_ROW;
	private Handler mBetaScanHandler;
	private Runnable mBetaScanRunnable = new Runnable() {

		@Override
		public void run() {

			pauseScan();
			if (isKeyboardVisible) {
				mKeyIndex = highlightNext(mKeyBoundsList, mKeyRowIndexes, mKeyIndex);
			} else {
				mLeafIndex = highlightNext(mLeafBoundsList, mLeafRowIndexes, mLeafIndex);
			}
			if (!isShuttingDown) {
				resumeScan();
			} else {
				hideHighlighter();
				mHighlighter = null;
			}
		}

	};
	
	private void pauseScan() {
		mBetaScanHandler.removeCallbacks(mBetaScanRunnable);
	}
	
	private void resumeScan() {
		mBetaScanHandler.postDelayed(mBetaScanRunnable, BETA_SCAN_DELAY);
	}
	
	private int highlightNext(ArrayList<Rect> bounds_list, int[] row_indexes, int index) {
		if (bounds_list.size() > 0) {
			int lastrowindex = row_indexes[index];
			switch (mScanDepth) {
			case SCAN_DEPTH_ROW:
				if (isDepthChanged) {
					isDepthChanged = false;
				} else {
					//Skip to next row!
					while (row_indexes[index] == lastrowindex) {
						index = incrementIndex(index, bounds_list);
					}
				}
				//TeclaDebug.logD(CLASS_TAG, "Highlighting row " + row_indexes[index]);
				mHighlighter.setBounds(
						TeclaUtils.getRowBounds(bounds_list, row_indexes, row_indexes[index]));
				showHighlighter();
				break;
			case SCAN_DEPTH_ITEM:
				if (isDepthChanged) {
					isDepthChanged = false;
				} else {
					index = incrementIndex(index, bounds_list);
					//Skip items in other rows
					while (row_indexes[index] != lastrowindex) {
						index = incrementIndex(index, bounds_list);
					}
				}
				//TeclaDebug.logD(CLASS_TAG, "Highlighting item " + index + ", in row " + row_indexes[index]);
				mHighlighter.setBounds(bounds_list.get(index));
				showHighlighter();
				break;
			}
		}
		return index;
	}

	private int incrementIndex(int index, ArrayList<Rect> bounds_list) {
		index++;
		if (index >= bounds_list.size()) {
			index = 0;
		}
		return index;
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}
