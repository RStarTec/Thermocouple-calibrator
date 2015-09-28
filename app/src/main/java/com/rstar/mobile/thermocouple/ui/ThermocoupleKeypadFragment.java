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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.activities.TypespecificActivity;


public class ThermocoupleKeypadFragment extends Fragment {
    private static final String TAG = ThermocoupleKeypadFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int button_ids[] = {
                R.id.fragmentTcKeypad_button1,
                R.id.fragmentTcKeypad_button2,
                R.id.fragmentTcKeypad_button3,
                R.id.fragmentTcKeypad_button4,
                R.id.fragmentTcKeypad_button5,
                R.id.fragmentTcKeypad_button6,
                R.id.fragmentTcKeypad_button7,
                R.id.fragmentTcKeypad_button8
    };

    private static final int size = ThermoCouple.size;
    private final Button[] mButtons = new Button[size];
    private final OnKeyClickListener[] mOnKeyClickListeners = new OnKeyClickListener[size];

    public static ThermocoupleKeypadFragment newInstance() {
        Bundle args = new Bundle();

        ThermocoupleKeypadFragment fragment = new ThermocoupleKeypadFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tc_keypad, parent, false);


        LinearLayout keypadPanel = (LinearLayout) v.findViewById(R.id.fragmentTcKeypad_id);

        for (int index=0; index<size; index++) {
            // Prepare listener. May reuse if available.
            if (mOnKeyClickListeners[index]==null)
                mOnKeyClickListeners[index] = new OnKeyClickListener(this);

            mButtons[index] = (Button) keypadPanel.findViewById(button_ids[index]);
            mButtons[index].setText(ThermoCouple.type[index]);
            mButtons[index].setTag(ThermoCouple.type[index]);
            mButtons[index].setOnClickListener(mOnKeyClickListeners[index]);
        }

        return v;
    }


    @Override
    public void onDestroyView() {
        for (int index=0; index<size; index++) {
            if (mButtons[index]!=null)
                mButtons[index].setOnClickListener(null);
            mButtons[index] = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        for (int index=0; index<size; index++) {
            if (mOnKeyClickListeners[index] != null)
                mOnKeyClickListeners[index].cleanup();
            mOnKeyClickListeners[index] = null;
        }
        super.onDestroy();
    }

    private static class OnKeyClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        ThermocoupleKeypadFragment hostFragment;
        public OnKeyClickListener(ThermocoupleKeypadFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            String type = (String) button.getTag();

            if (debug) {
                Toast.makeText(hostFragment.getActivity(), "Type " + type, Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(hostFragment.getActivity(), TypespecificActivity.class);
            intent.putExtra(TypespecificActivity.EXTRA_typeCode, type);
            hostFragment.startActivity(intent);
        }
        public void cleanup() { hostFragment = null; }
    }

}
