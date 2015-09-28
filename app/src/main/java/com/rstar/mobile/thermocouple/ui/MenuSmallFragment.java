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

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.activities.ToolActivity;


public class MenuSmallFragment extends Fragment {
    private static final String TAG = MenuSmallFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title[] = {"Tools", "Graphs", "Downloads"};

    public static final String EXTRA_tabNumber = MenuSmallFragment.class.getSimpleName()+".tabNumber";

    private static final int taskType[][] = {
            {ToolActivity.task_circuit, ToolActivity.task_formula},
            {ToolActivity.task_graph, ToolActivity.task_seebeck},
            {ToolActivity.task_sourcecode, ToolActivity.task_database}
    };

    private static final String panelLabel[][] = {
            {CircuitFragment.title, FormulaFragment.title},
            {GraphFragment.title, SeebeckFragment.title},
            {SourcecodeFragment.title, DatabaseFragment.title}
    };
    private static final int image_ids[][] = {
            {R.drawable.circuit_sml, R.drawable.formula_sml},
            {R.drawable.graph_sml, R.drawable.seebeck_sml},
            {R.drawable.sourcecode_sml, R.drawable.database_sml}
    };

    private static final int NumberOfTabs = taskType.length;

    private static final int panel_ids[][] = {
            {R.id.fragmentMenu_panel1, R.id.fragmentMenu_label1, R.id.fragmentMenu_button1},
            {R.id.fragmentMenu_panel2, R.id.fragmentMenu_label2, R.id.fragmentMenu_button2},
    };

    private int mTabNumber;
    private static final int size = panel_ids.length;
    private final LinearLayout[] mPanels = new LinearLayout[size];
    private final OnKeyClickListener[] mOnKeyClickListeners = new OnKeyClickListener[size];


    private static final String defaultTypeCode = ThermoCouple.all;

    public static MenuSmallFragment newInstance(int tabNumber) {
        Bundle args = new Bundle();

        if (tabNumber<0 || tabNumber>= NumberOfTabs) tabNumber=0;
        args.putInt(EXTRA_tabNumber, tabNumber);

        MenuSmallFragment fragment = new MenuSmallFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTabNumber = getArguments().getInt(EXTRA_tabNumber);

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu_small, parent, false);

        LinearLayout keypadPanel = (LinearLayout) v.findViewById(R.id.fragmentMenu_id);

        for (int index=0; index<size; index++) {
            // Prepare listener. May reuse if available.
            if (mOnKeyClickListeners[index]==null)
                mOnKeyClickListeners[index] = new OnKeyClickListener(this, index);

            // Hook up panels
            mPanels[index] = (LinearLayout) keypadPanel.findViewById(panel_ids[index][0]);
            mPanels[index].setOnClickListener(mOnKeyClickListeners[index]);

            TextView textView = (TextView) keypadPanel.findViewById(panel_ids[index][1]);
            textView.setText(panelLabel[mTabNumber][index]);
            ImageButton imageButton = (ImageButton) keypadPanel.findViewById(panel_ids[index][2]);

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                imageButton.setImageDrawable(getResources().getDrawable(image_ids[mTabNumber][index], getActivity().getTheme()));
            else
                imageButton.setImageDrawable(getResources().getDrawable(image_ids[mTabNumber][index]));
        }
        return v;
    }


    @Override
    public void onDestroyView() {
        for (int index=0; index<size; index++) {
            if (mPanels[index]!=null)
                mPanels[index].setOnClickListener(null);
            mPanels[index] = null;
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
        MenuSmallFragment hostFragment;
        int buttonType;
        public OnKeyClickListener(MenuSmallFragment hostFragment, int buttonType) {
            this.hostFragment = hostFragment;
            this.buttonType = buttonType;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(hostFragment.getActivity(), ToolActivity.class);
            intent.putExtra(ToolActivity.EXTRA_task, hostFragment.taskType[hostFragment.mTabNumber][buttonType]);
            hostFragment.startActivity(intent);
        }
        public void cleanup() { hostFragment = null; }
    }
}
