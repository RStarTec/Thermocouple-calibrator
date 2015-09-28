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

import com.rstar.mobile.thermocouple.fields.FnInvFields;
import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;

public class FnInvTest extends InstrumentationTestCase {
    private static final String TAG = FnInvTest.class.getSimpleName()+"_class";
    Context targetContext = null;
    Context testContext = null;
    ThermoCouple tc;
    int interval = 100000;

    FnInvFields.Constants constants = null;

    protected void setUp() throws Exception {
        super.setUp();
        targetContext = getInstrumentation().getTargetContext();
        testContext = getInstrumentation().getContext();
        tc = new ThermoCouple(targetContext);
        constants = new FnInvFields.Constants();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        constants.detach();
    }


    public void test1_FnInvAccuacy() {
        checkFnInvAccuracy();
    }

    public void test2_FnInvTime() {
        checkFnInvTime();
    }



    public double computeCompare(FnInv fnInv, double temperature, int method) throws Exception {
        FnInvFields.Variables fnInv_var = new FnInvFields.Variables(fnInv);
        fnInv_var.refresh(fnInv);
        FnInvFields.Functions fnInv_fn = new FnInvFields.Functions();

        // Throw an exception if temperature is out of range
        int index = fnInv_fn.findPolynomial(fnInv, temperature);
        if (index!=constants.BadIndex) {

            double data;
            if (method==1)
                data = fnInv_var.polynomials[index].compute(temperature);
            else {
                data = fnInv_var.polynomials[index].computeByPower(temperature);
            }
            return data;
        }
        else {
            throw new Exception("Input out of range");
        }
    }



    public void checkFnInvAccuracy() {
        Log.d(TAG, "Test FnInv difference using recursive method versus power method:");
        double maxDiff = 0.0;

        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            // Log.d(TAG, "Type=" + typeCode);
            FnInv fnInv = tc.getFnInv(typeCode);
            double Emin = fnInv.getEmin();
            double Emax = fnInv.getEmax();
            double deltaE = (Emax-Emin)/interval;
            for (int i=0; i<=interval; i++) {
                double V = Emin + deltaE*i;
                try {
                    double T = computeCompare(fnInv, V, 1);
                    double T2 = computeCompare(fnInv, V, 2);
                    double diff = Math.abs(T-T2);
                    if (diff>maxDiff) maxDiff = diff;

                    // Log.d(TAG, "V= " + V + " T1=" + T + " T2=" + T2 + " diff=" + diff);
                } catch (Exception e) {}
            }
        }
        Log.d(TAG, "FnInv max difference = " + maxDiff);
    }


    public void checkFnInvTime() {
        Log.d(TAG, "Test FnInv time-use using recursive method versus power method:");
        long startTime = System.currentTimeMillis();

        // Method 1
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            FnInv fnInv = tc.getFnInv(typeCode);
            double Emin = fnInv.getEmin();
            double Emax = fnInv.getEmax();
            double deltaE = (Emax-Emin)/interval;
            for (int i=0; i<=interval; i++) {
                double V = Emin + deltaE*i;
                try {
                    double T = computeCompare(fnInv, V, 1);
                } catch (Exception e) {}
            }
        }

        long endTime = System.currentTimeMillis();
        long diffTime = endTime-startTime;
        Log.d(TAG, "FnInv time used by method 1 = " + diffTime);

        // Method 2
        startTime = System.currentTimeMillis();
        for (int index=0; index< ThermoCouple.size; index++) {
            String typeCode = ThermoCouple.type[index];
            FnInv fnInv = tc.getFnInv(typeCode);
            double Emin = fnInv.getEmin();
            double Emax = fnInv.getEmax();
            double deltaE = (Emax-Emin)/interval;
            for (int i=0; i<=interval; i++) {
                double V = Emin + deltaE*i;
                try {
                    double T = computeCompare(fnInv, V, 2);
                } catch (Exception e) {}
            }
        }
        endTime = System.currentTimeMillis();
        diffTime = endTime-startTime;
        Log.d(TAG, "FnInv time used by method 2 = " + diffTime);
    }


}
