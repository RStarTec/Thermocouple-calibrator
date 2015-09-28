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
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.activities.ImageActivity;
import com.rstar.mobile.thermocouple.ui.activities.ToolActivity;


public class CircuitFragment extends Fragment {
    private static final String TAG = CircuitFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_typeCode = CircuitFragment.class.getSimpleName()+".typeCode";

    public static final String title = "Basic Thermocouple Circuit";
    private static final String defaultTypeCode = ThermoCouple.codeB; // Do not allow using all

    private static final String ThermocoupleTypes[] = ThermoCouple.type;

    private static final int input_Tr = 1;
    private static final int input_E = 2;
    private static final int input_others = -1;


    private String mTypeCode;
    private ThermoCouple mThermoCouple;
    private String mOutput = "";
    private double mTrInput = Double.NaN;
    private double mEInput = Double.NaN;

    private String Tunit;
    private String EMFunit;

    private Circuit mCircuit = null;
    private TextView mOutputView = null;
    private Spinner mSpinner = null;
    private ArrayAdapter<String> mAdapter = null;
    private ImageButton mButton = null;

    private String VoltmeterLabel;
    private String AmbientTempLabel;
    private String MJunctionTempLabel;
    private String TrLabel;
    private String ELabel;
    private OnCircuitElementPressedListener mCircuitElementListeners[]
            = new OnCircuitElementPressedListener[3];
    private SpinnerOnItemSelectedListener mSpinnerListener;
    private OnButtonClickListener mButtonClickListener;


    public static CircuitFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("") || typeCode.equals(ThermoCouple.all))
            typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        CircuitFragment fragment = new CircuitFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);

        mThermoCouple = new ThermoCouple(getActivity());
        Tunit = mThermoCouple.getUnitT();
        EMFunit = mThermoCouple.getUnitEMF();

        VoltmeterLabel = getString(R.string.label_voltmeterReading);
        AmbientTempLabel = getString(R.string.label_rjunctionTemp);
        MJunctionTempLabel = getString(R.string.label_mjunctionTemp);
        ELabel = getString(R.string.label_E);
        TrLabel = getString(R.string.label_Tr);

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
            textView.setText("Calculation cannot be done simultaneously for ALL types. Please select a specific type.");
            return v;
        }
        else {
            View v = inflater.inflate(R.layout.fragment_circuit, parent, false);

            getActivity().setTitle(title);

            // Allow reuse
            if (mAdapter==null)
                mAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, ThermocoupleTypes);
            // Allow reuse
            if (mSpinnerListener==null)
                mSpinnerListener = new SpinnerOnItemSelectedListener(this);
            // Allow reuse
            if (mButtonClickListener==null)
                mButtonClickListener = new OnButtonClickListener(this);

            mSpinner = (Spinner) v.findViewById(R.id.fragmentCircuit_thermocoupleType);
            mSpinner.setAdapter(mAdapter);
            mSpinner.setOnItemSelectedListener(mSpinnerListener);
            selectSpinnerItem();

            mCircuit = (Circuit) v.findViewById(R.id.fragmentCircuit_diagram);
            mCircuitElementListeners[0] = new OnCircuitElementPressedListener(this, input_E);
            mCircuitElementListeners[1] = new OnCircuitElementPressedListener(this, input_Tr);
            mCircuitElementListeners[2] = new OnCircuitElementPressedListener(this, input_others);
            mCircuit.setOnVoltmeterPressedListener(mCircuitElementListeners[0]);
            mCircuit.setOnIsothermoPressedListener(mCircuitElementListeners[1]);
            mCircuit.setOnMJunctionPressedListener(mCircuitElementListeners[2]);

            ImageButton mButton = (ImageButton) v.findViewById(R.id.fragmentCircuit_quick);
            mButton.setOnClickListener(mButtonClickListener);

            mOutputView = (TextView) v.findViewById(R.id.fragmentCircuit_output);
            showOutput();

            return v;
        }
    }

    @Override
    public void onDestroyView() {
        for (int index=0; index<mCircuitElementListeners.length; index++) {
            if (mCircuitElementListeners[index]!=null)
                mCircuitElementListeners[index].cleanup();
            mCircuitElementListeners[index] = null;
        }
        if (mCircuit!=null) {
            mCircuit.setOnMJunctionPressedListener(null);
            mCircuit.setOnVoltmeterPressedListener(null);
            mCircuit.setOnIsothermoPressedListener(null);
            mCircuit.cleanup();
            mCircuit = null;
        }
        if (mSpinner!=null) {
            mSpinner.setAdapter(null);
            mSpinner.setOnItemSelectedListener(null);
            mSpinner = null;
        }
        mOutputView = null;
        if (mButton!=null) {
            mButton.setOnClickListener(null);
            mButton = null;
        }
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        if (mSpinnerListener!=null) {
            mSpinnerListener.cleanup();
            mSpinnerListener = null;
        }
        mAdapter = null;
        if (mButtonClickListener!=null) {
            mButtonClickListener.cleanup();
            mButtonClickListener = null;
        }
        super.onDestroy();
    }


    private void selectSpinnerItem() {
        for (int index = 0; index < ThermocoupleTypes.length; index++) {
            if (ThermocoupleTypes[index].equals(mTypeCode)) {
                if (mSpinner != null) {
                    mSpinner.setSelection(index);
                }
                return;
            }
        }
    }



    private void showOutput() {
        String EString = Double.isNaN(mEInput) ? "(please enter)" : Double.toString(mEInput) + " " + EMFunit ;
        String TrString = Double.isNaN(mTrInput) ? "(please enter)" : Double.toString(mTrInput) + " " + Tunit;

        mOutput = "";
        mOutput += "\n" + VoltmeterLabel + ": " + EString;
        mOutput += "\n" + AmbientTempLabel + ": " + TrString;
        mOutput += "\n\n";


        try {
            String result = mThermoCouple.describeT(mTypeCode, mTrInput, mEInput);
            mOutput += MJunctionTempLabel + ": \n" + result;

        } catch (Exception e) {
            // If the user has already entered both inputs, then one of the inputs must be invalid
            if (!Double.isNaN(mEInput) && !Double.isNaN(mTrInput)) {
                mOutput += "Input invalid!";

                Fn fn = mThermoCouple.getFn(mTypeCode);
                FnInv fnInv = mThermoCouple.getFnInv(mTypeCode);
                if (fn!=null) {
                    mOutput += "\n" + getString(R.string.label_inputTr_range, fn.getTmin(), fn.getTmax()) + Tunit;

                    if (fn.getTmin()<=mTrInput && mTrInput<=fn.getTmax()) {
                        try {
                            double FTr = fn.computeE(mTrInput);
                            mOutput += "\n" + getString(R.string.label_intermediateE_range, (fnInv.getEmin()-FTr), (fnInv.getEmax()-FTr)) + EMFunit;
                        } catch (Exception e1) {
                        }
                    }
                }
                else {
                    if (fnInv != null)
                        mOutput += "\n" + getString(R.string.label_inputE_range, fnInv.getEmin(), fnInv.getEmax()) + EMFunit;
                }
            }


            if (debug) {
                mOutput += "Exception: " + e.getMessage();
            }

        }

        if (mOutputView!=null) {
            mOutputView.setText(mOutput);
        }

    }

    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        CircuitFragment hostFragment;
        public SpinnerOnItemSelectedListener(CircuitFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            hostFragment.mTypeCode = ThermoCouple.type[pos];
            hostFragment.showOutput();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void cleanup() {
            hostFragment = null;
        }
    }



    private static class OnCircuitElementPressedListener implements Circuit.OnElementPressedListener {
        CircuitFragment hostFragment;
        int elementType;

        public OnCircuitElementPressedListener(CircuitFragment hostFragment, int elementType) {
            this.hostFragment = hostFragment;
            this.elementType = elementType;
        }

        @Override
        public void onPressed() {
            FragmentManager fm = hostFragment.getActivity().getSupportFragmentManager();
            NumericKeypadDialogFragment dialogFragment;

            String hint = "";

            // provide a label to identify the input
            if (elementType==input_E) {
                FnInv fnInv = hostFragment.mThermoCouple.getFnInv(hostFragment.mTypeCode);
                if (fnInv!=null)
                    hint = hostFragment.getString(R.string.label_inputE_range, fnInv.getEmin(), fnInv.getEmax()) + fnInv.getUnitEMF();
                dialogFragment = NumericKeypadDialogFragment.newInstance(hostFragment.ELabel, hostFragment.mEInput, hint);
                dialogFragment.setTargetFragment(hostFragment);
                dialogFragment.show(fm, NumericKeypadDialogFragment.dialogTag);
            }
            else if (elementType==input_Tr) {
                Fn fn = hostFragment.mThermoCouple.getFn(hostFragment.mTypeCode);
                if (fn!=null)
                    hint = hostFragment.getString(R.string.label_inputTr_range, fn.getTmin(), fn.getTmax()) + fn.getUnitT();
                dialogFragment = NumericKeypadDialogFragment.newInstance(hostFragment.TrLabel, hostFragment.mTrInput, hint);
                dialogFragment.setTargetFragment(hostFragment);
                dialogFragment.show(fm, NumericKeypadDialogFragment.dialogTag);
            }
            else {
                Toast.makeText(hostFragment.getActivity(), "Press voltmeter or blue thermometer to enter input", Toast.LENGTH_SHORT).show();
            }
        }

        public void cleanup() {
            hostFragment = null;
        }
    }



    private static class OnButtonClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        CircuitFragment hostFragment;
        public OnButtonClickListener(CircuitFragment hostFragment) {
            super();
            this.hostFragment = hostFragment;
        }

        @Override
        public void onClick(View view) {
            // Start a new ToolActivity on top of the current one and host the QuickConvertFragment.
            Intent intent = new Intent(hostFragment.getActivity(), ToolActivity.class);
            intent.putExtra(ToolActivity.EXTRA_task, ToolActivity.task_quickconvert);
            hostFragment.startActivity(intent);
        }
        public void cleanup() { hostFragment = null; }
    }



    public void updateInput(double value, int status, String label) {
        if (status == NumericKeypadDialogFragment.status_ok) {
            // Find out which input this is
            if (label.equals(ELabel)) {
                mEInput = value;
            }
            else if (label.equals(TrLabel)) {
                mTrInput = value;
            }
            showOutput();
        }
        else if (status == NumericKeypadDialogFragment.status_delete) {
            // Find out which input this is
            if (label.equals(ELabel)) {
                mEInput = value;
            }
            else if (label.equals(TrLabel)) {
                mTrInput = value;
            }
            showOutput();
        }
    }
}
