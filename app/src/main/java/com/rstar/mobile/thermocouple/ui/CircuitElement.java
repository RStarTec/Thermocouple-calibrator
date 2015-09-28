/*
 * Copyright (c) 2015 Annie Hui @ RStar Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rstar.mobile.thermocouple.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

public class CircuitElement {
    public final Bitmap image;
    public final float width;
    public final float height;
    public final float halfWidth;
    public final float halfHeight;
    public PointF position;
    public float left;
    public float top;
    public float right;
    public float bottom;

    public String label;

    public CircuitElement(Context context, int imageId, String label) {
        if (imageId!=0) {
            image = BitmapFactory.decodeResource(context.getResources(), imageId);
            width = image.getWidth();
            height = image.getHeight();
        }
        else {
            image = null;
            width = 0;
            height = 0;
        }
        if (label!=null) this.label = label;
        else this.label = "";

        halfWidth = 0.5f * width;
        halfHeight = 0.5f * height;
        position = new PointF(0, 0);  // top right corner
    }

    public int getHeight() {
        if (image==null) return 0;
        else return image.getHeight();
    }

    public int getWidth() {
        if (image==null) return 0;
        else return image.getWidth();
    }


    public float getX() {
        return position.x;
    }  // x-coord of top right corner

    public float getY() {
        return position.y;
    }  // y-coord of top right corner

    public void setX(float x) {
        position.x = x;
        left = x;
        right = x + width;
    }

    public void setY(float y) {
        position.y = y;
        top = y;
        bottom = y + height;
    }

    public float getCenterX() {
        return position.x + halfWidth;
    }

    public float getCenterY() {
        return position.y + halfHeight;
    }

}
