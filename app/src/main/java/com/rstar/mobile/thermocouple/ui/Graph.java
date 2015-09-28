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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;


public class Graph extends ImageView {
    private static final String TAG = Graph.class.getSimpleName() + "_class";
    private static final boolean debug = AppSettings.defaultDebug;

    // Some recommended colors
    public static final int standardColors[] = {Color.parseColor("#C2185B"), Color.parseColor("#448AFF"),
            Color.parseColor("#F84B80"), Color.parseColor("#895548"),
            Color.parseColor("#009688"), Color.parseColor("#00BCD4"),
            Color.parseColor("#ffc107"), Color.parseColor("#8BC34A")};


    private static final int BOX_MINWIDTH_IN_DP = 200;
    private static final int BOX_MINHEIGHT_IN_DP = 200;
    private static final float Box_Margin_Ratio = 1.58f;  // define the margin as a ratio of the thumb's height


    private static final int DefaultTextSize_IN_DP = 14;
    private static final int DefaultVerticalBarColor = Color.parseColor("#4E4E4E");
    private static final int DefaultCurveColor = Color.RED;
    private static final int DefaultCurveThickness = 3;
    private static final int DefaultLineThickness = 1;
    private static final int ControlPointRadius = 2;


    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mTextsize;
    private RectF boundingBox;   // everything will be placed within the bounding box
    private RectF chartArea;
    private RectF verticalBar;
    private PointF thumbCoords;
    private Thumb thumb;

    private float mThumbCenterCoordX = 0;
    private boolean notifyWhileDragging = false;
    private OnVerticalBarChangeListener listener;

    // The actual limit of the function to be plotted
    private static final double tolerance = ThermoCouple.tolerance;

    private double FXmin;
    private double FXmax;
    private double FYmin;
    private double FYmax;
    private PointF[][] mCurves;
    private int[] mCurveColors;
    private String[] mCurveLabels;
    private String[] mAxesLabels;
    private VerticalBarMarker mMarker = null;
    private boolean mShowControlPoints = false;

    public Graph(Context context) {
        super(context);
        init(context);
    }


    public Graph(Context context, AttributeSet attributes) {
        super(context, attributes);
        init(context);
    }

    private void init(Context context) {
        thumb = new Thumb(context, R.mipmap.seek_pointer_up, R.mipmap.seek_pointer_up);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Initialize width and height
        int width;
        int height;

        width = PixelUtil.dpToPx(getContext(), BOX_MINWIDTH_IN_DP);
        height = PixelUtil.dpToPx(getContext(), BOX_MINHEIGHT_IN_DP);
        Savelog.d(TAG, debug, "onMeasure()");

        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = View.MeasureSpec.getSize(heightMeasureSpec);
        }

        mTextsize = PixelUtil.dpToPx(getContext(), DefaultTextSize_IN_DP);
        setStationaryComponents(width, height);

        setThumbPositionByMarker();
        setMeasuredDimension(width, height);
    }


    private void setStationaryComponents(int width, int height) {
        int squareWidth = width > height ? height : width;

        // First, fix the bounding box
        if (boundingBox == null) {
            boundingBox = new RectF();
        }
        boundingBox.left = 0;
        boundingBox.top = 0;
        boundingBox.right = squareWidth;
        boundingBox.bottom = squareWidth;

        int margin = getBoxMargin();

        if (chartArea == null) {
            chartArea = new RectF();
        }
        chartArea.left = boundingBox.left + margin;
        chartArea.top = boundingBox.top + margin;
        chartArea.right = boundingBox.right - margin;
        chartArea.bottom = boundingBox.bottom - margin;

    }


    // This is called by the graph itself onMeasure.
    // When the hostFragment is reusing an existing graph on new data type
    // But this must also be explicitly called by hostFragment after setting a new mMarker
    public void setThumbPositionByMarker() {
        // Make sure that the thumb is not outside the chart area
        if (mMarker!=null) {
            // The chart area is already defined. So it's safe to use it.
            mThumbCenterCoordX = getFXtoCoordX(mMarker.FX);
            Savelog.d(TAG, debug, "Using marker FX=" + mMarker.FX);
        }
        else if (mThumbCenterCoordX < chartArea.left || mThumbCenterCoordX > chartArea.right) {
            mThumbCenterCoordX = chartArea.centerX();
        }
        setThumbPosition(); // Then make sure to relocate the bitmap and the bar
    }


    // Once the mThumbCenterCoordX is set,
    // call this to relocate thumb image and vertical bar
    private void setThumbPosition() {

        if (thumbCoords == null) {
            thumbCoords = new PointF();
        }
        thumbCoords.x = mThumbCenterCoordX;
        thumbCoords.y = chartArea.bottom;

        if (verticalBar == null) {
            verticalBar = new RectF();
        }
        verticalBar.left = thumbCoords.x - (float) 0.1; // provide thickness to the bar by adding a small delta
        verticalBar.top = chartArea.top;
        verticalBar.right = thumbCoords.x + (float) 0.1; // provide thickness to the bar by adding a small delta
        verticalBar.bottom = chartArea.bottom;
    }



    private int getBoxMargin() {
        return (int)(thumb.height * Box_Margin_Ratio);
    }


    private void drawThumb(Canvas canvas) {
        Bitmap thumbToDraw;
        float screenCoordX = thumbCoords.x - thumb.halfWidth;  // thumb's xcoord is its center in X-dimension
        float screenCoordY = thumbCoords.y;                    // thumb's ycoord is its top
        Savelog.d(TAG, debug, "drawing thumb at x=" + screenCoordX + " y=" + screenCoordY);

        thumbToDraw = thumb.image;
        canvas.drawBitmap(thumbToDraw, screenCoordX, screenCoordY, paint);
        paint.setColor(DefaultVerticalBarColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(DefaultLineThickness);
        canvas.drawRect(verticalBar, paint);


        paint.setTextSize(mTextsize);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        // Get the thumb's position. But remember to translate it to screen coordinates
        float coordX = thumbCoords.x;
        float coordY = thumbCoords.y + thumb.heightBound;

        double FXthumb = getCoordXtoFX(coordX);
        String labelThumb = String.format("%.2f", FXthumb);

        float labelSize = paint.measureText(labelThumb);
        float textOffset = thumb.halfHeight * 0.6f;

        canvas.drawText(labelThumb, coordX - labelSize / 2, coordY - textOffset, paint);

    }


    private void drawLimitLabels(Canvas canvas) {
        String labelFXmin = String.format("%.2f", FXmin);
        String labelFXmax = String.format("%.2f", FXmax);
        String labelFYmin = String.format("%.2f", FYmin);
        String labelFYmax = String.format("%.2f", FYmax);

        final int textOffset = mTextsize;
        paint.setTextSize(mTextsize);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        float labelSize;

        canvas.drawText(labelFXmin, chartArea.left, chartArea.bottom + textOffset, paint);

        labelSize = paint.measureText(labelFXmax);
        canvas.drawText(labelFXmax, chartArea.right - labelSize, chartArea.bottom + textOffset, paint);

        // provide a small separation from the chart border by adding a small delta
        float delta = 3f;
        labelSize = paint.measureText(labelFYmin);
        canvas.drawText(labelFYmin, chartArea.left - labelSize - delta, chartArea.bottom, paint);

        // provide a small separation from the chart border by adding a small delta
        labelSize = paint.measureText(labelFYmax);
        canvas.drawText(labelFYmax, chartArea.left - labelSize - delta, chartArea.top + textOffset, paint);

        // Make sure the axis labels don't get out of the bounding box
        float start;

        String labelAxisX = mAxesLabels[0];
        labelSize = paint.measureText(labelAxisX);
        start = chartArea.right + textOffset;
        if (start+labelSize>=boundingBox.right) start = boundingBox.right - labelSize - delta;
        canvas.drawText(labelAxisX, start, chartArea.bottom + textOffset, paint);

        String labelAxisY = mAxesLabels[1];
        labelSize = paint.measureText(labelAxisY);
        start = chartArea.left - labelSize;
        if (start<=boundingBox.left) start = boundingBox.left + delta;
        canvas.drawText(labelAxisY, start, chartArea.top - textOffset, paint);
    }


    private void drawGraphs(Canvas canvas) {
        if (mCurves != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(DefaultCurveThickness); // draw curves with thicker lines

            // First draw the curves
            for (int index = 0; index < mCurves.length; index++) {
                if (mCurveColors == null)
                    paint.setColor(DefaultCurveColor);
                else
                    paint.setColor(mCurveColors[index]);
                paint.setStyle(Paint.Style.STROKE);

                Path path = getCurvePath(index);
                canvas.drawPath(path, paint);
            }

            // now put the labels
            if (mCurveLabels!=null && mCurveLabels.length!=0) {

                paint.setTextSize(mTextsize);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);

                float[] labelX = new float[mCurveLabels.length];
                float[] labelY = new float[mCurveLabels.length];
                float[] labelHalfWidth = new float[mCurveLabels.length];
                float labelHalfHeight = mTextsize / 2;
                String[] label = new String[mCurveLabels.length];

                // First, align the labels
                for (int index = 0; index < mCurveLabels.length; index++) {
                    if (mCurveLabels[index].length() > 0) {
                        label[index] = String.format("%s", mCurveLabels[index]);

                        // Get the last point. But remember to translate it to screen coordinates
                        int lastPoint = mCurves[index].length - 1;
                        labelX[index] = getFXtoCoordX(mCurves[index][lastPoint].x);
                        labelY[index] = getFYtoCoordY(mCurves[index][lastPoint].y);

                        labelHalfWidth[index] = paint.measureText(label[index]) / 2;

                        float centerXi = labelX[index] + labelHalfWidth[index];
                        float centerYi = labelY[index] - labelHalfHeight;

                        for (int j=0; j<index; j++) {
                            float centerXj = labelX[j] + labelHalfWidth[j];
                            float centerYj = labelY[j] - labelHalfHeight;

                            // find overlap

                            // Distance between two centers
                            float deltaX = centerXi-centerXj;
                            float deltaY = centerYi-centerYj;

                            // minimum separation so that there is no overlap
                            float optimalDX = labelHalfWidth[index] + labelHalfWidth[j];
                            float optimalDY = labelHalfHeight * 2;

                            // There is an overlap
                            if (Math.abs(deltaX)<optimalDX && Math.abs(deltaY)<optimalDY) {
                                if (deltaX<0) {  // Xi is before Xj
                                    labelX[index] -= optimalDX/2;
                                    labelX[j] += optimalDX/2;
                                } else {  // Xi is after Xj
                                    labelX[index] += optimalDX/2;
                                    labelX[j] -= optimalDX/2;
                                }

                                if (deltaY<0) {   // Yi is before Yj
                                    labelY[index] -= optimalDY / 2;
                                    labelY[j] += optimalDY / 2;
                                } else {   // Yi is after Yj
                                    labelY[index] += optimalDY / 2;
                                    labelY[j] -= optimalDY / 2;
                                }
                            }
                        }
                    }
                }

                // Next, put the labels
                for (int index = 0; index < mCurveLabels.length; index++) {
                    canvas.drawText(label[index], labelX[index], labelY[index], paint);
                }
            }
        }
    }


    private void drawMarkersAtVerticalBar(Canvas canvas) {
        boolean left = true;
        if (mMarker!=null && mMarker.FYs != null) {
            float coordX = thumbCoords.x;

            for (int index = 0; index < mMarker.FYs.length; index++) {
                if (mMarker.FYs[index]>=FYmin && mMarker.FYs[index]<=FYmax) {
                    // Translate function's FY value to screen coordY
                    float coordY = getFYtoCoordY(mMarker.FYs[index]);
                    String labelFY = String.format("%.2f", mMarker.FYs[index]);

                    Savelog.d(TAG, debug, "Y:" + labelFY);

                    float labelSize = paint.measureText(labelFY);

                    if (left)
                        canvas.drawText(labelFY, coordX - labelSize, coordY, paint);
                    else
                        canvas.drawText(labelFY, coordX, coordY, paint);
                    left = !left;
                }
                else {
                    // FY is out of range. Ignore it.
                }
            }
        }
    }




    private void drawControlPoints(Canvas canvas) {
        if (mShowControlPoints) {
            if (mCurves != null) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(DefaultCurveThickness); // draw curves with thicker lines

                // First draw the curves
                for (int index = 0; index < mCurves.length; index++) {
                    if (mCurveColors == null)
                        paint.setColor(DefaultCurveColor);
                    else
                        paint.setColor(mCurveColors[index]);

                    paint.setStyle(Paint.Style.STROKE);

                    PointF[] curve = mCurves[index];
                    if (curve == null || curve.length == 0) return;

                    // Use delta to control the increment. In case we have too many control points,
                    // set delta=3 to draw one every three points.
                    int delta = 3;
                    for (int point = 1; point <= curve.length - delta; point += delta) {
                        float coordX = getFXtoCoordX(curve[point].x);
                        float coordY = getFYtoCoordY(curve[point].y);

                        canvas.drawCircle(coordX, coordY, ControlPointRadius, paint);
                    }

                }
            }
        }
    }




    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Savelog.d(TAG, debug, "onDraw()");
        paint.setStrokeWidth(DefaultLineThickness);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(boundingBox, paint);
        canvas.drawRect(chartArea, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRect(chartArea, paint);

        drawLimitLabels(canvas);

        drawGraphs(canvas);
        drawControlPoints(canvas);

        drawThumb(canvas);
        drawMarkersAtVerticalBar(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        boolean isEventInChart = isEventInChart(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                Savelog.d(TAG, debug, "motion event: action_down");

                // Only handle upThumb presses.
                if (!isEventInChart) {
                    return super.onTouchEvent(event);
                }

                trackTouchEvent(event);
                setPressed(true);

                invalidate();
                attemptClaimDrag();
                break;

            case MotionEvent.ACTION_MOVE:
                Savelog.d(TAG, debug, "motion event: action_move");
                if (isEventInChart) {
                    trackTouchEvent(event);
                    invalidate();

                    if (notifyWhileDragging && listener != null) {
                        notifyListener(event);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                Savelog.d(TAG, debug, "motion event: action_up");

                setPressed(false);

                invalidate();
                if (listener != null) {
                    notifyListener(event);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                Savelog.d(TAG, debug, "motion event: action_cancel");

                setPressed(false);
                invalidate();
                break;
        }
        return true;
    }

    /**
     * Tries to claim the user's drag motion, and
     * request disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private boolean isEventInChart(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Cover the whole chart (with a little margin) and include the thumb at the bottom of it.
        if (touchX >= chartArea.left - thumb.halfWidth && touchX <= chartArea.right + thumb.halfWidth
                && touchY >= chartArea.top && touchY <= chartArea.bottom + thumb.heightBound)
            return true;
        else
            return false;
    }

    private void trackTouchEvent(MotionEvent event) {
        float touchX = event.getX();

        if (touchX < chartArea.left) touchX = chartArea.left;
        if (touchX > chartArea.right) touchX = chartArea.right;

        mThumbCenterCoordX = touchX;
        setThumbPosition();
        Savelog.d(TAG, debug, "x at " + mThumbCenterCoordX);
    }

    private void notifyListener(MotionEvent event) {
        listener.onVerticalBarValuesChanged(this, getCoordXtoFX(mThumbCenterCoordX));
    }

    public interface OnVerticalBarChangeListener {
        public void onVerticalBarValuesChanged(Graph graph, double x);
    }

    public void setOnVerticalBarChangeListener(OnVerticalBarChangeListener listener) {
        this.listener = listener;
    }



    private double getCoordXtoFX(double coordX) {
        if (coordX <= chartArea.left) return FXmin;
        if (coordX >= chartArea.right) return FXmax;
        float chartWidth = chartArea.right - chartArea.left;
        return (coordX - chartArea.left) / chartWidth * (FXmax - FXmin) + FXmin;
    }

    private float getFXtoCoordX(double fx) {
        if (fx <= FXmin) return chartArea.left;
        if (fx >= FXmax) return chartArea.right;
        float chartWidth = chartArea.right - chartArea.left;
        return (float) ((fx - FXmin) / (FXmax - FXmin)) * chartWidth + chartArea.left;
    }

    private float getFYtoCoordY(double fy) {
        if (fy <= FYmin) return chartArea.bottom;
        if (fy >= FYmax) return chartArea.top;
        float chartHeight = chartArea.bottom - chartArea.top;
        return chartArea.bottom - (float) ((fy - FYmin) / (FYmax - FYmin)) * chartHeight;
    }

    private Path getCurvePath(int curveIndex) {
        Path path = new Path();
        path.reset();

        if (mCurves == null) return path;
        if (curveIndex < 0 || curveIndex >= mCurves.length) return path;
        PointF[] curve = mCurves[curveIndex];
        if (curve == null || curve.length == 0) return path;

        float coordX1, coordY1;
        float coordX2, coordY2;
        float coordX3, coordY3;

        coordX1 = getFXtoCoordX(curve[0].x);
        coordY1 = getFYtoCoordY(curve[0].y);

        path.moveTo(coordX1, coordY1);

        int last = 0;
        for (int index = 1; index <= curve.length - 3; index += 3) {
            coordX1 = getFXtoCoordX(curve[index].x);
            coordY1 = getFYtoCoordY(curve[index].y);
            coordX2 = getFXtoCoordX(curve[index + 1].x);
            coordY2 = getFYtoCoordY(curve[index + 1].y);
            coordX3 = getFXtoCoordX(curve[index + 2].x);
            coordY3 = getFYtoCoordY(curve[index + 2].y);

            last = index + 2;
            path.cubicTo(coordX1, coordY1, coordX2, coordY2, coordX3, coordY3);
        }

        // Append whatever remaining points to the curve
        if (last < curve.length - 1) {
            for (int index = last + 1; index < curve.length; index++) {
                coordX1 = getFXtoCoordX(curve[index].x);
                coordY1 = getFYtoCoordY(curve[index].y);
                path.lineTo(coordX1, coordY1);

            }
        }
        return path;
    }


    private void clearCurves() {
        FXmin = 0x0.0p0;
        FXmax = 0x0.0p0;
        FYmin = 0x0.0p0;
        FYmax = 0x0.0p0;
        mCurves = null;
        mCurveColors = null;
        mCurveLabels = null;
        mAxesLabels = null;
    }

    private void setChartAreaLimits(double FXmin, double FXmax, double FYmin, double FYmax) throws Exception {
        if (FXmin >= FXmax || Math.abs(FXmax - FXmin) < tolerance
                || FYmin >= FYmax || Math.abs(FYmax - FYmin) < tolerance) {
            Savelog.e(TAG, "Bad chart area limit: FXmin=" + FXmin + " FXmax=" + FXmax + " FYmin=" + FYmin + " FYmax=" + FYmax);
            throw new Exception("Bad chart area limit");
        } else {
            this.FXmin = FXmin;
            this.FXmax = FXmax;
            this.FYmin = FYmin;
            this.FYmax = FYmax;
        }
    }

    public void setCurves(double FXmin, double FXmax, double FYmin, double FYmax, PointF[][] curves) {
        try {
            setChartAreaLimits(FXmin, FXmax, FYmin, FYmax);
            mCurves = curves;
            mCurveColors = null;
        } catch (Exception e) {
            clearCurves();
        }
    }

    public void setCurveColors(int colors[]) {
        if (colors != null) {
            if (mCurves != null) {
                mCurveColors = new int[mCurves.length];
                // Make sure that the colors provided are enough.
                // Substitute with default color if necessary
                for (int index = 0; index < mCurveColors.length; index++) {
                    if (index >= colors.length) mCurveColors[index] = DefaultCurveColor;
                    else mCurveColors[index] = colors[index];
                }
            }
        }
    }


    public void setCurveLabels(String curveLabels[]) {
        if (curveLabels != null) {
            if (mCurves != null) {
                mCurveLabels = new String[mCurves.length];
                // Make sure that the labels provided are enough.
                // Substitute with empty labels if necessary
                for (int index = 0; index < mCurveLabels.length; index++) {
                    if (index >= curveLabels.length) mCurveLabels[index] = "";
                    else mCurveLabels[index] = curveLabels[index];
                }
            }
        }
    }

    public void setAxesLabels(String axesLabelX, String axesLabelY) {
        if (axesLabelX != null && axesLabelY != null) {
            mAxesLabels = new String[2];
            mAxesLabels[0] = axesLabelX;
            mAxesLabels[1] = axesLabelY;
        }
    }



    public void setNotifyWhileDragging(boolean value) {
        notifyWhileDragging = value;
    }


    public void setVerticalBarMarker(VerticalBarMarker marker) {
        mMarker = marker;
    }


    public void showControlPoints(boolean show) {
        mShowControlPoints = show;
    }

    public void printCurves() {
        if (mCurves != null) {
            for (int index = 0; index < mCurves.length; index++) {
                if (mCurves[index] != null) {
                    for (int p = 0; p < mCurves[index].length; p++) {
                        Savelog.d(TAG, debug, "(" + mCurves[index][p].x + ", " + mCurves[index][p].y + ")");
                    }
                }
            }
        }
    }

    public void cleanup() {
        clearCurves();
        mMarker = null;
        listener = null;
        boundingBox = null;
        chartArea = null;
        verticalBar = null;
        thumbCoords = null;
        thumb = null;
    }

    public static class VerticalBarMarker {
        public double FX;
        public double FYs[];
    }
}