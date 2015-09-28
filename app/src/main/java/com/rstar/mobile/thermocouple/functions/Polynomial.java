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

public class Polynomial {

    private static final double BoundaryTolerance = 0.001;

    private int order = 0;
    // Use hex string 0x0.0p0 to represent positive zero
    double Tmin = 0x0.0p0;
    double Tmax = 0x0.0p0;
    double[] coefficients;  // coefficients of a polynomial

    public Polynomial(int order, double Tmin, double Tmax, double[] coefficients) {
        if (coefficients != null && (order + 1) == coefficients.length) {
            this.order = order;
            this.Tmin = Tmin;
            this.Tmax = Tmax;
            // Make a duplicate of the whole array. Do not just copy pointer
            this.coefficients = new double[coefficients.length];
            for (int index = 0; index < coefficients.length; index++) {
                this.coefficients[index] = coefficients[index];
            }
        } else { // bad input. Set polynomial to 0
            this.order = 0;
            this.coefficients = new double[1];
            this.coefficients[0] = 0x0.0p0;
        }
    }

    public boolean isInRange(double temperature) {
        if (temperature >= Tmin && temperature <= Tmax) return true;
        // accept a boundary
        if (Math.abs(temperature - Tmin) < BoundaryTolerance || Math.abs(temperature - Tmax) < BoundaryTolerance)
            return true;
        return false;
    }


    // A slow way to implement the polynomial computation.
    // Put here for benchmarking purpose only. Not to be used in practice.
    public double computeByPower(double temperature) throws Exception {
        double voltage = 0.0;
        if (isInRange(temperature)) {
            for (int index=0; index<coefficients.length; index++) {
                voltage += coefficients[index] * Math.pow(temperature, index);
            }
        }
        else {
            throw new Exception("Input temperature out of range [" + Tmin + "," + Tmax + "]");
        }
        return voltage;
    }


    public double compute(double temperature) throws Exception {
        double voltage = 0.0;
        if (isInRange(temperature)) {
            // Do sum in reverse order
            for (int index=coefficients.length-1; index>=0; index--) {
                voltage = coefficients[index] + voltage * temperature;
            }
        }
        else {
            throw new Exception("Input temperature out of range [" + Tmin + "," + Tmax + "]");
        }
        return voltage;
    }

    public double compute_dEdT(double temperature) throws Exception {
        double dEdT = 0.0;
        if (isInRange(temperature)) {
            // Do sum in reverse order
            for (int index=coefficients.length-1; index>=1; index--) {
                dEdT = index*coefficients[index] + dEdT * temperature;
            }
        }
        else {
            throw new Exception("Input temperature out of range [" + Tmin + "," + Tmax + "]");
        }
        return dEdT;
    }


    public boolean isAtBoundary(double temperature) {
        if (Math.abs(temperature - Tmin) < BoundaryTolerance || Math.abs(temperature - Tmax) < BoundaryTolerance)
            return true;
        return false;
    }

    public int getOrder() {
        return order;
    }

    public double getCoefficent(int term) {
        if (term>=0 && term<coefficients.length) return coefficients[term];
        return 0.0;
    }

    @Override
    public String toString() {
        String data = "";
        data += "Order=" + order + ",";
        data += "Tmin=" + String.format("%f", Tmin) + ",";
        data += "Tmax=" + String.format("%f", Tmax) + ",";

        data += "\n";
        for (int index = 0; index < coefficients.length; index++) {
            data += "" + index + ": ";
            data += String.format("%e", coefficients[index]) + "\n";
        }
        return data;
    }
}
