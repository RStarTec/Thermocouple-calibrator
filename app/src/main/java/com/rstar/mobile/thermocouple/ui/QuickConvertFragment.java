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
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;


public class QuickConvertFragment extends Fragment {
    private static final String TAG = QuickConvertFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title = "Quick Conversion";
    public static final String EXTRA_typeCode = QuickConvertFragment.class.getSimpleName()+".typeCode";

    private static final String defaultTypeCode = ThermoCouple.codeB;

    private static final int spinnerType_Tr = 3;
    private static final int spinnerType_Thermo = 4;

    private static final String ThermocoupleTypes[] = ThermoCouple.type;
    private static final double RJunctionTemperatures[] = {0d, 25d};
    private static final String RJunctionTemperatureCustom = "custom";

    private String mTypeCode;
    private ThermoCouple mThermoCouple;

    private double Tr = RJunctionTemperatures[0];

    private String VoltmeterLabel;
    private String RJunctionTempLabel;
    private String MJunctionTempLabel;
    private String ELabel;

    private String mInput = "";

    private Fn fn;
    private FnInv fnInv;
    private String Tunit;
    private String EMFunit;

    private GridView mInputGrid;
    private NumericKeypad.Adapter mNumericKeypadAdapter;
    private OnKeyClickListener mOnKeyClickListener;
    private OnEditButtonClickListener mOnEditButtonClickListener;
    private final SpinnerOnItemSelectedListener mSpinnerOnItemSelectedListener[] = new SpinnerOnItemSelectedListener[2];
    private TextView mOutputView;
    private TextView mTrCustomView;
    private ImageButton mTrCustomButton;

    private Spinner mSpinnerThermo;
    private Spinner mSpinnerTr;


    public static QuickConvertFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("") || typeCode.equals(ThermoCouple.all))
            typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        QuickConvertFragment fragment = new QuickConvertFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);

        mThermoCouple = new ThermoCouple(getActivity());

        fn = mThermoCouple.getFn(mTypeCode);
        fnInv = mThermoCouple.getFnInv(mTypeCode);

        Tunit = fn.getUnitT();
        EMFunit = fn.getUnitEMF();

        VoltmeterLabel = getString(R.string.label_voltmeterReading);
        RJunctionTempLabel = getString(R.string.label_rjunctionTemp);
        MJunctionTempLabel = getString(R.string.label_mjunctionTemp);
        ELabel = getString(R.string.label_E);

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        if (mTypeCode.equals(ThermoCouple.all)) {
            View v = inflater.inflate(R.layout.fragment_blank, parent, false);
            TextView textView = (TextView) v.findViewById(R.id.fragmentBlank_message);
            textView.setText("Conversion cannot be done simultaneously for ALL types. Please select a specific type.");
            return v;
        }
        else {
            View v = inflater.inflate(R.layout.fragment_quickconvert, parent, false);

            getActivity().setTitle(title);

            mSpinnerThermo = (Spinner) v.findViewById(R.id.fragmentQuickConvert_thermocoupleType);
            // Add adapter
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, ThermocoupleTypes);
            mSpinnerThermo.setAdapter(arrayAdapter);

            // First spinner
            // allow reuse
            if (mSpinnerOnItemSelectedListener[0]==null)
                mSpinnerOnItemSelectedListener[0] = new SpinnerOnItemSelectedListener(this, spinnerType_Thermo);
            mSpinnerThermo.setOnItemSelectedListener(mSpinnerOnItemSelectedListener[0]);

            // Set selection (in case of orientation change)
            for (int i=0; i<ThermocoupleTypes.length; i++) {
                if (mTypeCode.equals(ThermocoupleTypes[i])) {
                    mSpinnerThermo.setSelection(i);
                    break;
                }
            }


            mTrCustomView = (TextView) v.findViewById(R.id.fragmentQuickConvert_TrCustom);
            mTrCustomButton = (ImageButton) v.findViewById(R.id.fragmentQuickConvert_TrCustomButton);

            // allow reuse
            if (mOnEditButtonClickListener==null)
                mOnEditButtonClickListener = new OnEditButtonClickListener(this);
            mTrCustomButton.setOnClickListener(mOnEditButtonClickListener);

            mSpinnerTr = (Spinner) v.findViewById(R.id.fragmentQuickConvert_TrType);

            String[] TrLabels = new String[RJunctionTemperatures.length + 1]; // one more for custom value

            // Create labels
            for (int i=0; i<RJunctionTemperatures.length; i++) {
                TrLabels[i] = Double.toString(RJunctionTemperatures[i]) + " " + Tunit;
            }
            TrLabels[RJunctionTemperatures.length] = RJunctionTemperatureCustom; // last one is custom
            // Add the adapter
            ArrayAdapter<String> TrArrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, TrLabels);
            mSpinnerTr.setAdapter(TrArrayAdapter);

            // Second spinner
            // allow reuse
            if (mSpinnerOnItemSelectedListener[1]==null)
                mSpinnerOnItemSelectedListener[1] = new SpinnerOnItemSelectedListener(this, spinnerType_Tr);
            mSpinnerTr.setOnItemSelectedListener(mSpinnerOnItemSelectedListener[1]);


            // Set selection (in case of orientation change)
            boolean found = false;
            for (int i=0; i<RJunctionTemperatures.length; i++) {
                if (Tr==RJunctionTemperatures[i]) {
                    mSpinnerTr.setSelection(i);
                    found = true;
                    break;
                }
            }
            if (found) {
                updateTrCustomView(false);
            }
            else {
                // custom value
                mSpinnerTr.setSelection(RJunctionTemperatures.length); // last one (remember the label array is one bigger than the number array
                updateTrCustomView(true);
            }


            mOutputView = (TextView) v.findViewById(R.id.fragmentQuickConvert_output);

            mInputGrid = (GridView) v.findViewById(R.id.fragmentQuickConvert_grid);
            compute();

            // Allow reuse
            if (mNumericKeypadAdapter==null)
                mNumericKeypadAdapter = new NumericKeypad.Adapter(this);
            if (mOnKeyClickListener==null)
                mOnKeyClickListener = new OnKeyClickListener(this);
            mNumericKeypadAdapter.setOnKeyClickListener(mOnKeyClickListener);
            mInputGrid.setAdapter(mNumericKeypadAdapter);


            return v;
        }
    }



    @Override
    public void onDestroyView() {
        if (mSpinnerThermo!=null) {
            mSpinnerThermo.setOnItemSelectedListener(null);
            mSpinnerThermo.setAdapter(null);
            mSpinnerThermo = null;
        }

        mTrCustomView = null;

        if (mTrCustomButton!=null) {
            mTrCustomButton.setOnClickListener(null);
            mTrCustomButton = null;
        }


        if (mSpinnerTr!=null) {
            mSpinnerTr.setOnItemSelectedListener(null);
            mSpinnerTr.setAdapter(null);
            mSpinnerTr = null;
        }

        mOutputView = null;

        if (mNumericKeypadAdapter!=null) {
            mNumericKeypadAdapter.setOnKeyClickListener(null);
        }

        if (mInputGrid!=null) {
            mInputGrid.setAdapter(null);
            mInputGrid = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mThermoCouple = null;

        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        fn = null;
        fnInv = null;

        if (mNumericKeypadAdapter!=null)
            mNumericKeypadAdapter.cleanup();
        mNumericKeypadAdapter = null;

        if (mOnKeyClickListener!=null)
            mOnKeyClickListener.cleanup();
        mOnKeyClickListener = null;

        if (mOnEditButtonClickListener!=null)
            mOnEditButtonClickListener.cleanup();
        mOnEditButtonClickListener = null;

        for (int index=0; index<mSpinnerOnItemSelectedListener.length; index++) {
            if (mSpinnerOnItemSelectedListener[index]!=null)
                mSpinnerOnItemSelectedListener[index].cleanup();
            mSpinnerOnItemSelectedListener[index] = null;
        }

        super.onDestroy();
    }





    private void compute() {
        String data = "";

        data += VoltmeterLabel + ": ";

        if (mInput==null || mInput.length()==0) {
            mInput = "";
            data += "(please enter)";
        }
        else {
            data += mInput + " " + EMFunit + "\n";

            try {
                double Einput = Double.parseDouble(mInput);
                double Toutput = mThermoCouple.computeT(mTypeCode, Tr, Einput);
                data += "\n" + MJunctionTempLabel + ": " + String.format("%.3f", Toutput) + " " + Tunit;
            }
            catch (Exception e) {
                if (debug) {
                    data += "Exception: " + e.getMessage() + "\n";
                    Savelog.e(TAG, "bad input ", e);
                }

                data += "\nInput invalid!";
                if (fn!=null) {
                    data += "\n" + getString(R.string.label_inputTr_range, fn.getTmin(), fn.getTmax()) + Tunit;

                    if (fn.getTmin()<=Tr && Tr<=fn.getTmax()) {
                        try {
                            double FTr = fn.computeE(Tr);
                            data += "\n" + getString(R.string.label_intermediateE_range, (fnInv.getEmin()-FTr), (fnInv.getEmax()-FTr)) + EMFunit;
                        } catch (Exception e1) {
                        }
                    }
                }
                else {
                    if (fnInv != null)
                        data += "\n" + getString(R.string.label_inputE_range, fnInv.getEmin(), fnInv.getEmax()) + EMFunit;
                }

            }
        }

        if (mOutputView!=null) {
            mOutputView.setText(data);
        }
    }




    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        QuickConvertFragment hostFragment;
        int spinnerType;
        public SpinnerOnItemSelectedListener(QuickConvertFragment hostFragment, int spinnerType) {
            this.hostFragment = hostFragment;
            this.spinnerType = spinnerType;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (spinnerType==spinnerType_Thermo) {
                String newTypeCode = ThermocoupleTypes[pos];
                if (!newTypeCode.equals(hostFragment.mTypeCode)) {
                    hostFragment.mTypeCode = newTypeCode;
                    // Update fn and fnInv
                    hostFragment.fn = hostFragment.mThermoCouple.getFn(hostFragment.mTypeCode);
                    hostFragment.fnInv = hostFragment.mThermoCouple.getFnInv(hostFragment.mTypeCode);
                    hostFragment.compute();
                }
            }
            else {
                if (pos< RJunctionTemperatures.length) { // one of the default values
                    hostFragment.updateTrCustomView(false);
                    if (hostFragment.Tr != RJunctionTemperatures[pos]) {
                        hostFragment.Tr = RJunctionTemperatures[pos];
                        hostFragment.compute();
                    }
                }
                else {
                    // Selected custom value. Allow editing
                    hostFragment.updateTrCustomView(true);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void cleanup() {
            hostFragment = null;
        }
    }

    private void updateTrCustomView(boolean show) {
        if (mTrCustomView !=null && mTrCustomButton !=null) {
            mTrCustomView.setText("" + Tr + " " + Tunit);
            if (show) {
                mTrCustomView.setVisibility(View.VISIBLE);
                mTrCustomButton.setVisibility(View.VISIBLE);
            }
            else {
                mTrCustomView.setVisibility(View.GONE);
                mTrCustomButton.setVisibility(View.GONE);
            }
        }
    }

    // Process returned value from dialog
    public void updateInput(double value, int status, String label) {
        if (status == NumericKeypadDialogFragment.status_ok) {
            Tr = value;
            updateTrCustomView(true);
            compute();

        }
        else if (status == NumericKeypadDialogFragment.status_delete) {
            Tr = RJunctionTemperatures[0]; // use the first default value
            updateTrCustomView(false);
            if (mSpinnerTr !=null) {
                mSpinnerTr.setSelection(0);
            }
        }
    }

    private static class OnKeyClickListener extends NumericKeypad.OnKeyClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        QuickConvertFragment hostFragment;

        public OnKeyClickListener(QuickConvertFragment hostFragment) {
            super(hostFragment.mInput);
            this.hostFragment = hostFragment;
        }

        @Override
        public void onOk() {
            // Done! Finish
            hostFragment.getActivity().finish();
        }

        @Override
        public void onClickCompleted() {
            hostFragment.mInput = getBuffer();
            hostFragment.compute();
        }
        public void cleanup() {
            hostFragment = null;
        }
    }

    private static class OnEditButtonClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        QuickConvertFragment hostFragment;
        public OnEditButtonClickListener(QuickConvertFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onClick(View v) {
            // Edit custom value.
            String hint = hostFragment.RJunctionTempLabel + ": ";
            FragmentManager fm = hostFragment.getActivity().getSupportFragmentManager();
            NumericKeypadDialogFragment dialogFragment;
            dialogFragment = NumericKeypadDialogFragment.newInstance(hostFragment.ELabel, hostFragment.Tr, hint);
            dialogFragment.setTargetFragment(hostFragment);
            dialogFragment.show(fm, NumericKeypadDialogFragment.dialogTag);
        }
        public void cleanup() {
            hostFragment = null;
        }
    }

}
