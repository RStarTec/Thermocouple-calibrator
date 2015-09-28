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

package com.rstar.mobile.thermocouple.ui.unused;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.tables.ThermoCoupleTable;
import com.rstar.mobile.thermocouple.ui.Graph;


public class SeebeckCmpFragment extends Fragment {
    private static final String TAG = SeebeckCmpFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_typeCode = SeebeckCmpFragment.class.getSimpleName()+".typeCode";

    private static final int[] colors = Graph.standardColors; // use colors provided by the Graph


    private static final String defaultTypeCode = ThermoCouple.all;
    private static final double SeebeckScaleUp = 1000.0d;   // magnify the units
    private static final String SeebeckUnit = "μV";
    private String TLabel;
    private String SLabel;

    private String mTypeCode;
    private ThermoCoupleTable mThermoCoupleTable;
    private ThermoCouple mThermoCouple;

    double Tmin;
    double Tmax;
    double Smin;
    double Smax;

    PointF[][] mCurves;
    String[] mCurveLabels;

    private Graph.OnVerticalBarChangeListener mListener;
    private Graph.VerticalBarMarker mMarker;

    public static SeebeckCmpFragment newInstance(String typeCode) {
        Bundle args = new Bundle();

        if (typeCode==null || typeCode.equals("")) typeCode = defaultTypeCode;
        args.putString(EXTRA_typeCode, typeCode);

        SeebeckCmpFragment fragment = new SeebeckCmpFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mTypeCode = getArguments().getString(EXTRA_typeCode, defaultTypeCode);


        if (!mTypeCode.equals(ThermoCouple.all)) {
            mMarker = new Graph.VerticalBarMarker();
            SLabel = getString(R.string.label_S);
            TLabel = getString(R.string.label_T);

            mThermoCouple = new ThermoCouple(getActivity());
            try {
                mThermoCoupleTable = new ThermoCoupleTable(getActivity(), mTypeCode);

                mCurveLabels = new String[2];
                mCurves = new PointF[2][];  // only 1 type, but there 2 methods to compute
                mMarker.FYs = new double[2];

                mCurveLabels[0] = "Fn";
                Fn fn = mThermoCouple.getFn(mTypeCode);
                mCurves[0] = fn.getSeebeckCurveControlPoints();
                Tmin = fn.getTmin();
                Tmax = fn.getTmax();


                mCurveLabels[1] = "Table";
                mCurves[1] = mThermoCoupleTable.getSeebeckCurveControlPoints(ThermoCoupleTable.InternalPts);
                double Tmin2 = mThermoCoupleTable.getTMinOfTable();
                double Tmax2 = mThermoCoupleTable.getEMaxOfTable();
                if (Tmin > Tmin2) Tmin = Tmin2;
                if (Tmax < Tmax2) Tmax = Tmax2;
            }
            catch (Exception e) {
                Savelog.d(TAG, debug, "Cannot load Table " + e.getMessage());

                mCurveLabels = new String[1];
                mCurves = new PointF[1][];  // only 1 type, only 1 method available
                mMarker.FYs = new double[1];

                mCurveLabels[0] = "Fn";
                Fn fn = mThermoCouple.getFn(mTypeCode);
                mCurves[0] = fn.getSeebeckCurveControlPoints();
                Tmin = fn.getTmin();
                Tmax = fn.getTmax();
            }
            TLabel += "/" + mThermoCouple.getFn(mTypeCode).getUnitT();

            scaleSeebeckRange();

            // mid point
            computeSvalues((Tmin + Tmax) / 2);
        }
        else {
            // Do nothing
        }

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
            textView.setText("Seebeck approximation comparison cannot be done simultaneously for ALL types. Please select a specific type.");
            return v;
        }
        else {

            View v = inflater.inflate(R.layout.fragment_graph, parent, false);

            Graph graph = (Graph) v.findViewById(R.id.fragmentGraph_graph);

            graph.setCurves(Tmin, Tmax, Smin, Smax, mCurves);
            graph.setCurveLabels(mCurveLabels);
            graph.setAxesLabels(TLabel, SLabel);
            graph.setCurveColors(colors);
            mListener = new OnGraphValueChangedListener(this);
            graph.setOnVerticalBarChangeListener(mListener);
            graph.setNotifyWhileDragging(true);
            graph.setVerticalBarMarker(mMarker);
            return v;
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

        // The first one is always from the function approach
        Fn fn = mThermoCouple.getFn(mTypeCode);
        try {
            Svalues[0] = fn.compute_dEdT(t) * SeebeckScaleUp;
        } catch (Exception e) {
            Svalues[0] = badS;
            // Set to something smaller than minimum, which is to be ignored
        }

        if (Svalues.length>1) {
            try {
            Svalues[1] = mThermoCoupleTable.compute_dEdT(t) * SeebeckScaleUp;
            } catch (Exception e) {
                Svalues[1] = badS;
                // Set to something smaller than minimum, which is to be ignored
            }
        }
    }


    private static class OnGraphValueChangedListener implements Graph.OnVerticalBarChangeListener{
        SeebeckCmpFragment hostFragment;
        public OnGraphValueChangedListener(SeebeckCmpFragment hostFragment) {
            this.hostFragment = hostFragment;
        }
        @Override
        public void onVerticalBarValuesChanged(Graph graph, double x) {
            hostFragment.computeSvalues(x);
            graph.setVerticalBarMarker(hostFragment.mMarker);
        }
    }
}
