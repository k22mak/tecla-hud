package ca.idrc.tecla;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private AccessibilityNodeInfo original, parent;

	private static final int SCAN_DELAY = 1200;
	private static final int IMMEDIATELY = 0;
	private int child_count = 0;
	private int child_counter = 0;
	private Handler mHandler;
	
	/** Developer overlay for debugging touch exploration. */
	private TeclaAccessibilityOverlay mTeclaAccessibilityOverlay;

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");
		
		mHandler = new Handler();
		
		if (mTeclaAccessibilityOverlay == null) {
			mTeclaAccessibilityOverlay = new TeclaAccessibilityOverlay(this);
			mTeclaAccessibilityOverlay.show();
		}
//		mTeclaAccessibilityOverlay.getRootView().setOnTouchListener(mOverlayTouchListener);
		mTeclaAccessibilityOverlay.getRootView().setOnClickListener(mOverlayClickListener);
		mTeclaAccessibilityOverlay.getRootView().setOnLongClickListener(mOverlayLongClickListener);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());

		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
//			switch (event_type) {
//			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//			case AccessibilityEvent.TYPE_VIEW_SCROLLED:
//			case AccessibilityEvent.TYPE_VIEW_FOCUSED:
//			case AccessibilityEvent.TYPE_VIEW_SELECTED:
//				updateHighlights(getRoot(node));
//				break;
//			default:
//				break;
//			}
			if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
				stopScanning();
				Log.w("TeclaA11y", "Updating node!");
				original = node;
				setParent(node);
				startScanning(IMMEDIATELY);		
			}
		} else {
			Log.e("TeclaA11y", "Node is null!");
		}
		
//		if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//			node = event.getSource();
//			if (node != null) {
//				Log.d("TeclaA11y", "Drawing bounds");
//				TeclaAccessibilityOverlay.updateNodes(node, node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT));
//			} else {
//				Log.e("TeclaA11y", "Invalid node!");
//			}
//		}
	}
	
	private AccessibilityNodeInfo getRoot(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo root = node.getParent();
		while ((!node.equals(root)) && (root != null)) {
			node = root;
			root = node.getParent();
		}
		if (root == null) root = node;
		return root;
	}
	
	private AccessibilityNodeInfo findSelected(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo selected = null;
		if (node != null) {
			if (node.isSelected()) {
				selected = node;
			} else {
				AccessibilityNodeInfo child = null;
				int i = 0;
				while ((i < node.getChildCount()) && selected == null) {
					child = node.getChild(i);
					if (child.isSelected()) {
						selected = child;
					} else {
						selected = findSelected(child);
					}
					i++;
				}
			}
		}
		return selected;
	}
	
	private AccessibilityNodeInfo findFocused(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo focused = null;
		if (node != null) {
			if (node.isFocused()) {
				focused = node;
			} else {
				AccessibilityNodeInfo child = null;
				int i = 0;
				while ((i < node.getChildCount()) && (focused == null)) {
					child = node.getChild(i);
					if (child.isFocused()) {
						focused = child;
					} else {
						focused = findFocused(child);
					}
					i++;
				}
			}
		}
		return focused;
	}
	
	private void updateHighlights(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo focused = findFocused(node);
		if (focused != null) {
			AccessibilityNodeInfo selected = findSelected(focused);
			if (selected != null) {
				Log.d("TeclaA11y", "Drawing bounds for selected");
				TeclaAccessibilityOverlay.updateNodes(selected, selected);
			} else {
				Log.d("TeclaA11y", "Drawing bounds for focused");
				TeclaAccessibilityOverlay.updateNodes(focused, focused);
			}
		} else {
			Log.d("TeclaA11y", "Drawing bounds for parent");
			TeclaAccessibilityOverlay.updateNodes(node, node);
		}		
	}
	
	@Override
	public void onDestroy() {
		shutdownInfrastructure();
		super.onDestroy();
	}

	@Override
	public void onInterrupt() {
		stopScanning();
	}

	private void setParent(AccessibilityNodeInfo node) {
		parent = findMultipleChildParent(node);
		child_count = parent.getChildCount();
		child_counter = -1;
		Log.d("TeclaA11y", "New parent window ID " + parent.getWindowId() + " with " + child_count + " children");
	}

	private void startScanning(int delay) {
		mHandler.postDelayed(mNodeScanner, delay);
	}

	public Runnable mNodeScanner = new Runnable() {

		public void run() {
			stopScanning();
			int delay = IMMEDIATELY;
			if (child_counter < child_count - 1) {
				child_counter++;
			} else {
				child_counter = 0;
			}
			AccessibilityNodeInfo child = parent.getChild(child_counter);
			if (child != null) {
				Log.d("TeclaA11y", "Child " + (child_counter + 1) + " of " + child_count
						+ ": " + child.getText());
				child.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
				if (hasVisibleClickableNode(child)) {
					Log.d("TeclaA11y", "Scanning node with  " + getActiveChildCount(child)
							+ " clickable children.");
					TeclaAccessibilityOverlay.updateNodes(parent, findActiveNode(child));
					delay = SCAN_DELAY;
				}
			}
			startScanning(delay);
		}
		
	};

	private AccessibilityNodeInfo findMultipleChildParent(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo multiple_child_parent = node;
		if (multiple_child_parent != null) {
			if (getActiveChildCount(multiple_child_parent) == 1) {
				multiple_child_parent = findActiveChild(multiple_child_parent);
				multiple_child_parent = findMultipleChildParent(multiple_child_parent);
			}
		}
		return multiple_child_parent;
	}
	
	private boolean hasVisibleClickableNode(AccessibilityNodeInfo node) {
		boolean is_active = false;
		if (node != null) {
			if (node.isVisibleToUser()){
				is_active = node.isClickable();
				if (!is_active) {
					AccessibilityNodeInfo child = null;
					int i = 0;
					while ((i < node.getChildCount()) && !is_active) {
						child = node.getChild(i);
						if (child != null) {
							if (child.isClickable()) {
								is_active = true;
							} else {
								is_active = hasVisibleClickableNode(child);
							}
						}
						i++;
					}
				}
			}
		}
		return is_active;
	}
	
	private int getActiveChildCount(AccessibilityNodeInfo node) {
		int active_child_count = 0;
		if (node != null) {
			int child_count = node.getChildCount();
			AccessibilityNodeInfo child;
			for (int i=0;i < child_count; i++) {
				child = node.getChild(i);
				if (hasVisibleClickableNode(child)) {
					active_child_count++;
				}
			}
		}
		return active_child_count;
	}
	
	private AccessibilityNodeInfo findActiveChild(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo active_child = null;
		if (node != null) {
			int child_count = node.getChildCount();
			AccessibilityNodeInfo child;
			int i = 0;
			while(active_child == null) {
				child = node.getChild(i);
				if (hasVisibleClickableNode(child)) {
					active_child = child;
				}
				i++;
			}
		}
		return active_child;
	}
	
	private AccessibilityNodeInfo findActiveNode(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo active_node = null;
		if (node != null) {
			if (hasVisibleClickableNode(node)){
				active_node = node;
			} else {
				AccessibilityNodeInfo child;
				int i = 0;
				while(active_node == null) {
					child = node.getChild(i);
					if (hasVisibleClickableNode(child)) {
						active_node = child;
					} else {
						active_node = findActiveNode(child);
					}
					i++;
				}
			}
		}
		return active_node;
	}
	
	/**
	 * Listeners for full-screen switch actions
	 */
	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.v("TeclaA11y", "Tecla Overlay Touch Down!");
				break;
			case MotionEvent.ACTION_UP:
				Log.v("TeclaA11y", "Tecla Overlay Touch Up!");
				break;
			default:
				break;
			}
			return false;
		}
		
	};
	
	private View.OnClickListener mOverlayClickListener =  new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			stopScanning();
			if (parent != null) {
				AccessibilityNodeInfo child = parent.getChild(child_counter);
				if (child != null) {
					if (child.isClickable()) {
						child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
						Log.d("TeclaA11y", "Click performed!");
					}
					setParent(child);
					startScanning(IMMEDIATELY);
				} else {
					Log.e("TeclaA11y", "Invalid child!");
				}
			}
		}
	};
	
	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			shutdownInfrastructure();
			return true;
		}
	};
	
	private void stopScanning() {
		mHandler.removeCallbacks(mNodeScanner);
	}
		
	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	private void shutdownInfrastructure() {
		stopScanning();
		if (mTeclaAccessibilityOverlay != null) {
			mTeclaAccessibilityOverlay.hide();
			mTeclaAccessibilityOverlay = null;
		}
	}
	
	private void logNode(AccessibilityNodeInfo node) {
		if (node != null) {
			Log.v("TeclaA11y", "Parent: " + node.getText());
			AccessibilityNodeInfo child = null;
			int i = 0;
			while ((i < node.getChildCount())) {
				child = node.getChild(i);
				if (child != null) {
					Log.v("TeclaA11y", "Child: " + node.getText());
					logNode(child);
				}
				i++;
			}
		}
	}
	
}
