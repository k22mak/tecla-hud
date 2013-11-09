package ca.idrc.tecla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static String CLASS_TAG = "TeclaAccessibilityService";

	private ArrayList<AccessibilityNodeInfo> mActiveLeafs;
	private AccessibilityNodeInfo mCurrentLeaf;
	private int mLeafIndex;

	private Handler mHandler;
	
	private AccessibilityNodeInfo mLastNode;
	
	private TeclaAccessibilityOverlay mHighlighter;

	//private final static String MAP_VIEW = "android.view.View";
	// For later use for custom actions 
	//private final static String WEB_VIEW = "Web View";
	
	@Override
	public void onCreate() {
		TeclaStatic.logD(CLASS_TAG, "Service created");

		init();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		TeclaStatic.logD(CLASS_TAG, "Service connected");

	}

	private void init() {

		mDebugScanHandler = new Handler();
		mHandler = new Handler();

		mActiveLeafs = new ArrayList<AccessibilityNodeInfo>();
		mHighlighter = new TeclaAccessibilityOverlay(this);

		updateActiveLeafs(null);
		mDebugScanHandler.post(mDebugScanRunnable);

	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		TeclaStatic.logD(CLASS_TAG, AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
			switch (event_type) {
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
			case AccessibilityEvent.TYPE_VIEW_FOCUSED:
			case AccessibilityEvent.TYPE_VIEW_SELECTED:
			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
			case AccessibilityEvent.TYPE_VIEW_CLICKED:
				updateActiveLeafs(node);
			}
		}
	}

	private Runnable mUpdateActiveLeafsRunnable = new Runnable() {

		@Override
		public void run() {
			AccessibilityNodeInfo thisnode = mLastNode;
			ArrayList<AccessibilityNodeInfo> active_leafs = new ArrayList<AccessibilityNodeInfo>();
			Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
			q.add(thisnode);
			while (!q.isEmpty()) {
				thisnode = q.poll();
				if (isActive(thisnode)) {
					active_leafs.add(thisnode);
				}
				for (int i=0; i<thisnode.getChildCount(); ++i) {
					AccessibilityNodeInfo n = thisnode.getChild(i);
					if (n != null) q.add(n); // Don't add if null!
				}
			};
			Collections.sort(active_leafs, new Comparator<AccessibilityNodeInfo>(){
	
				@Override
				public int compare(AccessibilityNodeInfo lhs,
						AccessibilityNodeInfo rhs) {
					Rect outBoundsL = new Rect();
					Rect outBoundsR = new Rect();
					lhs.getBoundsInScreen(outBoundsL);
					rhs.getBoundsInScreen(outBoundsR);
					int swidth = mHighlighter.getRootView().getWidth();
					int sheight = mHighlighter.getRootView().getHeight();
					int smax;
					if (swidth <= sheight) { // Portrait
						smax = sheight;
					} else { // Landscape
						smax = swidth;
					}
	
					if ((outBoundsL.centerX() == outBoundsR.centerX())
							&& (outBoundsL.centerY() == outBoundsR.centerY())) {
						return 0;
					} else {
						return (smax * (outBoundsL.centerY() - outBoundsR.top)) + (outBoundsL.left - outBoundsR.right);
					}
				}
				
			});
			boolean is_same = false;
			if ((mActiveLeafs.size() == active_leafs.size()) && (mActiveLeafs.size() > 0)) {
				Rect cBounds = new Rect();
				Rect nBounds = new Rect();
				int i = 0;
				is_same = true;
				do {
					mActiveLeafs.get(i).getBoundsInScreen(cBounds);
					active_leafs.get(i).getBoundsInScreen(nBounds);
					if (!cBounds.equals(nBounds)) is_same = false;
					i++;
				} while (i < mActiveLeafs.size() && is_same);
			} else {
				mDebugScanHandler.removeCallbacks(mDebugScanRunnable);
				hideHighlighter();
				mLeafIndex = 0;
			}
			if (!is_same) {
				mDebugScanHandler.removeCallbacks(mDebugScanRunnable);
				mActiveLeafs = active_leafs;
				mDebugScanHandler.post(mDebugScanRunnable);
			}
			TeclaStatic.logD(CLASS_TAG, active_leafs.size() + " leafs in the node!");
		}
		
	};
	
	private void updateActiveLeafs(AccessibilityNodeInfo node) {
		if (node == null) {
			TeclaStatic.logW(CLASS_TAG, "Node is null, nothing to do!");
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
		TeclaStatic.logD(CLASS_TAG, "Shutting down...");
		
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
	
	private final static int DEBUG_SCAN_DELAY = 1000;
	private Handler mDebugScanHandler;
	private Runnable mDebugScanRunnable = new Runnable() {

		@Override
		public void run() {
			mDebugScanHandler.removeCallbacks(mDebugScanRunnable);
			if (mActiveLeafs.size() > 0) {
				if (mLeafIndex >= mActiveLeafs.size()) {
					mLeafIndex = 0;
				}
				mCurrentLeaf = mActiveLeafs.get(mLeafIndex);
				mHighlighter.setNode(mCurrentLeaf);
				showHighlighter();
				mLeafIndex++;
				//logProperties(mCurrentLeaf);
			}
			mDebugScanHandler.postDelayed(mDebugScanRunnable, DEBUG_SCAN_DELAY);
		}
		
	};
	
	private void logProperties(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo parent = node.getParent();
		TeclaStatic.logW(CLASS_TAG, "Node properties");
		TeclaStatic.logW(CLASS_TAG, "isA11yFocusable? " + Boolean.toString(isA11yFocusable(node)));
		TeclaStatic.logW(CLASS_TAG, "isInputFocusable? " + Boolean.toString(isInputFocusable(node)));
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
