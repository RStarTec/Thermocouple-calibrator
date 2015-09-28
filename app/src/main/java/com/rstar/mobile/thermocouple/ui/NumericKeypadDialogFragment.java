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

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

import java.text.DecimalFormat;


public class NumericKeypadDialogFragment extends DialogFragment {
    private static final String TAG = NumericKeypadDialogFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String dialogTag = NumericKeypadDialogFragment.class.getSimpleName()+"_tag";

    private static final String EXTRA_code = NumericKeypadDialogFragment.class.getSimpleName()+".code";
    private static final String EXTRA_hint = NumericKeypadDialogFragment.class.getSimpleName()+".hint";
    private static final String EXTRA_data = NumericKeypadDialogFragment.class.getSimpleName()+".data";

    public static final int status_ok = 11;
    public static final int status_delete = 12;

    private String mInputCode;
    private String mHint;
    private double mData;
    private Fragment targetFragment;

    private TextView mKeysView = null;

    private String mInput;
    private GridView mInputGrid;
    private NumericKeypad.Adapter mNumericKeypadAdapter;
    private OnKeyClickListener mOnKeyClickListener;

    public static NumericKeypadDialogFragment newInstance(String inputCode, double data, String hint) {
        Bundle args = new Bundle();
        NumericKeypadDialogFragment fragment = new NumericKeypadDialogFragment();
        if (hint==null) hint = "";

        args.putString(EXTRA_code, inputCode);
        args.putDouble(EXTRA_data, data);
        args.putString(EXTRA_hint, hint);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mInputCode = getArguments().getString(EXTRA_code);
        mData = getArguments().getDouble(EXTRA_data);
        mHint = getArguments().getString(EXTRA_hint);

        if (Double.isNaN(mData))
            mInput = "";
        else {
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(4);
            mInput = df.format(mData);
        }

        setRetainInstance(true);
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_numeric_keypad, null);

        mKeysView = (TextView) v.findViewById(R.id.dialogNumericKeypad_keys);

        mInputGrid = (GridView) v.findViewById(R.id.dialogNumericKeypad_grid);
        updateInputView();

        if (mNumericKeypadAdapter==null) { // may reuse the same adapter
            mNumericKeypadAdapter = new NumericKeypad.Adapter(this);
        }
        if (mOnKeyClickListener==null) { // may reuse the same listener
            mOnKeyClickListener = new OnKeyClickListener(this);
        }
        mNumericKeypadAdapter.setOnKeyClickListener(mOnKeyClickListener);
        mInputGrid.setAdapter(mNumericKeypadAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);

        Dialog dialog = builder.create();

        return dialog;

    } // end to onCreateDialog()


    // How to properly retain a dialog fragment through rotation
    // Reference:
    // http://stackoverflow.com/questions/14657490/how-to-properly-retain-a-dialogfragment-through-rotation

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        mInputGrid.setAdapter(null);
        mNumericKeypadAdapter.setOnKeyClickListener(null);
        mInputGrid = null;
        mKeysView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        targetFragment = null;
        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        if (mNumericKeypadAdapter!=null)
            mNumericKeypadAdapter.cleanup();
        if (mOnKeyClickListener!=null)
            mOnKeyClickListener.cleanup();
        mNumericKeypadAdapter = null;
        mOnKeyClickListener = null;
        super.onDestroy();
    }

    private void updateInputView() {
        if (mKeysView!=null) {
            mKeysView.setText(mHint + "\n" + mInput);
        }
    }

    private static class OnKeyClickListener extends NumericKeypad.OnKeyClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        NumericKeypadDialogFragment hostFragment;

        // This class of objects does not outlive its host, so no need to use weak references
        public OnKeyClickListener(NumericKeypadDialogFragment hostFragment) {
            super(hostFragment.mInput);
            this.hostFragment = hostFragment;
        }

        @Override
        public void onOk() {
            // Return to caller
            hostFragment.returnResult();
        }

        @Override
        public void onClickCompleted() {
            hostFragment.mInput = getBuffer();
            hostFragment.updateInputView();
        }
        public void cleanup() {
            hostFragment = null;
        }
    }

    private void returnResult() {
        if (targetFragment.getClass().equals(CircuitFragment.class)) {
            try {
                mData = Double.parseDouble(mInput);
                ((CircuitFragment)targetFragment).updateInput(mData, status_ok, mInputCode);
            } catch (Exception e) {
                ((CircuitFragment)targetFragment).updateInput(Double.NaN, status_delete, mInputCode);
            }
        }
        else if (targetFragment.getClass().equals(QuickConvertFragment.class)) {
            try {
                mData = Double.parseDouble(mInput);
                ((QuickConvertFragment)targetFragment).updateInput(mData, status_ok, mInputCode);
            } catch (Exception e) {
                ((QuickConvertFragment)targetFragment).updateInput(Double.NaN, status_delete, mInputCode);
            }
        }
        dismiss();
    }

    // Remember the caller fragment. When editing is done, pass data back to this fragment.
    public void setTargetFragment(CircuitFragment targetFragment) {
        this.targetFragment = targetFragment;
    }
    // Remember the caller fragment. When editing is done, pass data back to this fragment.
    public void setTargetFragment(QuickConvertFragment targetFragment) {
        this.targetFragment = targetFragment;
    }

}
