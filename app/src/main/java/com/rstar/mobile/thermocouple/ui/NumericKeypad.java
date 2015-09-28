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
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

public class NumericKeypad {
    private static final String TAG = NumericKeypad.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int ButtonTextColorId = R.color.text_light;

    public static final int MaxInputLength = 8;

    public static final String one = "1";
    public static final String two = "2";
    public static final String three = "3";
    public static final String four = "4";
    public static final String five = "5";
    public static final String six = "6";
    public static final String seven = "7";
    public static final String eight = "8";
    public static final String nine = "9";
    public static final String zero = "0";
    public static final String dot = ".";
    public static final String minus = "-";
    public static final String clr = "CLR";
    public static final String del = "DEL";
    public static final String ok = "OK";

    private static final String numericKey[] = {one, two, three, four, five, six, seven, eight, nine, dot, zero, minus, clr, del, ok};


    public static class Adapter extends BaseAdapter {
        View.OnClickListener onKeyClickListener;
        private Context appContext;
        Fragment hostFragment;

        public Adapter(Fragment hostFragment) {
            // This class of objects does not outlive its host, so no need to use weak references
            appContext = hostFragment.getActivity().getApplicationContext();
            this.hostFragment = hostFragment;
            Savelog.d(TAG, debug, "size of keypad = " + numericKey.length);
        }

        public void setOnKeyClickListener(View.OnClickListener onKeyClickListener) {
            this.onKeyClickListener = onKeyClickListener;
        }

        public int getCount() {
            return numericKey.length;
        }

        public Object getItem(int position) {
            return numericKey[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Button button;
            if (convertView == null) {
                // new
                button = new Button(appContext);
                String singleKey = numericKey[position];
                button.setTag(singleKey);
                button.setTextColor(hostFragment.getResources().getColor(ButtonTextColorId));

                // One listener to be shared by all on this adapter
                button.setOnClickListener(onKeyClickListener);

            } else {
                // recycled
                button = (Button) convertView;
            }

            // Now adjust color. Need to do this for both new views and recycled views
            button.setBackgroundResource(R.drawable.keypad_button);
            button.setText(numericKey[position]);
            return button;
        }
        public void cleanup() {
            hostFragment = null;
        }
    }



    public static abstract class OnKeyClickListener implements View.OnClickListener {
        String buffer = "";

        public OnKeyClickListener(String buffer) {
            if (buffer!=null)
                this.buffer = buffer;
        }

        @Override
        public void onClick(View v) {
            String key = (String) v.getTag();

            // In cases where the string in the buffer may be lengthened, check limit first.
            if (key.equals(NumericKeypad.minus)) {
                if (buffer.length()<MaxInputLength) {
                    if (buffer.length() > 0 && buffer.charAt(0) == '-') {
                        buffer = buffer.substring(1); // trim of existing negative sign
                    } else {
                        buffer = "-" + buffer; // add a new negative sign
                    }
                }  //Else: Do nothing when buffer has reached its maximum
            } else if (key.equals(NumericKeypad.dot)) {
                if (buffer.length()<MaxInputLength && !buffer.contains(".")) {
                    buffer += "."; // add a point, if there isn't one already
                }
            } else if (key.equals(NumericKeypad.clr)) {
                if (buffer.length() > 0) {
                    buffer = "";
                }
            } else if (key.equals(NumericKeypad.del)) {
                int length = buffer.length();
                if (length > 0) {
                    buffer = buffer.substring(0, length - 1);
                }
            } else if (key.equals(NumericKeypad.ok)) {
                onOk();
            } else if (buffer.length()<MaxInputLength) {
                buffer += key;
            }
            Savelog.d(TAG, debug, "pressed " + key);

            onClickCompleted();
        }

        public String getBuffer() {
            return buffer;
        }

        abstract public void onOk();

        abstract public void onClickCompleted();

    }


}
