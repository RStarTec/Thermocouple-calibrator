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

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.Polynomial;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;


public class SeebeckFragment extends Fragment {
    private static final String TAG = SeebeckFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String title = "Seebeck Coefficients";

    public static final String EXTRA_typeCode = SeebeckFragment.class.getSimpleName()+".typeCode";
    private static final String defaultTypeCode = ThermoCouple.all;

    private static final int[] colors = Graph.standardColors; // use colors provided by the Graph
    private static final double SeebeckScaleUp = 1000.0d;   // magnify the units
    private static final String SeebeckUnit = "μV";

    // T should be strictly within range. But S may need a little extra margin.
    // This margin is only needed for the Seebeck plot. The EMF plot does not need it.
    private static final double SBoundaryTolerance = 0.5;

    private String TLabel;
    private String SLabel;

    private String mTypeCode;
    private ThermoCouple mThermoCouple;


    double Tmin;
    double Tmax;
    double Smin;
    double Smax;

    PointF[][] mCurves;
    String[] mCurveLabels;

    private Graph mGraph;
    private OnGraphValueChangedListener mOnGraphValueChangedListener;
    private Graph.VerticalBarMarker mMarker;

    private String[] ThermocoupleTypes = new String[ThermoCouple.size+1];
    private Spinner mSpinner = null;
    private ArrayAdapter<String> mAdapter = null;
    private SpinnerOnItemSelectedListener mSpinnerListener;
    private boolean mShowControlPoints = false;


    public static SeebeckFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("")) typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        SeebeckFragment fragment = new SeebeckFragment();
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
        View v = inflater.inflate(R.layout.fragment_seebeck, parent, false);

        getActivity().setTitle(title);

        // Allow reuse
        if (mAdapter==null)
            mAdapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.spinner_item, ThermocoupleTypes);
        // Allow reuse
        if (mSpinnerListener==null)
            mSpinnerListener = new SpinnerOnItemSelectedListener(this);

        mSpinner = (Spinner) v.findViewById(R.id.fragmentSeebeck_thermocoupleType);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(mSpinnerListener);
        selectSpinnerItem();


        mGraph = (Graph) v.findViewById(R.id.fragmentSeebeck_graph);

        mGraph.setCurves(Tmin, Tmax, Smin-SBoundaryTolerance, Smax+SBoundaryTolerance, mCurves);
        mGraph.setCurveLabels(mCurveLabels);
        mGraph.setAxesLabels(TLabel, SLabel);
        mGraph.setCurveColors(colors);
        mGraph.showControlPoints(mShowControlPoints);

        // allow reuse
        if (mOnGraphValueChangedListener==null)
            mOnGraphValueChangedListener = new OnGraphValueChangedListener(this);

        mGraph.setOnVerticalBarChangeListener(mOnGraphValueChangedListener);
        mGraph.setNotifyWhileDragging(true);
        mGraph.setVerticalBarMarker(mMarker);

        TextView textView = (TextView) v.findViewById(R.id.fragmentSeebeck_text);
        textView.setText(getString(R.string.info_seebeck));

        return v;
    }



    @Override
    public void onDestroyView() {
        if (mGraph!=null) {
            mGraph.cleanup();
            mGraph = null;
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

        SLabel = getString(R.string.label_S);
        TLabel = getString(R.string.label_T);

        if (mTypeCode.equals(ThermoCouple.all)) {
            Tmin = mThermoCouple.getTMinOfFn();
            Tmax = mThermoCouple.getTMaxOfFn();

            mCurves = new PointF[ThermoCouple.size][];
            mCurveLabels = new String[ThermoCouple.size];
            String type = "";
            for (int index=0; index< ThermoCouple.size; index++) {
                type = ThermoCouple.type[index];
                mCurves[index] = mThermoCouple.getFn(type).getSeebeckCurveControlPoints();
                mCurveLabels[index] = type;
            }
            mMarker.FYs = new double[ThermoCouple.size];
            mShowControlPoints = false;
            TLabel += "/" + mThermoCouple.getFn(type).getUnitT();
        }
        else {
            Fn fn = mThermoCouple.getFn(mTypeCode);
            Tmin = fn.getTmin();
            Tmax = fn.getTmax();

            mCurveLabels = new String[1];
            mCurveLabels[0] = mTypeCode;
            mCurves = new PointF[1][];  // only 1 type
            mCurves[0] = fn.getSeebeckCurveControlPoints();
            mMarker.FYs = new double[1];
            mShowControlPoints = true;
            TLabel += "/" + mThermoCouple.getFn(mTypeCode).getUnitT();
        }

        scaleSeebeckRange();

        // mid point
        computeSvalues((Tmin + Tmax) / 2);


        if (mGraph!=null) {
            mGraph.setCurves(Tmin, Tmax, Smin-SBoundaryTolerance, Smax+SBoundaryTolerance, mCurves);
            mGraph.setCurveLabels(mCurveLabels);
            mGraph.setAxesLabels(TLabel, SLabel);
            mGraph.setCurveColors(colors);
            mGraph.showControlPoints(mShowControlPoints);
            mGraph.setVerticalBarMarker(mMarker);
            mGraph.setThumbPositionByMarker(); // must relocate thumb explicit when using the same graph for a different thermocouple type
            mGraph.invalidate();
        }
    }



    private void scaleSeebeckRange() {
        // Scale up the units
        for (int index=0; index<mCurves.length; index++) {
            if (mCurves[index]!=null) {
                for (int j = 0; j < mCurves[index].length; j++) {
                    mCurves[index][j].y *= SeebeckScaleUp;
                }
            }
        }

        SLabel += "/" + SeebeckUnit + "/" + mThermoCouple.getUnitT();

        if (mCurves!=null) {
            if (mCurves[0].length>0) {
                Smin = mCurves[0][0].y;
                Smax = mCurves[0][0].y;
            }
            for (int index=0; index<mCurves.length; index++) {
                if (mCurves[index]!=null) {
                    for (int j = 0; j < mCurves[index].length; j++) {
                        if (Smin>mCurves[index][j].y) Smin = mCurves[index][j].y;
                        if (Smax<mCurves[index][j].y) Smax = mCurves[index][j].y;
                    }
                }
            }
        }
    }

    private void computeSvalues(double t) {
        double[] Svalues = mMarker.FYs;
        mMarker.FX = t;

        double badS = Smin - 1.0;
        if (Svalues.length>1) {
            for (int index=0; index<Svalues.length; index++) {
                String typeCode = ThermoCouple.type[index];
                Fn fn = mThermoCouple.getFn(typeCode);
                try {
                    Svalues[index] = fn.compute_dEdT(t) * SeebeckScaleUp;
                } catch (Exception e) {
                    Svalues[index] = badS;
                    // Set to something smaller than minimum, which is to be ignored
                }
            }
        }
        else if (Svalues.length==1) {
            Fn fn = mThermoCouple.getFn(mTypeCode);
            try {
                Svalues[0] = fn.compute_dEdT(t) * SeebeckScaleUp;
            } catch (Exception e) {
                Svalues[0] = badS;
                // Set to something smaller than minimum, which is to be ignored
            }
        }
    }


    private static class OnGraphValueChangedListener implements Graph.OnVerticalBarChangeListener{
        SeebeckFragment hostFragment;
        public OnGraphValueChangedListener(SeebeckFragment hostFragment) {
            this.hostFragment = hostFragment;
        }
        @Override
        public void onVerticalBarValuesChanged(Graph graph, double x) {
            hostFragment.computeSvalues(x);
            graph.setVerticalBarMarker(hostFragment.mMarker);
        }
        public void cleanup() { hostFragment = null; }
    }



    private static class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        SeebeckFragment hostFragment;
        public SpinnerOnItemSelectedListener(SeebeckFragment hostFragment) {
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



}
