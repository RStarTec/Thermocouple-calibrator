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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;


public class BlankFragment extends Fragment {
    private static final String TAG = BlankFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title = "Blank";

    public static final String EXTRA_typeCode = BlankFragment.class.getSimpleName()+".typeCode";
    private static final String defaultTypeCode = ThermoCouple.all;

    private String mTypeCode;
    private ThermoCouple mThermoCouple;

    public static BlankFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("")) typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        BlankFragment fragment = new BlankFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);

        mThermoCouple = new ThermoCouple(getActivity());

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blank, parent, false);

        TextView textView = (TextView) v.findViewById(R.id.fragmentBlank_message);
        textView.setText("Blank");

        return v;
    }
}
