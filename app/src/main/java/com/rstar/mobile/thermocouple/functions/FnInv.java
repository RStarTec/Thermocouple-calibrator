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

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.Savelog;

import org.json.JSONArray;
import org.json.JSONObject;

public class FnInv {
    private static final String TAG = FnInv.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int BadIndex = -1;

    private String name = "";
    private String equation = "";
    private String coefficientType = "";
    private String unitT = "";
    private String unitEMF = "";
    private String citation = "";

    private PolynomialInv[] polynomials = null;

    public FnInv(String data) {
        try {
            load(data);
        } catch (Exception e) {
            clear();
        }
    }

    private void clear() {
        name = "";
        equation = "";
        coefficientType = "";
        unitT = "";
        unitEMF = "";
        citation = "";
        polynomials = null;
    }

    public double getEmin() {
        double Emin;
        if (polynomials ==null || polynomials.length==0) return 0.0;
        Emin = polynomials[0].Emin;
        for (int index=1; index< polynomials.length; index++) {
            if (Emin > polynomials[index].Emin)
                Emin = polynomials[index].Emin;
        }
        return Emin;
    }

    public double getEmax() {
        double Emax;
        if (polynomials ==null || polynomials.length==0) return 0.0;
        Emax = polynomials[0].Emax;
        for (int index=1; index< polynomials.length; index++) {
            if (Emax < polynomials[index].Emax)
                Emax = polynomials[index].Emax;
        }
        return Emax;
    }

    private int findPolynomial(double voltage) {
        if (polynomials ==null || polynomials.length==0) return BadIndex;
        for (int index=0; index< polynomials.length; index++) {
            if (polynomials[index].isInRange(voltage)) return index;
        }
        return BadIndex;
    }

    // To handle cases where a point is at the boundary
    private int findSecondPolynomial(double voltage) {
        int firstIndex = -1;
        if (polynomials ==null || polynomials.length==0) return BadIndex;
        for (int index=0; index< polynomials.length; index++) {
            if (polynomials[index].isInRange(voltage)) {
                if (firstIndex==-1) firstIndex=index;  // no first index
                else return index;  // First index exists. So this must be the second.
            }
        }
        return BadIndex;
    }


    public double computeT(double voltage) throws Exception {
        // Throw an exception if voltage is out of range
        int index = findPolynomial(voltage);
        if (index!=BadIndex) {
            double data = polynomials[index].compute(voltage);

            if (polynomials[index].isResultInRange(data))
                return data;

            else {
                // Give a second try
                int secondIndex = findSecondPolynomial(voltage);
                if (secondIndex!=BadIndex) {
                    double secondData = polynomials[secondIndex].compute(voltage);

                    if (polynomials[secondIndex].isResultInRange(secondData)) {
                        return secondData;
                    }
                    else {
                        // Return the result that has a smaller overshoot in absolute terms.
                        double overshoot1 = polynomials[index].getOvershootAtBoundary(data);
                        double overshoot2 = polynomials[secondIndex].getOvershootAtBoundary(secondData);

                        Savelog.d(TAG, debug, "Bad first try: " + polynomials[index].reportResult(voltage, data) + " overshoot=" + overshoot1);
                        Savelog.d(TAG, debug, "Bad second try: " + polynomials[secondIndex].reportResult(voltage, secondData) + " overshoot=" + overshoot2);

                        if (overshoot1<overshoot2) return data;
                        else return secondData;
                    }
                }
                else {
                    Savelog.d(TAG, debug, "Bad first try (no second try available): " + polynomials[index].reportResult(voltage, data));
                    return data;  // return the only available result
                }
            }

        }
        else {
            throw new Exception(getInputOutOrRangeMessage(voltage));
        }
    }

    public Result computeDetails(double voltage) throws Exception {
        Result result = new Result();
        result.Einput = voltage;

        // Throw an exception if voltage is out of range
        int index = findPolynomial(voltage);
        if (index!=BadIndex) {
            double data = polynomials[index].compute(voltage);

            if (polynomials[index].isResultInRange(data)) {
                result.T = data;
                result.polynomial = polynomials[index];
                return result;
            }

            else {
                // Give a second try
                int secondIndex = findSecondPolynomial(voltage);
                if (secondIndex!=BadIndex) {
                    double secondData = polynomials[secondIndex].compute(voltage);

                    if (polynomials[secondIndex].isResultInRange(secondData)) {
                        result.T = secondData;
                        result.polynomial = polynomials[secondIndex];
                        return result;
                    }
                    else {
                        // Return the result that has a smaller overshoot in absolute terms.
                        double overshoot1 = polynomials[index].getOvershootAtBoundary(data);
                        double overshoot2 = polynomials[secondIndex].getOvershootAtBoundary(secondData);

                        if (overshoot1<overshoot2) {
                            result.T = data;
                            result.polynomial = polynomials[index];
                        }
                        else {
                            result.T = secondData;
                            result.polynomial = polynomials[secondIndex];
                        }
                        return result;
                    }
                }
                else {
                    result.T = data;
                    result.polynomial = polynomials[index];  // return the only available result
                    return  result;
                }
            }

        }
        else {
            throw new Exception(getInputOutOrRangeMessage(voltage));
        }
    }



    private String getInputOutOrRangeMessage(double voltage) {
        return "Type " + name + " input V=" + voltage + " out of range";
    }


    public String getUnitT() {
        return unitT;
    }

    public String getUnitEMF() {
        return unitEMF;
    }



    // Inverse function: temperature as a function of voltage
    private static final String JSON_inv_type = "thermocouple type";
    private static final String JSON_inv_equation = "equation";
    private static final String JSON_inv_data = "data";
    private static final String JSON_inv_poly_Tmax = "Tmax";
    private static final String JSON_inv_poly_Tmin = "Tmin";
    private static final String JSON_inv_poly_Emax = "Emax";
    private static final String JSON_inv_poly_Emin = "Emin";
    private static final String JSON_inv_poly_coeffs = "Coefficients";
    private static final String JSON_inv_poly_ErrorRange = "ErrorRange";
    private static final String JSON_inv_coeff_type = "coefficient type";
    private static final String JSON_inv_units = "units";
    private static final String JSON_inv_unit_T = "T";
    private static final String JSON_inv_unit_EMF = "EMF";
    private static final String JSON_inv_citation = "citation";





    public void load(String data) throws Exception {
        try {
            // data may be null or bad

            JSONObject json;
            json = new JSONObject(data);

            name = json.getString(JSON_inv_type);
            if (name==null) name = "";

            equation = json.getString(JSON_inv_equation);
            if (equation==null) equation = "";


            JSONArray dataJSONArray = json.getJSONArray(JSON_inv_data);
            polynomials = new PolynomialInv[dataJSONArray.length()];

            for (int index=0; index< polynomials.length; index++) {

                JSONObject polyJSON = dataJSONArray.getJSONObject(index);

                double Tmin = polyJSON.getDouble(JSON_inv_poly_Tmin);
                double Tmax = polyJSON.getDouble(JSON_inv_poly_Tmax);

                JSONArray coeffsJSONArray = polyJSON.getJSONArray(JSON_inv_poly_coeffs);
                double[] coeffs = new double[coeffsJSONArray.length()];
                for (int j=0; j<coeffs.length; j++) {
                    coeffs[j] = coeffsJSONArray.getDouble(j);
                }

                double Emin = polyJSON.getDouble(JSON_inv_poly_Emin);
                double Emax = polyJSON.getDouble(JSON_inv_poly_Emax);

                JSONArray errJSONArray = polyJSON.getJSONArray(JSON_inv_poly_ErrorRange);
                double[] errorRange = new double[errJSONArray.length()];
                for (int j=0; j<errorRange.length; j++) {
                    errorRange[j] = errJSONArray.getDouble(j);
                }

                polynomials[index] = new PolynomialInv(Tmin, Tmax, coeffs, Emin, Emax, errorRange);
            }

            coefficientType = json.getString(JSON_inv_coeff_type);
            if (coefficientType==null) coefficientType = "";

            JSONObject unitsJSON = json.getJSONObject(JSON_inv_units);

            unitT = unitsJSON.getString(JSON_inv_unit_T);
            if (unitT==null) unitT = "";

            unitEMF = unitsJSON.getString(JSON_inv_unit_EMF);
            if (unitEMF==null) unitEMF = "";

            // citation is optional
            try {
                citation = json.getString(JSON_inv_citation);
                if (citation == null) citation = "";
            } catch (Exception e) {
                citation = "";
            }

        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String toString() {
        String data = "";
        data += JSON_inv_type + ": " + name + "\n";
        data += JSON_inv_equation + ": " + equation + "\n";
        data += JSON_inv_coeff_type + ": " + coefficientType + "\n";
        data += JSON_inv_unit_T + ": " + unitT + "\n";
        data += JSON_inv_unit_EMF + ": " + unitEMF + "\n";
        data += JSON_inv_citation + ": " + citation + "\n";

        data += "Polynomials:\n";
        if (polynomials !=null) {
            for (int index = 0; index< polynomials.length; index++)
                data += polynomials[index].toString();
        }
        return data;
    }


    public static class Result {
        public PolynomialInv polynomial;
        public double Einput;
        public double T;
    }

}
