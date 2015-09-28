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

package com.rstar.mobile.thermocouple.fields;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.tables.ThermoCoupleTable;

public class SeebeckTest extends InstrumentationTestCase {
    private static final String TAG = SeebeckTest.class.getSimpleName()+"_class";
    Context targetContext = null;
    Context testContext = null;
    int interval = 100;


    private static final double LargeError = 10.0;

    protected void setUp() throws Exception {
        super.setUp();
        targetContext = getInstrumentation().getTargetContext();
        testContext = getInstrumentation().getContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testSeebeckTableVSFn() throws Exception {
        ThermoCouple tc = new ThermoCouple(targetContext);

        double[] maxSDiff = new double[ThermoCouple.size];
        double[] TatMaxSDiff = new double[ThermoCouple.size];

        for (int index=0; index<ThermoCoupleTable.size; index++) {
            String typeCode = ThermoCoupleTable.type[index];
            ThermoCoupleTable tcTable = new ThermoCoupleTable(targetContext, typeCode);
            maxSDiff[index] = 0.0;
            TatMaxSDiff[index] = 0.0;

            double Tmin = tcTable.getTMinOfTable();
            double Tmax = tcTable.getTMaxOfTable();

            for (double T=Tmin; T<=Tmax; T+=1.0) {
                double SfromTable = tcTable.compute_dEdT(T);
                Fn fn = tc.getFn(typeCode);
                double SfromFn;
                try {
                    SfromFn = fn.compute_dEdT(T);
                } catch (Exception e) {
                    fn.reportTRange(T);
                    SfromFn = SfromTable + LargeError;
                }
                double difference = Math.abs(SfromFn - SfromTable);

                if (difference > maxSDiff[index]) {
                    maxSDiff[index] = difference;
                    TatMaxSDiff[index] = T;
                }
            }

            double maxSDiff_i = maxSDiff[index];
            double T_i = TatMaxSDiff[index];
            try {
                double Stable_Ti = tcTable.compute_dEdT(T_i);
                double Sfn_Ti = tc.getFn(typeCode).compute_dEdT(T_i);

                Log.d(TAG, "Type" + tcTable.getType() + " max S difference = " + maxSDiff_i
                        + " at T=" + T_i + " Stable=" + Stable_Ti + " Sfn=" + Sfn_Ti);
            }
            catch (Exception e) {
                Log.d(TAG, "WARNING: Type" + typeCode + " Cannot compute difference in S at T=" + T_i);
            }
        }
    }

}
