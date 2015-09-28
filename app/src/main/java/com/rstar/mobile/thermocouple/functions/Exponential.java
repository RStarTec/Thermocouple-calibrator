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

public class Exponential {
    double[] aTerm;  // null if no aTerm exist

    public Exponential(double[] aTerm) {
        if (aTerm==null || aTerm.length!=3) { // no exponent
            this.aTerm = null;
        }
        else {
            this.aTerm = new double[aTerm.length];
            for (int index=0; index< aTerm.length; index++) {
                this.aTerm[index] = aTerm[index];
            }
        }
    }

    public double compute(double temperature) {
        if (aTerm==null) return 0x0.0p0;
        return aTerm[0] * Math.exp(aTerm[1]*(temperature-aTerm[2])*(temperature-aTerm[2]));
    }

    public double compute_dEdT(double temperature) {
        if (aTerm==null) return 0x0.0p0;
        return 2.0 * aTerm[1] * (temperature-aTerm[2]) * aTerm[0] * Math.exp(aTerm[1]*(temperature-aTerm[2])*(temperature-aTerm[2]));
    }


    public double getTerm(int a) {
        if (a>=0 && a<=3) return aTerm[a];
        else return 0.0;
    }


    @Override
    public String toString() {
        String data = "";
        data += "a0=" + String.format("%f", aTerm[0]) + ",";
        data += "a1=" + String.format("%f", aTerm[1]) + ",";
        data += "a2=" + String.format("%f", aTerm[1]);
        return data;
    }

}
