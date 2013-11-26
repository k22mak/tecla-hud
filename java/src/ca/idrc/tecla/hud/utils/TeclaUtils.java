package ca.idrc.tecla.hud.utils;

import java.util.ArrayList;
import java.util.Comparator;

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

//	public static int getRowStart(int[] rowindexes, int rowindex) {
//		int i = 0;
//		while (rowindexes[i] != rowindex) {
//			i++;
//		}
//		return i;
//	}
	
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
			TeclaDebug.logD(CLASS_TAG, "Node " + i + " is in row " + j);
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
	
	public static boolean isActive(AccessibilityNodeInfo node) {
		boolean is_active = false;
		//AccessibilityNodeInfo parent = node.getParent();
		if (node.isVisibleToUser()
				&& node.isClickable()
				&& (isA11yFocusable(node) || isInputFocusable(node))
				//&& !(!isInputFocusable(node) && !(node.isScrollable() || parent.isScrollable()))
				&& (!node.isScrollable())
				&& node.isEnabled())
			is_active = true;
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

	public static Comparator<AccessibilityNodeInfo> mNodeComparator = new Comparator<AccessibilityNodeInfo>() {

		@Override
		public int compare(AccessibilityNodeInfo lhs, AccessibilityNodeInfo rhs) {
			Rect lBounds = new Rect();
			Rect rBounds = new Rect();
			lhs.getBoundsInScreen(lBounds);
			rhs.getBoundsInScreen(rBounds);
			return compareBounds(lBounds, rBounds);
		}
		
	};

	public static Comparator<Rect> mRectComparator = new Comparator<Rect>() {

		@Override
		public int compare(Rect lhs, Rect rhs) {
			return compareBounds(lhs, rhs);
		}

		
	};

	private static int compareBounds(Rect lhs, Rect rhs) {
		int imax = 100; //Arbitrary number that is larger than the max number of nodes along the screen width
		if (isSameBounds(lhs,rhs)) {
			return 0;
		} else {
			return (imax * (lhs.top - rhs.top)) + (lhs.centerX() - rhs.centerX());
		}
	}
	
	public static void logProperties(AccessibilityNodeInfo node) {
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

}
