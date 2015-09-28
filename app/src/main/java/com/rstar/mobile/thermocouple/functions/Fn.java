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

package com.rstar.mobile.thermocouple.functions;

import android.graphics.PointF;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.Savelog;

import org.json.JSONArray;
import org.json.JSONObject;

public class Fn {
    private static final String TAG = Fn.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final int InternalPtsPerSegmentForE = 8;   // choose 3*n+2 for E
    public static final int InternalPtsPerSegmentForS = 30;   // choose 3*n for S use 9
    private static final int BadIndex = -1;

    private String name = "";
    private String equation[] = null;
    private String coefficientType = "";
    private String unitT = "";
    private String unitEMF = "";

    private Polynomial[] polynomials = null;
    private Exponential exponential = null;


    public Fn(String data) {
        try {
            load(data);
        } catch (Exception e) {
            clear();
        }
    }

    private void clear() {
        name = "";
        equation = new String[0];
        coefficientType = "";
        unitT = "";
        unitEMF = "";
        polynomials = new Polynomial[0];
        exponential = null;
    }

    public double getTmin() {
        double Tmin;
        if (polynomials ==null || polynomials.length==0) return 0.0;
        Tmin = polynomials[0].Tmin;
        for (int index=1; index< polynomials.length; index++) {
            if (Tmin > polynomials[index].Tmin)
                Tmin = polynomials[index].Tmin;
        }
        return Tmin;
    }

    public double getTmax() {
        double Tmax;
        if (polynomials ==null || polynomials.length==0) return 0.0;
        Tmax = polynomials[0].Tmax;
        for (int index=1; index< polynomials.length; index++) {
            if (Tmax < polynomials[index].Tmax)
                Tmax = polynomials[index].Tmax;
        }
        return Tmax;
    }

    // NOTE: This is only an estimate without doing any thorough search.
    // E is NOT monotonously increasing with respect to Temperature, especially in the case of Type B.
    // However, the overall trend between T and E is approximately linear.
    // In this project, this function is used only for finding the scope of E for graphing purpose.
    // For such a purpose, an approximation of Emin and Emax is good enough.
    public double getApproxEmin() {
        try {
            return computeE(getTmin());
        }
        catch (Exception e) {
            return 0x0.0p0;
        }
    }

    // NOTE: This is only an estimate without doing any thorough search.
    // E is NOT monotonously increasing with respect to Temperature, especially in the case of Type B.
    // However, the overall trend between T and E is approximately linear.
    // In this project, this function is used only for finding the scope of E for graphing purpose.
    // For such a purpose, an approximation of Emin and Emax is good enough.
    public double getApproxEmax() {
        try {
            return computeE(getTmax());
        }
        catch (Exception e) {
            return 0x0.0p0;
        }
    }


    public double computeE(double temperature) throws Exception {
        // Throw an exception if temperature is out of range
        int index = findPolynomial(temperature);
        if (index!=BadIndex) {
            double data = polynomials[index].compute(temperature);

            double correction = 0.0;
            if (exponential!=null && temperature>0) {
                // according to the function definition from NIST,
                // the exponential term is available only for type K when temperature > 0
                // Reference http://srdata.nist.gov/its90/download/download.html
                correction = exponential.compute(temperature);
            }
            return data + correction;
        }
        else {
            throw new Exception(getInputOutOrRangeMessage(temperature));
        }
    }


    public void reportTRange(double temperature) {
        Savelog.d(TAG, debug, "Type" + name);
        for (int j=0; j<polynomials.length; j++) {
            Savelog.d(TAG, debug, "P" + j + ": [" + polynomials[j].Tmin + "," + polynomials[j].Tmax);
        }
        Savelog.d(TAG, debug, "T input = " + temperature + " not found");
    }

    public Result computeDetails(double temperature) throws Exception {

        int index = findPolynomial(temperature);
        if (index!=BadIndex) {
            Result result = new Result();
            result.Tinput = temperature;

            double data = polynomials[index].compute(temperature);

            double correction = 0.0;
            if (exponential!=null && temperature>0) {
                // according to the function definition from NIST,
                // the exponential term is available only for type K when temperature > 0
                // Reference http://srdata.nist.gov/its90/download/download.html
                correction = exponential.compute(temperature);
                result.exponential = exponential;
            }
            result.polynomial = polynomials[index];
            result.E = data + correction;
            result.Epoly = data;
            result.correction = correction;
            return result;
        }
        else {
            throw new Exception(getInputOutOrRangeMessage(temperature));
        }
    }

    public double compute_dEdT(double temperature) throws Exception {
        // Throw an exception if temperature is out of range
        int index = findPolynomial(temperature);
        if (index!=BadIndex) {
            double data = polynomials[index].compute_dEdT(temperature);

            double correction = 0.0;
            if (exponential!=null && temperature>0) {
                // according to the function definition from NIST,
                // the exponential term is available only for type K when temperature > 0
                // Reference http://srdata.nist.gov/its90/download/download.html
                correction = exponential.compute_dEdT(temperature);
            }
            return data + correction;
        }
        else {
            throw new Exception(getInputOutOrRangeMessage(temperature));
        }
    }

    private String getInputOutOrRangeMessage(double temperature) {
        return "Type " + name + " input T=" + temperature + " out of range";
    }

    public boolean isInRange(double temperature) {
        return (findPolynomial(temperature)!=BadIndex);
    }


    // For Fn, the segments are highly continuous,
    // the difference between two polynomials at any junction is practically 0
    // So just find the first polynomial that fits
    private int findPolynomial(double temperature) {
        if (polynomials ==null || polynomials.length==0) return BadIndex;
        for (int index=0; index< polynomials.length; index++) {
            if (polynomials[index].isInRange(temperature)) return index;
        }
        return BadIndex;
    }


    // To handle cases where a point is at the boundary
    private int findSecondPolynomial(double temperature) {
        int firstIndex = -1;
        if (polynomials ==null || polynomials.length==0) return BadIndex;
        for (int index=0; index< polynomials.length; index++) {
            if (polynomials[index].isInRange(temperature)) {
                if (firstIndex==-1) firstIndex=index;  // no first index
                else return index;  // First index exists. So this must be the second.
            }
        }
        return BadIndex;
    }





    private double[] getControlTemperaturesForEMF(int internalPtsPerSegment) {
        if (polynomials ==null || polynomials.length==0) return null;
        int numberOfSegments = polynomials.length;

        // Minimum number of control points must be located
        // at the boundaries of the segments
        int endPoints = numberOfSegments+1;

        // Note: for forward function, the range of temperature is 0-degree continuous

        if (internalPtsPerSegment<0) internalPtsPerSegment=0;

        double[] point = new double[endPoints + numberOfSegments*internalPtsPerSegment];

        int count = 0;
        for (int index=0; index<numberOfSegments; index++) {
            double Tmin = polynomials[index].Tmin;
            double Tmax = polynomials[index].Tmax;
            double Tinterval = (Tmax - Tmin) / (internalPtsPerSegment+1); // number of intervals = number of internal points+1
            point[count] = Tmin;  // starting point of the segment
            count++;
            for (int j=1; j<=internalPtsPerSegment; j++) {
                point[count] = Tmin + Tinterval*j;
                count++;
            }
        }
        // last control point
        int last = numberOfSegments-1;
        point[count] = polynomials[last].Tmax;
        count++;

        Savelog.d(TAG, debug, "control point count=" + count);
        return point;
    }

    public PointF[] getEMFCurveControlPoints(int internalPtsPerSegment) {
        double[] controlTemperature = getControlTemperaturesForEMF(internalPtsPerSegment);
        if (controlTemperature==null || controlTemperature.length==0) return null;

        try {
            int count = controlTemperature.length;
            double[] voltage = new double[count];
            for (int index=0; index<count; index++) {
                voltage[index] = computeE(controlTemperature[index]);
            }
            PointF[] pointF = new PointF[count];
            for (int index=0; index<count; index++) {
                pointF[index] = new PointF((float)controlTemperature[index], (float)voltage[index]);
            }
            return pointF;
        } catch (Exception e) {
            return null;
        }
    }


    private double[] getControlTemperaturesFor_dE() {
        if (polynomials ==null || polynomials.length==0) return null;
        int numberOfSegments = polynomials.length;

        // Since the E curve is not guaranteed to be smooth in order 1,
        // DO NOT use the end points of the segments as control points!
        // But for completeness's sake, include the start point and the end point of the whole curve


        // Add start and end points
        double[] point = new double[numberOfSegments*InternalPtsPerSegmentForS + 2];

        int count = 0;
        point[count] = getTmin();
        count++;

        for (int index=0; index<numberOfSegments; index++) {
            double Tmin = polynomials[index].Tmin;
            double Tmax = polynomials[index].Tmax;
            double Tinterval = (Tmax - Tmin) / (InternalPtsPerSegmentForS); // number of intervals = number of internal points

            // Start the first point at half of an interval into the segment
            for (int j=0; j<InternalPtsPerSegmentForS; j++) {
                point[count] = Tinterval/2 + Tmin + Tinterval*j;
                count++;
            }
        }

        point[count] = getTmax();
        count++;

        Savelog.d(TAG, debug, "control point count=" + count);
        return point;
    }

    public PointF[] getSeebeckCurveControlPoints() {
        double[] controlTemperature = getControlTemperaturesFor_dE();
        if (controlTemperature==null || controlTemperature.length==0) return null;

        try {
            int count = controlTemperature.length;
            double[] dEdT = new double[count];
            for (int index=0; index<count; index++) {
                dEdT[index] = compute_dEdT(controlTemperature[index]);
            }
            PointF[] pointF = new PointF[count];
            for (int index=0; index<count; index++) {
                pointF[index] = new PointF((float)controlTemperature[index], (float)dEdT[index]);
            }
            return pointF;
        } catch (Exception e) {
            return null;
        }
    }




    public String getUnitT() {
        return unitT;
    }

    public String getUnitEMF() {
        return unitEMF;
    }


    // voltage as a function of temperature
    private static final String JSON_type = "thermocouple type";
    private static final String JSON_equation = "equation";
    private static final String JSON_Tlo = "Tlo";
    private static final String JSON_Thi = "Thi";
    private static final String JSON_expo = "exponential";
    private static final String JSON_expo_a1 = "a1";
    private static final String JSON_expo_a2 = "a2";
    private static final String JSON_expo_a0 = "a0";
    private static final String JSON_data = "data";
    private static final String JSON_poly_order = "order(n)";
    private static final String JSON_poly_Tmax = "Tmax";
    private static final String JSON_poly_Tmin = "Tmin";
    private static final String JSON_poly_coeffs = "Coefficients";
    private static final String JSON_coeff_type = "coefficient type";
    private static final String JSON_units = "units";
    private static final String JSON_unit_T = "T";
    private static final String JSON_unit_EMF = "EMF";





    public void load(String data) throws Exception {
        try {
            // data may be null or bad

            JSONObject json;
            json = new JSONObject(data);

            name = json.getString(JSON_type);
            if (name==null) name = "";

            Savelog.d(TAG, debug, "name=" + name);

            // For type k, there are 2 equations, for others, only one
            if (name.equals("K")) {
                Savelog.d(TAG, debug, "type K has 2 equations");
                equation = new String[2];
                JSONObject equationJSON = json.getJSONObject(JSON_equation);
                equation[0] = equationJSON.getString(JSON_Tlo);
                equation[1] = equationJSON.getString(JSON_Thi);
                if (equation[0] == null) equation[0] = "";
                if (equation[1] == null) equation[1] = "";
                Savelog.d(TAG, debug, "equation1=" + equation[0] + " equation2=" + equation[1]);
            }
            else { // all other types have just one equation
                equation = new String[1];
                equation[0] = json.getString(JSON_equation);
                if (equation[0] == null) equation[0] = "";
                Savelog.d(TAG, debug, "equation=" + equation[0]);
            }


            exponential = null;
            // exponential term is optional
            try {
                double[] aTerm = null;
                JSONObject exponentsJSON = json.getJSONObject(JSON_expo);
                aTerm = new double[3];
                aTerm[0] = exponentsJSON.getDouble(JSON_expo_a0);
                aTerm[1] = exponentsJSON.getDouble(JSON_expo_a1);
                aTerm[2] = exponentsJSON.getDouble(JSON_expo_a2);
                exponential = new Exponential(aTerm);

                Savelog.d(TAG, debug, "exponential=" + exponential.toString());
            } catch (Exception e) {}  // leave aTerm as null


            JSONArray dataJSONArray = json.getJSONArray(JSON_data);
            polynomials = new Polynomial[dataJSONArray.length()];

            for (int index=0; index< polynomials.length; index++) {

                JSONObject polyJSON = dataJSONArray.getJSONObject(index);

                int order = polyJSON.getInt(JSON_poly_order);
                double Tmin = polyJSON.getDouble(JSON_poly_Tmin);
                double Tmax = polyJSON.getDouble(JSON_poly_Tmax);

                JSONArray coeffsJSONArray = polyJSON.getJSONArray(JSON_poly_coeffs);
                double[] coeffs = new double[coeffsJSONArray.length()];
                for (int j=0; j<coeffs.length; j++) {
                    coeffs[j] = coeffsJSONArray.getDouble(j);
                }

                polynomials[index] = new Polynomial(order, Tmin, Tmax, coeffs);

                Savelog.d(TAG, debug, "polynomials: " + polynomials[index].toString());
            }
            if (polynomials ==null) polynomials = new Polynomial[0];

            coefficientType = json.getString(JSON_coeff_type);
            if (coefficientType==null) coefficientType = "";

            Savelog.d(TAG, debug, "coefficientType=" + coefficientType);

            JSONObject unitsJSON = json.getJSONObject(JSON_units);

            unitT = unitsJSON.getString(JSON_unit_T);
            if (unitT==null) unitT = "";
            Savelog.d(TAG, debug, "unitT=" + unitT);

            unitEMF = unitsJSON.getString(JSON_unit_EMF);
            if (unitEMF==null) unitEMF = "";
            Savelog.d(TAG, debug, "unitEMF=" + unitEMF);

        }
        catch (Exception e) {
            Savelog.e(TAG, "Problem loading JSON ", e);
            throw e;
        }
    }


    @Override
    public String toString() {
        String data = "";
        data += JSON_type + ": " + name + "\n";
        data += JSON_equation + ": " + "\n";
        for (int index=0; index<equation.length; index++) {
            data += "  " + equation[index] + "\n";
        }
        data += JSON_coeff_type + ": " + coefficientType + "\n";
        data += JSON_unit_T + ": " + unitT + "\n";
        data += JSON_unit_EMF + ": " + unitEMF + "\n";
        data += "Polynomials:\n";
        if (polynomials !=null) {
            for (int index = 0; index< polynomials.length; index++)
                data += polynomials[index].toString();
        }
        if (exponential!=null) {
            data += "Exponential:\n";
            data += exponential.toString();
        }
        return data;
    }


    public static class Result {
        public Polynomial polynomial;
        public double Tinput;
        public double Epoly;
        public Exponential exponential;
        public double correction;
        public double E;
    }
}
