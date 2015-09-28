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

import com.rstar.mobile.thermocouple.functions.Polynomial;

public class PolynomialFields {
    public static class Constants extends Fields.Constants {

        private static final String privateField1 = "BoundaryTolerance";

        public int BoundaryTolerance = 0;

        public Constants() throws Exception {
            get();
        }

        // Constant fields only need to be copied once
        @Override
        public void get() throws Exception {
            BoundaryTolerance = getBoundaryTolerance();
        }

        @Override
        public void detach() {
            BoundaryTolerance = 0;
        }

        public static int getBoundaryTolerance() throws Exception {
            return RefUtil.getPrivateField(Polynomial.class, privateField1, Integer.class);
        }
    }

    public static class Variables extends Fields.Variables<Polynomial> {
        private static final String privateField1 = "order";
        private static final String privateField2 = "Tmin";
        private static final String privateField3 = "Tmax";
        private static final String privateField4 = "coefficients";

        public int order = -1;
        public double Tmin = 0.0;
        public double Tmax = 0.0;
        public double[] coefficients = null;

        public Variables(Polynomial polynomial) throws Exception {
            refresh(polynomial);
        }

        @Override
        public void refresh(Polynomial polynomial) throws Exception {
            order = getOrder(polynomial);
            Tmin = getTmin(polynomial);
            Tmax = getTmax(polynomial);
            coefficients = getCoefficients(polynomial);
        }

        @Override
        public void detach() {
            order = -1;
            Tmin = 0.0;
            Tmax = 0.0;
            coefficients = null;
        }

        public static Integer getOrder(Polynomial polynomial) throws Exception {
            return RefUtil.getPrivateField(polynomial, privateField1, Integer.class);
        }

        public static Double getTmin(Polynomial polynomial) throws Exception {
            return RefUtil.getPrivateField(polynomial, privateField2, Double.class);
        }

        public static Double getTmax(Polynomial polynomial) throws Exception {
            return RefUtil.getPrivateField(polynomial, privateField3, Double.class);
        }

        public static double[] getCoefficients(Polynomial polynomial) throws Exception {
            return RefUtil.getPrivateField(polynomial, privateField4, double[].class);
        }
    }
}