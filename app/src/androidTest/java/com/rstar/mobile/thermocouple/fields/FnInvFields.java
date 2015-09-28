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

import com.rstar.mobile.thermocouple.functions.FnInv;
import com.rstar.mobile.thermocouple.functions.PolynomialInv;

public class FnInvFields {
    public static class Constants extends Fields.Constants {
        private static final String privateField1 = "BadIndex";

        public int BadIndex = 0;

        public Constants() throws Exception {
            get();
        }

        // Constant fields only need to be copied once
        @Override
        public void get() throws Exception {
            BadIndex = getBadIndex();
        }

        @Override
        public void detach() {
            BadIndex = 0;
        }


        public static int getBadIndex() throws Exception {
            return RefUtil.getPrivateField(FnInv.class, privateField1, Integer.class);
        }
    }

    public static class Variables extends Fields.Variables<FnInv> {
        private static final String privateField1 = "order";
        private static final String privateField2 = "equation";
        private static final String privateField3 = "coefficientType";
        private static final String privateField4 = "unitT";
        private static final String privateField5 = "unitEMF";
        private static final String privateField6 = "citation";
        private static final String privateField7 = "polynomials";

        public String name = null;
        public String equation = null;
        public String coefficientType = null;
        public String unitT = null;
        public String unitEMF = null;
        public String citation = null;
        public PolynomialInv[] polynomials = null;

        public Variables(FnInv fnInv) throws Exception {
            refresh(fnInv);
        }

        @Override
        public void refresh(FnInv fnInv) throws Exception {
            name = getName(fnInv);
            equation = getEquation(fnInv);
            coefficientType = getCoefficientType(fnInv);
            unitT = getUnitT(fnInv);
            unitEMF = getUnitEMF(fnInv);
            citation = getCitation(fnInv);
            polynomials = getPolynomials(fnInv);
        }

        @Override
        public void detach() {
            name = null;
            equation = null;
            coefficientType = null;
            unitT = null;
            unitEMF = null;
            citation = null;
            polynomials = null;
        }

        public static String getName(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField1, String.class);
        }

        public static String getEquation(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField2, String.class);
        }

        public static String getCoefficientType(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField3, String.class);
        }

        public static String getUnitT(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField4, String.class);
        }

        public static String getUnitEMF(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField5, String.class);
        }

        public static String getCitation(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField6, String.class);
        }
        public static PolynomialInv[] getPolynomials(FnInv fnInv) throws Exception {
            return RefUtil.getPrivateField(fnInv, privateField7, PolynomialInv[].class);
        }

    }

    public static class Functions extends Fields.Functions<FnInv> {
        private static final String privateField1 = "findPolynomial";

        public static Integer findPolynomial(FnInv fnInv, double temperature) throws Exception {
            Object params[] = new Object[1];
            params[0] = temperature;
            Class<?> paramClasses[] = new Class[1];
            paramClasses[0] = Double.class;
            return RefUtil.runPrivateMethod(FnInv.class, fnInv, privateField1, paramClasses, params, Integer.class);
        }


    }
}