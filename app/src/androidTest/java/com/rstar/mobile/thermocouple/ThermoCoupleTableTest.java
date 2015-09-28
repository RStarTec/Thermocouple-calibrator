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

import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.tables.ThermoCoupleTable;

public class ThermoCoupleTableTest extends InstrumentationTestCase {
    private static final String TAG = ThermoCoupleTableTest.class.getSimpleName()+"_class";
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

    public void testLoad() throws Exception {
        for (int index=0; index<ThermoCoupleTable.size; index++) {
            String typeCode = ThermoCoupleTable.type[index];
            ThermoCoupleTable tcTable = new ThermoCoupleTable(targetContext, typeCode);
            assertEquals(typeCode, tcTable.getType());

            Log.d(TAG, "Type" + tcTable.getType() + " Tmin=" + tcTable.getTMinOfTable() + " Tmax=" + tcTable.getTMaxOfTable()
                    + " Emin=" + tcTable.getEMinOfTable() + " Emax=" + tcTable.getEMaxOfTable());
        }
    }


    public void testTableVSFn() throws Exception {
        ThermoCouple tc = new ThermoCouple(targetContext);

        double[] maxEDiff = new double[ThermoCouple.size];
        double[] TatMaxEDiff = new double[ThermoCouple.size];

        for (int index=0; index<ThermoCoupleTable.size; index++) {
            String typeCode = ThermoCoupleTable.type[index];
            ThermoCoupleTable tcTable = new ThermoCoupleTable(targetContext, typeCode);
            maxEDiff[index] = 0.0;
            TatMaxEDiff[index] = 0.0;

            double Tmin = tcTable.getTMinOfTable();
            double Tmax = tcTable.getTMaxOfTable();

            for (double T=Tmin; T<=Tmax; T+=1.0) {
                double EfromTable = tcTable.getE(T);
                Fn fn = tc.getFn(typeCode);
                double EfromFn;
                try {
                    EfromFn = fn.computeE(T);
                } catch (Exception e) {
                    fn.reportTRange(T);
                    EfromFn = EfromTable + LargeError;
                }
                double difference = Math.abs(EfromFn - EfromTable);

                if (difference > maxEDiff[index]) {
                    maxEDiff[index] = difference;
                    TatMaxEDiff[index] = T;
                }
            }

            double maxEDiff_i = maxEDiff[index];
            double T_i = TatMaxEDiff[index];
            try {
                double Etable_Ti = tcTable.getE(T_i);
                double Efn_Ti = tc.getFn(typeCode).computeE(T_i);

                Log.d(TAG, "Type" + tcTable.getType() + " max E difference = " + maxEDiff_i
                        + " at T=" + T_i + " Etable=" + Etable_Ti + " Efn=" + Efn_Ti);
            }
            catch (Exception e) {
                Log.d(TAG, "WARNING: Type" + typeCode + " Cannot compute difference in E at T=" + T_i);
            }
        }
    }

}
