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

package com.rstar.mobile.thermocouple;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.rstar.mobile.thermocouple.fields.FnFields;
import com.rstar.mobile.thermocouple.fields.PolynomialFields;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;

public class FnTest extends InstrumentationTestCase {
    private static final String TAG = FnTest.class.getSimpleName()+"_class";
    Context targetContext = null;
    Context testContext = null;
    ThermoCouple tc;
    int interval = 100;

    FnFields.Constants constants = null;

    protected void setUp() throws Exception {
        super.setUp();
        targetContext = getInstrumentation().getTargetContext();
        testContext = getInstrumentation().getContext();
        tc = new ThermoCouple(targetContext);
        constants = new FnFields.Constants();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        constants.detach();
    }


    public void test1_FnAccuacy() {
        checkFnAccuracy();
    }

    public void test2_FnTime() {
        checkFnTime();
    }

    public void test3_FnBoundaries() throws Exception {
        checkAllFnBoundaries();
    }

    public void test4_TtoVtoT() {
        checkTtoVtoT();
    }

    public void test5_VtoTtoV() {
        checkVtoTtoV();
    }





    public void checkFnAccuracy() {
        Log.d(TAG, "Test Fn difference using recursive method versus power method:");
        double maxDiff = 0.0;

        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            double Tmin = fn.getTmin();
            double Tmax = fn.getTmax();
            double deltaT = (Tmax-Tmin)/interval;
            for (int i=0; i<=interval; i++) {
                double T = Tmin + deltaT*i;
                try {
                    double V = computeCompare(fn, T, 1);
                    double V2 = computeCompare(fn, T, 2);
                    double diff = Math.abs(V-V2);
                    if (diff>maxDiff) maxDiff = diff;
                    Log.d(TAG, "T= " + T + " V1=" + V + " V2=" + V2 + " diff=" + diff);
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
        }
        Log.d(TAG, "Fn max difference = " + maxDiff);
    }



    public void checkFnTime() {
        Log.d(TAG, "Test Fn time-use using recursive method versus power method:");
        long startTime = System.currentTimeMillis();

        // Method 1
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            double Tmin = fn.getTmin();
            double Tmax = fn.getTmax();
            double deltaT = (Tmax-Tmin)/interval;
            for (int i=0; i<=interval; i++) {
                double T = Tmin + deltaT*i;
                try {
                    double V = computeCompare(fn, T, 1);
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long diffTime = endTime-startTime;
        Log.d(TAG, "Fn time used by method 1 = " + diffTime);

        // Method 2
        startTime = System.currentTimeMillis();
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            double Tmin = fn.getTmin();
            double Tmax = fn.getTmax();
            double deltaT = (Tmax-Tmin)/interval;
            for (int i=0; i<=interval; i++) {
                double T = Tmin + deltaT*i;
                try {
                    double V = computeCompare(fn, T, 2);
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
        }
        endTime = System.currentTimeMillis();
        diffTime = endTime-startTime;
        Log.d(TAG, "Fn time used by method 2 = " + diffTime);
    }


    public double computeCompare(Fn fn, double temperature, int method) throws Exception {
        FnFields.Variables fn_var = new FnFields.Variables(fn);
        fn_var.refresh(fn);
        FnFields.Functions fn_fn = new FnFields.Functions();

        // Throw an exception if temperature is out of range
        int index = fn_fn.findPolynomial(fn, temperature);
        if (index!=constants.BadIndex) {

            double data;
            if (method==1)
                data = fn_var.polynomials[index].compute(temperature);
            else {
                data = fn_var.polynomials[index].computeByPower(temperature);
            }

            double correction = 0.0;
            if (fn_var.exponential!=null && temperature>0) {
                // according to the function definition from NIST,
                // the exponential term is available only for type K when temperature > 0
                // Reference http://srdata.nist.gov/its90/download/download.html
                correction = fn_var.exponential.compute(temperature);
            }
            return data + correction;
        }
        else {
            throw new Exception("Input out of range");
        }
    }

    private double checkPolynomialDiscontinuity(Fn fn, double temperature) throws Exception {
        FnFields.Variables fn_var = new FnFields.Variables(fn);
        fn_var.refresh(fn);
        FnFields.Functions fn_fn = new FnFields.Functions();

        // Throw an exception if temperature is out of range
        double difference = 0.0;

        int index = fn_fn.findPolynomial(fn, temperature);
        if (index!=constants.BadIndex) {
            double data = fn_var.polynomials[index].compute(temperature);

            if (fn_var.polynomials[index].isAtBoundary(temperature)) {

                int secondIndex = fn_fn.findSecondPolynomial(fn, temperature);
                double secondData;
                if (secondIndex!=constants.BadIndex) {

                    secondData = fn_var.polynomials[secondIndex].compute(temperature);
                    difference = Math.abs(data-secondData);

                    PolynomialFields.Variables polynomial1_var = new PolynomialFields.Variables(fn_var.polynomials[index]);
                    PolynomialFields.Variables polynomial2_var = new PolynomialFields.Variables(fn_var.polynomials[secondIndex]);

                    Log.d(TAG, "Type " + fn_var.name
                            + " at boundary T=" + temperature
                            + " V1=" + data + " from[" + polynomial1_var.Tmin + "," + polynomial1_var.Tmax + "]"
                            + " V2=" + secondData + " from[" + polynomial2_var.Tmin + "," + polynomial2_var.Tmax + "]"
                            + " diff=" + difference);

                }
                else {
                    Log.d(TAG, "Type " + fn_var.name
                            + " at boundary T=" + temperature
                            + " no second poly");
                }
            }


        }
        return difference;
    }

    public void checkAllFnBoundaries() throws Exception {
        Log.d(TAG, "Test Fn boundary continuity");
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            checkBoundary(fn);
        }
    }

    public void checkBoundary(Fn fn) throws Exception {


        FnFields.Variables fn_var = new FnFields.Variables(fn);
        fn_var.refresh(fn);

        // Note: The only significant discontinuity seems to be for Type K, at temperature=0,
        // where the first polynomials seems to give better result because it gives V=0 while the second poly gives V=-0.0176
        // For all other types, the differences seem to be less than E-8
        for (int j=0; j< fn_var.polynomials.length; j++) {
            PolynomialFields.Variables polynomial_var = new PolynomialFields.Variables(fn_var.polynomials[j]);

            double[] Ts ={ polynomial_var.Tmin, polynomial_var.Tmax};

            for (double T : Ts) {
                try {
                    double V = checkPolynomialDiscontinuity(fn, T);
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
        }
    }




    public void checkTtoVtoT() {
        Log.d(TAG, "Test max difference between T and Trecovered in T -> V -> Trecovered ");
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            FnInv fnInv = tc.getFnInv(typeCode);
            double Tmin = fn.getTmin();
            double Tmax = fn.getTmax();
            double deltaT = (Tmax-Tmin)/interval;
            double maxDiff = 0.0;
            for (int i=0; i<=interval; i++) {
                double T = Tmin + deltaT*i;
                try {
                    double V = fn.computeE(T);
                    double Trecovered = fnInv.computeT(V);
                    double difference = Math.abs(T-Trecovered);
                    if (difference>maxDiff)
                        maxDiff = difference;
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
            Log.d(TAG, "Type " + typeCode + ": max T diff=" + maxDiff);
        }
    }


    public void checkVtoTtoV() {
        Log.d(TAG, "Test max difference between V and Vrecovered in V -> T -> Vrecovered ");
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            Fn fn = tc.getFn(typeCode);
            FnInv fnInv = tc.getFnInv(typeCode);
            double Emin = fnInv.getEmin();
            double Emax = fnInv.getEmax();
            double deltaE = (Emax-Emin)/interval;
            double maxDiff = 0.0;
            for (int i=0; i<=interval; i++) {
                double V = Emin + deltaE*i;
                try {
                    double T = fnInv.computeT(V);
                    double Vrecovered = fn.computeE(T);
                    double difference = Math.abs(V-Vrecovered);
                    if (difference>maxDiff)
                        maxDiff = difference;
                } catch (Exception e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
            Log.d(TAG, "Type " + typeCode + ": max V diff=" + maxDiff);
        }
    }

}
