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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;


public class FormulaFragment extends Fragment {
    private static final String TAG = FormulaFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title = "Reference Function Calculator";

    public static final String EXTRA_typeCode = FormulaFragment.class.getSimpleName()+".typeCode";
    private static final String defaultTypeCode = ThermoCouple.codeB;

    private static final String ThermocoupleTypes[] = ThermoCouple.type;

    private static final int inputType_T = 1;
    private static final int inputType_E = 2;


    private String mTypeCode;
    private ThermoCouple mThermoCouple;

    private int inputType = inputType_T;
    private double mInput = Double.NaN;

    private Fn fn;
    private FnInv fnInv;
    private Fn.Result fnResult;
    private FnInv.Result fnInvResult;
    private String EequalLabel;
    private String TequalLabel;

    private int tablePolynomial_Id = 0;
    private int tableExponential_Id = 0;
    private LinearLayout mPolynomialView; // The anchor for the polynomial.
    private LinearLayout mExponentialView;

    private TextView mInputLabelView;
    private EditText mInputView;
    private TextView mMessageView;
    private Button mButtonFindE;
    private Button mButtonFindT;
    private TextView mResultView;
    private TextView mPolynomialLabelView;
    private TextView mExponentLabelView;
    private TextView mExponentConcludeView;
    private TextView mErrorRangeView;

    private InputTextWatcher mInputTextWatcher;
    private final OnButtonClickListener mOnButtonClickListeners[]
            = new OnButtonClickListener[2];

    private Spinner mSpinner = null;
    private ArrayAdapter<String> mAdapter = null;
    private SpinnerOnItemSelectedListener mSpinnerListener;


    public static FormulaFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("") || typeCode.equals(ThermoCouple.all))
            typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        FormulaFragment fragment = new FormulaFragment();
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

        computeDetails();

        EequalLabel = getString(R.string.label_Eequals);
        TequalLabel = getString(R.string.label_Tequals);

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_formula, parent, false);

        getActivity().setTitle(title);

        // Allow reuse
        if (mAdapter==null)
            mAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, ThermocoupleTypes);
        // Allow reuse
        if (mSpinnerListener==null)
            mSpinnerListener = new SpinnerOnItemSelectedListener(this);

        mSpinner = (Spinner) v.findViewById(R.id.fragmentFormula_thermocoupleType);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(mSpinnerListener);
        selectSpinnerItem();


        mPolynomialView = (LinearLayout) v.findViewById(R.id.fragmentFormula_polynomial);
        mExponentialView = (LinearLayout) v.findViewById(R.id.fragmentFormula_exponential);

        mInputLabelView = (TextView) v.findViewById(R.id.fragmentFormula_inputType);
        mInputView = (EditText) v.findViewById(R.id.fragmentFormula_x);

        if (Double.isNaN(mInput)) {
            mInputView.setText("");
            Savelog.d(TAG, debug, "Input is empty");
        }
        else {
            mInputView.setText("" + mInput);
            Savelog.d(TAG, debug, "Input is " + mInput);
        }


        mPolynomialLabelView = (TextView) v.findViewById(R.id.fragmentFormula_polynomialLabel);
        mMessageView = (TextView) v.findViewById(R.id.fragmentFormula_message);
        mResultView = (TextView) v.findViewById(R.id.fragmentFormula_result);
        mExponentLabelView = (TextView) v.findViewById(R.id.fragmentFormula_exponentialLabel);
        mExponentConcludeView = (TextView) v.findViewById(R.id.fragmentFormula_exponentialConclude);
        mErrorRangeView = (TextView) v.findViewById(R.id.fragmentFormula_errorRange);

        mButtonFindE = (Button) v.findViewById(R.id.fragmentFormula_Fn);
        mButtonFindT = (Button) v.findViewById(R.id.fragmentFormula_FnInv);

        // May reuse listeners
        if (mOnButtonClickListeners[0]==null)
            mOnButtonClickListeners[0] = new OnButtonClickListener(this, inputType_T);
        if (mOnButtonClickListeners[1]==null)
            mOnButtonClickListeners[1] = new OnButtonClickListener(this, inputType_E);

        mButtonFindE.setOnClickListener(mOnButtonClickListeners[0]);
        mButtonFindT.setOnClickListener(mOnButtonClickListeners[1]);

        displayTexts();
        displayPolynomial(inflater);
        displayExponential(inflater);

        // May reuse listener
        if (mInputTextWatcher==null)
            mInputTextWatcher = new InputTextWatcher(this);
        mInputView.addTextChangedListener(mInputTextWatcher);


        TextView textView = (TextView) v.findViewById(R.id.fragmentFormula_footnote);
        textView.setText(getString(R.string.info_referenceFunctions) + mThermoCouple.getUnitT());

        return v;
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


    private void computeDetails() {
        if (inputType==inputType_T) {
            try {
                fnResult = fn.computeDetails(mInput);
            } catch (Exception e) {
                fnResult = null;
            }
        }
        else if (inputType==inputType_E) {
            try {
                fnInvResult = fnInv.computeDetails(mInput);
            } catch (Exception e) {
                fnInvResult = null;
            }
        }
    }


    @Override
    public void onDestroyView() {
        mPolynomialView = null;
        mExponentialView = null;
        mInputLabelView = null;
        if (mInputView!=null) {
            mInputView.removeTextChangedListener(mInputTextWatcher);
            mInputView = null;
        }
        mPolynomialLabelView = null;
        mMessageView = null;
        mResultView = null;
        mExponentLabelView = null;
        mExponentConcludeView = null;
        mErrorRangeView = null;
        if (mButtonFindE!=null) {
            mButtonFindE.setOnClickListener(null);
            mButtonFindE = null;
        }
        if (mButtonFindT!=null) {
            mButtonFindT.setOnClickListener(null);
            mButtonFindT = null;
        }
        if (mSpinner!=null) {
            mSpinner.setAdapter(null);
            mSpinner.setOnItemSelectedListener(null);
            mSpinner = null;
        }
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        mThermoCouple = null;
        fn = null;
        fnInv = null;
        fnResult = null;
        fnInvResult = null;

        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        for (int index=0; index<mOnButtonClickListeners.length; index++) {
            if (mOnButtonClickListeners[index] != null)
                mOnButtonClickListeners[index].cleanup();
            mOnButtonClickListeners[index] = null;
        }
        if (mInputTextWatcher!=null) {
            mInputTextWatcher.cleanup();
            mInputTextWatcher = null;
        }
        if (mSpinnerListener!=null) {
            mSpinnerListener.cleanup();
            mSpinnerListener = null;
        }
        mAdapter = null;

        super.onDestroy();
    }



    private void displayTexts() {
        String inputLabel = "";
        String polynomialLabel = "";
        String resultText = "";
        String messageText = "";
        String errorRangeText = "";

        if (inputType==inputType_T && fn!=null) {
            inputLabel = TequalLabel;
            if (fnResult!=null) {
                String data = String.format("%.3f", fnResult.E);
                messageText = "Result:";
                resultText = "At " + TequalLabel + " " + fnResult.Tinput + " " + fn.getUnitT()
                             + ", " + EequalLabel + " " + data + " " + fn.getUnitEMF();
                polynomialLabel = getString(R.string.label_polynomialFn, fn.getTmin(), fn.getTmax());
            }
            else {
                messageText = getString(R.string.label_inputT_range, fn.getTmin(), fn.getTmax()) + fn.getUnitT();
            }
        }
        else if (inputType==inputType_E && fnInv!=null) {
            inputLabel = EequalLabel;
            if (fnInvResult!=null) {
                String data = String.format("%.3f", fnInvResult.T);
                messageText = "Result:";
                resultText = "At " + EequalLabel + " " + fnInvResult.Einput + " " + fnInv.getUnitEMF()
                             + ", " + TequalLabel + " " + data + " " + fnInv.getUnitT();
                polynomialLabel = getString(R.string.label_polynomialFnInv, fnInv.getEmin(), fnInv.getEmax());
                errorRangeText = "Error Range: [" + fnInvResult.polynomial.getErrorRange()[0]
                             + ", " + fnInvResult.polynomial.getErrorRange()[1] + "]";
            }
            else {
                messageText = getString(R.string.label_inputE_range, fnInv.getEmin(), fnInv.getEmax()) + fnInv.getUnitEMF();
            }
        }

        if (mInputLabelView!=null)
            mInputLabelView.setText(inputLabel);
        if (mPolynomialLabelView!=null)
            mPolynomialLabelView.setText(polynomialLabel);
        if (mResultView!=null)
            mResultView.setText(resultText);
        if (mMessageView!=null)
            mMessageView.setText(messageText);

        if (mInputView!=null) {
            mInputView.setSelection(mInputView.getText().length());
        }
        if (mErrorRangeView!=null && !errorRangeText.equals("")) {
            mErrorRangeView.setVisibility(View.VISIBLE);
            mErrorRangeView.setText(errorRangeText);
        }
        else {
            mErrorRangeView.setVisibility(View.GONE);
        }

    }

    private void displayPolynomial(LayoutInflater inflater) {
        if (inflater == null) {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // Remove any existing table
        LinearLayout parent = mPolynomialView;
        if (tablePolynomial_Id != 0) {
            View view = parent.findViewById(tablePolynomial_Id);
            if (view != null) parent.removeView(view);
        }


        int order = -1;

        if (inputType == inputType_T) {
            if (fnResult == null) return;
            order = fnResult.polynomial.getOrder();
        } else if (inputType == inputType_E) {
            if (fnInvResult == null) return;
            order = fnInvResult.polynomial.getOrder();
        }
        Savelog.d(TAG, debug, "order=" + order);

        if (order == -1) {
            return;
        }

        int tableSize = 0;
        tablePolynomial_Id = 0;
        TableLayout tableLayout = null;

        if (order>=0) {
            View view = inflater.inflate(R.layout.table_polynomial, parent, true);
            tableLayout = (TableLayout) view.findViewById(R.id.tablePolynomial_id);
            tableSize = PolynomialTable.MaxOrder;
            tablePolynomial_Id = R.id.tablePolynomial_id;
        }

        // Each row is a term in the polynomial


        for (int row = 0; row <= order; row++) {
            int cell_id;
            int column;
            int term = row;
            TextView cell;

            double coefficient = 0.0;
            if (inputType == inputType_T) {
                coefficient = fnResult.polynomial.getCoefficent(term);
            } else if (inputType == inputType_E) {
                coefficient = fnInvResult.polynomial.getCoefficent(term);
            }

            if (row == order) {
                column = 0;
                cell_id = PolynomialTable.cell_ids[row][column];
                cell = (TextView) tableLayout.findViewById(cell_id);
                cell.setText("+");
            }

            column = 1;
            cell_id = PolynomialTable.cell_ids[row][column];
            cell = (TextView) tableLayout.findViewById(cell_id);
            cell.setText("" + coefficient);

            column = 2;
            cell_id = PolynomialTable.cell_ids[row][column];
            cell = (TextView) tableLayout.findViewById(cell_id);
            cell.setText(Html.fromHtml("(" + mInput + ")<sup>" + term + "</sup>"));

        }
        for (int row = order + 1; row <= tableSize; row++) {
            int row_id = PolynomialTable.row_ids[row];
            TableRow tableRow = (TableRow) tableLayout.findViewById(row_id);
            tableLayout.removeView(tableRow);
        }


        // The result row shows the value computed by the polynomial

        {
            CharSequence sumPolyLabel = "";
            String sumPoly = "";
            if (inputType == inputType_T) {
                sumPoly = String.format("%.3f", fnResult.Epoly);
                sumPolyLabel = EequalLabel;
                // If there is an exponential term, use a different label
                if (fnResult.exponential!=null)
                    sumPolyLabel = getText(R.string.label_EPequals);

            } else if (inputType == inputType_E) {
                sumPoly = String.format("%.3f", fnInvResult.T);
                sumPolyLabel = TequalLabel;
            }

            int result_id;

            TextView resultCell;
            result_id = PolynomialTable.result_ids[1];  // column 1
            resultCell = (TextView) tableLayout.findViewById(result_id);
            resultCell.setText(sumPolyLabel);

            result_id = PolynomialTable.result_ids[2];  // column 2
            resultCell = (TextView) tableLayout.findViewById(result_id);
            resultCell.setText(sumPoly);
        }
    }

    private void displayExponential(LayoutInflater inflater) {
        if (inputType==inputType_T && fnResult!=null && fnResult.exponential!=null) {
            // Also display exponential
            String exponentialLabel = "For T>0, add correction term:";
            Savelog.d(TAG, debug, "Exponential term=" + exponentialLabel);
            if (mExponentLabelView!=null) {
                mExponentLabelView.setText(exponentialLabel);
                mExponentLabelView.setVisibility(View.VISIBLE);
            }
            String data = String.format("%.3f", fnResult.E);
            CharSequence exponentConclude = TextUtils.concat(getText(R.string.label_E_typeK), " = ", data);
            if (mExponentConcludeView!=null) {
                mExponentConcludeView.setText(exponentConclude);
                mExponentConcludeView.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (mExponentLabelView!=null)
                mExponentLabelView.setVisibility(View.GONE);
            if (mExponentConcludeView!=null)
                mExponentConcludeView.setVisibility(View.GONE);
        }

        LinearLayout parent = mExponentialView;

        // Remove any existing table
        TableLayout tableLayoutE;
        if (tableExponential_Id!=0) {
            tableLayoutE = (TableLayout) parent.findViewById(tableExponential_Id);
            parent.removeView(tableLayoutE);
            tableExponential_Id = 0;
        }

        // Handle exponential term if exists
        if (inputType==inputType_T && fnResult!=null && fnResult.exponential!=null) {
            if (inflater == null) {
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            tableExponential_Id = R.id.tableExponential_id;
            View view = inflater.inflate(R.layout.table_exponent, parent, true);
            tableLayoutE = (TableLayout) view.findViewById(tableExponential_Id);

            int cell_id;
            TextView cell;

            for (int row=0; row<=2; row++) {
                int term = row;
                cell_id = ExponentialTable.cell_ids[row][1];
                cell = (TextView) tableLayoutE.findViewById(cell_id);
                cell.setText("" + fnResult.exponential.getTerm(term));
            }

            // T
            cell_id = ExponentialTable.cell_ids[3][1];
            cell = (TextView) tableLayoutE.findViewById(cell_id);
            cell.setText("" + fnResult.Tinput);

            // correction
            cell_id = ExponentialTable.result_ids[1];
            cell = (TextView) tableLayoutE.findViewById(cell_id);
            String correction = String.format("%.3f", fnResult.correction);
            cell.setText("" + correction);
        }
    }


    private static class InputTextWatcher implements TextWatcher {
        // This class of objects does not outlive its host, so no need to use weak references
        FormulaFragment hostFragment;
        public InputTextWatcher(FormulaFragment hostFragment) {
            super();
            this.hostFragment = hostFragment;
        }
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            try {
                String data = c.toString().trim();
                hostFragment.mInput = Double.valueOf(data);
            } catch (Exception e) {
                hostFragment.mInput = Double.NaN;
            }
            hostFragment.computeDetails();
            hostFragment.displayTexts();
            hostFragment.displayPolynomial(null);
            hostFragment.displayExponential(null);
        }
        public void cleanup() { hostFragment = null; }
    }




    private static class OnButtonClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        FormulaFragment hostFragment;
        int buttonType;
        public OnButtonClickListener(FormulaFragment hostFragment, int buttonType) {
            super();
            this.hostFragment = hostFragment;
            this.buttonType = buttonType;
        }

        @Override
        public void onClick(View view) {
            if (hostFragment.inputType!=buttonType) {
                int newInputType = buttonType;
                hostFragment.inputType = newInputType;

                // reset input
                if (hostFragment.mInputView!=null)
                    hostFragment.mInputView.setText(""); // expect to trigger a series of updates
            }
        }
        public void cleanup() { hostFragment = null; }
    }



    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        FormulaFragment hostFragment;
        public SpinnerOnItemSelectedListener(FormulaFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (!hostFragment.mTypeCode.equals(hostFragment.ThermocoupleTypes[pos])) {
                hostFragment.mTypeCode = hostFragment.ThermocoupleTypes[pos];

                // Update the functions.
                hostFragment.fn = hostFragment.mThermoCouple.getFn(hostFragment.mTypeCode);
                hostFragment.fnInv = hostFragment.mThermoCouple.getFnInv(hostFragment.mTypeCode);

                // reset input
                if (hostFragment.mInputView!=null)
                    hostFragment.mInputView.setText(""); // expect to trigger a series of updates
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void cleanup() {
            hostFragment = null;
        }
    }


}
