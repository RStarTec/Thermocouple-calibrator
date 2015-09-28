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

package com.rstar.mobile.thermocouple.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Sourcecode;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.CircuitFragment;
import com.rstar.mobile.thermocouple.ui.DatabaseFragment;
import com.rstar.mobile.thermocouple.ui.FormulaFragment;
import com.rstar.mobile.thermocouple.ui.GraphFragment;
import com.rstar.mobile.thermocouple.ui.QuickConvertFragment;
import com.rstar.mobile.thermocouple.ui.SeebeckFragment;
import com.rstar.mobile.thermocouple.ui.SourcecodeFragment;


public class ToolActivity extends AppCompatActivity {
    private static final String TAG = ToolActivity.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_task = TypespecificActivity.class.getSimpleName()+".task";

    public static final int task_circuit = 101;
    public static final int task_graph = 102;
    public static final int task_seebeck = 103;
    public static final int task_formula = 104;
    public static final int task_sourcecode = 105;
    public static final int task_database = 106;
    public static final int task_quickconvert = 107;


    public static final int task_default = task_circuit;

    private static final String DefaultTypeCode = ThermoCouple.all;

    private Fragment mFragment;
    private int fragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_thermocouple);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        setContentView(R.layout.activity_container);

        int computeType = getIntent().getIntExtra(EXTRA_task, task_default);

        fragmentId = R.id.activityContainer_id;

        FragmentManager fm = getSupportFragmentManager();

        mFragment = fm.findFragmentById(fragmentId);
        if (mFragment==null) {
            if (computeType==task_circuit)
                mFragment = CircuitFragment.newInstance(DefaultTypeCode);
            else if (computeType==task_formula)
                mFragment = FormulaFragment.newInstance(DefaultTypeCode);
            else if (computeType==task_graph)
                mFragment = GraphFragment.newInstance(DefaultTypeCode);
            else if (computeType==task_seebeck)
                mFragment = SeebeckFragment.newInstance(DefaultTypeCode);
            if (computeType==task_sourcecode)
                mFragment = SourcecodeFragment.newInstance(Sourcecode.defaultIndex);
            else if (computeType==task_database)
                mFragment = DatabaseFragment.newInstance(DefaultTypeCode);
            else if (computeType==task_quickconvert)
                mFragment = QuickConvertFragment.newInstance(DefaultTypeCode);

            if (mFragment!=null)
                fm.beginTransaction().add(fragmentId, mFragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        mFragment = null;
        fragmentId = 0;
        super.onDestroy();
    }

}

