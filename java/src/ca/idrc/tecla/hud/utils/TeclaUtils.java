package ca.idrc.tecla.hud.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import ca.idrc.tecla.lib.TeclaDebug;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaUtils {
	
	private final static String CLASS_TAG = "TeclaUtils";
	
	private final static double mTargetTolerance = 0.15; //The target tolerance in inches
	
	private Display mDisplay;
	private DisplayMetrics mDisplayMetrics;
	
	public TeclaUtils(Context context) {
		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		mDisplayMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mDisplayMetrics);
	}

	public int[] getRowIndexes(ArrayList<Rect> boundslist) {
		int[] indexes = new int[boundslist.size()];
		int i = 0;
		int j = 0;
		Rect prevBounds = boundslist.get(i);
		Rect theseBounds;
		indexes = new int[boundslist.size()];
		indexes[i] = j;
		for (i=1;i<boundslist.size();i++) {
			//theseBounds = new Rect();
			theseBounds = boundslist.get(i);
			if (!isSameRow(prevBounds, theseBounds)) {
				j++;
			}
			indexes[i] = j;
			prevBounds = theseBounds;
			//TeclaDebug.logD(CLASS_TAG, "Node " + i + " is in row " + j);
		}
		return indexes;
	}
	
	public static Rect getRowBounds(ArrayList<Rect> boundslist, int[] rowindexes, int rowindex) {
		int first;
		int i = 0;
		do {
			first = i;
			i++;
		} while (rowindexes[first] != rowindex);
		int last = first;
		boolean islast = false;
		do {
			if (i == rowindexes.length) {
				islast = true;
			} else if (rowindexes[i] != rowindex) {
				islast = true;
			} else {
				last = i;
				i++;
			}
		} while (!islast);
		Rect rowbounds = new Rect();
		rowbounds.set(boundslist.get(first));
		rowbounds.union(boundslist.get(last));
		//TeclaDebug.logD(CLASS_TAG, "Row " + rowindex + " has bounds " + rowbounds.toShortString());
		return rowbounds;
	}
	
	public static ArrayList<Rect> getBoundsList(ArrayList<AccessibilityNodeInfo> nodes) {
		ArrayList<Rect> boundslist = new ArrayList<Rect>();
		Rect bounds;
		for (AccessibilityNodeInfo node : nodes) {
			bounds = new Rect();
			node.getBoundsInScreen(bounds);
			boundslist.add(bounds);
		}
		return boundslist;
	}
	
	public static boolean isActiveLeaf(AccessibilityNodeInfo node) {
		//return (isActive(node) && !hasActiveDescendents(node)); //Doesn't seem to do much!
		return isActive(node);
	}
	
	public static boolean isActive(AccessibilityNodeInfo node) {
		boolean is_active = false;
		if (node != null) {
			//AccessibilityNodeInfo parent = node.getParent();
			if (node.isVisibleToUser()
					&& node.isClickable()
					&& (isA11yFocusable(node) || isInputFocusable(node))
					//&& !node.isLongClickable()
					//&& !(!isInputFocusable(node) && !(node.isScrollable() || parent.isScrollable()))
					//&& (!node.isScrollable()) // doesn't seem to do much
					//&& (node.getChildCount() == 0) // nope
					&& node.isEnabled())
				is_active = true;
		}
		return is_active;
	}

	private static boolean isInputFocusable(AccessibilityNodeInfo node) {
		return (node.getActions() & AccessibilityNodeInfo.ACTION_FOCUS) == AccessibilityNodeInfo.ACTION_FOCUS;
	}

	private static boolean isA11yFocusable(AccessibilityNodeInfo node) {
		return (node.getActions() & AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS;
	}

	public boolean isSameRow(Rect lhs, Rect rhs) {
		int ytol = (int) Math.round(mTargetTolerance * mDisplayMetrics.ydpi);
		return	((rhs.top >= lhs.top - ytol) && (rhs.top <= lhs.top + ytol)) &&
				((rhs.bottom >= lhs.bottom - ytol) && (rhs.bottom <= lhs.bottom + ytol));
	}

	public static boolean isSameBounds(Rect lhs, Rect rhs) {
		return ((lhs.top == rhs.top) &&
				(lhs.right == rhs.right) &&
				(lhs.bottom == rhs.bottom) &&
				(lhs.left == rhs.left));
	}

	public Comparator<AccessibilityNodeInfo> mNodeComparator = new Comparator<AccessibilityNodeInfo>() {

		@Override
		public int compare(AccessibilityNodeInfo lhs, AccessibilityNodeInfo rhs) {
			Rect lBounds = new Rect();
			Rect rBounds = new Rect();
			lhs.getBoundsInScreen(lBounds);
			rhs.getBoundsInScreen(rBounds);
			return compareBounds(lBounds, rBounds);
		}
		
	};

	public Comparator<Rect> mRectComparator = new Comparator<Rect>() {

		@Override
		public int compare(Rect lhs, Rect rhs) {
			return compareBounds(lhs, rhs);
		}

		
	};

	private int compareBounds(Rect lhs, Rect rhs) {
		int imax = 100; //Arbitrary number that is larger than the max number of nodes along the screen width
		if (isSameBounds(lhs,rhs)) {
			return 0;
		} else {
			if (isSameRow(lhs, rhs)) {
				return lhs.centerX() - rhs.centerX();				
			} else {
				return (imax * (lhs.top - rhs.top)) + (lhs.centerX() - rhs.centerX());
			}
		}
	}
	
	public static void logProperties(AccessibilityNodeInfo node) {
		AccessibilityNodeInfo parent = node.getParent();
		TeclaDebug.logW(CLASS_TAG, "Node properties");
		TeclaDebug.logD(CLASS_TAG, "isA11yFocusable? " + Boolean.toString(isA11yFocusable(node)));
		TeclaDebug.logD(CLASS_TAG, "isInputFocusable? " + Boolean.toString(isInputFocusable(node)));
		TeclaDebug.logD(CLASS_TAG, "isVisible? " + Boolean.toString(node.isVisibleToUser()));
		TeclaDebug.logD(CLASS_TAG, "isClickable? " + Boolean.toString(node.isClickable()));
		TeclaDebug.logD(CLASS_TAG, "isEnabled? " + Boolean.toString(node.isEnabled()));
		TeclaDebug.logD(CLASS_TAG, "isScrollable? " + Boolean.toString(node.isScrollable()));
		TeclaDebug.logD(CLASS_TAG, "isSelected? " + Boolean.toString(node.isSelected()));
		TeclaDebug.logD(CLASS_TAG, "isLongClickable? " + Boolean.toString(node.isLongClickable()));
		TeclaDebug.logW(CLASS_TAG, "Parent properties");
		TeclaDebug.logD(CLASS_TAG, "isVisible? " + Boolean.toString(parent.isVisibleToUser()));
		TeclaDebug.logD(CLASS_TAG, "isClickable? " + Boolean.toString(parent.isClickable()));
		TeclaDebug.logD(CLASS_TAG, "isEnabled? " + Boolean.toString(parent.isEnabled()));
		TeclaDebug.logD(CLASS_TAG, "isA11yFocusable? " + Boolean.toString((parent.getActions() & AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS));
		TeclaDebug.logD(CLASS_TAG, "isInputFocusable? " + Boolean.toString((parent.getActions() & AccessibilityNodeInfo.ACTION_FOCUS) == AccessibilityNodeInfo.ACTION_FOCUS));
		TeclaDebug.logD(CLASS_TAG, "isScrollable? " + Boolean.toString(parent.isScrollable()));
		TeclaDebug.logD(CLASS_TAG, "isSelected? " + Boolean.toString(parent.isSelected()));
		TeclaDebug.logD(CLASS_TAG, "isLongClickable? " + Boolean.toString(parent.isLongClickable()));
	}

	private static boolean hasActiveDescendents(AccessibilityNodeInfo node) {
		boolean has_active_children = false;
		if (node != null) {
			Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
			do {
				for (int i=0; i<node.getChildCount(); ++i) {
					AccessibilityNodeInfo n = node.getChild(i);
					if (n != null) q.add(n); // Don't add if null!
				}
				node = q.poll();
				has_active_children = isActive(node);
			} while (!q.isEmpty() || has_active_children);
			//q.clear();
		}
		return has_active_children;
	}

	private static int getRowStart(int[] rowindexes, int rowindex) {
		int i = 0;
		while (rowindexes[i] != rowindex) {
			i++;
		}
		return i;
	}
	
}
