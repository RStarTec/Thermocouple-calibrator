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
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.IO;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

import java.io.IOException;

public class LegalDialogFragment extends DialogFragment {
    private static final String TAG = LegalDialogFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String dialogTag = LegalDialogFragment.class.getSimpleName()+"_tag";
    private static final String EXTRA_id = LegalDialogFragment.class.getSimpleName()+".id";

    private CharSequence mData = null;
    private TextView mTextView = null;
    private Button mOkButton = null;

    public static LegalDialogFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_id, id);

        LegalDialogFragment fragment = new LegalDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = getArguments().getInt(EXTRA_id);
        mData = loadData(getActivity().getApplicationContext(), id);
        setRetainInstance(true);
        Savelog.d(TAG, debug, "This dialog fragment is retained.");
    }

    
    /* This dialog has a title, a TextView and one button (OK).
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_legal, null);
        mTextView = (TextView) v.findViewById(R.id.dialogCopyright_content_id);
        mTextView.setText(mData);
        /* Use the Builder class for convenient dialog construction.
         * The dialog builder just needs to handle OK.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
            .setPositiveButton(R.string.button_OK, null);
        
        Dialog dialog = builder.create();
        return dialog;
        
    } // end to onCreateDialog()
    
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d!=null) {
            mOkButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
        }
    }

    @Override
    public void onDestroyView() {
        /* As of Aug 2013, Dialog Fragment has a bug with its 
         * SetRetainedInstance() method. Therefore, the following
         * need to be done to retain the dialog fragment
         */
        if (getDialog()!=null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        if (mOkButton!=null) {
            mOkButton.setOnClickListener(null);
            mOkButton = null;
        }
        super.onDestroyView();
    }

    
    private String loadData(Context context, int id) {
        try {
            return IO.getRawResourceAsString(context, id);
        } catch (IOException e) {
            Savelog.e(TAG, "message not available.");
            return "";
        }
    }
}
