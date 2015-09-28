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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;


public class Circuit extends ImageView {
    private static final String TAG = Circuit.class.getSimpleName() + "_class";
    private static final boolean debug = AppSettings.defaultDebug;


    private static final int DefaultBoxMargin = 20;  // in px
    private static final int DefaultBlockMargin = 1;  // in px

    private static final int DefaultTextSize_IN_DP = 14;

    private static final int ColorCopper = Color.parseColor("#FFA000");

    private static final int RJunctionTemperature = R.string.label_rjunctionTemp;
    private static final int MJunctionTemperature = R.string.label_mjunctionTemp;
    private static final int VoltmeterReading = R.string.label_voltmeterReading;
    private static final int Isothermal = R.string.label_isothermal;
    private static final int MJunction = R.string.label_mjunction;

    private int mTextsize;
    private RectF boundingBox;   // everything will be placed within the bounding box
    private RectF circuitArea;
    private CircuitElement voltmeter;
    private CircuitElement thermometerBlue;
    private CircuitElement thermometerRed;
    private CircuitElement isothermo1;
    private CircuitElement isothermo2;
    private CircuitElement mjunction;
    private RectF voltmeterBlock;
    private RectF isothermoBlock;
    private RectF mjunctionBlock;


    private boolean isVoltmeterPressed = false;
    private boolean isIsothermoPressed = false;
    private boolean isMJunctionPressed = false;
    private OnElementPressedListener voltmeterListener = null;
    private OnElementPressedListener isothermoListener = null;
    private OnElementPressedListener mjunctionListener = null;


    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public Circuit(Context context) {
        super(context.getApplicationContext());
        init(context.getApplicationContext());
    }


    public Circuit(Context context, AttributeSet attributes) {
        super(context.getApplicationContext(), attributes);
        init(context.getApplicationContext());
    }

    private void init(Context context) {
        thermometerBlue = new CircuitElement(context, R.mipmap.circuit_thermometer_blue, context.getString(RJunctionTemperature));
        thermometerRed = new CircuitElement(context, R.mipmap.circuit_thermometer_red, context.getString(MJunctionTemperature));
        isothermo1 = new CircuitElement(context, R.mipmap.circuit_isothermo, context.getString(Isothermal));
        isothermo2 = new CircuitElement(context, R.mipmap.circuit_isothermo, context.getString(Isothermal));
        voltmeter = new CircuitElement(context, R.mipmap.circuit_voltmeter, context.getString(VoltmeterReading));
        mjunction = new CircuitElement(context, 0, context.getString(MJunction));  // no picture for now

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void cleanup() {
        boundingBox = null;
        circuitArea = null;
        voltmeter = null;
        thermometerBlue = null;
        thermometerRed = null;
        isothermo1 = null;
        isothermo2 = null;
        mjunction = null;
        voltmeterBlock = null;
        isothermoBlock = null;
        mjunctionBlock = null;
        voltmeterListener = null;
        isothermoListener = null;
        mjunctionListener = null;
        this.destroyDrawingCache();
        this.setImageDrawable(null);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Initialize width and height
        int width;
        int height;

        int requiredWidth = ( voltmeter.getWidth() + isothermo1.getWidth() + mjunction.getWidth() + thermometerRed.getWidth() ) * 3;
        int requiredHeight = (int) (voltmeter.getHeight() * 1.8 + DefaultBoxMargin*2);


        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        else width = requiredWidth;

        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(MeasureSpec.getSize(heightMeasureSpec), requiredHeight);
        }
        else height = requiredHeight;

        Savelog.d(TAG, debug, "onMeasure()");

        mTextsize = PixelUtil.dpToPx(getContext(), DefaultTextSize_IN_DP);
        setStationaryComponents(width, height);

        setMeasuredDimension(width, height);
    }


    private void setStationaryComponents(int width, int height) {
        Savelog.d(TAG, debug, "box height=" + height
                + "voltmeter height " + voltmeter.getHeight() + " (px)");

        int boxMargin = DefaultBoxMargin;  // for bounding box
        int blockMargin = DefaultBlockMargin;  // for blocks of elements (used for pressing

        int boxHeight = height;
        int boxWidth = width;

        // First, fix the bounding box
        if (boundingBox == null) {
            boundingBox = new RectF();
        }
        boundingBox.left = 0;
        boundingBox.top = 0;
        boundingBox.right = boxWidth;
        boundingBox.bottom = boxHeight;


        if (circuitArea == null) {
            circuitArea = new RectF();
        }

        circuitArea.left = boundingBox.left + boxMargin;
        circuitArea.top = boundingBox.top + boxMargin;
        circuitArea.right = boundingBox.right - boxMargin;
        circuitArea.bottom = boundingBox.bottom - boxMargin;

        Savelog.d(TAG, debug, "Box size: Width=" + boxWidth + " Height=" + boxHeight);

        voltmeter.setX(circuitArea.left);
        voltmeter.setY(circuitArea.top);

        if (voltmeterBlock==null) {
            voltmeterBlock = new RectF();
        }
        voltmeterBlock.left = voltmeter.left - blockMargin;
        voltmeterBlock.top = voltmeter.top - blockMargin;
        voltmeterBlock.right = voltmeter.right + blockMargin;
        voltmeterBlock.bottom = voltmeter.bottom + blockMargin;



        isothermo1.setX(voltmeter.getX() + voltmeter.width + boxMargin);
        isothermo1.setY(voltmeter.getY() + voltmeter.height/4 - isothermo1.halfHeight);

        isothermo2.setX(isothermo1.getX());
        isothermo2.setY(isothermo1.getY() + voltmeter.height/2);

        thermometerBlue.setX(isothermo1.getX() + boxMargin);
        thermometerBlue.setY(voltmeter.getY());

        if (isothermoBlock==null) {
            isothermoBlock = new RectF();
        }
        isothermoBlock.left = isothermo1.left - blockMargin;
        isothermoBlock.top = Math.min(isothermo1.top, thermometerBlue.top) - blockMargin;
        isothermoBlock.right = thermometerBlue.right + blockMargin;
        isothermoBlock.bottom = isothermo2.bottom + blockMargin;



        thermometerRed.setX(circuitArea.right - thermometerRed.width);
        thermometerRed.setY(voltmeter.getY());

        // location of heat source
        mjunction.setX(thermometerRed.getX());
        mjunction.setY(voltmeter.getCenterY());

        if (mjunctionBlock==null) {
            mjunctionBlock = new RectF();
        }
        mjunctionBlock.left = mjunction.left - thermometerRed.halfWidth - blockMargin; // add extra left margin to this block
        mjunctionBlock.top = thermometerRed.top - blockMargin;
        mjunctionBlock.right = circuitArea.right + blockMargin;
        mjunctionBlock.bottom = mjunction.bottom + thermometerRed.halfHeight + blockMargin; // add extra bottom margin to this block

    }




    private void drawLabels(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(mTextsize);

        // Add a small separation (in px) between two rows of texts
        float delta = 3f;

        // Voltmeter
        try {
            String[] segments = voltmeter.label.split("\\s");
            int count = segments.length;
            for (int i=0; i<count; i++) {
                float textWidth = paint.measureText(segments[i]);
                float start = voltmeter.getCenterX() - textWidth/2;
                canvas.drawText(segments[i], start, circuitArea.bottom-(mTextsize+delta)*(count-i), paint);
            }
        } catch (Exception e) {
            Savelog.d(TAG, debug, "No label");
        }

        // RJunction temperature
        try {
            String[] segments = thermometerBlue.label.split("\\s");
            int count = segments.length;
            for (int i=0; i<count; i++) {
                float textWidth = paint.measureText(segments[i]);
                float start = thermometerBlue.getCenterX() - textWidth/2;
                canvas.drawText(segments[i], start, circuitArea.bottom-(mTextsize+delta)*(count-i), paint);
            }
        } catch (Exception e) {
            Savelog.d(TAG, debug, "No label");
        }

        // Measuring junction
        try {
            String[] segments = thermometerRed.label.split("\\s");
            int count = segments.length;
            for (int i=0; i<count; i++) {
                float textWidth = paint.measureText(segments[i]);
                float start = mjunction.getCenterX() - textWidth/2;
                canvas.drawText(segments[i], start, circuitArea.bottom-(mTextsize+delta)*(count-i), paint);
            }
        } catch (Exception e) {
            Savelog.d(TAG, debug, "No label");
        }

    }




    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (debug) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(boundingBox.left, boundingBox.top, boundingBox.right, boundingBox.bottom, paint);

            paint.setColor(Color.DKGRAY);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(circuitArea.left, circuitArea.top, circuitArea.right, circuitArea.bottom, paint);
        }

        if (isVoltmeterPressed) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.25f);
            canvas.drawRect(voltmeterBlock.left, voltmeterBlock.top, voltmeterBlock.right, voltmeterBlock.bottom, paint);
        }
        if (isIsothermoPressed) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.25f);
            canvas.drawRect(isothermoBlock.left, isothermoBlock.top, isothermoBlock.right, isothermoBlock.bottom, paint);
        }
        if (isMJunctionPressed) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.25f);
            canvas.drawRect(mjunctionBlock.left, mjunctionBlock.top, mjunctionBlock.right, mjunctionBlock.bottom, paint);
        }


        paint.setColor(ColorCopper);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawBitmap(voltmeter.image, voltmeter.getX(), voltmeter.getY(), paint);
        canvas.drawBitmap(isothermo1.image, isothermo1.getX(), isothermo1.getY(), paint);
        canvas.drawBitmap(isothermo2.image, isothermo2.getX(), isothermo2.getY(), paint);

        // Draw copper wires from voltimeter to isothermos
        canvas.drawLine(voltmeter.right, isothermo1.getCenterY(), isothermo1.getX(), isothermo1.getCenterY(), paint);
        canvas.drawLine(voltmeter.right, isothermo2.getCenterY(), isothermo2.getX(), isothermo2.getCenterY(), paint);

        // Measuring junction is a red dot
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mjunction.getCenterX(), mjunction.getCenterY(), 6, paint);

        Path path1 = getPath1();
        Path path2 = getPath2();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawPath(path1, paint);
        canvas.drawPath(path2, paint);

        canvas.drawBitmap(thermometerBlue.image, thermometerBlue.getX(), thermometerBlue.getY(), paint);
        canvas.drawBitmap(thermometerRed.image, thermometerRed.getX(), thermometerRed.getY(), paint);

        drawLabels(canvas);
    }

    private Path getPath1() {
        Path path = new Path();
        path.moveTo(isothermo1.getCenterX(), isothermo1.getCenterY());
        float distX = mjunction.getCenterX() - isothermo1.getCenterX();
        float X1 = isothermo1.getCenterX() + distX/3;
        float X2 = isothermo1.getCenterX() + distX/3*2;
        float X3 = mjunction.getCenterX();
        float Y1 = isothermo1.getCenterY() * 1.7f;
        float Y2 = isothermo1.getCenterY() * 0.9f;
        float Y3 = mjunction.getCenterY();

        path.cubicTo(X1, Y1, X2, Y2, X3, Y3);
        return path;
    }

    private Path getPath2() {
        Path path = new Path();
        path.moveTo(isothermo2.getCenterX(), isothermo2.getCenterY());
        float distX = mjunction.getCenterX() - isothermo2.getCenterX();
        float X1 = isothermo2.getCenterX() + distX/3;
        float X2 = isothermo2.getCenterX() + distX/3*2;
        float X3 = mjunction.getCenterX();
        float Y1 = isothermo2.getCenterY() * 1.3f;
        float Y2 = isothermo2.getCenterY() * 0.7f;
        float Y3 = mjunction.getCenterY();

        path.cubicTo(X1, Y1, X2, Y2, X3, Y3);
        return path;
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }


        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                Savelog.d(TAG, debug, "motion event: action_down");

                isVoltmeterPressed = isEventInVoltmeter(event);
                isIsothermoPressed = isEventInIsothermos(event);
                isMJunctionPressed = isEventInMJunction(event);

                // Only handle up presses.
                if (!isVoltmeterPressed && !isIsothermoPressed && !isMJunctionPressed) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);

                invalidate();

                break;

            case MotionEvent.ACTION_UP:
                Savelog.d(TAG, debug, "motion event: action_up");

                setPressed(false);

                if (isVoltmeterPressed && isEventInVoltmeter(event)) {
                    if (voltmeterListener!=null)
                        voltmeterListener.onPressed();
                }
                else if (isIsothermoPressed && isEventInIsothermos(event)) {
                    if (isothermoListener!=null)
                        isothermoListener.onPressed();
                }
                else if (isMJunctionPressed && isEventInMJunction(event)) {
                    if (mjunctionListener!=null)
                        mjunctionListener.onPressed();
                }

                isVoltmeterPressed = false;
                isIsothermoPressed = false;
                isMJunctionPressed = false;

                invalidate();

                break;

            case MotionEvent.ACTION_CANCEL:
                Savelog.d(TAG, debug, "motion event: action_cancel");

                isVoltmeterPressed = false;
                isIsothermoPressed = false;
                isMJunctionPressed = false;

                setPressed(false);

                invalidate();

                break;
        }
        return true;
    }


    private boolean isEventInVoltmeter(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Fine-tune the area that is accepted as a valid event
        // Do not use the right boundary of the voltmeter block as the right boundary.
        // Instead, give more room to the isothermo block
        if (touchX >= voltmeterBlock.left  && touchX <= voltmeter.right
                && touchY >= voltmeterBlock.top && touchY <= circuitArea.bottom)
            return true;
        else
            return false;
    }

    private boolean isEventInIsothermos(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Fine-tune the area that is accepted as a valid event
        // Do not start at the left boundary of the isothermo block.
        // Instead, start at the right boundary of the voltmeter itself.
        if (touchX >= voltmeter.right  && touchX <= isothermoBlock.right
                && touchY >= isothermoBlock.top && touchY <= circuitArea.bottom)
            return true;
        else
            return false;
    }

    private boolean isEventInMJunction(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Fine-tune the area that is accepted as a valid event
        if (touchX >= mjunctionBlock.left  && touchX <= mjunctionBlock.right
                && touchY >= thermometerRed.top && touchY <= circuitArea.bottom)
            return true;
        else
            return false;
    }

    public interface OnElementPressedListener {
        public void onPressed();

    }

    public void setOnVoltmeterPressedListener(OnElementPressedListener listener) {
        this.voltmeterListener = listener;
    }
    public void setOnIsothermoPressedListener(OnElementPressedListener listener) {
        this.isothermoListener = listener;
    }
    public void setOnMJunctionPressedListener(OnElementPressedListener listener) {
        this.mjunctionListener = listener;
    }


}