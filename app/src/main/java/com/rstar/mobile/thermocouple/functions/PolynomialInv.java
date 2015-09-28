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

public class PolynomialInv {
    private static final String TAG = PolynomialInv.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final double BoundaryTolerance = 0.001;

    // Use hex string 0x0.0p0 to represent positive zero
    double Tmin = 0x0.0p0;
    double Tmax = 0x0.0p0;
    double[] coefficients;
    double Emin = 0x0.0p0;
    double Emax = 0x0.0p0;
    double[] errorRange;

    public PolynomialInv(double Tmin, double Tmax, double[] coefficients, double Emin, double Emax, double[] errorRange) {
        if (coefficients!=null && errorRange!=null && errorRange.length==2) {
            this.Tmin = Tmin;
            this.Tmax = Tmax;
            // Make a duplicate of the whole array. Do not just copy pointer
            this.coefficients = new double[coefficients.length];
            for (int index=0; index<coefficients.length; index++) {
                this.coefficients[index] = coefficients[index];
            }

            // Make a duplicate of the whole array. Do not just copy pointer
            this.Emin = Emin;
            this.Emax = Emax;
            this.errorRange = new double[errorRange.length];
            for (int index=0; index<errorRange.length; index++) {
                this.errorRange[index] = errorRange[index];
            }
        }
        else { // bad input. Set polynomial to 0
            this.coefficients = new double[1];
            this.coefficients[0] = 0x0.0p0;

            // Set error range to 0
            final int length = 2;
            this.errorRange = new double[length];
            for (int index=0; index<length; index++) {
                this.errorRange[index] = 0x0.0p0;
            }
        }
    }


    public boolean isInRange(double voltage) {
        if (voltage>=Emin && voltage<=Emax) return true;
        // accept a boundary
        if (Math.abs(voltage-Emin)< BoundaryTolerance || Math.abs(voltage-Emax)< BoundaryTolerance) return true;
        return false;
    }


    public boolean isResultInRange(double temperature) {
        if (errorRange==null) return false;
        if (temperature < Tmin+errorRange[0] || temperature > Tmax+errorRange[1]) return false;
        return true;
    }

    public double getOvershootAtBoundary(double temperature) {
        if (errorRange==null) return 0.0;
        if (temperature < Tmin+errorRange[0]) return (Tmin+errorRange[0]) - temperature;
        else if (temperature > Tmax+errorRange[1]) return temperature - (Tmax+errorRange[1]);
        return 0.0;
    }

    public String reportResult(double voltage, double temperature) {
        String data = "";
        if (errorRange!=null) {
            data = "For V=" + voltage
                    + " Tbad=" + temperature
                    + " For Emf=[" + Emin + "," + Emax + "]"
                    + " expected T=[" + Tmin + "," + Tmax + "]"
                    + " err=[" + errorRange[0] + "," + errorRange[1] + "]";
        }
        return data;
    }




    // A slow way to implement the polynomial computation.
    // Put here for benchmarking purpose only. Not to be used in practice.
    public double computeByPower(double voltage) throws Exception {
        double temperature = 0.0;
        if (isInRange(voltage)) {
            for (int index=0; index<coefficients.length; index++) {
                temperature += coefficients[index] * Math.pow(voltage, index);
            }
        }
        else {
            throw new Exception("Input voltage out of range [" + Emin + "," + Emax + "]");
        }
        return temperature;
    }


    public double compute(double voltage) throws Exception {
        double temperature = 0.0;
        if (isInRange(voltage)) {
            // Do sum in reverse order
            for (int index=coefficients.length-1; index>=0; index--) {
                temperature = coefficients[index] + temperature * voltage;
            }
        }
        else {
            throw new Exception("Input voltage out of range [" + Emin + "," + Emax + "]");
        }
        return temperature;
    }

    public int getOrder() {
        return coefficients.length;
    }

    public double getCoefficent(int term) {
        if (term>=0 && term<coefficients.length) return coefficients[term];
        return 0.0;
    }

    public double[] getErrorRange() {
        return errorRange;
    }


    @Override
    public String toString() {
        String data = "";
        data += "Tmin=" + String.format("%f", Tmin) + ",";
        data += "Tmax=" + String.format("%f", Tmax) + ",";
        data += "Emin=" + String.format("%f", Emin) + ",";
        data += "Emax=" + String.format("%f", Emax) + ",";

        data += "\n";
        for (int index = 0; index < coefficients.length; index++) {
            data += "" + index + ": ";
            data += String.format("%e", coefficients[index]) + "\n";
        }

        data += "ErrorRange: ";
        for (int index = 0; index < errorRange.length; index++) {
            data += String.format("%f", errorRange[index]);
            if (index<errorRange.length-1) data += ",";
            else data += "\n";
        }

        return data;
    }
}
