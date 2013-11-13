/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// package com.google.android.marvin.utils;
package ca.idrc.tecla.hud.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Handles drawing a frame on-screen.
 */
public class HUDView extends View {

	public static final int FRAME_PIXEL_STROKE_WIDTH = 6;
	private static final int FILL_COLOR = Color.argb(0x50, 0x33, 0xB5, 0xE5);
	private static final int FRAME_INNER_COLOR = Color.rgb(0xEE, 0xEE, 0xEE);
	private static final int FRAME_OUTER_COLOR = Color.rgb(0x11, 0x11, 0x11);

	private Rect mBounds = new Rect();
    private Paint mPaint = new Paint();
    private int frame_inset;
    

    /**
     * Constructs a new highlight bounds view using the specified attributes.
     *
     * @param context The parent context.
     * @param attrs The view attributes.
     */
    public HUDView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

    }
    
    private void init() {
        mPaint.setStrokeJoin(Join.ROUND);
    }

    /* (non-Javadoc)
	 * @see android.view.View#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
    	//mPaint.setStrokeWidth(FRAME_PIXEL_STROKE_WIDTH / (1.f * (right - left)));
    	mPaint.setStrokeWidth(FRAME_PIXEL_STROKE_WIDTH);
	}

	@Override
    public void onDraw(Canvas c) {
    	c.getClipBounds(mBounds);
    	frame_inset = Math.round(FRAME_PIXEL_STROKE_WIDTH / 2.f);
    	mBounds.inset(frame_inset, frame_inset);
    	mPaint.setStyle(Style.FILL);
    	mPaint.setColor(FILL_COLOR);
        c.drawRect(mBounds, mPaint);
    	mPaint.setStyle(Style.STROKE);
    	mPaint.setColor(FRAME_OUTER_COLOR);
        c.drawRect(mBounds, mPaint);
    	mBounds.inset(FRAME_PIXEL_STROKE_WIDTH, FRAME_PIXEL_STROKE_WIDTH);
    	mPaint.setColor(FRAME_INNER_COLOR);
        c.drawRect(mBounds, mPaint);
    }

    /**
     * Sets the color of the highlighted bounds.
     *
     * @param color
     */
    public void setColor(int color) {
    	mPaint.setColor(color);
    	invalidate();
    }

}