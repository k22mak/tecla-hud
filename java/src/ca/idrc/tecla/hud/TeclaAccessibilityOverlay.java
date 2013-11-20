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

package ca.idrc.tecla.hud;

import android.content.Context;
import android.graphics.Rect;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import ca.idrc.tecla.hud.R;
import ca.idrc.tecla.hud.utils.HUDView;
import ca.idrc.tecla.hud.utils.SimpleOverlay;

public class TeclaAccessibilityOverlay extends SimpleOverlay {
	
	public static final String CLASS_TAG = "Highlighter";

    private HUDView mHUDView;
    private int mNodeInset;
    
	public TeclaAccessibilityOverlay(Context context) {
		super(context);
		
		init();
		
	}
	
	private void init() {
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		//params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		setParams(params);
		
		mNodeInset = -(HUDView.FRAME_PIXEL_STROKE_WIDTH * 2);

		setContentView(R.layout.tecla_accessibility_overlay);

		mHUDView = (HUDView) findViewById(R.id.bounds);
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
		    node_bounds.inset(mNodeInset, mNodeInset);
		    mHUDView.setLeft(node_bounds.left);
		    mHUDView.setTop(node_bounds.top);
		    mHUDView.setRight(node_bounds.right);
		    mHUDView.setBottom(node_bounds.bottom);
		    mHUDView.postInvalidate();
			
		}
	}    
}