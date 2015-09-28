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

package com.rstar.mobile.thermocouple.tables;

import android.content.Context;
import android.graphics.PointF;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.IO;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

public class ThermoCoupleTable {
    private static final String TAG = ThermoCoupleTable.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final int InternalPts = 64;   // choose 3*n+2

    public static final int size = 8;

    public static final String codeB = "B";
    public static final String codeE = "E";
    public static final String codeJ = "J";
    public static final String codeK = "K";
    public static final String codeN = "N";
    public static final String codeR = "R";
    public static final String codeS = "S";
    public static final String codeT = "T";

    private static final int BadIndex = -1;

    private static final int[] ResourceTableId = {
            R.raw.type_b_tab,
            R.raw.type_e_tab,
            R.raw.type_j_tab,
            R.raw.type_k_tab,
            R.raw.type_n_tab,
            R.raw.type_r_tab,
            R.raw.type_s_tab,
            R.raw.type_t_tab
    };

    private static final String DataFile_extension = ".txt";

    public static final String all = "All";
    public static final String[] type = { codeB, codeE, codeJ, codeK, codeN, codeR, codeS, codeT };


    public static final double tolerance = 0.0001;

    public static final int Trow = 0;
    public static final int Erow = 1;

    private int typeIndex = BadIndex;
    private double[][] table = null;
    private int TminIndex = BadIndex;
    private int TmaxIndex = BadIndex;
    private int EminIndex = BadIndex;
    private int EmaxIndex = BadIndex;

    public ThermoCoupleTable(Context context, String code) throws Exception {
        int index = getIndex(code);
        if (context==null || index==BadIndex) {
            clean();
            throw new Exception("Bad type");
        }

        if (table==null || index!=typeIndex) {
            String data = getData(context, index);
            TableParser tp = new TableParser();
            table = tp.parse(data);
            typeIndex = index;
            setLimits();
            Savelog.d(TAG, debug, "loading table completed for Type " + code);
        }
        else {
            // already exists. Do nothing.
        }
    }

    private void setLimits() {
        if (table==null) return;

        TminIndex = 0;
        TmaxIndex = 0;
        EminIndex = 0;
        EmaxIndex = 0;

        double Tmin = table[Trow][TminIndex];
        double Tmax = table[Trow][TmaxIndex];
        double Emin = table[Erow][EminIndex];
        double Emax = table[Erow][EmaxIndex];

        for (int index=1; index<table[Trow].length; index++) {
            if (Tmin > table[Trow][index]) {
                Tmin = table[Trow][index];
                TminIndex = index;
            }
            if (Tmax < table[Trow][index]) {
                Tmax = table[Trow][index];
                TmaxIndex = index;
            }
            if (Emin > table[Erow][index]) {
                Emin = table[Erow][index];
                EminIndex = index;
            }
            if (Emax < table[Erow][index]) {
                Emax = table[Erow][index];
                EmaxIndex = index;
            }
        }
    }

    private void clean() {
        table = null;
        typeIndex = BadIndex;
        TminIndex = BadIndex;
        TmaxIndex = BadIndex;
        EminIndex = BadIndex;
        EmaxIndex = BadIndex;
    }

    public double getTMinOfTable() {
        return table[Trow][TminIndex];
    }
    public double getTMaxOfTable() {
        return table[Trow][TmaxIndex];
    }
    public double getEMinOfTable() {
        return table[Erow][EminIndex];
    }
    public double getEMaxOfTable() {
        return table[Erow][EmaxIndex];
    }

    public int getTableSize() {
        return table[Trow].length;
    }


    public int getTindex(double T) {
        // T is sorted. So use binary search
        int low = 0;
        int high = getTableSize()-1;
        int midpt = BadIndex;
        if (T>table[Trow][high]) return BadIndex;
        if (T<table[Trow][low]) return BadIndex;

        double difference = -1.0;

        while (high >= low) {
            midpt = (low+high)/2;
            difference = table[Trow][midpt] - T;

            if (Math.abs(difference) < tolerance)
                return midpt;
            else if (difference < 0) {
                low = midpt + 1;
            }
            else if (difference > 0) {
                high = midpt - 1;
            }
        }
        // Not found

        double lowDiff = Math.abs(table[Trow][low] - T);
        double highDiff = Math.abs(table[Trow][high] - T);
        double midDiff = Math.abs(difference);

        if (lowDiff <= highDiff && lowDiff <= midDiff) return low;
        else if (highDiff <= lowDiff && highDiff <= midDiff) return high;
        else return midpt;
    }

    public double getE(double T) throws Exception {
        int index = getTindex(T);
        Savelog.d(TAG, debug, "T=" + T + " closest entry at t=" + table[Trow][index] + " E=" + table[Erow][index]);
        if (index==BadIndex)
            throw new Exception(getInputOutOrRangeMessage(T));
        return table[Erow][index];
    }

    public double compute_dEdT(double T) throws Exception {
        int index = getTindex(T);

        Savelog.d(TAG, debug, "T=" + T + " closest entry at t=" + table[Trow][index] + " E=" + table[Erow][index]);
        if (index==BadIndex)
            throw new Exception(getInputOutOrRangeMessage(T));
        else{
            int prevIndex = BadIndex;
            int nextIndex = BadIndex;
            double S1 = 0;
            double S2 = 0;
            double S3 = 0;
            if (index>0) {
                prevIndex = index-1;
                S1 = (table[Erow][index] - table[Erow][prevIndex])/(table[Trow][index]-table[Trow][prevIndex]);
            }
            if (index<getTableSize()-1) {
                nextIndex = index+1;
                S2 = (table[Erow][nextIndex] - table[Erow][index])/(table[Trow][nextIndex]-table[Trow][index]);
            }
            if (prevIndex==BadIndex) return S2;
            if (nextIndex==BadIndex) return S1;

            S3 = (table[Erow][nextIndex] - table[Erow][prevIndex])/(table[Trow][nextIndex]-table[Trow][prevIndex]);
            return S3;
        }

    }



    private String getInputOutOrRangeMessage(double temperature) {
        return "Type " + type[typeIndex] + " input T=" + temperature + " out of range";
    }



    public PointF[] getSeebeckCurveControlPoints(int internalPts) throws Exception {
        if (internalPts<=0) return null;

        PointF[] point = new PointF[internalPts];

        int totalPoints = getTableSize();
        double intervals = internalPts;
        int pointsPerInterval = (int)(totalPoints / intervals);

        int startPoint = pointsPerInterval/2;

        for (int count=0; count<internalPts; count++) {
            int index = startPoint + count*pointsPerInterval;
            double T = table[Trow][index];
            point[count] = new PointF();
            point[count].x = (float) T;
            point[count].y = (float) compute_dEdT(T);
        }

        Savelog.d(TAG, debug, "control point count=" + internalPts);
        return point;
    }


    private int getIndex(String code) {
        if (code==null || code.equals("")) return BadIndex;
        for (int index=0; index<size; index++) {
            if (code.equals(type[index])) return index;
        }
        return BadIndex;
    }

    public String getType() {
        return type[typeIndex];
    }


    // Use the files that come with the resource.
    // (Note: resource file name is not the same as the filename on NIST)
    // Require the file names to be ordered properly.
    private String getData(Context context, int index) {
        int id = ResourceTableId[index];

        // Get the file name based on id
        String resourceName = context.getResources().getResourceEntryName(id) + DataFile_extension;

        // Get the data from the resource file
        String data = "";
        try {
            data = IO.getRawResourceAsString(context, id);
            Savelog.d(TAG, debug, "Loaded resource file " + resourceName);
        } catch (Exception e1) {
            data = "";
        }
        return data;
    }
}
