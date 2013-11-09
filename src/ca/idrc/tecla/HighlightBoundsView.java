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
package ca.idrc.tecla;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Handles drawing a frame on-screen.
 */
public class HighlightBoundsView extends View {

    private Rect mBounds = new Rect();
    private Paint mPaint = new Paint();

    /**
     * Constructs a new highlight bounds view using the specified attributes.
     *
     * @param context The parent context.
     * @param attrs The view attributes.
     */
    public HighlightBoundsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeJoin(Join.ROUND);

    }

    @Override
    public void onDraw(Canvas c) {
    	c.getClipBounds(mBounds);
    	mBounds.inset(Math.round(mPaint.getStrokeWidth()), Math.round(mPaint.getStrokeWidth()));
        c.drawRect(mBounds, mPaint);
    }

    /**
     * Sets the color of the highlighted bounds.
     *
     * @param color
     */
    public void setHighlightColor(int color) {
    	mPaint.setColor(color);
    	invalidate();
    }

    /**
     * Sets the stroke width of the highlighted bounds.
     *
     * @param color
     */
    public void setStrokeWidth(float width) {
        mPaint.setStrokeWidth(width);
    	invalidate();
    }

}