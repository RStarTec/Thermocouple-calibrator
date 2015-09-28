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
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.activities.ImageActivity;


public class GraphFragment extends Fragment {
    private static final String TAG = GraphFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title = "Reference Function Graphs";

    public static final String EXTRA_typeCode = GraphFragment.class.getSimpleName()+".typeCode";
    private static final String defaultTypeCode = ThermoCouple.all;

    private static final int[] colors = Graph.standardColors; // use colors provided by the Graph

    private static final int Button_referenceFunction = 0;
    private static final int Button_inverseFunction = 1;
    private static final int[] buttonIds = {R.id.fragmentGraph_buttonFn, R.id.fragmentGraph_buttonFnInv};
    private static final int buttonSize = buttonIds.length;


    private String TLabel;
    private String ELabel;

    private String mTypeCode;
    private ThermoCouple mThermoCouple;

    double Tmin;
    double Tmax;
    double Emin;
    double Emax;

    PointF[][] mCurves;
    String[] mCurveLabels;

    private Graph mGraph;
    private Graph.VerticalBarMarker mMarker;
    private final Button[] mButtons = new Button[buttonSize];
    private View mButtonBar;
    private OnGraphValueChangedListener mOnGraphValueChangedListener;
    private final OnButtonClickListener[] mOnButtonClickListeners = new OnButtonClickListener[buttonSize];

    private String[] ThermocoupleTypes = new String[ThermoCouple.size+1];
    private Spinner mSpinner = null;
    private ArrayAdapter<String> mAdapter = null;
    private SpinnerOnItemSelectedListener mSpinnerListener;


    public static GraphFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("")) typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        GraphFragment fragment = new GraphFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);

        // Create an array of all the types plus ALL
        for (int index=0; index<ThermocoupleTypes.length-1; index++)
            ThermocoupleTypes[index] = ThermoCouple.type[index];
        ThermocoupleTypes[ThermocoupleTypes.length-1] = ThermoCouple.all;

        mThermoCouple = new ThermoCouple(getActivity());
        initiateGraphParameters();

        // Make sure to retain the fragment so that data retrieval is
        // not restarted at every rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_graph, parent, false);

        getActivity().setTitle(title);

        // Allow reuse
        if (mAdapter==null)
            mAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, ThermocoupleTypes);
        // Allow reuse
        if (mSpinnerListener==null)
            mSpinnerListener = new SpinnerOnItemSelectedListener(this);

        mSpinner = (Spinner) v.findViewById(R.id.fragmentGraph_thermocoupleType);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(mSpinnerListener);
        selectSpinnerItem();

        mGraph = (Graph) v.findViewById(R.id.fragmentGraph_graph);

        mGraph.setCurves(Tmin, Tmax, Emin, Emax, mCurves);
        mGraph.setCurveLabels(mCurveLabels);
        mGraph.setAxesLabels(TLabel, ELabel);
        mGraph.setCurveColors(colors);

        // Allow reuse
        if (mOnGraphValueChangedListener==null)
            mOnGraphValueChangedListener = new OnGraphValueChangedListener(this);

        mGraph.setOnVerticalBarChangeListener(mOnGraphValueChangedListener);
        mGraph.setNotifyWhileDragging(true);
        mGraph.setVerticalBarMarker(mMarker);

        // hook up the images
        for (int index=0; index<buttonSize; index++) {
            mButtons[index] = (Button) v.findViewById(buttonIds[index]);

            // Allow reuse
            if (mOnButtonClickListeners[index]==null)
                mOnButtonClickListeners[index] = new OnButtonClickListener(this, index);

            mButtons[index].setOnClickListener(mOnButtonClickListeners[index]);
        }
        mButtonBar = v.findViewById(R.id.fragmentGraph_buttonBar);


        if (mTypeCode.equals(defaultTypeCode)) {
            mButtonBar.setVisibility(View.GONE);
        }
        else {
            mButtonBar.setVisibility(View.VISIBLE);
        }

        return v;
    }



    @Override
    public void onDestroyView() {
        if (mGraph!=null) {
            mGraph.cleanup();
            mGraph = null;
        }
        for (int index=0; index<buttonSize; index++) {
            if (mButtons[index]!=null)
                mButtons[index].setOnClickListener(null);
            mButtons[index] = null;
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
        mMarker = null;
        mCurveLabels = null;
        mCurves = null;

        // Since these listeners are reusable when orientation changes,
        // clean them up only on destroy.
        if (mOnGraphValueChangedListener!=null) {
            mOnGraphValueChangedListener.cleanup();
            mOnGraphValueChangedListener = null;
        }

        for (int index=0; index<buttonSize; index++) {
            if (mOnButtonClickListeners[index]!=null)
                mOnButtonClickListeners[index] = null;
        }

        if (mSpinnerListener!=null) {
            mSpinnerListener.cleanup();
            mSpinnerListener = null;
        }
        mAdapter = null;

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



    private void initiateGraphParameters() {
        if (mMarker==null) {
            mMarker = new Graph.VerticalBarMarker();
        }

        ELabel = getString(R.string.label_E);
        TLabel = getString(R.string.label_T);

        if (mTypeCode.equals(ThermoCouple.all)) {
            Tmin = mThermoCouple.getTMinOfFn();
            Tmax = mThermoCouple.getTMaxOfFn();
            Emin = mThermoCouple.getApproxEMinOfFn();
            Emax = mThermoCouple.getApproxEMaxOfFn();

            mCurves = new PointF[ThermoCouple.size][];
            mCurveLabels = new String[ThermoCouple.size];
            String type = "";
            for (int index=0; index< ThermoCouple.size; index++) {
                type = ThermoCouple.type[index];
                mCurves[index] = mThermoCouple.getFn(type).getEMFCurveControlPoints(Fn.InternalPtsPerSegmentForE);
                mCurveLabels[index] = type;
            }
            mMarker.FYs = new double[ThermoCouple.size];

            ELabel += "/" + mThermoCouple.getFn(type).getUnitEMF();
            TLabel += "/" + mThermoCouple.getFn(type).getUnitT();
        }
        else {
            Fn fn = mThermoCouple.getFn(mTypeCode);
            Tmin = fn.getTmin();
            Tmax = fn.getTmax();
            Emin = fn.getApproxEmin();
            Emax = fn.getApproxEmax();

            mCurveLabels = new String[1];
            mCurveLabels[0] = mTypeCode;
            mCurves = new PointF[1][];  // only 1 type
            mCurves[0] = fn.getEMFCurveControlPoints(Fn.InternalPtsPerSegmentForE);
            mMarker.FYs = new double[1];
            ELabel += "/" + mThermoCouple.getFn(mTypeCode).getUnitEMF();
            TLabel += "/" + mThermoCouple.getFn(mTypeCode).getUnitT();
        }

        // mid point
        computeEvalues((Tmin+Tmax)/2);


        // If the graph view already exists, then update it.
        if (mGraph!=null) {
            mGraph.setCurves(Tmin, Tmax, Emin, Emax, mCurves);
            mGraph.setCurveLabels(mCurveLabels);
            mGraph.setAxesLabels(TLabel, ELabel);
            mGraph.setCurveColors(colors);
            mGraph.setVerticalBarMarker(mMarker);
            mGraph.setThumbPositionByMarker(); // must relocate thumb explicit when using the same graph for a different thermocouple type
            mGraph.invalidate();
        }

        // If the buttons are already hooked up, then update them.
        if (mButtonBar!=null) {
            if (mTypeCode.equals(defaultTypeCode)) {
                mButtonBar.setVisibility(View.GONE);
            }
            else {
                mButtonBar.setVisibility(View.VISIBLE);
            }
        }

    }


    private void computeEvalues(double t) {
        double[] Evalues = mMarker.FYs;
        mMarker.FX = t;
        Savelog.d(TAG, debug, "setting marker.x="+mMarker.FX);

        double badE = Emin - 1.0;
        if (Evalues.length>1) {
            for (int index=0; index<Evalues.length; index++) {
                String typeCode = ThermoCouple.type[index];
                Fn fn = mThermoCouple.getFn(typeCode);
                try {
                    Evalues[index] = fn.computeE(t);
                } catch (Exception e) {
                    Evalues[index] = badE;
                    // Set to something smaller than minimum, which is to be ignored
                }
            }
        }
        else if (Evalues.length==1) {
            Fn fn = mThermoCouple.getFn(mTypeCode);
            try {
                Evalues[0] = fn.computeE(t);
            } catch (Exception e) {
                Evalues[0] = badE;
                // Set to something smaller than minimum, which is to be ignored
            }
        }
    }



    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        GraphFragment hostFragment;
        public SpinnerOnItemSelectedListener(GraphFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (!hostFragment.mTypeCode.equals(hostFragment.ThermocoupleTypes[pos])) {
                hostFragment.mTypeCode = hostFragment.ThermocoupleTypes[pos];
                // redraw graph
                hostFragment.initiateGraphParameters();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void cleanup() {
            hostFragment = null;
        }
    }



    private static class OnGraphValueChangedListener implements Graph.OnVerticalBarChangeListener{
        // This class of objects does not outlive its host, so no need to use weak references
        GraphFragment hostFragment;
        public OnGraphValueChangedListener(GraphFragment hostFragment) {
            this.hostFragment = hostFragment;
        }
        @Override
        public void onVerticalBarValuesChanged(Graph graph, double x) {
            hostFragment.computeEvalues(x);
            graph.setVerticalBarMarker(hostFragment.mMarker);
        }
        public void cleanup() { hostFragment = null; }
    }


    private static class OnButtonClickListener implements View.OnClickListener {
        // This class of objects does not outlive its host, so no need to use weak references
        GraphFragment hostFragment;
        int buttonType;
        public OnButtonClickListener(GraphFragment hostFragment, int buttonType) {
            super();
            this.hostFragment = hostFragment;
            this.buttonType = buttonType;
        }

        @Override
        public void onClick(View view) {
            int id = 0;
            if (buttonType==Button_referenceFunction) {
                // use fn
                id = hostFragment.mThermoCouple.getFnImageId(hostFragment.mTypeCode);
            }
            else if (buttonType==Button_inverseFunction) {
                // use inverse fn
                id = hostFragment.mThermoCouple.getFnInvImageId(hostFragment.mTypeCode);
            }
            if (id!=0) {
                Intent intent = new Intent(hostFragment.getActivity(), ImageActivity.class);
                intent.putExtra(ImageActivity.EXTRA_imageId, id);
                hostFragment.startActivity(intent);
            }
        }
        public void cleanup() { hostFragment = null; }
    }

}
