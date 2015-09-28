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

import com.rstar.mobile.thermocouple.functions.Exponential;
import com.rstar.mobile.thermocouple.functions.Fn;
import com.rstar.mobile.thermocouple.functions.Polynomial;

public class FnFields {
    private static final String TAG = FnFields.class.getSimpleName()+"_class";

    public static class Constants extends Fields.Constants {

        private static final String privateField1 = "InternalPtsPerSegmentForE";
        private static final String privateField2 = "InternalPtsPerSegmentForS";
        private static final String privateField3 = "BadIndex";

        public int InternalPtsPerSegmentForE = 0;
        public int InternalPtsPerSegmentForS = 0;
        public int BadIndex = 0;

        public Constants() throws Exception {
            get();
        }

        // Constant fields only need to be copied once
        @Override
        public void get() throws Exception {
            InternalPtsPerSegmentForE = getInternalPtsPerSegmentForE();
            InternalPtsPerSegmentForS = getInternalPtsPerSegmentForS();
            BadIndex = getBadIndex();
        }

        @Override
        public void detach() {
            InternalPtsPerSegmentForE = 0;
            InternalPtsPerSegmentForS = 0;
            BadIndex = 0;
        }


        public static int getInternalPtsPerSegmentForE() throws Exception {
            return RefUtil.getPrivateField(Fn.class, privateField1, Integer.class);
        }
        public static int getInternalPtsPerSegmentForS() throws Exception {
            return RefUtil.getPrivateField(Fn.class, privateField2, Integer.class);
        }
        public static int getBadIndex() throws Exception {
            return RefUtil.getPrivateField(Fn.class, privateField3, Integer.class);
        }
    }

    public static class Variables extends Fields.Variables<Fn> {
        private static final String privateField1 = "name";
        private static final String privateField2 = "equation";
        private static final String privateField3 = "coefficientType";
        private static final String privateField4 = "unitT";
        private static final String privateField5 = "unitEMF";
        private static final String privateField6 = "polynomials";
        private static final String privateField7 = "exponential";

        public String name = null;
        public String[] equation = null;
        public String coefficientType = null;
        public String unitT = null;
        public String unitEMF = null;
        public Polynomial[] polynomials = null;
        public Exponential exponential = null;

        public Variables(Fn fn) throws Exception {
            refresh(fn);
        }

        @Override
        public void refresh(Fn fn) throws Exception {
            name = getName(fn);
            equation = getEquation(fn);
            coefficientType = getCoefficientType(fn);
            unitT = getUnitT(fn);
            unitEMF = getUnitEMF(fn);
            polynomials = getPolynomials(fn);
            exponential = getExponential(fn);
        }

        @Override
        public void detach() {
            name = null;
            equation = null;
            coefficientType = null;
            unitT = null;
            unitEMF = null;
            polynomials = null;
            exponential = null;
        }

        public static String getName(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField1, String.class);
        }

        public static String[] getEquation(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField2, String[].class);
        }

        public static String getCoefficientType(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField3, String.class);
        }

        public static String getUnitT(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField4, String.class);
        }

        public static String getUnitEMF(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField5, String.class);
        }

        public static Polynomial[] getPolynomials(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField6, Polynomial[].class);
        }

        public static Exponential getExponential(Fn fn) throws Exception {
            return RefUtil.getPrivateField(fn, privateField7, Exponential.class);
        }

    }

    public static class Functions extends Fields.Functions<Fn> {
        private static final String privateField1 = "findPolynomial";
        private static final String privateField2 = "findSecondPolynomial";

        public static Integer findPolynomial(Fn fn, double temperature) throws Exception {
            Object params[] = new Object[1];
            params[0] = temperature;
            Class<?> paramClasses[] = new Class[1];
            paramClasses[0] = double.class;
            return RefUtil.runPrivateMethod(Fn.class, fn, privateField1, paramClasses, params, Integer.class);
        }

        public static Integer findSecondPolynomial(Fn fn, double temperature) throws Exception {
            Object params[] = new Object[1];
            params[0] = temperature;
            Class<?> paramClasses[] = new Class[1];
            paramClasses[0] = double.class;
            return RefUtil.runPrivateMethod(Fn.class, fn, privateField2, paramClasses, params, Integer.class);
        }

    }
}