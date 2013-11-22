package ca.idrc.tecla.lib;

import java.util.Comparator;

import android.content.Context;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaUtils {

	public static boolean isAccessibilityEnabled(Context context) {
		AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
		return manager.isEnabled();
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
	
	public static boolean isSameBounds(Rect lhs, Rect rhs) {
		return ((lhs.top == rhs.top) &&
				(lhs.right == rhs.right) &&
				(lhs.bottom == rhs.bottom) &&
				(lhs.left == rhs.left));
	}
	
}
