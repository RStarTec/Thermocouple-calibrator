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

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Sourcecode;


public class SourcecodeFragment extends Fragment {
    private static final String TAG = SourcecodeFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_index = SourcecodeFragment.class.getSimpleName()+".sourcecodeIndex";
    public static final String title = "Download programs";

    private static final int default_index = Sourcecode.defaultIndex;

    private static final int button_ids[] = {
            R.id.fragmentSourcecode_button1,
            R.id.fragmentSourcecode_button2,
            R.id.fragmentSourcecode_button3
    };



    private WebView mWebView;
    private int mSourcecodeIndex;

    private static final int size = Sourcecode.size;
    private final Button mButtons[] = new Button[size];
    private final OnButtonClickListener mOnButtonClickListeners[] = new OnButtonClickListener[size];


    public static SourcecodeFragment newInstance(int sourcecodeIndex) {
        Bundle args = new Bundle();

        args.putInt(EXTRA_index, sourcecodeIndex);

        SourcecodeFragment fragment = new SourcecodeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mSourcecodeIndex = getArguments().getInt(EXTRA_index, default_index);

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sourcecode, parent, false);

        getActivity().setTitle(title);

        mWebView = (WebView) v.findViewById(R.id.fragmentSourcecode_webView);
        load();

        for (int index=0; index<size; index++) {
            mButtons[index] = (Button) v.findViewById(button_ids[index]);
            mButtons[index].setText(Sourcecode.type[index]);

            // May reuse listeners
            if (mOnButtonClickListeners[index]==null)
                mOnButtonClickListeners[index] = new OnButtonClickListener(this, index);

            mButtons[index].setOnClickListener(mOnButtonClickListeners[index]);
        }

        return v;
    }


    @Override
    public void onDestroyView() {
        for (int index=0; index<mButtons.length; index++)
            mButtons[index].setOnClickListener(null);
        // Do not destroy listeners. They may be reused.
        mWebView = null;

        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        // These listeners may be reused on orientation change.
        // So only destroy them when the fragment is destroyed.
        for (int index=0; index<mOnButtonClickListeners.length; index++) {
            if (mOnButtonClickListeners[index] != null)
                mOnButtonClickListeners[index].cleanup();
            mOnButtonClickListeners[index] = null;
        }
        super.onDestroy();
    }


    private void load() {
        try {
            Uri path = Uri.parse("file:///android_asset/" + Sourcecode.getFilename(mSourcecodeIndex));
            mWebView.loadUrl(path.toString());

        } catch (Exception e) {
        }
    }


    private static class OnButtonClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        SourcecodeFragment hostFragment;
        int sourcecodeIndex;
        public OnButtonClickListener(SourcecodeFragment hostFragment, int sourcecodeIndex) {
            super();
            this.hostFragment = hostFragment;
            this.sourcecodeIndex = sourcecodeIndex;
        }

        @Override
        public void onClick(View view) {
            if (hostFragment.mSourcecodeIndex != sourcecodeIndex) {
                hostFragment.mSourcecodeIndex = sourcecodeIndex;
                hostFragment.load();
            }
        }
        public void cleanup() { hostFragment = null; }
    }
}
