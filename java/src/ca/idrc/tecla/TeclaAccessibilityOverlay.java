/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.idrc.tecla;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import ca.idrc.tecla.HighlightBoundsView;
import ca.idrc.tecla.SimpleOverlay;
import ca.idrc.teclaaccessibilityservice.R;

public class TeclaAccessibilityOverlay extends SimpleOverlay {
	
	public static final String CLASS_TAG = "Highlighter";
	private static final int DEFAULT_COLOR = Color.rgb(0x6F, 0xBF, 0xF5);
	private static final int FRAME_COLOR = Color.rgb(0x38, 0x38, 0x38);

    private final HighlightBoundsView mInnerBoundsView;
    private final HighlightBoundsView mOuterBoundsView;
    
	public TeclaAccessibilityOverlay(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		//params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		setParams(params);
		
		setContentView(R.layout.tecla_accessibility_overlay);

		mInnerBoundsView = (HighlightBoundsView) findViewById(R.id.announce_bounds);
		mInnerBoundsView.setHighlightColor(DEFAULT_COLOR);
		
		
		mOuterBoundsView = (HighlightBoundsView) findViewById(R.id.bounds);
		mOuterBoundsView.setHighlightColor(FRAME_COLOR);
	}

	@Override
	protected void onShow() {
		TeclaStatic.logD(CLASS_TAG, "Showing Highlighter");
	}

	@Override
	protected void onHide() {
		TeclaStatic.logD(CLASS_TAG, "Hiding Highlighter");
//        mOuterBounds.clear();
//        mInnerBounds.clear();
	}
	

//	public void clearHighlight() {
//        mInnerBounds.clear();
//        mInnerBounds.postInvalidate();
//        mOuterBounds.clear();
//        mOuterBounds.postInvalidate();
//	}
	
//    public void removeInvalidNodes() {
//
//        mOuterBounds.removeInvalidNodes();
//        mOuterBounds.postInvalidate();
//
//        mInnerBounds.removeInvalidNodes();
//        mInnerBounds.postInvalidate();
//    }

	public void setNode(AccessibilityNodeInfo node) {
	
		//clearHighlight();
		if(node != null) {
		    Rect node_bounds = new Rect();
		    node.getBoundsInScreen(node_bounds);
		    mOuterBoundsView.setLeft(node_bounds.left);
		    mOuterBoundsView.setTop(node_bounds.top);
		    mOuterBoundsView.setRight(node_bounds.right);
		    mOuterBoundsView.setBottom(node_bounds.bottom);
		    mInnerBoundsView.setLeft(node_bounds.left);
		    mInnerBoundsView.setTop(node_bounds.top);
		    mInnerBoundsView.setRight(node_bounds.right);
		    mInnerBoundsView.setBottom(node_bounds.bottom);
		    //mOuterBoundsView.setBounds(node_bounds);
		    //mInnerBoundsView.setBounds(node_bounds);
		    mOuterBoundsView.setStrokeWidth(20);
		    mInnerBoundsView.setStrokeWidth(6);
		    mOuterBoundsView.postInvalidate();        	
		    mInnerBoundsView.postInvalidate();
			
		}
	}    
}